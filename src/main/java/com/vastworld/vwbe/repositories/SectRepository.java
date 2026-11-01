package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entities.Sect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectRepository extends JpaRepository<Sect, Integer> {
    List<Sect> findByRegion_RegionId(Integer regionId);
}
