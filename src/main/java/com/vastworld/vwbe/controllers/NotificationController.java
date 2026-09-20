package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.security.AuthenticatedUser;
import com.vastworld.vwbe.services.SseNotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/notifications")
public class NotificationController {
    private final SseNotificationService sseNotificationService;

    public NotificationController(SseNotificationService sseNotificationService) {
        this.sseNotificationService = sseNotificationService;
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(Authentication authentication) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        return sseNotificationService.subscribe(Objects.requireNonNull(user).playerId());
    }
}