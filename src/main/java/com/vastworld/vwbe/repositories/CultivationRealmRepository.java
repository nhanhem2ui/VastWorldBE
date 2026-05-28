package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.dto.cultivationrealm.CultivationRealmDTO;
import com.vastworld.vwbe.entites.CultivationRealm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CultivationRealmRepository extends JpaRepository<CultivationRealm, Integer> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
