package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.quests.AcceptQuestRequest;
import com.vastworld.vwbe.dto.quests.GetAcceptedQuestsResponse;
import com.vastworld.vwbe.dto.quests.GetAvailableQuestsResponse;
import com.vastworld.vwbe.dto.quests.ObjectiveOfQuest;
import com.vastworld.vwbe.entites.PlayerQuest;
import com.vastworld.vwbe.entites.PlayerQuestObjectiveProgress;
import com.vastworld.vwbe.enums.quests.PlayerQuestStatus;
import com.vastworld.vwbe.repositories.PlayerQuestObjectiveProgressRepository;
import com.vastworld.vwbe.repositories.PlayerQuestRepository;
import com.vastworld.vwbe.repositories.QuestObjectiveRepository;
import com.vastworld.vwbe.repositories.QuestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestService {
    private final QuestRepository questRepository;
    private final PlayerQuestRepository playerQuestRepository;
    private final PlayerService playerService;
    private final QuestObjectiveRepository questObjectiveRepository;
    private final RedisService redisService;
    private final PlayerQuestObjectiveProgressRepository playerQuestObjectiveProgressRepository;

    public QuestService(QuestRepository questRepository, PlayerService playerService,
                        PlayerQuestRepository playerQuestRepository, QuestObjectiveRepository questObjectiveRepository,
                        RedisService redisService, PlayerQuestObjectiveProgressRepository playerQuestObjectiveProgressRepository) {
        this.questRepository = questRepository;
        this.playerService = playerService;
        this.playerQuestRepository = playerQuestRepository;
        this.questObjectiveRepository = questObjectiveRepository;
        this.redisService = redisService;
        this.playerQuestObjectiveProgressRepository = playerQuestObjectiveProgressRepository;

    }

    public ServiceResult<List<GetAvailableQuestsResponse>> getAvailableQuests(UUID playerId) {
        try {
            var cachedKey = CacheKeys.quests("availableQuests", playerId);

            var cached = redisService.get(cachedKey, new TypeReference<List<GetAvailableQuestsResponse>>(){});

            if(cached != null){
                return ServiceResult.success("Get Available Quests", cached, HttpStatus.OK);
            }

            var playerDto = playerService.getPlayerById(playerId).getData();
            if (playerDto == null) {
                return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
            }

            var unlockedQuests = questRepository.findUnlockedByRealm(playerDto.realmId());
            var playerQuests = playerQuestRepository.findByPlayer_Id(playerId);

            var playerQuestsMap = playerQuests.stream()
                            .collect(Collectors.toMap(
                                    pq -> pq.getQuest().getId(),
                                    Function.identity()
                            ));

            var now = LocalDateTime.now();

            var filteredQuest = unlockedQuests.stream()

                    // Null (not accepted) or Completed (for repeatable)
                    .filter(q -> {
                        var pq = playerQuestsMap.get(q.getId());
                        return pq == null ||
                                pq.getStatus() == PlayerQuestStatus.COMPLETED ||
                                pq.getStatus() == PlayerQuestStatus.CLAIMED;
                    })

                    //Prerequisite
                    .filter(q ->{
                        if (q.getPrerequisiteQuest() == null)
                            return true;
                        var prereq = playerQuestsMap.get(q.getPrerequisiteQuest().getId());

                        if(prereq != null) {
                            return prereq.getStatus() == PlayerQuestStatus.CLAIMED ||
                                    prereq.getStatus() == PlayerQuestStatus.COMPLETED;
                        }
                        return false;
                    })

                    //Repeatable
                    .filter(q -> {
                        var pq = playerQuestsMap.get(q.getId());

                        // Never accepted before
                        if (pq == null) {
                            return true;
                        }

                        if (!q.getIsRepeatable()) {
                            return false;
                        }

                        if (q.getMaxCompletions() != null &&
                                pq.getCompletionCount() >= q.getMaxCompletions()) {
                            return false;
                        }

                        if (q.getCooldownMinutes() != null &&
                                pq.getClaimedAt() != null) {

                            return pq.getClaimedAt()
                                    .plusMinutes(q.getCooldownMinutes())
                                    .isBefore(now);
                        }

                        return true;
                    })
                    .map(quest ->{
                        var objectiveList = questObjectiveRepository.findByQuest_Id(quest.getId());
                        if (objectiveList.isEmpty()) return null;
                        var mappedObjective = objectiveList.stream()
                                .map(obj -> {
                                    switch(obj.getObjectiveType()) {
                                        case LEVEL_UP -> {
                                            return new ObjectiveOfQuest(obj.getDescription(), obj.getTargetRealm().getName(), obj.getTargetRealmStage().getStageName());
                                        }
                                        case KILL_MONSTER -> {
                                            return new ObjectiveOfQuest(obj.getDescription(), obj.getRequiredCount(), obj.getMonsterId());
                                        }
                                        case REACH_MAP ->{
                                            return new ObjectiveOfQuest(obj.getDescription(), obj.getTargetMap().getMapId());
                                        }
                                        case REACH_COORDINATE -> {
                                            return new ObjectiveOfQuest(obj.getDescription(),  obj.getTargetMap().getMapId(), obj.getTargetX(), obj.getTargetY());
                                        }
                                        case COLLECT_ITEM -> {
                                            return new ObjectiveOfQuest(obj.getDescription(), obj.getRequiredCount(), obj.getItem().getId());
                                        }
                                    }
                                    return null;
                                }).toList();
                        return new GetAvailableQuestsResponse(
                                quest.getId(),
                                quest.getName(),
                                quest.getRequiredRealm().getName(),
                                mappedObjective
                        );
                    })
                    .filter(Objects::nonNull)
                    .toList();

            redisService.set(cachedKey, filteredQuest);

            if(filteredQuest.isEmpty()){
                return ServiceResult.success("You have done all quests", HttpStatus.NO_CONTENT);
            }

            return ServiceResult.success("Get Available Quests", filteredQuest, HttpStatus.OK);
        }
        catch (Exception e) {
            return ServiceResult.failure(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResult<List<GetAcceptedQuestsResponse>> getAcceptedQuests(UUID playerId){

    }

    public ServiceResult<Void> acceptQuest(AcceptQuestRequest request) {
        try{
            var questList = getAvailableQuests(request.playerId()).getData();

            if(questList.isEmpty()) {
                return ServiceResult.failure("Not available quest", HttpStatus.NOT_FOUND);
            }

            var questMap = questList.stream()
                    .collect(Collectors.toMap(GetAvailableQuestsResponse::id, Function.identity()));

            if(!questMap.containsKey(request.questId())){
                return ServiceResult.failure("Not available quest", HttpStatus.NOT_FOUND);
            }

            var quest = questRepository.findById(request.questId());
            if(quest.isEmpty()){
                return ServiceResult.failure("Quest not found", HttpStatus.NOT_FOUND);
            }

            var player = playerService.getPlayerEntityById(request.playerId()).getData();
            if(player == null){
                return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
            }

            var playerQuest = new PlayerQuest();
            playerQuest.setPlayer(player);
            playerQuest.setQuest(quest.get());
            playerQuest.setStatus(PlayerQuestStatus.IN_PROGRESS);
            playerQuestRepository.save(playerQuest);

            var questObjectives = questObjectiveRepository.findByQuest_Id(request.questId());

            if(questObjectives.isEmpty()){
                return ServiceResult.failure("Something went wrong..", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            var progresses = questObjectives.stream()
                    .map(qOj -> {
                        var progress = new PlayerQuestObjectiveProgress();
                        progress.setPlayerQuest(playerQuest);
                        progress.setQuestObjective(qOj);
                        progress.setIsCompleted(false);
                        return progress;
                    })
                    .toList();

            playerQuestObjectiveProgressRepository.saveAll(progresses);

            var cachedKey = CacheKeys.quests("availableQuests", request.playerId());
            redisService.delete(cachedKey);

            return ServiceResult.success("Accepted quest", HttpStatus.NO_CONTENT);
        }
        catch (Exception e) {
            return ServiceResult.failure("Error accepting quest", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
