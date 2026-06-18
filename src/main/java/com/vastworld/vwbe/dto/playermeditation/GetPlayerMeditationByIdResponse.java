package com.vastworld.vwbe.dto.playermeditation;

import java.time.LocalDateTime;

public record GetPlayerMeditationByIdResponse(
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long cultivationPerMinute,
        Long totalCultivationReward,
        Boolean isClaimed
){}
