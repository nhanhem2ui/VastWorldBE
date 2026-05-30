package com.vastworld.vwbe.services;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playerequipment.PlayerEquipmentDTO;
import com.vastworld.vwbe.entites.Player;
import com.vastworld.vwbe.entites.PlayerEquipment;
import com.vastworld.vwbe.entites.PlayerInventory;
import com.vastworld.vwbe.repositories.PlayerEquipmentRepository;
import com.vastworld.vwbe.repositories.PlayerInventoryRepository;
import com.vastworld.vwbe.repositories.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlayerEquipmentService {
    private final PlayerEquipmentRepository playerEquipmentRepository;
    private final PlayerRepository playerRepository;
    private final PlayerInventoryRepository playerInventoryRepository;

    public PlayerEquipmentService(
            PlayerEquipmentRepository playerEquipmentRepository,
            PlayerRepository playerRepository,
            PlayerInventoryRepository playerInventoryRepository) {
        this.playerEquipmentRepository = playerEquipmentRepository;
        this.playerRepository = playerRepository;
        this.playerInventoryRepository = playerInventoryRepository;
    }

    public ServiceResult<List<PlayerEquipmentDTO>> getAllPlayerEquipments() {
        try {
            var playerEquipmentList = playerEquipmentRepository.findAll();
            if (playerEquipmentList.isEmpty()) {
                return ServiceResult.failure("No player equipment found");
            }

            var dtoList = playerEquipmentList.stream()
                    .map(this::toDto)
                    .toList();

            return ServiceResult.success("Player equipment retrieved successfully", dtoList);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player equipment", ex);
        }
    }

    public ServiceResult<PlayerEquipmentDTO> getPlayerEquipmentById(Long id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Player equipment id is invalid");
            }

            var playerEquipment = playerEquipmentRepository.findById(id);
            if (playerEquipment.isEmpty()) {
                return ServiceResult.failure("Player equipment not found");
            }

            return ServiceResult.success("Player equipment retrieved successfully", toDto(playerEquipment.get()));
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving player equipment", ex);
        }
    }

    public ServiceResult<PlayerEquipmentDTO> createPlayerEquipment(PlayerEquipmentDTO dto) {
        try {
            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            if (playerEquipmentRepository.existsByPlayer_IdAndEquipmentSlotIgnoreCase(dto.playerId(), dto.equipmentSlot().trim())) {
                return ServiceResult.failure("Player equipment slot already exists");
            }

            if (playerEquipmentRepository.existsByInventory_Id(dto.inventoryId())) {
                return ServiceResult.failure("Inventory already equipped");
            }

            var referencesResult = resolveReferences(dto);
            if (!referencesResult.isSuccess()) {
                return ServiceResult.failure(referencesResult.getMessage());
            }

            PlayerEquipment playerEquipment = new PlayerEquipment();
            applyDto(playerEquipment, dto, referencesResult.getData());

            var savedPlayerEquipment = playerEquipmentRepository.save(playerEquipment);
            return ServiceResult.success("Player equipment created successfully", toDto(savedPlayerEquipment));
        } catch (Exception ex) {
            return ServiceResult.failure("Error creating player equipment", ex);
        }
    }

    public ServiceResult<PlayerEquipmentDTO> updatePlayerEquipment(Long id, PlayerEquipmentDTO dto) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Player equipment id is invalid");
            }

            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            var playerEquipmentOptional = playerEquipmentRepository.findById(id);
            if (playerEquipmentOptional.isEmpty()) {
                return ServiceResult.failure("Player equipment not found");
            }

            if (playerEquipmentRepository.existsByPlayer_IdAndEquipmentSlotIgnoreCaseAndIdNot(dto.playerId(), dto.equipmentSlot().trim(), id)) {
                return ServiceResult.failure("Player equipment slot already exists");
            }

            if (playerEquipmentRepository.existsByInventory_IdAndIdNot(dto.inventoryId(), id)) {
                return ServiceResult.failure("Inventory already equipped");
            }

            var referencesResult = resolveReferences(dto);
            if (!referencesResult.isSuccess()) {
                return ServiceResult.failure(referencesResult.getMessage());
            }

            var playerEquipment = playerEquipmentOptional.get();
            applyDto(playerEquipment, dto, referencesResult.getData());

            var updatedPlayerEquipment = playerEquipmentRepository.save(playerEquipment);
            return ServiceResult.success("Player equipment updated successfully", toDto(updatedPlayerEquipment));
        } catch (Exception ex) {
            return ServiceResult.failure("Error updating player equipment", ex);
        }
    }

    public ServiceResult<Void> deletePlayerEquipment(Long id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Player equipment id is invalid");
            }

            if (!playerEquipmentRepository.existsById(id)) {
                return ServiceResult.failure("Player equipment not found");
            }

            playerEquipmentRepository.deleteById(id);
            return ServiceResult.success("Player equipment deleted successfully");
        } catch (Exception ex) {
            return ServiceResult.failure("Error deleting player equipment", ex);
        }
    }

    private ServiceResult<PlayerEquipmentReferences> resolveReferences(PlayerEquipmentDTO dto) {
        var player = playerRepository.findById(dto.playerId());
        if (player.isEmpty()) {
            return ServiceResult.failure("Player not found");
        }

        var inventory = playerInventoryRepository.findById(dto.inventoryId());
        if (inventory.isEmpty()) {
            return ServiceResult.failure("Player inventory not found");
        }

        if (!inventory.get().getPlayer().getId().equals(dto.playerId())) {
            return ServiceResult.failure("Player inventory does not belong to player");
        }

        return ServiceResult.success("Player equipment references resolved", new PlayerEquipmentReferences(player.get(), inventory.get()));
    }

    private void applyDto(PlayerEquipment playerEquipment, PlayerEquipmentDTO dto, PlayerEquipmentReferences references) {
        playerEquipment.setPlayer(references.player());
        playerEquipment.setInventory(references.inventory());
        playerEquipment.setEquipmentSlot(dto.equipmentSlot().trim());
    }

    private PlayerEquipmentDTO toDto(PlayerEquipment playerEquipment) {
        var inventory = playerEquipment.getInventory();
        return new PlayerEquipmentDTO(
                playerEquipment.getId(),
                playerEquipment.getPlayer().getId(),
                inventory.getId(),
                inventory.getItem().getId(),
                inventory.getItem().getName(),
                playerEquipment.getEquipmentSlot()
        );
    }

    private ServiceResult<PlayerEquipmentDTO> validateDto(PlayerEquipmentDTO dto) {
        if (dto == null) {
            return ServiceResult.failure("Player equipment data is required");
        }

        if (dto.playerId() == null) {
            return ServiceResult.failure("Player id is invalid");
        }

        if (dto.inventoryId() == null || dto.inventoryId() <= 0) {
            return ServiceResult.failure("Player inventory id is invalid");
        }

        if (dto.equipmentSlot() == null || dto.equipmentSlot().trim().isEmpty()) {
            return ServiceResult.failure("Equipment slot is required");
        }

        return null;
    }

    private record PlayerEquipmentReferences(Player player, PlayerInventory inventory) {
    }
}
