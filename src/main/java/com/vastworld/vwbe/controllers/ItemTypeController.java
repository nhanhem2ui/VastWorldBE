package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.itemtype.ItemTypeDTO;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.ItemTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.vastworld.vwbe.common.Common.resolveStatus;

@RestController
@RequestMapping("/api/item-types")
@PreAuthorize("isAuthenticated()")
@RateLimit(limit = 30)
public class ItemTypeController {
    private final ItemTypeService itemTypeService;

    public ItemTypeController(ItemTypeService itemTypeService) {
        this.itemTypeService = itemTypeService;
    }

    @GetMapping
    public ResponseEntity<ServiceResult<List<ItemTypeDTO>>> getAllItemTypes() {
        var result = itemTypeService.getAllItemTypes();
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResult<ItemTypeDTO>> getItemTypeById(@PathVariable Integer id) {
        var result = itemTypeService.getItemTypeById(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping
    public ResponseEntity<ServiceResult<ItemTypeDTO>> createItemType(@RequestBody ItemTypeDTO dto) {
        var result = itemTypeService.createItemType(dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResult<ItemTypeDTO>> updateItemType(
            @PathVariable Integer id,
            @RequestBody ItemTypeDTO dto) {
        var result = itemTypeService.updateItemType(id, dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResult<Void>> deleteItemType(@PathVariable Integer id) {
        var result = itemTypeService.deleteItemType(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}
