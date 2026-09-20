package com.vastworld.vwbe.listeners;

import com.vastworld.vwbe.dto.listeners.PLayerLeveledUpEvent;
import com.vastworld.vwbe.dto.listeners.PlayerTravelEvent;
import com.vastworld.vwbe.dto.listeners.QuestCompletedEvent;
import com.vastworld.vwbe.dto.listeners.QuestCompletedNotification;
import com.vastworld.vwbe.entites.*;
import com.vastworld.vwbe.enums.quests.PlayerQuestStatus;
import com.vastworld.vwbe.repositories.*;
import com.vastworld.vwbe.services.CultivationRealmService;
import com.vastworld.vwbe.services.RealmStageService;
import com.vastworld.vwbe.services.SseNotificationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
@Transactional
public class QuestEventListener {
    private final PlayerQuestRepository playerQuestRepository;
    private final QuestObjectiveRepository questObjectiveRepository;
    private final PlayerLocationRepository playerLocationRepository;
    private final PlayerQuestObjectiveProgressRepository progressRepository;
    private final QuestRepository questRepository;
    private final SseNotificationService sseNotificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final QuestRewardRepository questRewardRepository;
    private final PlayerRepository playerRepository;
    private final CultivationRealmService cultivationRealmService;
    private final RealmStageService realmStageService;

