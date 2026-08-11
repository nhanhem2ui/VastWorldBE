package com.vastworld.vwbe.dto.quests;

import com.vastworld.vwbe.enums.quests.QuestObjectiveTypes;

import java.util.UUID;

public record ObjectiveOfQuest(
        QuestObjectiveTypes objectiveType,
        String description,
        Integer requiredCount,
        Integer monsterId,
        UUID itemId,
        String targetRealm,
        String targetRealmStage,
        Integer targetMap,
        Integer targetX,
        Integer targetY
) {

    //LEVEL_UP Constructor
    public ObjectiveOfQuest(String description, String targetRealm, String targetRealmStage) {
        this(QuestObjectiveTypes.LEVEL_UP, description, null,
                null, null, targetRealm, targetRealmStage, null, null, null);
    }

    //KILL_MONSTER Constructor
    public ObjectiveOfQuest(String description, Integer requiredCount, Integer monsterId) {
        this(QuestObjectiveTypes.KILL_MONSTER, description, requiredCount,
                monsterId, null, null, null, null, null, null);
    }

    //REACH_MAP Constructor
    public ObjectiveOfQuest(String description, Integer targetMap) {
        this(QuestObjectiveTypes.REACH_MAP, description, 1,
                null, null, null, null, targetMap, null, null);
    }

    //REACH_COORDINATE Constructor
    public ObjectiveOfQuest(String description, Integer targetMap, Integer targetX, Integer targetY) {
        this(QuestObjectiveTypes.REACH_COORDINATE, description, null,
                null, null, null, null, targetMap, targetX, targetY);
    }

    //COLLECT_ITEM Constructor
    public ObjectiveOfQuest(String description, Integer requiredCount, UUID itemId) {
        this(QuestObjectiveTypes.COLLECT_ITEM, description, requiredCount,
                null, itemId, null, null, null, null, null);
    }
}