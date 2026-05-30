package com.vastworld.vwbe.dto.realmbreakthrough;

public record RealmBreakthroughDTO(
        Integer id,
        Integer realmId,
        String realmName,
        Integer stageLevel,
        Long requiredCultivationPoint,
        Boolean requiresTribulation,
        Double successRate
) {
}
