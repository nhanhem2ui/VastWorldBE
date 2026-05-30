package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.PlayerEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerEquipmentRepository extends JpaRepository<PlayerEquipment, Long> {
    boolean existsByPlayer_IdAndEquipmentSlotIgnoreCase(UUID playerId, String equipmentSlot);
    boolean existsByPlayer_IdAndEquipmentSlotIgnoreCaseAndIdNot(UUID playerId, String equipmentSlot, Long id);
    boolean existsByInventory_Id(Long inventoryId);
    boolean existsByInventory_IdAndIdNot(Long inventoryId, Long id);
}
