package com.vastworld.vwbe.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vastworld.vwbe.common.CacheKeys;
import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.realmstage.GetRealmStageResponse;
import com.vastworld.vwbe.dto.realmstage.RealmStageDTO;
import com.vastworld.vwbe.entites.RealmStage;
import com.vastworld.vwbe.repositories.RealmStageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class RealmStageService {
    private final RealmStageRepository realmStageRepository;
    private final RedisService redisService;

    public RealmStageService(RealmStageRepository realmStageRepository, RedisService redisService) {
        this.realmStageRepository = realmStageRepository;
        this.redisService = redisService;
    }

    public ServiceResult<List<RealmStageDTO>> getAllRealmStages() {
        try {
            var realmStageList = realmStageRepository.findAll();

            if (realmStageList.isEmpty()) {
                return ServiceResult.failure("No realm stage found");
            }

            var dtoList = realmStageList.stream()
                    .map(this::toDto)
                    .toList();

            return ServiceResult.success("Realm stage retrieved successfully", dtoList);
        } catch (Exception ex) {
            ServiceResult<List<RealmStageDTO>> result = new ServiceResult<>();
            result.setSuccess(false);
            result.setMessage("Error retrieving realm stage");
            result.setErrors(Collections.singletonList(ex.getMessage()));
            return ServiceResult.failure("Error retrieving realm stage",ex);
        }
    }

    public ServiceResult<GetRealmStageResponse> getRealmStageById(Integer id) {
        try {

            var cachedKey = CacheKeys.realmStages(id);
            var cached = redisService.get(cachedKey, new TypeReference<GetRealmStageResponse>() {});

            if(cached != null){
                return ServiceResult.success("Realm stage retrieved successfully", cached, HttpStatus.OK);
            }
            var realmStage = getRealmStageEntityById(id).getData();

            var data  = new GetRealmStageResponse(realmStage.getStageLevel(), realmStage.getStageName());

            return ServiceResult.success("Realm stage retrieved successfully", data, HttpStatus.OK);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving realm stage", ex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResult<RealmStage> getRealmStageEntityById(Integer id){
        if (id == null || id <= 0) {
            return ServiceResult.failure("Realm stage id is invalid");
        }
        var realmStageOptional = realmStageRepository.findById(id);
        return realmStageOptional.map(realmStage -> ServiceResult.success("Realm stage retrieved successfully", realmStage))
                .orElseGet(() -> ServiceResult.failure("Realm stage not found"));
    }

    public ServiceResult<RealmStageDTO> createRealmStage(RealmStageDTO dto) {
        try {
            var validationResult = validateDto(dto, false);
            if (validationResult != null) {
                return validationResult;
            }

            if (realmStageRepository.existsByStageLevel(dto.stageLevel())) {
                return ServiceResult.failure("Realm stage level already exists");
            }

            if (realmStageRepository.existsByStageNameIgnoreCase(dto.stageName().trim())) {
                return ServiceResult.failure("Realm stage name already exists");
            }

            RealmStage realmStage = new RealmStage();
            realmStage.setStageLevel(dto.stageLevel());
            realmStage.setStageName(dto.stageName().trim());

            var savedRealmStage = realmStageRepository.save(realmStage);
            return ServiceResult.success("Realm stage created successfully", toDto(savedRealmStage));
        } catch (Exception ex) {
            return ServiceResult.failure("Error creating realm stage", ex);
        }
    }

    public ServiceResult<RealmStageDTO> updateRealmStage(Integer id, RealmStageDTO dto) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Realm stage id is invalid");
            }

            var validationResult = validateDto(dto, true);
            if (validationResult != null) {
                return validationResult;
            }

            var realmStageOptional = realmStageRepository.findById(id);
            if (realmStageOptional.isEmpty()) {
                return ServiceResult.failure("Realm stage not found");
            }

            if (realmStageRepository.existsByStageLevelAndIdNot(dto.stageLevel(), id)) {
                return ServiceResult.failure("Realm stage level already exists");
            }

            if (realmStageRepository.existsByStageNameIgnoreCaseAndIdNot(dto.stageName().trim(), id)) {
                return ServiceResult.failure("Realm stage name already exists");
            }

            var realmStage = realmStageOptional.get();
            realmStage.setStageLevel(dto.stageLevel());
            realmStage.setStageName(dto.stageName().trim());

            var updatedRealmStage = realmStageRepository.save(realmStage);
            return ServiceResult.success("Realm stage updated successfully", toDto(updatedRealmStage));
        } catch (Exception ex) {
            return ServiceResult.failure("Error updating realm stage", ex);
        }
    }

    public ServiceResult<Void> deleteRealmStage(Integer id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Realm stage id is invalid");
            }

            if (!realmStageRepository.existsById(id)) {
                return ServiceResult.failure("Realm stage not found");
            }

            realmStageRepository.deleteById(id);

            ServiceResult<Void> result = new ServiceResult<>();
            result.setSuccess(true);
            result.setMessage("Realm stage deleted successfully");
            return result;
        } catch (Exception ex) {
            return ServiceResult.failure("Error deleting realm stage", ex);
        }
    }

    private RealmStageDTO toDto(RealmStage realmStage) {
        return new RealmStageDTO(
                realmStage.getId(),
                realmStage.getStageLevel(),
                realmStage.getStageName()
        );
    }

    private ServiceResult<RealmStageDTO> validateDto(RealmStageDTO dto, boolean requireIdInPayload) {
        if (dto == null) {
            return ServiceResult.failure("Realm stage data is required");
        }

        if (requireIdInPayload && dto.id() != null && dto.id() <= 0) {
            return ServiceResult.failure("Realm stage id is invalid");
        }

        if (dto.stageLevel() == null || dto.stageLevel() <= 0) {
            return ServiceResult.failure("Realm stage level must be greater than 0");
        }

        if (dto.stageName() == null || dto.stageName().trim().isEmpty()) {
            return ServiceResult.failure("Realm stage name is required");
        }

        return null;
    }
}
