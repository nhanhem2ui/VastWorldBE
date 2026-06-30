package com.vastworld.vwbe.dto.map;

import com.vastworld.vwbe.common.enums.AssetType;

public record AssetTile(
        Boolean isAnimated,
        String assetUrl,
        String frameConfig,
        Double offsetX,
        Double offsetY,
        Double width,
        Double height,
        AssetType assetType
){
}
