package com.vastworld.vwbe.services;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.itemtype.ItemTypeDTO;
import com.vastworld.vwbe.entites.ItemType;
import com.vastworld.vwbe.repositories.ItemTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemTypeService {
    private final ItemTypeRepository itemTypeRepository;

    public ItemTypeService(ItemTypeRepository itemTypeRepository) {
        this.itemTypeRepository = itemTypeRepository;
    }

    public ServiceResult<List<ItemTypeDTO>> getAllItemTypes() {
        try {
            var itemTypeList = itemTypeRepository.findAll();

            if (itemTypeList.isEmpty()) {
                return ServiceResult.failure("No item type found");
            }

            var dtoList = itemTypeList.stream()
                    .map(this::toDto)
                    .toList();

            return ServiceResult.success("Item type retrieved successfully", dtoList);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving item type", ex);
        }
    }

    public ServiceResult<ItemTypeDTO> getItemTypeById(Integer id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Item type id is invalid");
            }

            var itemType = itemTypeRepository.findById(id);
            if (itemType.isEmpty()) {
                return ServiceResult.failure("Item type not found");
            }

            return ServiceResult.success("Item type retrieved successfully", toDto(itemType.get()));
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving item type", ex);
        }
    }

    public ServiceResult<ItemTypeDTO> createItemType(ItemTypeDTO dto) {
        try {
            var validationResult = validateDto(dto, false);
            if (validationResult != null) {
                return validationResult;
            }

            if (itemTypeRepository.existsByNameIgnoreCase(dto.name().trim())) {
                return ServiceResult.failure("Item type name already exists");
            }

            ItemType itemType = new ItemType();
            itemType.setName(dto.name().trim());

            var savedItemType = itemTypeRepository.save(itemType);
            return ServiceResult.success("Item type created successfully", toDto(savedItemType));
        } catch (Exception ex) {
            return ServiceResult.failure("Error creating item type", ex);
        }
    }

    public ServiceResult<ItemTypeDTO> updateItemType(Integer id, ItemTypeDTO dto) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Item type id is invalid");
            }

            var validationResult = validateDto(dto, true);
            if (validationResult != null) {
                return validationResult;
            }

            var itemTypeOptional = itemTypeRepository.findById(id);
            if (itemTypeOptional.isEmpty()) {
                return ServiceResult.failure("Item type not found");
            }

            if (itemTypeRepository.existsByNameIgnoreCaseAndIdNot(dto.name().trim(), id)) {
                return ServiceResult.failure("Item type name already exists");
            }

            var itemType = itemTypeOptional.get();
            itemType.setName(dto.name().trim());

            var updatedItemType = itemTypeRepository.save(itemType);
            return ServiceResult.success("Item type updated successfully", toDto(updatedItemType));
        } catch (Exception ex) {
            return ServiceResult.failure("Error updating item type", ex);
        }
    }

    public ServiceResult<Void> deleteItemType(Integer id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Item type id is invalid");
            }

            if (!itemTypeRepository.existsById(id)) {
                return ServiceResult.failure("Item type not found");
            }

            itemTypeRepository.deleteById(id);
            return ServiceResult.success("Item type deleted successfully");
        } catch (Exception ex) {
            return ServiceResult.failure("Error deleting item type", ex);
        }
    }

    private ItemTypeDTO toDto(ItemType itemType) {
        return new ItemTypeDTO(itemType.getId(), itemType.getName());
    }

    private ServiceResult<ItemTypeDTO> validateDto(ItemTypeDTO dto, boolean requireIdInPayload) {
        if (dto == null) {
            return ServiceResult.failure("Item type data is required");
        }

        if (requireIdInPayload && dto.id() != null && dto.id() <= 0) {
            return ServiceResult.failure("Item type id is invalid");
        }

        if (dto.name() == null || dto.name().trim().isEmpty()) {
            return ServiceResult.failure("Item type name is required");
        }

        return null;
    }
}
