package com.vastworld.vwbe.dto.quests;

import java.util.List;

public record GetAvailableQuestsResponse(
    Integer id,
    String name,
    String requiredRealm,
    List<ObjectiveOfQuest> questObjectives
){}
