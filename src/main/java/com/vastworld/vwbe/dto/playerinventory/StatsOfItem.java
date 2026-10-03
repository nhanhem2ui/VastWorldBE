package com.vastworld.vwbe.dto.playerinventory;

public record StatsOfItem(
        Long hp,
        Long attack,
        Long defense,
        Double critRate,
        Double critDamage,
        Double speed,
        Double lifeSteal,
        Double cultivationSpeed
) {}
