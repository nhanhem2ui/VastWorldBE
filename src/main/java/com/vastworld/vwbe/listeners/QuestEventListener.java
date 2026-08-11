package com.vastworld.vwbe.listeners;

import com.vastworld.vwbe.dto.listeners.PlayerTravelEvent;
import com.vastworld.vwbe.repositories.PlayerLocationRepository;
import com.vastworld.vwbe.repositories.PlayerQuestRepository;
import com.vastworld.vwbe.repositories.QuestObjectiveRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@Transactional
public class QuestEventListener {
    private final PlayerQuestRepository playerQuestRepository;
    private final QuestObjectiveRepository questObjectiveRepository;
    private final PlayerLocationRepository playerLocationRepository;
    public QuestEventListener(PlayerQuestRepository playerQuestRepository, QuestObjectiveRepository questObjectiveRepository,
                              PlayerLocationRepository playerLocationRepository) {
        this.playerQuestRepository = playerQuestRepository;
        this.questObjectiveRepository = questObjectiveRepository;
        this.playerLocationRepository = playerLocationRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPlayerTraveled(@Valid PlayerTravelEvent event){
            var playerQuests = playerQuestRepository.findByPlayer_Id(event.playerId());

            var playerLocationOptional = playerLocationRepository.findByPlayer_Id(event.playerId());

            if(playerLocationOptional.isEmpty()){
                log.warn("Player {} has no location", event.playerId());
                return;
            }

            var playerLocation = playerLocationOptional.get();

            var mapObjectivesOfQuests = playerQuests.stream()
                    .flatMap(quest -> questObjectiveRepository.findMapQuests(quest.getId().intValue())
                            .stream()).toList();

            for (var objective : mapObjectivesOfQuests) {
                switch (objective.getObjectiveType()) {
                    case REACH_MAP -> {
                        if(playerLocation.getCurrentMap().equals(objective.getTargetMap())){

                        }
                    }
                    case REACH_COORDINATE -> {

                    }
                }
            }
    }
}
