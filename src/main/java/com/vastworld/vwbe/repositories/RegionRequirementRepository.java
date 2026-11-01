package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entities.RegionRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RegionRequirementRepository extends JpaRepository<RegionRequirement, Integer> {
    List<RegionRequirement> findAllByRegion_RegionIdIn(Collection<Integer> regionIds);
}