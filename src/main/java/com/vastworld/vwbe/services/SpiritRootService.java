package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.spiritroot.SpiritRootDTO;
import com.vastworld.vwbe.entites.SpiritRoot;
import com.vastworld.vwbe.repositories.SpiritRootRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SpiritRootService {
    private final SpiritRootRepository spiritRootRepository;
    private final RedisService redisService;

    public SpiritRootService(SpiritRootRepository spiritRootRepository, RedisService redisService) {
        this.spiritRootRepository = spiritRootRepository;
        this.redisService = redisService;
    }

    public ServiceResult<List<SpiritRootDTO>> getAllSpiritRoots() {
        try {
            var cacheKey = CacheKeys.spiritRoots("all");

            var cached = redisService.get(cacheKey, new TypeReference<List<SpiritRootDTO>>() {});

            if(cached != null){
                return ServiceResult.success("Spirit root retrieved successfully", cached);
            }

            var spiritRootList = spiritRootRepository.findAll();
            if (spiritRootList.isEmpty()) {
                return ServiceResult.failure("No spirit root found");
            }

            var dtoList = spiritRootList.stream()
                    .map(this::toDto)
                    .toList();

            return ServiceResult.success("Spirit root retrieved successfully", dtoList);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving spirit root", ex);
        }
    }

    public ServiceResult<SpiritRootDTO> getSpiritRootById(Integer id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Spirit root id is invalid");
            }

            var cacheKey = CacheKeys.spiritRoots(id);
            var cached = redisService.get(cacheKey, new TypeReference<SpiritRootDTO>(){});

            if(cached != null){
                return ServiceResult.success("Spirit root retrieved successfully", cached);
            }

            var spiritRoot = spiritRootRepository.findById(id);
            if (spiritRoot.isEmpty()) {
                return ServiceResult.failure("Spirit root not found");
            }

            var data = toDto(spiritRoot.get());
            redisService.set(cacheKey, data);

            return ServiceResult.success("Spirit root retrieved successfully", data);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving spirit root", ex);
        }
    }

    public ServiceResult<SpiritRootDTO> createSpiritRoot(SpiritRootDTO dto) {
        try {
            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            if (spiritRootRepository.existsByNameIgnoreCase(dto.name().trim())) {
                return ServiceResult.failure("Spirit root name already exists");
            }

            SpiritRoot spiritRoot = new SpiritRoot();
            applyDto(spiritRoot, dto);

            var savedSpiritRoot = spiritRootRepository.save(spiritRoot);
            return ServiceResult.success("Spirit root created successfully", toDto(savedSpiritRoot));
        } catch (Exception ex) {
            return ServiceResult.failure("Error creating spirit root", ex);
        }
    }

    public ServiceResult<SpiritRootDTO> updateSpiritRoot(Integer id, SpiritRootDTO dto) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Spirit root id is invalid");
            }

            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            var spiritRootOptional = spiritRootRepository.findById(id);
            if (spiritRootOptional.isEmpty()) {
                return ServiceResult.failure("Spirit root not found");
            }

            if (spiritRootRepository.existsByNameIgnoreCaseAndIdNot(dto.name().trim(), id)) {
                return ServiceResult.failure("Spirit root name already exists");
            }

            var spiritRoot = spiritRootOptional.get();
            applyDto(spiritRoot, dto);

            var updatedSpiritRoot = spiritRootRepository.save(spiritRoot);
            return ServiceResult.success("Spirit root updated successfully", toDto(updatedSpiritRoot));
        } catch (Exception ex) {
            return ServiceResult.failure("Error updating spirit root", ex);
        }
    }

    public ServiceResult<Void> deleteSpiritRoot(Integer id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Spirit root id is invalid");
            }

            if (!spiritRootRepository.existsById(id)) {
                return ServiceResult.failure("Spirit root not found");
            }

            spiritRootRepository.deleteById(id);
            return ServiceResult.success("Spirit root deleted successfully");
        } catch (Exception ex) {
            return ServiceResult.failure("Error deleting spirit root", ex);
        }
    }

    private void applyDto(SpiritRoot spiritRoot, SpiritRootDTO dto) {
        spiritRoot.setName(dto.name().trim());
        spiritRoot.setIsVariant(Boolean.TRUE.equals(dto.isVariant()));
    }

    private SpiritRootDTO toDto(SpiritRoot spiritRoot) {
        return new SpiritRootDTO(spiritRoot.getId(), spiritRoot.getName(), spiritRoot.getIsVariant());
    }

    private ServiceResult<SpiritRootDTO> validateDto(SpiritRootDTO dto) {
        if (dto == null) {
            return ServiceResult.failure("Spirit root data is required");
        }

        if (dto.name() == null || dto.name().trim().isEmpty()) {
            return ServiceResult.failure("Spirit root name is required");
        }

        return null;
    }
}
