package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.auth.AuthResponse;
import com.vastworld.vwbe.dto.auth.LoginRequest;
import com.vastworld.vwbe.dto.auth.RegisterRequest;
import com.vastworld.vwbe.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.vastworld.vwbe.common.Common.resolveStatus;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ServiceResult<AuthResponse>> register(@RequestBody RegisterRequest request) {
        var result = authService.register(request);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<ServiceResult<AuthResponse>> login(@RequestBody LoginRequest request) {
        var result = authService.login(request);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<ServiceResult<Void>> confirmEmail(@RequestParam UUID userId, @RequestParam String token) {
        var result = authService.confirmEmail(userId, token);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}
