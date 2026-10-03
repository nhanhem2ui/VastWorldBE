package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playerinventory.AddToInventoryRequest;
import com.vastworld.vwbe.dto.quests.GetAcceptedQuestsResponse;
import com.vastworld.vwbe.dto.quests.GetAvailableQuestsResponse;
import com.vastworld.vwbe.dto.quests.ObjectiveOfQuest;
import com.vastworld.vwbe.dto.quests.objectiveAndProgress.*;
import com.vastworld.vwbe.entites.PlayerQuest;
import com.vastworld.vwbe.entites.PlayerQuestObjectiveProgress;
import com.vastworld.vwbe.enums.quests.PlayerQuestStatus;
import com.vastworld.vwbe.enums.quests.QuestObjectiveTypes;
import com.vastworld.vwbe.repositories.*;
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
    private final RealmStageRepository realmStageRepository;
    private final CultivationRealmRepository cultivationRealmRepository;
    private final PlayerLocationRepository playerLocationRepository;
    private final QuestRewardRepository questRewardRepository;
    private final PlayerInventoryService playerInventoryService;

    public QuestService(QuestRepository questRepository, PlayerService playerService,
                        PlayerQuestRepository playerQuestRepository, QuestObjectiveRepository questObjectiveRepository,
                        RedisService redisService, PlayerQuestObjectiveProgressRepository playerQuestObjectiveProgressRepository,
                        RealmStageRepository realmStageRepository, CultivationRealmRepository cultivationRealmRepository,
                        PlayerLocationRepository playerLocationRepository, QuestRewardRepository questRewardRepository,
                        PlayerInventoryService playerInventoryService) {
        this.questRepository = questRepository;
        this.playerService = playerService;
        this.playerQuestRepository = playerQuestRepository;
        this.questObjectiveRepository = questObjectiveRepository;
        this.redisService = redisService;
        this.playerQuestObjectiveProgressRepository = playerQuestObjectiveProgressRepository;
        this.realmStageRepository = realmStageRepository;
        this.cultivationRealmRepository = cultivationRealmRepository;
        this.playerLocationRepository = playerLocationRepository;
        this.questRewardRepository = questRewardRepository;
        this.playerInventoryService = playerInventoryService;
    }

    public ServiceResult<List<GetAvailableQuestsResponse>> getAvailableQuests(UUID playerId) {
        try {
            var cachedKey = CacheKeys.quests("availableQuests", playerId);

            var cached = redisService.get(cachedKey, new TypeReference<List<GetAvailableQuestsResponse>>() {
            });

            if (cached != null) {
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
                    .filter(q -> {
                        if (q.getPrerequisiteQuest() == null)
                            return true;
                        var prereq = playerQuestsMap.get(q.getPrerequisiteQuest().getId());

                        if (prereq != null) {
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
                    .map(quest -> {
                        var objectiveList = questObjectiveRepository.findByQuest_Id(quest.getId());
                        if (objectiveList.isEmpty()) return null;
                        var mappedObjective = objectiveList.stream()
                                .map(obj -> {
                                    switch (obj.getObjectiveType()) {
                                        case LEVEL_UP -> {
                                            return new ObjectiveOfQuest(obj.getDescription(), obj.getTargetRealm().getName(), obj.getTargetRealmStage().getStageName());
                                        }
                                        case KILL_MONSTER -> {
                                            return new ObjectiveOfQuest(obj.getDescription(), obj.getRequiredCount(), obj.getMonsterId());
                                        }
                                        case REACH_MAP -> {
                                            return new ObjectiveOfQuest(obj.getDescription(), obj.getTargetMap().getMapId());
                                        }
                                        case REACH_COORDINATE -> {
                                            return new ObjectiveOfQuest(obj.getDescription(), obj.getTargetMap().getMapId(), obj.getTargetX(), obj.getTargetY());
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

            if (filteredQuest.isEmpty()) {
                return ServiceResult.success("You have done all quests", HttpStatus.NO_CONTENT);
            }

            return ServiceResult.success("Get Available Quests", filteredQuest, HttpStatus.OK);
        } catch (Exception e) {
            return ServiceResult.failure(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResult<List<GetAcceptedQuestsResponse>> getAcceptedQuests(UUID playerId) {
        try {
            var player = playerService.getPlayerEntityById(playerId).getData();

            if (player == null) {
                return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
            }

            var playerQuests = playerQuestRepository
                    .findByPlayer_Id(playerId)
                    .stream()
                    .filter(pq -> pq.getStatus() == PlayerQuestStatus.IN_PROGRESS)
                    .toList();

            var playerLocationOptional =
                    playerLocationRepository.findByPlayer_Id(playerId);

            if (playerLocationOptional.isEmpty()) {
                return ServiceResult.failure("Location not found", HttpStatus.NOT_FOUND);
            }

            var playerRealm = cultivationRealmRepository.getReferenceById(player.getRealmId());
            var playerRealmStage = realmStageRepository.getReferenceById(player.getRealmStage());

            var acceptedQuests = playerQuests.stream()
                    .map(playerQuest -> {

                        var quest = playerQuest.getQuest();

                        var progresses = playerQuestObjectiveProgressRepository
                                .findByPlayerQuest_Id(playerQuest.getId());

                        List<ObjectiveAndProgressOfQuest> objectives = progresses.stream()
                                .<ObjectiveAndProgressOfQuest>map(progress -> {

                                    var objective = progress.getQuestObjective();

                                    return switch (objective.getObjectiveType()) {

                                        case LEVEL_UP -> new LevelUpObjective(
                                                objective.getDescription(),
                                                objective.getTargetRealm().getName(),
                                                playerRealm.getName(),
                                                objective.getTargetRealmStage().getStageName(),
                                                playerRealmStage.getStageName(),
                                                progress.getIsCompleted(),
                                                QuestObjectiveTypes.LEVEL_UP
                                        );


                                        case KILL_MONSTER -> new KillMonsterObjective(
                                                objective.getDescription(),
                                                objective.getMonsterId(),
                                                objective.getRequiredCount(),
                                                progress.getCurrentCount(),
                                                progress.getIsCompleted(),
                                                QuestObjectiveTypes.KILL_MONSTER
                                        );

                                        case REACH_MAP -> new ReachMapObjective(
                                                objective.getDescription(),
                                                objective.getTargetMap().getMapId(),
                                                playerLocationOptional.get().getCurrentMap().getMapId(),
                                                progress.getIsCompleted(),
                                                QuestObjectiveTypes.REACH_MAP
                                        );


                                        case REACH_COORDINATE -> new ReachCoordinateObjective(
                                                objective.getDescription(),
                                                objective.getTargetMap().getMapId(),
                                                playerLocationOptional.get().getCurrentMap().getMapId(),
                                                objective.getTargetX(),
                                                objective.getTargetY(),
                                                playerLocationOptional.get().getX(),
                                                playerLocationOptional.get().getY(),
                                                progress.getIsCompleted(),
                                                QuestObjectiveTypes.REACH_COORDINATE
                                        );


                                        case COLLECT_ITEM -> new CollectItemObjective(
                                                objective.getDescription(),
                                                objective.getItem().getId(),
                                                objective.getRequiredCount(),
                                                progress.getCurrentCount(),
                                                progress.getIsCompleted(),
                                                QuestObjectiveTypes.COLLECT_ITEM
                                        );
                                    };
                                })
                                .toList();

                        return new GetAcceptedQuestsResponse(
                                quest.getId(),
                                quest.getName(),
                                quest.getRequiredRealm().getName(),
                                objectives
                        );
                    })
                    .toList();

            return ServiceResult.success(
                    acceptedQuests.isEmpty() ? "No accepted quests" : "Get accepted quests",
                    acceptedQuests,
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return ServiceResult.failure(
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    public ServiceResult<Void> acceptQuest(UUID playerId, Integer questId) {
        try {
            var questList = getAvailableQuests(playerId).getData();

            if (questList.isEmpty()) {
                return ServiceResult.failure("Not available quest", HttpStatus.NOT_FOUND);
            }

            var questMap = questList.stream().collect(Collectors.toMap(GetAvailableQuestsResponse::id, Function.identity()));

            if (!questMap.containsKey(questId)) {
                return ServiceResult.failure("Not available quest", HttpStatus.NOT_FOUND);
            }

            var quest = questRepository.findById(questId);
            if (quest.isEmpty()) {
                return ServiceResult.failure("Quest not found", HttpStatus.NOT_FOUND);
            }

            var player = playerService.getPlayerEntityById(playerId).getData();
            if (player == null) {
                return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
            }

            var playerQuest = new PlayerQuest();
            playerQuest.setPlayer(player);
            playerQuest.setQuest(quest.get());
            playerQuest.setStatus(PlayerQuestStatus.IN_PROGRESS);
            playerQuestRepository.save(playerQuest);

            var questObjectives = questObjectiveRepository.findByQuest_Id(questId);

            if (questObjectives.isEmpty()) {
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

            var cachedKey = CacheKeys.quests("availableQuests", playerId);
            redisService.delete(cachedKey);

            return ServiceResult.success("Accepted quest", HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ServiceResult.failure("Error accepting quest", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResult<Void> claimQuestReward(UUID playerId, Integer questId) {
        try {
            if (playerId == null || questId == null) {
                return ServiceResult.failure("Player id and quest id are required", HttpStatus.BAD_REQUEST);
            }

            var player = playerService.getPlayerEntityById(playerId).getData();

            if (player == null) {
                return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
            }

            var playerQuestOptional = playerQuestRepository.findByPlayer_IdAndQuest_Id(playerId, questId);

            if (playerQuestOptional.isEmpty()) {
                return ServiceResult.failure("Quest not found for player", HttpStatus.NOT_FOUND);
            }

            var playerQuest = playerQuestOptional.get();

            if (playerQuest.getStatus() != PlayerQuestStatus.COMPLETED) {
                return ServiceResult.failure("Quest is not completed", HttpStatus.BAD_REQUEST);
            }

            var rewards = questRewardRepository.findByQuest_Id(questId);

            if (rewards.isEmpty()) {
                return ServiceResult.failure("Quest has no rewards", HttpStatus.BAD_REQUEST);
            }

            for (var reward : rewards) {

                switch (reward.getRewardType()) {

                    case SPIRIT_STONE -> {
                        long amount = Objects.requireNonNullElse(reward.getAmount(), 0L);
                        player.setSpiritStone(player.getSpiritStone() + amount);
                    }

                    case CULTIVATION_POINT -> {
                        long amount = Objects.requireNonNullElse(reward.getAmount(), 0L);
                        player.setCultivationPoint(player.getCultivationPoint() + amount);
                    }

                    case REPUTATION -> {
                        long amount = Objects.requireNonNullElse(reward.getAmount(), 0L);
                        player.setReputation(player.getReputation() + amount);
                    }

                    case ITEM -> {
                        if (reward.getItem() == null) {
                            return ServiceResult.failure("Quest item reward is invalid", HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                        long amount = Objects.requireNonNullElse(reward.getAmount(), 1L);
                        if (amount <= 0) {
                            return ServiceResult.failure("Quest item reward amount is invalid", HttpStatus.INTERNAL_SERVER_ERROR);
                        }

                        var result = playerInventoryService.addToInventory(
                                new AddToInventoryRequest(
                                        reward.getItem().getId(),
                                        Math.toIntExact(amount)
                                ), playerId
                        );

                        if (!result.isSuccess()) {
                            return ServiceResult.failure(result.getMessage(), HttpStatus.resolve(result.getStatusCode()));
                        }
                    }

                    case SKILL -> {
                        // TODO: LOW, Implement adding the rewarded skill to the player's skills.
                    }
                }
            }

            playerQuest.setStatus(PlayerQuestStatus.CLAIMED);
            playerQuest.setClaimedAt(LocalDateTime.now());

            // If you track completion count:
            //TODO: LOW, Check completion count in quest
            playerQuest.setCompletionCount(playerQuest.getCompletionCount() + 1);

            playerQuestRepository.save(playerQuest);

            return ServiceResult.success("Quest rewards claimed successfully", HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            return ServiceResult.failure("Error claiming quest reward", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
