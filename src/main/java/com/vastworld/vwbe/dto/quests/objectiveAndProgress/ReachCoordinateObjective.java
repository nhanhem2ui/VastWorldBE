package com.vastworld.vwbe.dto.quests.objectiveAndProgress;

import com.vastworld.vwbe.enums.quests.QuestObjectiveTypes;

public record ReachCoordinateObjective(
        String description,
        Integer mapId,
        Integer currentMapId,
        Integer targetX,
        Integer targetY,
        Integer currentX,
        Integer currentY,
        Boolean isCompleted,
        QuestObjectiveTypes objectiveType
) implements ObjectiveAndProgressOfQuest {}
