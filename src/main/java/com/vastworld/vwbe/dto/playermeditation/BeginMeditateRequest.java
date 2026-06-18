package com.vastworld.vwbe.dto.playermeditation;

import java.time.LocalDateTime;
import java.util.UUID;

public record BeginMeditateRequest(
        UUID playerId,
        LocalDateTime startTime,
        Integer durationMinutes
){}