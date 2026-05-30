package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.spiritroot.SpiritRootDTO;
import com.vastworld.vwbe.services.SpiritRootService;
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
@RequestMapping("/api/spirit-roots")
public class SpiritRootController {
    private final SpiritRootService spiritRootService;

    public SpiritRootController(SpiritRootService spiritRootService) {
        this.spiritRootService = spiritRootService;
    }

    @GetMapping
    public ResponseEntity<ServiceResult<List<SpiritRootDTO>>> getAllSpiritRoots() {
        var result = spiritRootService.getAllSpiritRoots();
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResult<SpiritRootDTO>> getSpiritRootById(@PathVariable Integer id) {
        var result = spiritRootService.getSpiritRootById(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping
    public ResponseEntity<ServiceResult<SpiritRootDTO>> createSpiritRoot(@RequestBody SpiritRootDTO dto) {
        var result = spiritRootService.createSpiritRoot(dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResult<SpiritRootDTO>> updateSpiritRoot(
            @PathVariable Integer id,
            @RequestBody SpiritRootDTO dto) {
        var result = spiritRootService.updateSpiritRoot(id, dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResult<Void>> deleteSpiritRoot(@PathVariable Integer id) {
        var result = spiritRootService.deleteSpiritRoot(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}
