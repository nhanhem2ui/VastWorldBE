package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.PlayerQuest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerQuestRepository extends JpaRepository<PlayerQuest, Integer> {

    @EntityGraph(attributePaths = {"quest"})
    List<PlayerQuest> findByPlayer_Id(UUID id);

}
