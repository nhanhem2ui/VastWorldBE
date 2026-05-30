package com.vastworld.vwbe.dto.account;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountDTO(
        UUID id,
        String email,
        String username,
        String role,
        String authProvider,
        String providerId,
        Boolean emailVerified,
        Boolean isBanned,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
}
