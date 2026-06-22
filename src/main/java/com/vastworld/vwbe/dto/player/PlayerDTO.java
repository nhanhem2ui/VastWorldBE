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
        @NotNull
        Integer rollNum,
        @Positive
        Integer realmStage,
        String realmStageName,
        @Positive
        Long hp,
        @Positive
        Long attack,
        @Positive
        Long defense,
        @Positive
        Double critRate,
        @Positive
        Double critDamage,
        @Positive
        Double speed,
        @Positive
        Double lifeSteal,
        @Positive
        Double cultivationSpeed,
        @Positive
        Long cultivationPoint,
        @Positive
        Long reputation,
        @Positive
        Long spiritStone,
        @PastOrPresent
        LocalDateTime createdAt
) {
}
