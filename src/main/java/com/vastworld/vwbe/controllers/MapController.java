package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.map.GetMapResponse;
import com.vastworld.vwbe.dto.map.GetPlayerLocationResponse;
import com.vastworld.vwbe.dto.map.TravelRequest;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.MapService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.vastworld.vwbe.common.Common.resolveStatus;

@RestController
@RequestMapping("/api/map")
@RateLimit
@PreAuthorize("isAuthenticated()")
public class MapController {
    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/{mapId}")
    public ResponseEntity<ServiceResult<GetMapResponse>> getMap(@PathVariable Integer mapId) {
        var result = mapService.getMap(mapId);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/playerPosition/{playerId}")
    public ResponseEntity<ServiceResult<GetPlayerLocationResponse>> getPlayerPosition(@PathVariable UUID playerId) {
        var result = mapService.getPlayerLocation(playerId);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping("/travel")
    @RateLimit(limit = 20)
    public ResponseEntity<ServiceResult<Void>> travel(@Valid @RequestBody TravelRequest travelRequest) {
        var result = mapService.travel(travelRequest);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}
