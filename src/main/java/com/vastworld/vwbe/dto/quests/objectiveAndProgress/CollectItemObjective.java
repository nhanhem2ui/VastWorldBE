package com.vastworld.vwbe.dto.quests.objectiveAndProgress;

import com.vastworld.vwbe.enums.quests.QuestObjectiveTypes;

import java.util.UUID;

public record CollectItemObjective(
        String description,
        UUID itemId,
        Integer requiredCount,
        Integer currentCount,
        Boolean isCompleted,
        QuestObjectiveTypes objectiveType
) implements ObjectiveAndProgressOfQuest {}
