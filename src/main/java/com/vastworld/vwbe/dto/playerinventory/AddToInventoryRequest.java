package com.vastworld.vwbe.dto.playerinventory;

import java.util.UUID;

public record AddToInventoryRequest (
    UUID itemId,
    Integer quantity
){}
