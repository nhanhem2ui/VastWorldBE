package com.vastworld.vwbe.dto.playerinventory;

import java.util.UUID;

public record PlayerInventoryDTO(
        Long id,
        UUID playerId,
        UUID itemId,
        String itemName,
        Integer quantity
) {
}
