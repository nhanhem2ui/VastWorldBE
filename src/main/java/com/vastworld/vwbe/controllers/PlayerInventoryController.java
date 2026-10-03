package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playerinventory.AddToInventoryRequest;
import com.vastworld.vwbe.dto.playerinventory.GetEquipmentsResponse;
import com.vastworld.vwbe.dto.playerinventory.GetPlayerInventoryResponse;
import com.vastworld.vwbe.dto.playerinventory.UnequipRequest;
import com.vastworld.vwbe.security.AuthenticatedUser;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.PlayerInventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
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

    @GetMapping("/equipments")
    public ResponseEntity<ServiceResult<List<GetEquipmentsResponse>>> getPlayerEquipments(Authentication authentication) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = playerInventoryService.getPlayerEquipments(Objects.requireNonNull(user).playerId());
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }

    @PostMapping("/use")
    public ResponseEntity<ServiceResult<Void>> useItem(Authentication authentication, @Valid @RequestBody AddToInventoryRequest request) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = playerInventoryService.useItem(Objects.requireNonNull(user).playerId(), request);
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }

    @PostMapping("/unequip")
    public ResponseEntity<ServiceResult<Void>> unequipItem(Authentication authentication, @Valid @RequestBody UnequipRequest request) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = playerInventoryService.unequipItem(Objects.requireNonNull(user).playerId(), request);
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }

    @PostMapping("/add")
    public ResponseEntity<ServiceResult<Void>> addPlayerInventory(Authentication authentication, @Valid @RequestBody AddToInventoryRequest request){
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = playerInventoryService.addToInventory(request, Objects.requireNonNull(user).playerId());
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }
}
