package com.vastworld.vwbe.dto.quests.objectiveAndProgress;

public sealed interface ObjectiveAndProgressOfQuest permits
        KillMonsterObjective,
        CollectItemObjective,
        ReachMapObjective,
        ReachCoordinateObjective,
        LevelUpObjective {}
