package com.vastworld.vwbe.dto.player;

import java.util.UUID;

public record NewPlayableDTO(
        UUID accountId,
        Boolean gender
) {
}
