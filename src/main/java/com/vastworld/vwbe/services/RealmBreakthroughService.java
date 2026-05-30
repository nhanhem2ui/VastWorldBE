package com.vastworld.vwbe.services;

import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.realmbreakthrough.RealmBreakthroughDTO;
import com.vastworld.vwbe.entites.CultivationRealm;
import com.vastworld.vwbe.entites.RealmBreakthrough;
import com.vastworld.vwbe.repositories.CultivationRealmRepository;
import com.vastworld.vwbe.repositories.RealmBreakthroughRepository;
import com.vastworld.vwbe.repositories.RealmStageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RealmBreakthroughService {
    private final RealmBreakthroughRepository realmBreakthroughRepository;
    private final CultivationRealmRepository cultivationRealmRepository;
    private final RealmStageRepository realmStageRepository;

    public RealmBreakthroughService(
            RealmBreakthroughRepository realmBreakthroughRepository,
            CultivationRealmRepository cultivationRealmRepository,
            RealmStageRepository realmStageRepository) {
        this.realmBreakthroughRepository = realmBreakthroughRepository;
        this.cultivationRealmRepository = cultivationRealmRepository;
        this.realmStageRepository = realmStageRepository;
    }

    public ServiceResult<List<RealmBreakthroughDTO>> getAllRealmBreakthroughs() {
        try {
            var realmBreakthroughList = realmBreakthroughRepository.findAll();
            if (realmBreakthroughList.isEmpty()) {
                return ServiceResult.failure("No realm breakthrough found");
            }

            var dtoList = realmBreakthroughList.stream()
                    .map(this::toDto)
                    .toList();

            return ServiceResult.success("Realm breakthrough retrieved successfully", dtoList);
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving realm breakthrough", ex);
        }
    }

    public ServiceResult<RealmBreakthroughDTO> getRealmBreakthroughById(Integer id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Realm breakthrough id is invalid");
            }

            var realmBreakthrough = realmBreakthroughRepository.findById(id);
            if (realmBreakthrough.isEmpty()) {
                return ServiceResult.failure("Realm breakthrough not found");
            }

            return ServiceResult.success("Realm breakthrough retrieved successfully", toDto(realmBreakthrough.get()));
        } catch (Exception ex) {
            return ServiceResult.failure("Error retrieving realm breakthrough", ex);
        }
    }

    public ServiceResult<RealmBreakthroughDTO> createRealmBreakthrough(RealmBreakthroughDTO dto) {
        try {
            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            if (realmBreakthroughRepository.existsByRealm_IdAndStageLevel(dto.realmId(), dto.stageLevel())) {
                return ServiceResult.failure("Realm breakthrough already exists");
            }

            var realmResult = resolveRealm(dto.realmId());
            if (!realmResult.isSuccess()) {
                return ServiceResult.failure(realmResult.getMessage());
            }

            RealmBreakthrough realmBreakthrough = new RealmBreakthrough();
            applyDto(realmBreakthrough, dto, realmResult.getData());

            var savedRealmBreakthrough = realmBreakthroughRepository.save(realmBreakthrough);
            return ServiceResult.success("Realm breakthrough created successfully", toDto(savedRealmBreakthrough));
        } catch (Exception ex) {
            return ServiceResult.failure("Error creating realm breakthrough", ex);
        }
    }

    public ServiceResult<RealmBreakthroughDTO> updateRealmBreakthrough(Integer id, RealmBreakthroughDTO dto) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Realm breakthrough id is invalid");
            }

            var validationResult = validateDto(dto);
            if (validationResult != null) {
                return validationResult;
            }

            var realmBreakthroughOptional = realmBreakthroughRepository.findById(id);
            if (realmBreakthroughOptional.isEmpty()) {
                return ServiceResult.failure("Realm breakthrough not found");
            }

            if (realmBreakthroughRepository.existsByRealm_IdAndStageLevelAndIdNot(dto.realmId(), dto.stageLevel(), id)) {
                return ServiceResult.failure("Realm breakthrough already exists");
            }

            var realmResult = resolveRealm(dto.realmId());
            if (!realmResult.isSuccess()) {
                return ServiceResult.failure(realmResult.getMessage());
            }

            var realmBreakthrough = realmBreakthroughOptional.get();
            applyDto(realmBreakthrough, dto, realmResult.getData());

            var updatedRealmBreakthrough = realmBreakthroughRepository.save(realmBreakthrough);
            return ServiceResult.success("Realm breakthrough updated successfully", toDto(updatedRealmBreakthrough));
        } catch (Exception ex) {
            return ServiceResult.failure("Error updating realm breakthrough", ex);
        }
    }

    public ServiceResult<Void> deleteRealmBreakthrough(Integer id) {
        try {
            if (id == null || id <= 0) {
                return ServiceResult.failure("Realm breakthrough id is invalid");
            }

            if (!realmBreakthroughRepository.existsById(id)) {
                return ServiceResult.failure("Realm breakthrough not found");
            }

            realmBreakthroughRepository.deleteById(id);
            return ServiceResult.success("Realm breakthrough deleted successfully");
        } catch (Exception ex) {
            return ServiceResult.failure("Error deleting realm breakthrough", ex);
        }
    }

    private ServiceResult<CultivationRealm> resolveRealm(Integer realmId) {
        var realm = cultivationRealmRepository.findById(realmId);
        if (realm.isEmpty()) {
            return ServiceResult.failure("Realm not found");
        }

        return ServiceResult.success("Realm resolved", realm.get());
    }

    private void applyDto(RealmBreakthrough realmBreakthrough, RealmBreakthroughDTO dto, CultivationRealm realm) {
        realmBreakthrough.setRealm(realm);
        realmBreakthrough.setStageLevel(dto.stageLevel());
        realmBreakthrough.setRequiredCultivationPoint(dto.requiredCultivationPoint());
        realmBreakthrough.setRequiresTribulation(Boolean.TRUE.equals(dto.requiresTribulation()));
        realmBreakthrough.setSuccessRate(dto.successRate() != null ? dto.successRate() : 100D);
    }

    private RealmBreakthroughDTO toDto(RealmBreakthrough realmBreakthrough) {
        var realm = realmBreakthrough.getRealm();
        return new RealmBreakthroughDTO(
                realmBreakthrough.getId(),
                realm.getId(),
                realm.getName(),
                realmBreakthrough.getStageLevel(),
                realmBreakthrough.getRequiredCultivationPoint(),
                realmBreakthrough.getRequiresTribulation(),
                realmBreakthrough.getSuccessRate()
        );
    }

    private ServiceResult<RealmBreakthroughDTO> validateDto(RealmBreakthroughDTO dto) {
        if (dto == null) {
            return ServiceResult.failure("Realm breakthrough data is required");
        }

        if (dto.realmId() == null || dto.realmId() <= 0) {
            return ServiceResult.failure("Realm id is invalid");
        }

        if (dto.stageLevel() == null || dto.stageLevel() <= 0) {
            return ServiceResult.failure("Stage level is invalid");
        }

        if (!realmStageRepository.existsByStageLevel(dto.stageLevel())) {
            return ServiceResult.failure("Realm stage not found");
        }

        if (dto.requiredCultivationPoint() == null || dto.requiredCultivationPoint() < 0) {
            return ServiceResult.failure("Required cultivation point must be greater than or equal to 0");
        }

        if (dto.successRate() != null && (dto.successRate() < 0 || dto.successRate() > 100)) {
            return ServiceResult.failure("Success rate must be between 0 and 100");
        }

        return null;
    }
}
