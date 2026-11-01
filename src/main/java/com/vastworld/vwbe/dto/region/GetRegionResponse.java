package com.vastworld.vwbe.dto.region;

import java.util.List;

public record GetRegionResponse(
        String name,
        Integer width,
        Integer height,
        String backgroundImage,
        List<DecorationsOfRegion> regionDecorations
){}
