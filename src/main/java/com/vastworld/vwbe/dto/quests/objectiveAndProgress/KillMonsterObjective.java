package com.vastworld.vwbe.dto.quests.objectiveAndProgress;

import com.vastworld.vwbe.enums.quests.QuestObjectiveTypes;

public record KillMonsterObjective(
        String description,
        Integer monsterId,
        Integer requiredCount,
        Integer currentCount,
        Boolean isCompleted,
        QuestObjectiveTypes objectiveType
) implements ObjectiveAndProgressOfQuest {}