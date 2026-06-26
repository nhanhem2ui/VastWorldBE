package com.vastworld.vwbe.dto.map;

import java.util.List;

public record GetMapResponse(
        String mapName,
        Integer mapWidth,
        Integer mapHeight,
        List<TilesOfMaps> mapTiles
){}