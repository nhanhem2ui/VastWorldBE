package com.vastworld.vwbe.dto.map;

import com.vastworld.vwbe.enums.MapInteractableTypes;

public record InteractableOfMap(
        Integer x,
        Integer y,
        MapInteractableTypes type
) { }
