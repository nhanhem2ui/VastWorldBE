package com.vastworld.vwbe.dto.quests.objectiveAndProgress;

import com.vastworld.vwbe.enums.quests.QuestObjectiveTypes;

public record ReachMapObjective(
        String description,
        Integer mapId,
        Integer currentMapId,
        Boolean isCompleted,
        QuestObjectiveTypes objectiveType
) implements ObjectiveAndProgressOfQuest {}
