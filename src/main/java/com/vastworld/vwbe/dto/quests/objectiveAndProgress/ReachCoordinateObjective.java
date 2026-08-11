package com.vastworld.vwbe.dto.quests.objectiveAndProgress;

public record ReachCoordinateObjective(
        String description,
        Integer mapId,
        Integer currentMapId,
        Integer targetX,
        Integer targetY,
        Integer currentX,
        Integer currentY
) implements ObjectiveAndProgressOfQuest {}
