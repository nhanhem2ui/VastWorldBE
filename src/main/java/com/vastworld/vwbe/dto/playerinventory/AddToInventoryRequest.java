package com.vastworld.vwbe.dto.playerinventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@NotNull
public record AddToInventoryRequest (
    @NotNull
    UUID itemId,
    @Positive
    Integer quantity
){}
