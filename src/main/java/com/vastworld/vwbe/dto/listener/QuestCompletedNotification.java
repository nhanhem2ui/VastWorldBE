package com.vastworld.vwbe.dto.listener;

public record QuestCompletedNotification(
        Integer questId,
        String questName,
        String description,
        String completedImageUrl,
        String rewardsText
) {}