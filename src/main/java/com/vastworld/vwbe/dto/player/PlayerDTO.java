package com.vastworld.vwbe.dto.player;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlayerDTO(
        @NotNull
        UUID id,
        @NotNull
        UUID accountId,
        @NotEmpty
        String accountUsername,
        @NotEmpty
        String accountEmail,
        @Positive
        Integer realmId,
        String realmName,
        Boolean gender,
        Integer rollNum,
        @Positive
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
        @PastOrPresent
        LocalDateTime createdAt
) {
}
