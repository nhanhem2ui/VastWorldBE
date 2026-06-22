package com.vastworld.vwbe.dto.player;

public record GetPlayerNextBreakthroughResponse(
    Boolean isTribulation,
    Long breakthroughPoints,
    Float chanceOfSuccess
) {}
