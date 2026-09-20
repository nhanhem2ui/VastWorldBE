package com.vastworld.vwbe.dto.listeners;

public record QuestCompletedNotification(
        Integer questId,
        String questName,
        String description,
        String completedImageUrl,
        String rewardsText
) {}