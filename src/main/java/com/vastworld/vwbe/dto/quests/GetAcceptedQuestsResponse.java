package com.vastworld.vwbe.dto.quests;

import com.vastworld.vwbe.dto.quests.objectiveAndProgress.ObjectiveAndProgressOfQuest;

import java.util.List;

public record GetAcceptedQuestsResponse(
        Integer id,
        String name,
        String requiredRealm,
        List<ObjectiveAndProgressOfQuest> objectiveAndProgressOfQuests
) {}
