package com.vastworld.vwbe.dto.quests.objectiveAndProgress;

public record ReachMapObjective(
        String description,
        Integer mapId,
        Integer currentMapId
) implements ObjectiveAndProgressOfQuest {}
