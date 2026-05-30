package com.vastworld.vwbe.services;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playerinventory.PlayerInventoryDTO;
import com.vastworld.vwbe.entites.Item;
import com.vastworld.vwbe.entites.Player;
import com.vastworld.vwbe.entites.PlayerInventory;
import com.vastworld.vwbe.repositories.ItemRepository;
import com.vastworld.vwbe.repositories.PlayerInventoryRepository;
import com.vastworld.vwbe.repositories.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlayerInventoryService {
    private final PlayerInventoryRepository playerInventoryRepository;
    private final PlayerRepository playerRepository;
    private final ItemRepository itemRepository;

    public PlayerInventoryService(
            PlayerInventoryRepository playerInventoryRepository,
            PlayerRepository playerRepository,
            ItemRepository itemRepository) {
        this.playerInventoryRepository = playerInventoryRepository;
        this.playerRepository = playerRepository;
        this.itemRepository = itemRepository;
    }

    public ServiceResult<List<PlayerInventoryDTO>> getAllPlayerInventories() {
        try {
            var playerInventoryList = playerInventoryRepository.findAll();
            if (playerInventoryList.isEmpty()) {
                return ServiceResult.failure("No player inventory found");
            }

            var dtoList = playerInventoryList.stream()
                    .map(this::toDto)
                    .toList();

            return ServiceResult.success("Player inventory retrieved successfully", dtoList);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player inventory", ex);
        }
    }

    public ServiceResult<PlayerInventoryDTO> getPlayerInventoryById(Long id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Player inventory id is invalid");
            }

            var playerInventory = playerInventoryRepository.findById(id);
            if (playerInventory.isEmpty()) {
                return ServiceResult.failure("Player inventory not found");
            }

            return ServiceResult.success("Player inventory retrieved successfully", toDto(playerInventory.get()));
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player inventory", ex);
        }
    }

    public ServiceResult<PlayerInventoryDTO> createPlayerInventory(PlayerInventoryDTO dto) {
        try {
            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            var referencesResult = resolveReferences(dto);
            if (!referencesResult.isSuccess()) {
                return ServiceResult.failure(referencesResult.getMessage());
            }

            PlayerInventory playerInventory = new PlayerInventory();
            applyDto(playerInventory, dto, referencesResult.getData());

            var savedPlayerInventory = playerInventoryRepository.save(playerInventory);
            return ServiceResult.success("Player inventory created successfully", toDto(savedPlayerInventory));
        } catch (Exception ex) {
            return ServiceResult.failure("Error creating player inventory", ex);
        }
    }

    public ServiceResult<PlayerInventoryDTO> updatePlayerInventory(Long id, PlayerInventoryDTO dto) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Player inventory id is invalid");
            }

            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            var playerInventoryOptional = playerInventoryRepository.findById(id);
            if (playerInventoryOptional.isEmpty()) {
                return ServiceResult.failure("Player inventory not found");
            }

            var referencesResult = resolveReferences(dto);
            if (!referencesResult.isSuccess()) {
                return ServiceResult.failure(referencesResult.getMessage());
            }

            var playerInventory = playerInventoryOptional.get();
            applyDto(playerInventory, dto, referencesResult.getData());

            var updatedPlayerInventory = playerInventoryRepository.save(playerInventory);
            return ServiceResult.success("Player inventory updated successfully", toDto(updatedPlayerInventory));
        } catch (Exception ex) {
            return ServiceResult.failure("Error updating player inventory", ex);
        }
    }

    public ServiceResult<Void> deletePlayerInventory(Long id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Player inventory id is invalid");
            }

            if (!playerInventoryRepository.existsById(id)) {
                return ServiceResult.failure("Player inventory not found");
            }

            playerInventoryRepository.deleteById(id);
            return ServiceResult.success("Player inventory deleted successfully");
        } catch (Exception ex) {
            return ServiceResult.failure("Error deleting player inventory", ex);
        }
    }

    private ServiceResult<PlayerInventoryReferences> resolveReferences(PlayerInventoryDTO dto) {
        var player = playerRepository.findById(dto.playerId());
        if (player.isEmpty()) {
            return ServiceResult.failure("Player not found");
        }

        var item = itemRepository.findById(dto.itemId());
        if (item.isEmpty()) {
            return ServiceResult.failure("Item not found");
        }

        return ServiceResult.success("Player inventory references resolved", new PlayerInventoryReferences(player.get(), item.get()));
    }

    private void applyDto(PlayerInventory playerInventory, PlayerInventoryDTO dto, PlayerInventoryReferences references) {
        playerInventory.setPlayer(references.player());
        playerInventory.setItem(references.item());
        playerInventory.setQuantity(dto.quantity());
    }

    private PlayerInventoryDTO toDto(PlayerInventory playerInventory) {
        return new PlayerInventoryDTO(
                playerInventory.getId(),
                playerInventory.getPlayer().getId(),
                playerInventory.getItem().getId(),
                playerInventory.getItem().getName(),
                playerInventory.getQuantity()
        );
    }

    private ServiceResult<PlayerInventoryDTO> validateDto(PlayerInventoryDTO dto) {
        if (dto == null) {
            return ServiceResult.failure("Player inventory data is required");
        }

        if (dto.playerId() == null) {
            return ServiceResult.failure("Player id is invalid");
        }

        if (dto.itemId() == null) {
            return ServiceResult.failure("Item id is invalid");
        }

        if (dto.quantity() == null || dto.quantity() <= 0) {
            return ServiceResult.failure("Quantity must be greater than 0");
        }

        return null;
    }

    private record PlayerInventoryReferences(Player player, Item item) {
    }
}
