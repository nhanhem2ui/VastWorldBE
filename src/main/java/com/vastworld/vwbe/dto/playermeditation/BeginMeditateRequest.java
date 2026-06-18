package com.vastworld.vwbe.dto.playermeditation;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record BeginMeditateRequest(
        @NotNull
        UUID playerId,
        @NotNull
        @Min(1)
        @Max(1440) //24hr
        Integer durationMinutes
){}