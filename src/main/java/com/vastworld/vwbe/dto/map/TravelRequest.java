package com.vastworld.vwbe.dto.map;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@NotNull
public record TravelRequest(
        @PositiveOrZero
        Integer x,
        @PositiveOrZero
        Integer y,
        @PositiveOrZero
        Integer mapId
){}
