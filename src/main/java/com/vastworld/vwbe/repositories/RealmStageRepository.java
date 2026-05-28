package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.RealmStage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealmStageRepository extends JpaRepository<RealmStage, Integer> {
    boolean existsByStageLevel(Integer stageLevel);

    boolean existsByStageNameIgnoreCase(String stageName);

    boolean existsByStageLevelAndIdNot(Integer stageLevel, Integer id);

    boolean existsByStageNameIgnoreCaseAndIdNot(String stageName, Integer id);
}
