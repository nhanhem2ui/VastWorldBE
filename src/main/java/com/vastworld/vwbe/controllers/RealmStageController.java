package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.realmstage.GetRealmStageResponse;
import com.vastworld.vwbe.dto.realmstage.RealmStageDTO;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.RealmStageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.vastworld.vwbe.common.Common.resolveStatus;

@RestController
@RequestMapping("/api/realm-stages")
@PreAuthorize("isAuthenticated()")
@RateLimit(limit = 30)
public class RealmStageController {
    private final RealmStageService realmStageService;

    public RealmStageController(RealmStageService realmStageService) {
        this.realmStageService = realmStageService;
    }

    @GetMapping
    public ResponseEntity<ServiceResult<List<RealmStageDTO>>> getAllRealmStages() {
        var result = realmStageService.getAllRealmStages();
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResult<GetRealmStageResponse>> getRealmStageById(@PathVariable Integer id) {
        var result = realmStageService.getRealmStageById(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping
    public ResponseEntity<ServiceResult<RealmStageDTO>> createRealmStage(@RequestBody RealmStageDTO dto) {
        var result = realmStageService.createRealmStage(dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResult<RealmStageDTO>> updateRealmStage(
            @PathVariable Integer id,
            @RequestBody RealmStageDTO dto) {
        var result = realmStageService.updateRealmStage(id, dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResult<Void>> deleteRealmStage(@PathVariable Integer id) {
        var result = realmStageService.deleteRealmStage(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}
