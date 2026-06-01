package com.vastworld.vwbe.dto.auth;

public record AuthResponse(
        String token,
        long expiresIn,
        String username,
        String email,
        String role
) {
}
