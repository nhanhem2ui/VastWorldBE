package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.region.GetPlayerRegionResponse;
import com.vastworld.vwbe.dto.region.GetRegionResponse;
import com.vastworld.vwbe.security.AuthenticatedUser;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.RegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

import static com.vastworld.vwbe.common.Common.resolveStatus;

@RestController
@RequestMapping("/api/region")
@RateLimit
@PreAuthorize("isAuthenticated()")
public class RegionController {
    private final RegionService regionService;
    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResult<GetRegionResponse>> getRegion(@PathVariable Integer id){
        var result =  regionService.getRegion(id);
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }

    @GetMapping("/player")
    public ResponseEntity<ServiceResult<GetPlayerRegionResponse>> getPlayerRegion(Authentication authentication){
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = regionService.getPlayerRegion(Objects.requireNonNull(user).playerId());
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }
}
