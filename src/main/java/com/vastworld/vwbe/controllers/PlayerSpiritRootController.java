package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playerspiritroot.PlayerSpiritRootDTO;
import com.vastworld.vwbe.dto.playerspiritroot.RollSpiritRootDTO;
import com.vastworld.vwbe.security.AuthenticatedUser;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.PlayerSpiritRootService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.vastworld.vwbe.common.Common.resolveStatus;

@RestController
@RequestMapping("/api/player-spirit-roots")
@PreAuthorize("isAuthenticated()")
@RateLimit(limit = 30)
public class PlayerSpiritRootController {
    private final PlayerSpiritRootService playerSpiritRootService;

    public PlayerSpiritRootController(PlayerSpiritRootService playerSpiritRootService) {
        this.playerSpiritRootService = playerSpiritRootService;
    }

    @GetMapping
    public ResponseEntity<ServiceResult<List<String>>> getPlayerSpiritRootById(Authentication authentication) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = playerSpiritRootService.getPlayerSpiritRootById(Objects.requireNonNull(user).playerId());
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping("/roll")
    public ResponseEntity<ServiceResult<RollSpiritRootDTO>> rollSpiritRoot(Authentication authentication) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = playerSpiritRootService.rollSpiritRoot(Objects.requireNonNull(user).playerId());
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}
