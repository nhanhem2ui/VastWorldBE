package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playermeditation.PlayerMeditationDTO;
import com.vastworld.vwbe.services.PlayerMeditationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/player-meditations")
public class PlayerMeditationController {
    private final PlayerMeditationService playerMeditationService;

    public PlayerMeditationController(PlayerMeditationService playerMeditationService) {
        this.playerMeditationService = playerMeditationService;
    }

    @GetMapping
    public ResponseEntity<ServiceResult<List<PlayerMeditationDTO>>> getAllPlayerMeditations() {
        var result = playerMeditationService.getAllPlayerMeditations();
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResult<PlayerMeditationDTO>> getPlayerMeditationById(@PathVariable Long id) {
        var result = playerMeditationService.getPlayerMeditationById(id);
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
