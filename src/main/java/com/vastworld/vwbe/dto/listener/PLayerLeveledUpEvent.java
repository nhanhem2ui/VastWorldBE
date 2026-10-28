package com.vastworld.vwbe.dto.listener;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PLayerLeveledUpEvent(
        @NotNull
        UUID playerId
) {}
