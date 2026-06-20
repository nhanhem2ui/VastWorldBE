package com.vastworld.vwbe.dto.player;

public record GetPlayerNextBreakthroughResponse(
    String nextRealm,
    String nextStage,
    Boolean isTribulation,
    Long breakthroughPoints,
    Float chanceOfSuccess
) {}
