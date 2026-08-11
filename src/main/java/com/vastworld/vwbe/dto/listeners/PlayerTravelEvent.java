package com.vastworld.vwbe.dto.listeners;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record PlayerTravelEvent(
    @NotNull
    UUID playerId,
    @PositiveOrZero
    Integer mapId,
    @PositiveOrZero
    Integer x,
    @PositiveOrZero
    Integer y
) {}
