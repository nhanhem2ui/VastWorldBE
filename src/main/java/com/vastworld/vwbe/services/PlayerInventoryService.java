package com.vastworld.vwbe.services;

import com.vastworld.vwbe.common.GameBalance;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playerinventory.AddToInventoryRequest;
import com.vastworld.vwbe.dto.playerinventory.GetPlayerInventoryResponse;
import com.vastworld.vwbe.dto.playerinventory.PlayerInventoryDTO;
import com.vastworld.vwbe.entites.Item;
import com.vastworld.vwbe.entites.Player;
import com.vastworld.vwbe.entites.PlayerInventory;
import com.vastworld.vwbe.repositories.ItemRepository;
import com.vastworld.vwbe.repositories.PlayerInventoryRepository;
import com.vastworld.vwbe.repositories.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlayerInventoryService {
    private final PlayerInventoryRepository playerInventoryRepository;
    private final PlayerService playerService;
    private final ItemRepository itemRepository;

    public PlayerInventoryService(
            PlayerInventoryRepository playerInventoryRepository,
            PlayerService playerService,
            ItemRepository itemRepository) {
        this.playerInventoryRepository = playerInventoryRepository;
        this.playerService = playerService;
        this.itemRepository = itemRepository;
    }

    public ServiceResult<List<GetPlayerInventoryResponse>> getPlayerInventory(UUID playerId) {
        try {
            var playerEntity = playerService.getPlayerEntityById(playerId).getData();
            var playerInventory = playerInventoryRepository.findByPlayer_Id(playerId);

            int requiredSlots = GameBalance.INVENTORY_SLOT;
            int currentSlots = playerInventory.size();

            if (currentSlots < requiredSlots) {
                var missingSlots = new ArrayList<PlayerInventory>();
                for (int i = currentSlots; i < requiredSlots; i++) {
                    var slot = new PlayerInventory();
                    slot.setPlayer(playerEntity);
                    missingSlots.add(slot);
                }
                // Add the newly created slots to the current list
                playerInventoryRepository.saveAll(missingSlots);
                playerInventory.addAll(missingSlots);
            }

            var data = new ArrayList<GetPlayerInventoryResponse>();
            for (var slot : playerInventory) {
                var dataSlot = new GetPlayerInventoryResponse(slot.getItem() == null ? null : slot.getItem().getId(), slot.getItem() == null ? null : slot.getItem().getImageUrl(), slot.getQuantity() == null ? 0 : slot.getQuantity());
                data.add(dataSlot);
            }

            return ServiceResult.success("Success", data, HttpStatus.OK);
        } catch (Exception ex) {
            return ServiceResult.failure("Error getting inventory", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResult<Void> addToInventory(AddToInventoryRequest request, UUID playerId) {
        try {
            if (request == null || request.itemId() == null) {
                return ServiceResult.failure("Item id is required");
            }

            if (request.quantity() == null || request.quantity() <= 0) {
                return ServiceResult.failure("Quantity must be greater than 0");
            }

            var playerEntity = playerService
                    .getPlayerEntityById(playerId)
                    .getData();

            if (playerEntity == null) {
                return ServiceResult.failure("Player not found");
            }

            var item = itemRepository.findById(request.itemId());

            if (item.isEmpty()) {
                return ServiceResult.failure("Item not found");
            }

            Item itemEntity = item.get();

            var playerInventory = playerInventoryRepository.findByPlayer_Id(playerId);

            if (playerInventory.isEmpty()) {
                return ServiceResult.failure("Player inventory not found", HttpStatus.NOT_FOUND);
            }

            for (var slot : playerInventory) {
                if (slot.getItem() != null && slot.getItem().getId().equals(itemEntity.getId())) {
                    int currentQuantity = slot.getQuantity() == null ? 0 : slot.getQuantity();

                    slot.setQuantity(currentQuantity + request.quantity());

                    playerInventoryRepository.save(slot);

                    return ServiceResult.success("Item added to inventory successfully", HttpStatus.NO_CONTENT);
                }
            }

            for (var slot : playerInventory) {
                if (slot.getItem() == null) {
                    slot.setItem(itemEntity);
                    slot.setQuantity(request.quantity());

                    playerInventoryRepository.save(slot);

                    return ServiceResult.success("Item added to inventory successfully", HttpStatus.NO_CONTENT);
                }
            }

            return ServiceResult.failure("Inventory is full", HttpStatus.BAD_REQUEST);

        } catch (Exception ex) {
            return ServiceResult.failure("Error adding item to inventory", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
