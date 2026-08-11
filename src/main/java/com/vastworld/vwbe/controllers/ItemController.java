package com.vastworld.vwbe.controllers;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.item.ItemDTO;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.vastworld.vwbe.common.Common.resolveStatus;

@RestController
@RequestMapping("/api/items")
@RateLimit(limit = 30)
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<ServiceResult<List<ItemDTO>>> getAllItems() {
        var result = itemService.getAllItems();
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResult<ItemDTO>> getItemById(@PathVariable UUID id) {
        var result = itemService.getItemById(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @PostMapping
    public ResponseEntity<ServiceResult<ItemDTO>> createItem(@RequestBody ItemDTO dto) {
        var result = itemService.createItem(dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.CREATED)).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResult<ItemDTO>> updateItem(
            @PathVariable UUID id,
            @RequestBody ItemDTO dto) {
        var result = itemService.updateItem(id, dto);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResult<Void>> deleteItem(@PathVariable UUID id) {
        var result = itemService.deleteItem(id);
        return ResponseEntity.status(resolveStatus(result, HttpStatus.OK)).body(result);
    }
}
