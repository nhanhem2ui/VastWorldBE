package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.realmbreakthrough.RealmBreakthroughDTO;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.RealmBreakthroughService;
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
@RequestMapping("/api/realm-breakthroughs")
@PreAuthorize("isAuthenticated()")
@RateLimit(limit = 30)
public class RealmBreakthroughController {
    private final RealmBreakthroughService realmBreakthroughService;

    public RealmBreakthroughController(RealmBreakthroughService realmBreakthroughService) {
        this.realmBreakthroughService = realmBreakthroughService;
    }

    @GetMapping
    public ResponseEntity<ServiceResult<List<RealmBreakthroughDTO>>> getAllRealmBreakthroughs() {
        var result = realmBreakthroughService.getAllRealmBreakthroughs();
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResult<RealmBreakthroughDTO>> getRealmBreakthroughById(@PathVariable Integer id) {
        var result = realmBreakthroughService.getRealmBreakthroughById(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/getPlayerNextBreakthrough/{playerId}")


    @PostMapping
    public ResponseEntity<ServiceResult<RealmBreakthroughDTO>> createRealmBreakthrough(@RequestBody RealmBreakthroughDTO dto) {
        var result = realmBreakthroughService.createRealmBreakthrough(dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResult<RealmBreakthroughDTO>> updateRealmBreakthrough(
            @PathVariable Integer id,
            @RequestBody RealmBreakthroughDTO dto) {
        var result = realmBreakthroughService.updateRealmBreakthrough(id, dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResult<Void>> deleteRealmBreakthrough(@PathVariable Integer id) {
        var result = realmBreakthroughService.deleteRealmBreakthrough(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}
