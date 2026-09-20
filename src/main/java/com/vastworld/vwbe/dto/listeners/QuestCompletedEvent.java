package com.vastworld.vwbe.dto.listeners;
import java.util.UUID;

public record QuestCompletedEvent(
        UUID playerId,
        Integer questId,
        String rewardsText
) {}
