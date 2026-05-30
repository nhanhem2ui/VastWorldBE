package com.vastworld.vwbe.dto.playerspiritroot;

import java.util.UUID;

public record PlayerSpiritRootDTO(
        Integer id,
        UUID playerId,
        Integer spiritRootId,
        String spiritRootName
) {
}
