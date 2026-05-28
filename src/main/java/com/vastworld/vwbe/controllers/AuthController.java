package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.auth.LoginRequest;
import com.vastworld.vwbe.dto.auth.RegisterRequest;
import com.vastworld.vwbe.services.AuthService;
import com.vastworld.vwbe.services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final EmailService emailService;

    public AuthController(AuthService authService, EmailService emailService) {
        this.authService = authService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        var result = authService.register(request);

        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(Map.of("message", result.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", result.getMessage()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        var result = authService.login(request);

        if (!result.isSuccess()) {
            if ("User is banned".equals(result.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", result.getMessage()));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", result.getMessage()));
        }

        return ResponseEntity.ok(result.getData());
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail(@RequestParam UUID userId, @RequestParam String token) {
        var result = authService.confirmEmail(userId, token);

        if (!result.isSuccess()) {
            if ("User not found".equals(result.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", result.getMessage()));
            }

            return ResponseEntity.badRequest().body(Map.of("message", result.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", result.getMessage()));
    }
}