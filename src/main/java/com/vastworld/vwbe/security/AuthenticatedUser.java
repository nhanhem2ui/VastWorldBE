package com.vastworld.vwbe.security;

import java.util.Objects;
import java.util.UUID;

public record AuthenticatedUser(
        UUID accountId,
        UUID playerId
) {
    public AuthenticatedUser {
        Objects.requireNonNull(accountId);
        Objects.requireNonNull(playerId);
    }
}