    public QuestEventListener(
            PlayerQuestRepository playerQuestRepository, QuestObjectiveRepository questObjectiveRepository,
            PlayerLocationRepository playerLocationRepository, PlayerQuestObjectiveProgressRepository progressRepository,
            QuestRepository questRepository, QuestRewardRepository questRewardRepository,
            SseNotificationService sseNotificationService,ApplicationEventPublisher eventPublisher,
            PlayerRepository playerRepository, CultivationRealmService cultivationRealmService,
            RealmStageService realmStageService) {

        this.playerQuestRepository = playerQuestRepository;
        this.questObjectiveRepository = questObjectiveRepository;
        this.playerLocationRepository = playerLocationRepository;
        this.progressRepository = progressRepository;
        this.questRepository = questRepository;
        this.questRewardRepository = questRewardRepository;
        this.sseNotificationService = sseNotificationService;
        this.eventPublisher = eventPublisher;
        this.playerRepository = playerRepository;
        this.cultivationRealmService = cultivationRealmService;
        this.realmStageService = realmStageService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPlayerLeveledUp(@Valid PLayerLeveledUpEvent event){
        var playerQuests = playerQuestRepository.findByPlayer_IdAndStatus(event.playerId(), PlayerQuestStatus.IN_PROGRESS);
        var playerOptional = playerRepository.findById(event.playerId());

        if(playerOptional.isEmpty()){
            log.warn("No playerId {} at onPlayerLeveledUp", event.playerId());
            return;
        }

        var player = playerOptional.get();

        for (var playerQuest : playerQuests) {
            var objectives = questObjectiveRepository.findLeveledUpQuests(playerQuest.getQuest().getId());
            for(var objective : objectives){

                var realmResult = cultivationRealmService.getCultivationRealmEntityById(player.getRealmId());
                var realmEntity = new CultivationRealm();
                if(realmResult.isSuccess()){
                    realmEntity =  realmResult.getData();
                }
                else{
                    log.warn("Cultivation realm id {} not exists in onPlayerLeveledUp event", player.getRealmId());
                    return;
                }

                var stageResult = realmStageService.getRealmStageEntityById(player.getRealmId());
                var stageEntity = new RealmStage();
                if(stageResult.isSuccess()){
                    stageEntity =  stageResult.getData();
                }
                else{
                    log.warn("Realm stage {} not exists in onPlayerLeveledUp event", player.getRealmStage());
                    return;
                }
                if(realmEntity.equals(objective.getTargetRealm()) && stageEntity.equals(objective.getTargetRealmStage())){
                    markObjectiveComplete(playerQuest, objective);
                }
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPlayerTraveled(@Valid PlayerTravelEvent event) {
        var playerQuests = playerQuestRepository.findByPlayer_IdAndStatus(event.playerId(), PlayerQuestStatus.IN_PROGRESS);

        var playerLocationOptional = playerLocationRepository.findByPlayer_Id(event.playerId());
        if (playerLocationOptional.isEmpty()) {
            log.warn("Player {} has no location", event.playerId());
            return;
        }

        var playerLocation = playerLocationOptional.get();

        for (var playerQuest : playerQuests) {
            var objectives = questObjectiveRepository.findMapQuests(playerQuest.getQuest().getId());

            for (var objective : objectives) {
                boolean met = switch (objective.getObjectiveType()) {
                    case REACH_MAP -> playerLocation.getCurrentMap().equals(objective.getTargetMap());
                    case REACH_COORDINATE -> playerLocation.getCurrentMap().equals(objective.getTargetMap())
                            && playerLocation.getX().equals(objective.getTargetX())
                            && playerLocation.getY().equals(objective.getTargetY());
                    default -> false;
                };

                if (met) {
                    markObjectiveComplete(playerQuest, objective);
                }
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onQuestComplete(@Valid QuestCompletedEvent event) {

        var quest = questRepository.findById(event.questId()).orElse(null);

        if (quest == null) {
            log.warn(
                    "Quest {} not found for completion event, player {}",
                    event.questId(),
                    event.playerId()
            );
            return;
        }

        var payload = new QuestCompletedNotification(
                quest.getId(),
                quest.getName(),
                quest.getDescription(),
                quest.getCompletedImageUrl(),
                event.rewardsText()
        );

        sseNotificationService.sendToPlayer(
                event.playerId(),
                "quest-completed",
                payload
        );
    }

    private void markObjectiveComplete(PlayerQuest playerQuest, QuestObjective objective) {

        var progressOptional = progressRepository.findByPlayerQuest_IdAndQuestObjective_Id(
                playerQuest.getId(), objective.getId());

        if (progressOptional.isEmpty()) {
            log.warn("No progress row for playerQuest {} objective {}", playerQuest.getId(), objective.getId());
            return;
        }

        var progress = progressOptional.get();
        if (progress.getIsCompleted()) {
            return;
        }

        progress.setIsCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);

        var allProgress = progressRepository.findByPlayerQuest_Id(playerQuest.getId());
        boolean allDone = allProgress.stream().allMatch(PlayerQuestObjectiveProgress::getIsCompleted);
        
        if (allDone) {
            playerQuest.setStatus(PlayerQuestStatus.COMPLETED);
            playerQuestRepository.save(playerQuest);

            var questId = playerQuest.getQuest().getId();
            var rewardsText = formatRewards(questId);

            giveRewards(questId, playerQuest.getPlayer().getId());

            eventPublisher.publishEvent(new QuestCompletedEvent(
                    playerQuest.getPlayer().getId(),
                    questId,
                    rewardsText
            ));
        }
    }

    private String formatRewards(Integer questId) {
        var rewardsOptional = questRewardRepository.findByQuest_Id(questId);
        if(rewardsOptional.isEmpty()) {
            return "";
        }
        var rewards = rewardsOptional.get();
        return rewards.stream()
                .map(reward -> switch (reward.getRewardType()) {
                    case SPIRIT_STONE -> "x" + reward.getAmount() + " Spirit Stone";

                    case CULTIVATION_POINT -> "x" + reward.getAmount() + " Cultivation Point";

                    case REPUTATION -> "x" + reward.getAmount() + " Reputation";

                    case ITEM -> "x" + reward.getAmount() + " " +
                                    reward.getItem().getName();

                    case SKILL ->"x" + reward.getAmount() + " " +
                                    reward.getSkill().getName();
                })
                .collect(Collectors.joining(System.lineSeparator()));
    }

    //TODO: Finish 3 cases missing
    private void giveRewards(Integer questId, UUID playerId) {
        var rewardsOptional = questRewardRepository.findByQuest_Id(questId);
        if(rewardsOptional.isEmpty()) {
            return;
        }
        var rewards = rewardsOptional.get();
        var playerOptional = playerRepository.findById(playerId);

        if(playerOptional.isEmpty()) {
            log.warn("No player with id {} found at giveRewards", playerId);
            return;
        }

        var player = playerOptional.get();

        for(var reward : rewards) {
            switch (reward.getRewardType()) {
                case CULTIVATION_POINT ->
                        player.setCultivationPoint(player.getCultivationPoint() + reward.getAmount());
                case SPIRIT_STONE ->
                        player.setSpiritStone(player.getSpiritStone() + reward.getAmount());
//                case ITEM ->;
//                case REPUTATION -> ;
//                case SKILL -> ;
            }
            playerRepository.save(player);
        }
    }
}
