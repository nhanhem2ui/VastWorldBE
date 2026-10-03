package com.vastworld.vwbe.services;

import com.vastworld.vwbe.common.GameBalance;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.playerinventory.*;
import com.vastworld.vwbe.entites.Item;
import com.vastworld.vwbe.entites.Player;
import com.vastworld.vwbe.entites.PlayerEquipment;
import com.vastworld.vwbe.entites.PlayerInventory;
import com.vastworld.vwbe.enums.EquipmentSlotTypes;
import com.vastworld.vwbe.repositories.ItemRepository;
import com.vastworld.vwbe.repositories.PlayerEquipmentRepository;
import com.vastworld.vwbe.repositories.PlayerInventoryRepository;
import com.vastworld.vwbe.repositories.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class PlayerInventoryService {
    private final PlayerInventoryRepository playerInventoryRepository;
    private final PlayerEquipmentRepository playerEquipmentRepository;
    private final ItemRepository itemRepository;
    private final PlayerRepository playerRepository;

    public PlayerInventoryService(
            PlayerInventoryRepository playerInventoryRepository,ItemRepository itemRepository,
            PlayerEquipmentRepository playerEquipmentRepository,
            PlayerRepository playerRepository) {
        this.playerInventoryRepository = playerInventoryRepository;
        this.itemRepository = itemRepository;
        this.playerEquipmentRepository = playerEquipmentRepository;
        this.playerRepository = playerRepository;
    }

    public ServiceResult<List<GetPlayerInventoryResponse>> getPlayerInventory(UUID playerId) {
        try {
            var playerOptional = playerRepository.findById(playerId);
            if(playerOptional.isEmpty()) {
                return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
            }
            var playerEntity = playerOptional.get();
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

                playerInventoryRepository.saveAll(missingSlots);
                playerInventory.addAll(missingSlots);
            }

            var data = new ArrayList<GetPlayerInventoryResponse>();

            for (var slot : playerInventory) {
                var item = slot.getItem();
                StatsOfItem stats = null;

                if (item != null) {
                    stats = new StatsOfItem(
                            item.getHp(),
                            item.getAttack(),
                            item.getDefense(),
                            item.getCritRate(),
                            item.getCritDamage(),
                            item.getSpeed(),
                            item.getLifeSteal(),
                            item.getCultivationSpeed()
                    );
                }

                var dataSlot = new GetPlayerInventoryResponse(
                        item == null ? null : item.getId(),
                        item == null ? null : item.getName(),
                        item == null ? null : item.getImageUrl(),
                        stats,
                        item == null ? null : item.getItemType().getName(),
                        slot.getQuantity() == null ? 0 : slot.getQuantity(),
                        item == null ? null : item.isCombatOnly()
                );
                data.add(dataSlot);
            }
            data.sort(Comparator.comparing(GetPlayerInventoryResponse::itemName, Comparator.nullsLast(Comparator.naturalOrder())));
            return ServiceResult.success("Success", data, HttpStatus.OK);

        } catch (Exception ex) {
            return ServiceResult.failure("Error getting inventory", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResult<List<GetEquipmentsResponse>> getPlayerEquipments(UUID playerId) {
        try {
            var playerEntityOptional = playerRepository.findById(playerId);

            if (playerEntityOptional.isEmpty()) {
                return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
            }

            var playerEntity = playerEntityOptional.get();

            var equipments = playerEquipmentRepository.findByPlayer_Id(playerId);

            for (var slotType : EquipmentSlotTypes.values()) {

                boolean exists = equipments.stream()
                        .anyMatch(equipment ->
                                equipment.getEquipmentSlotType().equals(slotType)
                        );

                if (!exists) {
                    var equipment = new PlayerEquipment();

                    equipment.setPlayer(playerEntity);
                    equipment.setEquipmentSlotType(slotType);
                    equipment.setItem(null);

                    equipments.add(equipment);
                }
            }

            playerEquipmentRepository.saveAll(equipments);

            var data = new ArrayList<GetEquipmentsResponse>();

            for (var equipment : equipments) {
                var item = equipment.getItem();

                StatsOfItem stats = null;

                if (item != null) {
                    stats = new StatsOfItem(
                            item.getHp(),
                            item.getAttack(),
                            item.getDefense(),
                            item.getCritRate(),
                            item.getCritDamage(),
                            item.getSpeed(),
                            item.getLifeSteal(),
                            item.getCultivationSpeed()
                    );
                }

                data.add(new GetEquipmentsResponse(
                        equipment.getId(),
                        item == null ? null : item.getId(),
                        item == null ? null : item.getImageUrl(),
                        item == null ? null : item.getName(),
                        stats,
                        equipment.getEquipmentSlotType().name()
                ));
            }

            // Keep the order defined by EquipmentSlotTypes
            data.sort(Comparator.comparing(
                    equipment -> EquipmentSlotTypes.valueOf(
                            equipment.equipmentSlotType()
                    ).ordinal()
            ));

            return ServiceResult.success("Success", data, HttpStatus.OK);

        } catch (Exception ex) {
            return ServiceResult.failure("Error getting player equipments", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResult<Void> useItem(UUID playerId, AddToInventoryRequest request) {
        try {
            if (request.itemId() == null || playerId == null) {
                return ServiceResult.failure("Item id and player id are required", HttpStatus.BAD_REQUEST);
            }

            if (request.quantity() == null || request.quantity() <= 0) {
                return ServiceResult.failure("Quantity must be greater than 0", HttpStatus.BAD_REQUEST);
            }

            var inventorySlotOptional = playerInventoryRepository.findByPlayer_IdAndItem_Id(playerId, request.itemId());

            if (inventorySlotOptional.isEmpty()) {
                return ServiceResult.failure("Item not found in player's inventory", HttpStatus.NOT_FOUND);
            }

            var inventorySlot = inventorySlotOptional.get();

            int currentQuantity = inventorySlot.getQuantity() == null ? 0 : inventorySlot.getQuantity();

            if (currentQuantity < request.quantity()) {
                return ServiceResult.failure("Not enough items", HttpStatus.BAD_REQUEST);
            }

            var item = inventorySlot.getItem();

            if (item == null || item.getItemType() == null) {
                return ServiceResult.failure("Invalid inventory item", HttpStatus.BAD_REQUEST);
            }

            if(item.isCombatOnly()){
                return ServiceResult.failure("Combat Only", HttpStatus.BAD_REQUEST);
            }

            var playerOptional = playerRepository.findById(playerId);

            if (playerOptional.isEmpty()) {
                return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
            }

            var player = playerOptional.get();

            String itemType = item.getItemType().getName();

            switch (itemType) {
                case "Vũ Khí" -> equipItem(player, inventorySlot, EquipmentSlotTypes.WEAPON);
                case "Mũ" -> equipItem(player, inventorySlot, EquipmentSlotTypes.HELMET);
                case "Áo" -> equipItem(player, inventorySlot, EquipmentSlotTypes.CHESTPLATE);
                case "Quần" -> equipItem(player, inventorySlot, EquipmentSlotTypes.LEGGINGS);
                case "Giày" -> equipItem(player, inventorySlot, EquipmentSlotTypes.BOOTS);
                case "Trang sức" -> equipItem(player, inventorySlot,EquipmentSlotTypes.ACCESSORY);

                case "Đan dược" -> {
                    // TODO: Apply the item's consumable effect here.
                    inventorySlot.setQuantity(currentQuantity - request.quantity());

                    if (inventorySlot.getQuantity() <= 0) {
                        inventorySlot.setQuantity(0);
                        inventorySlot.setItem(null);
                    }

                    playerInventoryRepository.save(inventorySlot);
                }

                default -> {
                    return ServiceResult.failure("Unsupported item type", HttpStatus.BAD_REQUEST);
                }
            }

            return ServiceResult.success("Item used successfully", HttpStatus.NO_CONTENT);

        } catch (Exception ex) {
            return ServiceResult.failure("Error using item", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void equipItem(Player player, PlayerInventory inventorySlot, EquipmentSlotTypes slotType) {
        var newItem = inventorySlot.getItem();
        var equipmentOptional = playerEquipmentRepository.findByPlayer_IdAndEquipmentSlotType(
                player.getId(),
                slotType
        );

        PlayerEquipment equipment;

        if (equipmentOptional.isPresent()) {
            equipment = equipmentOptional.get();
        } else {
            equipment = new PlayerEquipment();
            equipment.setPlayer(player);
            equipment.setEquipmentSlotType(slotType);
        }

        var oldItem = equipment.getItem();

        if (oldItem != null) {
            player.setHp(player.getHp() - Objects.requireNonNullElse(oldItem.getHp(), 0L));
            player.setAttack(player.getAttack() - Objects.requireNonNullElse(oldItem.getAttack(), 0L));
            player.setDefense(player.getDefense() - Objects.requireNonNullElse(oldItem.getDefense(), 0L));
            player.setSpeed(player.getSpeed() - Objects.requireNonNullElse(oldItem.getSpeed(), 0D));
            player.setCritRate(player.getCritRate() - Objects.requireNonNullElse(oldItem.getCritRate(), 0D));
            player.setCritDamage(player.getCritDamage() - Objects.requireNonNullElse(oldItem.getCritDamage(), 0D));
            player.setLifeSteal(player.getLifeSteal() - Objects.requireNonNullElse(oldItem.getLifeSteal(), 0D));
            equipment.setItem(null);
        }

        player.setHp(player.getHp() + Objects.requireNonNullElse(newItem.getHp(), 0L));
        player.setAttack(player.getAttack() + Objects.requireNonNullElse(newItem.getAttack(), 0L));
        player.setDefense(player.getDefense() + Objects.requireNonNullElse(newItem.getDefense(), 0L));
        player.setSpeed(player.getSpeed() + Objects.requireNonNullElse(newItem.getSpeed(), 0D));
        player.setCritRate(player.getCritRate() + Objects.requireNonNullElse(newItem.getCritRate(), 0D));
        player.setCritDamage(player.getCritDamage() + Objects.requireNonNullElse(newItem.getCritDamage(), 0D));
        player.setLifeSteal(player.getLifeSteal() + Objects.requireNonNullElse(newItem.getLifeSteal(), 0D));

        equipment.setItem(newItem);

        inventorySlot.setQuantity(inventorySlot.getQuantity() - 1);

        if(inventorySlot.getQuantity() <= 0) {
            inventorySlot.setQuantity(null);
            inventorySlot.setItem(null);
        }

        playerInventoryRepository.save(inventorySlot);
        playerRepository.save(player);
        playerEquipmentRepository.save(equipment);
    }

    public ServiceResult<Void> unequipItem(UUID playerId, UnequipRequest request) {
        try {
            if (playerId == null || request == null) {
                return ServiceResult.failure("Player ID and request body are required", HttpStatus.BAD_REQUEST);
            }

            if (request.equipmentSlotType() == null) {
                return ServiceResult.failure("Equipment slot type is required", HttpStatus.BAD_REQUEST);
            }

            var playerOptional = playerRepository.findById(playerId);
            if (playerOptional.isEmpty()) {
                return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
            }
            var player = playerOptional.get();

            var equipmentOptional = playerEquipmentRepository.findByPlayer_IdAndEquipmentSlotType(playerId, request.equipmentSlotType());

            if (equipmentOptional.isEmpty()) {
                return ServiceResult.failure("Equipment slot not found", HttpStatus.NOT_FOUND);
            }

            var equipment = equipmentOptional.get();
            var itemToUnequip = equipment.getItem();

            if (itemToUnequip == null) {
                return ServiceResult.failure("No item equipped in this slot", HttpStatus.BAD_REQUEST);
            }

            if (request.id() != null && equipment.getId() != null && !equipment.getId().equals(request.id())) {
                return ServiceResult.failure("Equipment slot ID mismatch", HttpStatus.BAD_REQUEST);
            }

            var playerInventory = playerInventoryRepository.findByPlayer_Id(playerId);
            if (playerInventory.isEmpty()) {
                return ServiceResult.failure("Player inventory not found", HttpStatus.NOT_FOUND);
            }

            PlayerInventory targetSlot = null;

            for (var slot : playerInventory) {
                if (slot.getItem() != null && slot.getItem().getId().equals(itemToUnequip.getId())) {
                    targetSlot = slot;
                    break;
                }
            }

            if (targetSlot == null) {
                for (var slot : playerInventory) {
                    if (slot.getItem() == null || slot.getQuantity() == null || slot.getQuantity() == 0) {
                        targetSlot = slot;
                        break;
                    }
                }
            }

            if (targetSlot == null) {
                return ServiceResult.failure("Inventory is full. Cannot unequip item.", HttpStatus.BAD_REQUEST);
            }

            player.setHp(player.getHp() - Objects.requireNonNullElse(itemToUnequip.getHp(), 0L));
            player.setAttack(player.getAttack() - Objects.requireNonNullElse(itemToUnequip.getAttack(), 0L));
            player.setDefense(player.getDefense() - Objects.requireNonNullElse(itemToUnequip.getDefense(), 0L));
            player.setSpeed(player.getSpeed() - Objects.requireNonNullElse(itemToUnequip.getSpeed(), 0D));
            player.setCritRate(player.getCritRate() - Objects.requireNonNullElse(itemToUnequip.getCritRate(), 0D));
            player.setCritDamage(player.getCritDamage() - Objects.requireNonNullElse(itemToUnequip.getCritDamage(), 0D));
            player.setLifeSteal(player.getLifeSteal() - Objects.requireNonNullElse(itemToUnequip.getLifeSteal(), 0D));

            int currentQuantity = (targetSlot.getQuantity() == null) ? 0 : targetSlot.getQuantity();
            targetSlot.setItem(itemToUnequip);
            targetSlot.setQuantity(currentQuantity + 1);

            equipment.setItem(null);

            playerInventoryRepository.save(targetSlot);
            playerEquipmentRepository.save(equipment);
            playerRepository.save(player);

            return ServiceResult.success("Item unequipped successfully", HttpStatus.NO_CONTENT);

        } catch (Exception ex) {
            return ServiceResult.failure("Error unequipping item", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //TODO: MEDIUM, Confirm logic
    public ServiceResult<Void> addToInventory(AddToInventoryRequest request, UUID playerId) {
        try {
            if (request == null || request.itemId() == null) {
                return ServiceResult.failure("Item id is required", HttpStatus.BAD_REQUEST);
            }

            if (request.quantity() == null || request.quantity() <= 0) {
                return ServiceResult.failure("Quantity must be greater than 0", HttpStatus.BAD_REQUEST);
            }

            var playerEntity = playerRepository.findById(playerId);

            if (playerEntity.isEmpty()) {
                return ServiceResult.failure("Player not found", HttpStatus.NOT_FOUND);
            }

            var item = itemRepository.findById(request.itemId());

            if (item.isEmpty()) {
                return ServiceResult.failure("Item not found", HttpStatus.NOT_FOUND);
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
                if (slot.getItem() == null || slot.getQuantity() == null || slot.getQuantity() == 0) {
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
