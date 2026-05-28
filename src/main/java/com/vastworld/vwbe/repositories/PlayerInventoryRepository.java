package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.PlayerInventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerInventoryRepository extends JpaRepository<PlayerInventory, Long> {
}