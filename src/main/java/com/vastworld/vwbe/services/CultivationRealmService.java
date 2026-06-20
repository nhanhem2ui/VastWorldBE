package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.cultivationrealm.CultivationRealmDTO;
import com.vastworld.vwbe.entites.CultivationRealm;
import com.vastworld.vwbe.repositories.CultivationRealmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CultivationRealmService {
    private final CultivationRealmRepository cultivationRealmRepository;
    private final RedisService redisService;

    public CultivationRealmService(CultivationRealmRepository cultivationRealmRepository, RedisService redisService) {
        this.cultivationRealmRepository = cultivationRealmRepository;
        this.redisService = redisService;
    }

    public ServiceResult<List<CultivationRealmDTO>> getAllCultivationRealms() {

        try {
            String cacheKey = CacheKeys.cultivationRealms("all");

            var cached = redisService.get(cacheKey, new TypeReference<List<CultivationRealmDTO>>() {});

            if(cached != null){
                return ServiceResult.success("Cultivation realm retrieved successfully", cached);
            }

            var cultivationRealmList =
                    cultivationRealmRepository.findAll();

            if(cultivationRealmList.isEmpty()){
                return ServiceResult.failure("No cultivation realm found");
            }

            var dtoList = cultivationRealmList.stream()
                            .map(this::toDto)
                            .toList();
            redisService.set(cacheKey, dtoList);

            return ServiceResult.success("Cultivation realm retrieved successfully", dtoList);
        }
        catch(Exception ex){
            return ServiceResult.failure("Error retrieving cultivation realm", ex);
        }

    }

    public ServiceResult<CultivationRealmDTO> getCultivationRealmById(Integer id) {
        try {
            var serviceResult = getCultivationRealmEntityById(id);
            if(!serviceResult.isSuccess()){
                return ServiceResult.failure(serviceResult.getMessage());
            }
            return ServiceResult.success(serviceResult.getMessage(), toDto(serviceResult.getData()));
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving cultivation realm", ex);
        }
    }

    public ServiceResult<CultivationRealm> getCultivationRealmEntityById(Integer id){
        if (id == null || id <= 0) {
            return ServiceResult.failure("Cultivation realm id is invalid");
        }

        var cachedKey = CacheKeys.cultivationRealms("entity", id);
        var cached = redisService.get(cachedKey, new TypeReference<CultivationRealm>() {});

        if(cached != null){
            return ServiceResult.success("Cultivation realm retrieved successfully", cached);
        }

        var cultivationRealm = cultivationRealmRepository.findById(id);
        return cultivationRealm.map(realm -> ServiceResult.success("Cultivation realm retrieved successfully", realm))
                .orElseGet(() -> ServiceResult.failure("Cultivation realm not found"));
    }

    public ServiceResult<CultivationRealmDTO> createCultivationRealm(CultivationRealmDTO dto) {
        try {
            var validationResult = validateDto(dto, false);
            if (validationResult != null) {
                return validationResult;
            }

            if (cultivationRealmRepository.existsByNameIgnoreCase(dto.name().trim())) {
                return ServiceResult.failure("Cultivation realm name already exists");
            }

            CultivationRealm cultivationRealm = new CultivationRealm();
            cultivationRealm.setName(dto.name().trim());
            cultivationRealm.setRealmOrder(dto.realmOrder());
            cultivationRealm.setIsImmortal(Boolean.TRUE.equals(dto.isImmortal()));

            var savedCultivationRealm = cultivationRealmRepository.save(cultivationRealm);

            return ServiceResult.success("Cultivation realm created successfully", toDto(savedCultivationRealm));
        } catch (Exception ex) {
            return ServiceResult.failure("Error creating cultivation realm", ex);
        }
    }

    public ServiceResult<CultivationRealmDTO> updateCultivationRealm(Integer id, CultivationRealmDTO dto) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Cultivation realm id is invalid");
            }

            var validationResult = validateDto(dto, true);
            if (validationResult != null) {
                return validationResult;
            }

            var cultivationRealmOptional = cultivationRealmRepository.findById(id);
            if (cultivationRealmOptional.isEmpty()) {
                return ServiceResult.failure("Cultivation realm not found");
            }

            if (cultivationRealmRepository.existsByNameIgnoreCaseAndIdNot(dto.name().trim(), id)) {
                return ServiceResult.failure("Cultivation realm name already exists");
            }

            var cultivationRealm = cultivationRealmOptional.get();
            cultivationRealm.setName(dto.name().trim());
            cultivationRealm.setRealmOrder(dto.realmOrder());
            cultivationRealm.setIsImmortal(Boolean.TRUE.equals(dto.isImmortal()));

            var updatedCultivationRealm = cultivationRealmRepository.save(cultivationRealm);
            return ServiceResult.success("Cultivation realm updated successfully", toDto(updatedCultivationRealm));
        } catch (Exception ex) {
            return ServiceResult.failure("Error updating cultivation realm", ex);
        }
    }

    public ServiceResult<Void> deleteCultivationRealm(Integer id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Cultivation realm id is invalid");
            }

            if (!cultivationRealmRepository.existsById(id)) {
                return ServiceResult.failure("Cultivation realm not found");
            }

            cultivationRealmRepository.deleteById(id);

            return ServiceResult.success("Cultivation realm deleted successfully");
        } catch (Exception ex) {
            return ServiceResult.failure("Error deleting cultivation realm", ex);
        }
    }

    private CultivationRealmDTO toDto(CultivationRealm cultivationRealm) {
        return new CultivationRealmDTO(
                cultivationRealm.getId(),
                cultivationRealm.getName(),
                cultivationRealm.getRealmOrder(),
                cultivationRealm.getIsImmortal() != null ? cultivationRealm.getIsImmortal() : false
        );
    }

    private ServiceResult<CultivationRealmDTO> validateDto(CultivationRealmDTO dto, boolean requireIdInPayload) {
        if (dto == null) {
            return ServiceResult.failure("Cultivation realm data is required");
        }

        if (requireIdInPayload && dto.id() != null && dto.id() <= 0) {
            return ServiceResult.failure("Cultivation realm id is invalid");
        }

        if (dto.name() == null || dto.name().trim().isEmpty()) {
            return ServiceResult.failure("Cultivation realm name is required");
        }

        if (dto.realmOrder() == null || dto.realmOrder() <= 0) {
            return ServiceResult.failure("Cultivation realm order must be greater than 0");
        }

        return null;
    }
}
