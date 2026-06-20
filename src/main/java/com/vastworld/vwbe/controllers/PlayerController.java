package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.player.GetPlayerNextBreakthroughResponse;
import com.vastworld.vwbe.dto.player.NewPlayableDTO;
import com.vastworld.vwbe.dto.player.PlayerDTO;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.PlayerService;
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
@RequestMapping("/api/players")
@PreAuthorize("isAuthenticated()")
@RateLimit(limit = 30)
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResult<List<PlayerDTO>>> getAllPlayers() {
        var result = playerService.getAllPlayers();
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResult<PlayerDTO>> getPlayerById(@PathVariable UUID id) {
        var result = playerService.getPlayerById(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResult<PlayerDTO>> createPlayer(@RequestBody PlayerDTO dto) {
        var result = playerService.createPlayer(dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PostMapping("/playable")
    public ResponseEntity<ServiceResult<PlayerDTO>> createNewPlayable(@RequestBody NewPlayableDTO dto) {
        var result = playerService.createNewPlayable(dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @GetMapping("/nextBreakthrough/{playerId}")
    public ResponseEntity<ServiceResult<GetPlayerNextBreakthroughResponse>> getPlayerNextBreakthrough(@PathVariable UUID playerId){
        var result = playerService.getPlayerNextBreakthrough(playerId);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResult<PlayerDTO>> updatePlayer(
            @PathVariable UUID id,
            @RequestBody PlayerDTO dto) {
        var result = playerService.updatePlayer(id, dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}
