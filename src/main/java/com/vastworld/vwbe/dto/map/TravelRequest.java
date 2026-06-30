package com.vastworld.vwbe.dto.map;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

@NotNull
public record TravelRequest(
        @NotNull
        UUID playerId,
        @PositiveOrZero
        Integer x,
        @PositiveOrZero
        Integer y,
        @PositiveOrZero
        Integer mapId
){}
