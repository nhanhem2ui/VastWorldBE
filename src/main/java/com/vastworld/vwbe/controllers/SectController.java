package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.sect.GetAllSectResponse;
import com.vastworld.vwbe.security.AuthenticatedUser;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.SectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

import static com.vastworld.vwbe.common.Common.resolveStatus;

@RestController
@RequestMapping("/api/sects")
@PreAuthorize("isAuthenticated()")
@RateLimit(limit = 30)
public class SectController {
    private final SectService sectService;

    public SectController(SectService sectService) {
        this.sectService = sectService;
    }

    /**
     * Get all available sect (base on region, default = current region)
     * use as display in list
     */
    @GetMapping
    public ResponseEntity<ServiceResult<List<GetAllSectResponse>>> getSects(
        @RequestParam(required = false) Integer region,
        @RequestParam(required = false, defaultValue = "false") boolean allRegions,
        Authentication authentication) {
        var user =  (AuthenticatedUser) authentication.getPrincipal();
        var result = sectService.getAllCultivationRealms(Objects.requireNonNull(user).playerId(), region, allRegions);
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }
}