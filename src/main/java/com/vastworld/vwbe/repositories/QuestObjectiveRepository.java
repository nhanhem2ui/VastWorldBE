package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.QuestObjective;
import com.vastworld.vwbe.enums.quests.QuestObjectiveTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestObjectiveRepository extends JpaRepository<QuestObjective, Integer> {
    List<QuestObjective> findByQuest_Id(Integer id);

    @Query("select q from QuestObjective q " +
            "where q.quest.id = ?1 and (q.objectiveType = 'REACH_MAP' " +
            "or q.objectiveType = 'REACH_COORDINATE')")
    List<QuestObjective> findMapQuests(Integer id);
}

