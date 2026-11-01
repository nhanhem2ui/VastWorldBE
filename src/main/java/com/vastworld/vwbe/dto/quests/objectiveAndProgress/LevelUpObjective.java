package com.vastworld.vwbe.dto.quests.objectiveAndProgress;

import com.vastworld.vwbe.enums.quests.QuestObjectiveTypes;

public record LevelUpObjective(
        String description,
        String targetRealm,
        String currentRealm,
        String targetRealmStage,
        String currentRealmStage,
        Boolean isCompleted,
        QuestObjectiveTypes objectiveType
) implements ObjectiveAndProgressOfQuest {}
