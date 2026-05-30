package com.vastworld.vwbe.dto.playermeditation;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlayerMeditationDTO(
        Long id,
        UUID playerId,
        LocalDateTime startTime,
        Integer durationMinutes,
        LocalDateTime endTime,
        Long cultivationPerMinute,
        Long totalCultivationReward,
        Boolean isCompleted,
        Boolean isClaimed
) {
}
