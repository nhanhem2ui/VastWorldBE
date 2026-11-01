package com.vastworld.vwbe.dto.item;

import java.util.UUID;

public record ItemDTO(
        UUID id,
        String name,
        Integer itemTypeId,
        String itemTypeName,
        Integer maxUsageCount,
        Integer requiredRealmId,
        String requiredRealmName,
        Long hp,
        Long attack,
        Long defense,
        Double critRate,
        Double critDamage,
        Double speed,
        Double lifeSteal,
        Double cultivationSpeed
) {
}
