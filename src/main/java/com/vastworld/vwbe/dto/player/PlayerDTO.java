package com.vastworld.vwbe.dto.player;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlayerDTO(
        UUID id,
        UUID accountId,
        String accountUsername,
        String accountEmail,
        Integer realmId,
        String realmName,
        Boolean gender,
        Integer rollNum,
        Integer realmStage,
        String realmStageName,
        Long hp,
        Long attack,
        Long defense,
        Double critRate,
        Double critDamage,
        Double speed,
        Double lifeSteal,
        Double cultivationSpeed,
        Long cultivationPoint,
        Long reputation,
        Long spiritStone,
        LocalDateTime createdAt
) {
}
