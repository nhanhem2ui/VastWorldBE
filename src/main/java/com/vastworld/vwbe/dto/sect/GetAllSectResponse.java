package com.vastworld.vwbe.dto.sect;

public record GetAllSectResponse(
        Integer id,
        String name,
        String description,
        String regionName,
        String mapName,
        Integer x,
        Integer y,
        String imageUrl
) {}
