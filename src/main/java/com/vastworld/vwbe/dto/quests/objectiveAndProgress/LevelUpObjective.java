package com.vastworld.vwbe.dto.quests.objectiveAndProgress;

public record LevelUpObjective(
        String description,
        String targetRealm,
        String currentRealm,
        String targetRealmStage,
        String currentRealmStage
) implements ObjectiveAndProgressOfQuest {}
