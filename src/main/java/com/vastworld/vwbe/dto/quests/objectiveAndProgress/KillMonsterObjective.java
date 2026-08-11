package com.vastworld.vwbe.dto.quests.objectiveAndProgress;

public record KillMonsterObjective(
        String description,
        int monsterId,
        int requiredCount,
        int currentCount
) implements ObjectiveAndProgressOfQuest {}