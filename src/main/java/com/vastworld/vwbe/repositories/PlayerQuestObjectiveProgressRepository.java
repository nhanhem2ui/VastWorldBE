package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.PlayerQuestObjectiveProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerQuestObjectiveProgressRepository extends JpaRepository<PlayerQuestObjectiveProgress, Long> {
    Optional<PlayerQuestObjectiveProgress> findByPlayerQuest_IdAndQuestObjective_Id(Long playerQuestId, Integer questObjectiveId);

    List<PlayerQuestObjectiveProgress> findByPlayerQuest_Id(Long playerQuestId);
}
