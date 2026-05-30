package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.SpiritRoot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpiritRootRepository extends JpaRepository<SpiritRoot, Integer>{
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}
