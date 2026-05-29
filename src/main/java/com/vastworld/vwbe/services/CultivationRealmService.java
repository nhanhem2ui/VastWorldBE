package com.vastworld.vwbe.services;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.cultivationrealm.CultivationRealmDTO;
import com.vastworld.vwbe.entites.CultivationRealm;
import com.vastworld.vwbe.repositories.CultivationRealmRepository;
import jakarta.servlet.ServletContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class CultivationRealmService {
    private final CultivationRealmRepository cultivationRealmRepository;

    public CultivationRealmService(CultivationRealmRepository cultivationRealmRepository) {
        this.cultivationRealmRepository = cultivationRealmRepository;
    }

    public ServiceResult<List<CultivationRealmDTO>> getAllCultivationRealms() {
        try {
            var cultivationRealmList = cultivationRealmRepository.findAll();

            if (cultivationRealmList.isEmpty()) {
                return ServiceResult.failure("No cultivation realm found");
            }

            var dtoList = cultivationRealmList.stream()
                    .map(this::toDto)
                    .toList();

            return ServiceResult.success("Cultivation realm retrieved successfully", dtoList);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving cultivation realm", ex);
        }
    }

    public ServiceResult<CultivationRealmDTO> getCultivationRealmById(Integer id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Cultivation realm id is invalid");
            }

            var cultivationRealm = cultivationRealmRepository.findById(id);
            if (cultivationRealm.isPresent()) {
                return ServiceResult.success("Cultivation realm retrieved successfully", toDto(cultivationRealm.get()));
            } else {
                return ServiceResult.failure("Cultivation realm not found");
            }
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving cultivation realm", ex);
        }
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
