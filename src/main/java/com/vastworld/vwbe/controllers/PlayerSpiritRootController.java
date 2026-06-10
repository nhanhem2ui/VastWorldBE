package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playerspiritroot.PlayerSpiritRootDTO;
import com.vastworld.vwbe.dto.playerspiritroot.RollSpiritRootDTO;
import com.vastworld.vwbe.services.PlayerSpiritRootService;
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
import java.util.UUID;

import static com.vastworld.vwbe.common.Common.resolveStatus;

@RestController
@RequestMapping("/api/player-spirit-roots")
@PreAuthorize("isAuthenticated()")
public class PlayerSpiritRootController {
    private final PlayerSpiritRootService playerSpiritRootService;

    public PlayerSpiritRootController(PlayerSpiritRootService playerSpiritRootService) {
        this.playerSpiritRootService = playerSpiritRootService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResult<List<PlayerSpiritRootDTO>>> getAllPlayerSpiritRoots() {
        var result = playerSpiritRootService.getAllPlayerSpiritRoots();
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResult<List<String>>> getPlayerSpiritRootById(@PathVariable UUID id) {
        var result = playerSpiritRootService.getPlayerSpiritRootById(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping("/roll/{playerId}")
    public ResponseEntity<ServiceResult<RollSpiritRootDTO>> rollSpiritRoot(@PathVariable UUID playerId) {
        var result = playerSpiritRootService.rollSpiritRoot(playerId);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResult<PlayerSpiritRootDTO>> createPlayerSpiritRoot(@RequestBody PlayerSpiritRootDTO dto) {
        var result = playerSpiritRootService.createPlayerSpiritRoot(dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResult<PlayerSpiritRootDTO>> updatePlayerSpiritRoot(
            @PathVariable Integer id,
            @RequestBody PlayerSpiritRootDTO dto) {
        var result = playerSpiritRootService.updatePlayerSpiritRoot(id, dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResult<Void>> deletePlayerSpiritRoot(@PathVariable Integer id) {
        var result = playerSpiritRootService.deletePlayerSpiritRoot(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}
