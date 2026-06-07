package com.vastworld.vwbe.dto.playerspiritroot;

import java.util.UUID;

public record PlayerSpiritRootDTO(
        UUID playerId,
        Integer spiritRootId,
        String spiritRootName
) {
}
