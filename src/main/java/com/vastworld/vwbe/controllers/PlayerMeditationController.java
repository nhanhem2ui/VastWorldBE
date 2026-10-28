package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playermeditation.GetPlayerMeditationByIdResponse;
import com.vastworld.vwbe.security.AuthenticatedUser;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.PlayerMeditationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static com.vastworld.vwbe.common.Common.resolveStatus;

@RestController
@RequestMapping("/api/player-meditations")
@PreAuthorize("isAuthenticated()")
@Validated
@RateLimit(limit = 30)
public class PlayerMeditationController {
    private final PlayerMeditationService playerMeditationService;

    public PlayerMeditationController(PlayerMeditationService playerMeditationService) {
        this.playerMeditationService = playerMeditationService;
    }

    @GetMapping
    public ResponseEntity<ServiceResult<GetPlayerMeditationByIdResponse>> getPlayerMeditationByPlayerId(Authentication authentication) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = playerMeditationService.getPlayerMeditationByPlayerId(Objects.requireNonNull(user).playerId());
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping("/beginMeditate/{durationMinutes}")
    public ResponseEntity<ServiceResult<Void>> beginMeditate(Authentication authentication, @PathVariable @NotNull @Min(1) @Max(1440) Integer durationMinutes) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = playerMeditationService.beginMeditate(Objects.requireNonNull(user).playerId(), durationMinutes);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PostMapping("/claimMeditate")
    public ResponseEntity<ServiceResult<Void>> claimMeditate(Authentication authentication) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = playerMeditationService.claimMeditate(Objects.requireNonNull(user).playerId());
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}