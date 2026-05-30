package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.RealmBreakthrough;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealmBreakthroughRepository extends JpaRepository<RealmBreakthrough, Integer> {
    boolean existsByRealm_IdAndStageLevel(Integer realmId, Integer stageLevel);
    boolean existsByRealm_IdAndStageLevelAndIdNot(Integer realmId, Integer stageLevel, Integer id);
}
