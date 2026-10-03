package com.vastworld.vwbe.dto.playerinventory;

import com.vastworld.vwbe.enums.EquipmentSlotTypes;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UnequipRequest(
        @NotNull UUID id,
        @NotNull EquipmentSlotTypes equipmentSlotType
) {}
