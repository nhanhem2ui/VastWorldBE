package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.RealmStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RealmStageRepository extends JpaRepository<RealmStage, Integer> {
    boolean existsByStageLevel(Integer stageLevel);

    Optional<RealmStage> findByStageLevel(Integer stageLevel);

    boolean existsByStageNameIgnoreCase(String stageName);

    boolean existsByStageLevelAndIdNot(Integer stageLevel, Integer id);

    boolean existsByStageNameIgnoreCaseAndIdNot(String stageName, Integer id);
}
