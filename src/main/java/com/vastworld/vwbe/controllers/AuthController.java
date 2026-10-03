package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.account.MeResponse;
import com.vastworld.vwbe.dto.auth.AuthResponse;
import com.vastworld.vwbe.dto.auth.LoginRequest;
import com.vastworld.vwbe.dto.auth.RegisterRequest;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

import static com.vastworld.vwbe.common.Common.resolveStatus;

@RestController
@RequestMapping("/api/auth")
@RateLimit
public class AuthController {

    private final AuthService authService;
    private final long jwtExpiration;

    public AuthController(AuthService authService,
                          @Value("${jwt.expiration}") long jwtExpiration) {
        this.authService = authService;
        this.jwtExpiration = jwtExpiration;
    }

    @PostMapping("/register")
    public ResponseEntity<ServiceResult<AuthResponse>> register(@RequestBody RegisterRequest request) {
        var result = authService.register(request);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PostMapping("/login")
    @RateLimit(limit = 5)
    public ResponseEntity<ServiceResult<AuthResponse>> login(@RequestBody LoginRequest request,
                                                             HttpServletResponse response) {
        var result = authService.login(request);

        if (result.isSuccess() && result.getData() != null) {
            ResponseCookie cookie = ResponseCookie.from("accessToken", result.getData().token())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofMillis(jwtExpiration))
                    .sameSite("None")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie expired = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expired.toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<ServiceResult<Void>> confirmEmail(@RequestParam UUID userId,
                                                            @RequestParam String token) {
        var result = authService.confirmEmail(userId, token);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/me")
    public ResponseEntity<ServiceResult<MeResponse>> me(HttpServletRequest request) {
        var result = authService.Me(request);
        return ResponseEntity.status(resolveStatus(result,HttpStatus.OK)).body(result);
    }
}