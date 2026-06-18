package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.cultivationrealm.CultivationRealmDTO;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.CultivationRealmService;
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
@RequestMapping("/api/cultivation-realms")
@RateLimit(limit = 30)
@PreAuthorize("isAuthenticated()")
public class CultivationRealmController {
    private final CultivationRealmService cultivationRealmService;

    public CultivationRealmController(CultivationRealmService cultivationRealmService) {
        this.cultivationRealmService = cultivationRealmService;
    }

    @GetMapping
    public ResponseEntity<ServiceResult<List<CultivationRealmDTO>>> getAllCultivationRealms() {
        var result = cultivationRealmService.getAllCultivationRealms();
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResult<CultivationRealmDTO>> getCultivationRealmById(@PathVariable Integer id) {
        var result = cultivationRealmService.getCultivationRealmById(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping
    public ResponseEntity<ServiceResult<CultivationRealmDTO>> createCultivationRealm(@RequestBody CultivationRealmDTO dto) {
        var result = cultivationRealmService.createCultivationRealm(dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResult<CultivationRealmDTO>> updateCultivationRealm(
            @PathVariable Integer id,
            @RequestBody CultivationRealmDTO dto) {
        var result = cultivationRealmService.updateCultivationRealm(id, dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResult<Void>> deleteCultivationRealm(@PathVariable Integer id) {
        var result = cultivationRealmService.deleteCultivationRealm(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}
