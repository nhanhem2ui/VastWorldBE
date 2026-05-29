package com.vastworld.vwbe.services;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.item.ItemDTO;
import com.vastworld.vwbe.entites.CultivationRealm;
import com.vastworld.vwbe.entites.Item;
import com.vastworld.vwbe.entites.ItemType;
import com.vastworld.vwbe.repositories.CultivationRealmRepository;
import com.vastworld.vwbe.repositories.ItemRepository;
import com.vastworld.vwbe.repositories.ItemTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final CultivationRealmRepository cultivationRealmRepository;

    public ItemService(ItemRepository itemRepository, ItemTypeRepository itemTypeRepository, CultivationRealmRepository cultivationRealmRepository) {
        this.itemRepository = itemRepository;
        this.itemTypeRepository = itemTypeRepository;
        this.cultivationRealmRepository = cultivationRealmRepository;
    }

    public ServiceResult<List<ItemDTO>> getAllItems() {
        try {
            var itemList = itemRepository.findAll();

            if (itemList.isEmpty()) {
                return ServiceResult.failure("No item found");
            }

            var dtoList = itemList.stream()
                    .map(this::toDto)
                    .toList();

            return ServiceResult.success("Item retrieved successfully", dtoList);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving item", ex);
        }
    }

    public ServiceResult<ItemDTO> getItemById(UUID id) {
        try {
            if (id == null) {
                return ServiceResult.failure("Item id is invalid");
            }

            var item = itemRepository.findById(id);
            if (item.isEmpty()) {
                return ServiceResult.failure("Item not found");
            }

            return ServiceResult.success("Item retrieved successfully", toDto(item.get()));
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving item", ex);
        }
    }

    public ServiceResult<ItemDTO> createItem(ItemDTO dto) {
        try {
            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            if (itemRepository.existsByNameIgnoreCase(dto.name().trim())) {
                return ServiceResult.failure("Item name already exists");
            }

            var referencesResult = resolveReferences(dto);
            if (!referencesResult.isSuccess()) {
                return ServiceResult.failure(referencesResult.getMessage());
            }

            Item item = new Item();
            applyDto(item, dto);
            item.setItemType(referencesResult.getData().itemType());
            item.setRequiredRealm(referencesResult.getData().requiredRealm());

            var savedItem = itemRepository.save(item);
            return ServiceResult.success("Item created successfully", toDto(savedItem));
        } catch (Exception ex) {
            return ServiceResult.failure("Error creating item", ex);
        }
    }

    public ServiceResult<ItemDTO> updateItem(UUID id, ItemDTO dto) {
        try {
            if (id == null) {
                return ServiceResult.failure("Item id is invalid");
            }

            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            var itemOptional = itemRepository.findById(id);
            if (itemOptional.isEmpty()) {
                return ServiceResult.failure("Item not found");
            }

            if (itemRepository.existsByNameIgnoreCaseAndIdNot(dto.name().trim(), id)) {
                return ServiceResult.failure("Item name already exists");
            }

            var referencesResult = resolveReferences(dto);
            if (!referencesResult.isSuccess()) {
                return ServiceResult.failure(referencesResult.getMessage());
            }

            var item = itemOptional.get();
            applyDto(item, dto);
            item.setItemType(referencesResult.getData().itemType());
            item.setRequiredRealm(referencesResult.getData().requiredRealm());

            var updatedItem = itemRepository.save(item);
            return ServiceResult.success("Item updated successfully", toDto(updatedItem));
        } catch (Exception ex) {
            return ServiceResult.failure("Error updating item", ex);
        }
    }

    public ServiceResult<Void> deleteItem(UUID id) {
        try {
            if (id == null) {
                return ServiceResult.failure("Item id is invalid");
            }

            if (!itemRepository.existsById(id)) {
                return ServiceResult.failure("Item not found");
            }

            itemRepository.deleteById(id);
            return ServiceResult.success("Item deleted successfully");
        } catch (Exception ex) {
            return ServiceResult.failure("Error deleting item", ex);
        }
    }

    private ServiceResult<ItemReferences> resolveReferences(ItemDTO dto) {
        var itemTypeOptional = itemTypeRepository.findById(dto.itemTypeId());
        if (itemTypeOptional.isEmpty()) {
            return ServiceResult.failure("Item type not found");
        }

        if (dto.requiredRealmId() == null) {
            return ServiceResult.success("Item references resolved", new ItemReferences(itemTypeOptional.get(), null));
        }

        if (dto.requiredRealmId() < 0) {
            return ServiceResult.failure("Required realm id is invalid");
        }

        var requiredRealm = cultivationRealmRepository.findById(dto.requiredRealmId());
        if (requiredRealm.isEmpty()) {
            return ServiceResult.failure("Required realm not found");
        }

        return ServiceResult.success("Item references resolved", new ItemReferences(itemTypeOptional.get(), requiredRealm.get()));
    }

    private void applyDto(Item item, ItemDTO dto) {
        item.setName(dto.name().trim());
        item.setMaxUsageCount(dto.maxUsageCount());
        item.setHp(defaultLong(dto.hp()));
        item.setAttack(defaultLong(dto.attack()));
        item.setDefense(defaultLong(dto.defense()));
        item.setCritRate(defaultDouble(dto.critRate()));
        item.setCritDamage(defaultDouble(dto.critDamage()));
        item.setSpeed(defaultDouble(dto.speed()));
        item.setLifeSteal(defaultDouble(dto.lifeSteal()));
        item.setCultivationSpeed(defaultDouble(dto.cultivationSpeed()));
    }

    private ItemDTO toDto(Item item) {
        var requiredRealm = item.getRequiredRealm();

        return new ItemDTO(
                item.getId(),
                item.getName(),
                item.getItemType().getId(),
                item.getItemType().getName(),
                item.getMaxUsageCount(),
                requiredRealm != null ? requiredRealm.getId() : null,
                requiredRealm != null ? requiredRealm.getName() : null,
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

    private ServiceResult<ItemDTO> validateDto(ItemDTO dto) {
        if (dto == null) {
            return ServiceResult.failure("Item data is required");
        }

        if (dto.name() == null || dto.name().trim().isEmpty()) {
            return ServiceResult.failure("Item name is required");
        }

        if (dto.itemTypeId() == null || dto.itemTypeId() <= 0) {
            return ServiceResult.failure("Item type id is invalid");
        }

        if (dto.maxUsageCount() != null && dto.maxUsageCount() <= 0) {
            return ServiceResult.failure("Max usage count must be greater than 0");
        }

        if (isNegative(dto.hp())
                || isNegative(dto.attack())
                || isNegative(dto.defense())
                || isNegative(dto.critRate())
                || isNegative(dto.critDamage())
                || isNegative(dto.speed())
                || isNegative(dto.lifeSteal())
                || isNegative(dto.cultivationSpeed())) {
            return ServiceResult.failure("Item stats must be greater than or equal to 0");
        }

        return null;
    }

    private Long defaultLong(Long value) {
        return value != null ? value : 0L;
    }

    private Double defaultDouble(Double value) {
        return value != null ? value : 0D;
    }

    private boolean isNegative(Number value) {
        return value != null && value.doubleValue() < 0;
    }

    private record ItemReferences(ItemType itemType, CultivationRealm requiredRealm) {
    }
}
