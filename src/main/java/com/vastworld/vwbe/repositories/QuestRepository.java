package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.Quest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestRepository extends JpaRepository<Quest, Integer> {
    @EntityGraph(attributePaths = {"prerequisiteQuest"})
    @Query("SELECT q FROM Quest q WHERE q.requiredRealm.realmOrder <= ?1 OR q.requiredRealm IS NULL")
    List<Quest> findUnlockedByRealm(Integer realmOrder);


}
