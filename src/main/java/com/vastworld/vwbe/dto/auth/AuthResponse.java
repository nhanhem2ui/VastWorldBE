package com.vastworld.vwbe.dto.auth;

import java.util.UUID;

public record AuthResponse(
        String token,
        long expiresIn,
        String username,
        String email
) {
}