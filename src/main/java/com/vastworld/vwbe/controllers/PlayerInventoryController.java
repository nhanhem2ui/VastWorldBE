package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playerinventory.PlayerInventoryDTO;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.PlayerInventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseEntity<ServiceResult<List<PlayerInventoryDTO>>> getAllPlayerInventories() {
        var result = playerInventoryService.getAllPlayerInventories();
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResult<PlayerInventoryDTO>> getPlayerInventoryById(@PathVariable Long id) {
        var result = playerInventoryService.getPlayerInventoryById(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping
    public ResponseEntity<ServiceResult<PlayerInventoryDTO>> createPlayerInventory(@RequestBody PlayerInventoryDTO dto) {
        var result = playerInventoryService.createPlayerInventory(dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResult<PlayerInventoryDTO>> updatePlayerInventory(
            @PathVariable Long id,
            @RequestBody PlayerInventoryDTO dto) {
        var result = playerInventoryService.updatePlayerInventory(id, dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResult<Void>> deletePlayerInventory(@PathVariable Long id) {
        var result = playerInventoryService.deletePlayerInventory(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}
