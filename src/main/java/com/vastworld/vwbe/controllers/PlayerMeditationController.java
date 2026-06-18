package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playermeditation.BeginMeditateRequest;
import com.vastworld.vwbe.dto.playermeditation.GetCultivationPointPerMinsResponse;
import com.vastworld.vwbe.dto.playermeditation.GetPlayerMeditationByIdResponse;
import com.vastworld.vwbe.dto.playermeditation.PlayerMeditationDTO;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.PlayerMeditationService;
import jakarta.validation.Valid;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
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
@RequestMapping("/api/player-meditations")
@PreAuthorize("isAuthenticated()")
@RateLimit(limit = 30)
public class PlayerMeditationController {
    private final PlayerMeditationService playerMeditationService;

    public PlayerMeditationController(PlayerMeditationService playerMeditationService) {
        this.playerMeditationService = playerMeditationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResult<List<PlayerMeditationDTO>>> getAllPlayerMeditations() {
        var result = playerMeditationService.getAllPlayerMeditations();
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<ServiceResult<GetPlayerMeditationByIdResponse>> getPlayerMeditationByPlayerId(@PathVariable UUID playerId) {
        var result = playerMeditationService.getPlayerMeditationByPlayerId(playerId);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/cpPerMins/{playerId}")
    public ResponseEntity<ServiceResult<GetCultivationPointPerMinsResponse>> getCultivationPointPerMins(@PathVariable UUID playerId){
        var result = playerMeditationService.getCultivationPointPerMins(playerId);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping("/beginMeditate")
    public ResponseEntity<ServiceResult<Void>> beginMeditate(@Valid @RequestBody BeginMeditateRequest request){
        var result = playerMeditationService.beginMeditate(request);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PostMapping("/claimMeditate/{playerId}")
    public ResponseEntity<ServiceResult<Void>> claimMeditate(@PathVariable UUID playerId){
        var result = playerMeditationService.claimMeditate(playerId);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping
    public ResponseEntity<ServiceResult<PlayerMeditationDTO>> createPlayerMeditation(@RequestBody PlayerMeditationDTO dto) {
        var result = playerMeditationService.createPlayerMeditation(dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResult<PlayerMeditationDTO>> updatePlayerMeditation(
            @PathVariable Long id,
            @RequestBody PlayerMeditationDTO dto) {
        var result = playerMeditationService.updatePlayerMeditation(id, dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResult<Void>> deletePlayerMeditation(@PathVariable Long id) {
        var result = playerMeditationService.deletePlayerMeditation(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}
