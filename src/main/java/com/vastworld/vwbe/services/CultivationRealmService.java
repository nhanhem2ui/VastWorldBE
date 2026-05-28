package com.vastworld.vwbe.services;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.cultivationrealm.CultivationRealmDTO;
import com.vastworld.vwbe.entites.CultivationRealm;
import com.vastworld.vwbe.repositories.CultivationRealmRepository;
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
                ServiceResult<List<CultivationRealmDTO>> result = new ServiceResult<>();
                result.setSuccess(true);
                result.setMessage("No cultivation realm found");
                result.setData(Collections.emptyList());
                return result;
            }

            var dtoList = cultivationRealmList.stream()
                    .map(n -> new CultivationRealmDTO(
                            n.getId(),
                            n.getName(),
                            n.getRealmOrder(),
                            n.getIsImmortal() != null ? n.getIsImmortal() : false
                    ))
                    .toList();

            ServiceResult<List<CultivationRealmDTO>> result = new ServiceResult<>();
            result.setSuccess(true);
            result.setMessage("Cultivation realm retrieved successfully");
            result.setData(dtoList);
            return result;
        }
        catch (Exception ex) {
            ServiceResult<List<CultivationRealmDTO>> result = new ServiceResult<>();
            result.setSuccess(false);
            result.setMessage("Error retrieving cultivation realm");
            result.setErrors(Collections.singletonList(ex.getMessage()));
            return result;
        }
    }

    public ServiceResult<CultivationRealmDTO> getCultivationRealmById(Integer id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Cultivation realm id is invalid");
            }

            var cultivationRealm = cultivationRealmRepository.findById(id);
            if (cultivationRealm.isEmpty())
                return ServiceResult.failure("Cultivation realm not found");
            else
                return ServiceResult.success("Cultivation realm retrieved successfully", toDto(cultivationRealm.get()));
        }
        catch (Exception ex) {
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
        }
        catch (Exception ex) {
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
        }
        catch (Exception ex) {
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

            ServiceResult<Void> result = new ServiceResult<>();
            result.setSuccess(true);
            result.setMessage("Cultivation realm deleted successfully");
            return result;
        }
        catch (Exception ex) {
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
