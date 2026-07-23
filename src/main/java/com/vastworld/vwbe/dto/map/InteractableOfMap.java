package com.vastworld.vwbe.dto.map;

import com.vastworld.vwbe.common.enums.MapInteractableTypes;

public record InteractableOfMap(
        Integer x,
        Integer y,
        MapInteractableTypes type
) { }
