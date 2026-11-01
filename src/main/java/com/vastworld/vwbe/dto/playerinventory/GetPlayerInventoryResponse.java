package com.vastworld.vwbe.dto.playerinventory;

import java.util.UUID;

public record GetPlayerInventoryResponse(
    UUID itemId,
    String itemName,
    String itemImageUrl,
    StatsOfItem stats,
    String itemType,
    Integer quantity,
    Boolean combatOnly
){}
