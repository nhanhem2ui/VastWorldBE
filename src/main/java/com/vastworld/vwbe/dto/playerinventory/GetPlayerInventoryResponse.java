package com.vastworld.vwbe.dto.playerinventory;

import java.util.UUID;

public record GetPlayerInventoryResponse(
    UUID itemId,
    String itemImageUrl,
    Integer quantity
){}
