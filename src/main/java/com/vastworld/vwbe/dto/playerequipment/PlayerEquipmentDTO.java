package com.vastworld.vwbe.dto.playerequipment;

import java.util.UUID;

public record PlayerEquipmentDTO(
        Long id,
        UUID playerId,
        Long inventoryId,
        UUID itemId,
        String itemName,
        String equipmentSlot
) {
}
