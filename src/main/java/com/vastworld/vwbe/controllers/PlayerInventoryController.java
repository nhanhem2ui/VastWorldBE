package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playerinventory.AddToInventoryRequest;
import com.vastworld.vwbe.dto.playerinventory.GetPlayerInventoryResponse;
import com.vastworld.vwbe.dto.playerinventory.PlayerInventoryDTO;
import com.vastworld.vwbe.security.AuthenticatedUser;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.PlayerInventoryService;
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
@RequestMapping("/api/player-inventories")
@PreAuthorize("isAuthenticated()")
@RateLimit(limit = 30)
public class PlayerInventoryController {
    private final PlayerInventoryService playerInventoryService;

    public PlayerInventoryController(PlayerInventoryService playerInventoryService) {
        this.playerInventoryService = playerInventoryService;
    }

    @GetMapping
    public ResponseEntity<ServiceResult<List<GetPlayerInventoryResponse>>> getPlayerInventory(Authentication authentication) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = playerInventoryService.getPlayerInventory(Objects.requireNonNull(user).playerId());
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }

    @PostMapping("/add")
    public ResponseEntity<ServiceResult<Void>> addPlayerInventory(Authentication authentication, AddToInventoryRequest request){
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = playerInventoryService.addToInventory(request, Objects.requireNonNull(user).playerId());
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }
}
