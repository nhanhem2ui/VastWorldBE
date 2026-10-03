package com.vastworld.vwbe.dto.playerinventory;

import java.util.UUID;

public record GetEquipmentsResponse(
        UUID id,
        UUID itemId,
        String itemImageUrl,
        String itemName,
        StatsOfItem statsOfItem,
        String equipmentSlotType
) {}
