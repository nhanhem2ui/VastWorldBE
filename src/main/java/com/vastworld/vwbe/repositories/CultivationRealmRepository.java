package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.CultivationRealm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CultivationRealmRepository extends JpaRepository<CultivationRealm, Integer> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
