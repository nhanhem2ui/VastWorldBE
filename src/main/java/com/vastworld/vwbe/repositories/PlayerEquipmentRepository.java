package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.PlayerEquipment;
import com.vastworld.vwbe.enums.EquipmentSlotTypes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerEquipmentRepository extends JpaRepository<PlayerEquipment, UUID> {
    List<PlayerEquipment> findByPlayer_Id(UUID id);
    Optional<PlayerEquipment> findByPlayer_IdAndEquipmentSlotType(UUID id, EquipmentSlotTypes equipmentSlotType);
}
