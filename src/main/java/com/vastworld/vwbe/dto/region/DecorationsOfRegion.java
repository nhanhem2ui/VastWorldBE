package com.vastworld.vwbe.dto.region;

public record DecorationsOfRegion(
        Integer mapId,
        String label,
        Integer x,
        Integer y,
        Integer width,
        Integer height,
        Integer topHeight,
        Integer rightWidth,
        Integer bottomHeight,
        Integer leftWidth,
        String backgroundTexture,
        String textColor
){}
