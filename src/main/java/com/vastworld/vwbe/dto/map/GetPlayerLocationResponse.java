package com.vastworld.vwbe.dto.map;

public record GetPlayerLocationResponse(
        Integer mapId,
        Integer x,
        Integer y
){}
