package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.PlayerInventory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerInventoryRepository extends JpaRepository<PlayerInventory, Long> {

    @EntityGraph(attributePaths = {"item"})
    List<PlayerInventory> findByPlayer_Id(UUID id);
}