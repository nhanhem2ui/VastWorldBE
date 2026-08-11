package com.vastworld.vwbe.dto.quests.objectiveAndProgress;

import java.util.UUID;

public record CollectItemObjective(
        String description,
        UUID itemId,
        int requiredCount,
        int currentCount
) implements ObjectiveAndProgressOfQuest {}
