package com.vastworld.vwbe.dto.playerspiritroot;

import java.util.List;
import java.util.UUID;

public record RollSpiritRootDTO(
        UUID playerId,
        Integer remainingRollNum,
        Boolean isVariantRoll,
        List<PlayerSpiritRootDTO> spiritRoots
) {
}
