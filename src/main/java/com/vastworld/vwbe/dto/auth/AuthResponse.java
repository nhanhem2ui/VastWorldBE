package com.vastworld.vwbe.dto.auth;

import jakarta.annotation.Nullable;

import java.util.UUID;

public record AuthResponse(
        UUID userID,
        @Nullable UUID playerID,
        String token,
        long expiresIn,
        String username,
        String email,
        String role
) {
}
