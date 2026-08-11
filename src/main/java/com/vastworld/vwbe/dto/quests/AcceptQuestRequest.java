package com.vastworld.vwbe.dto.quests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AcceptQuestRequest(
        @NotNull UUID playerId,
        @NotNull Integer questId
) {}