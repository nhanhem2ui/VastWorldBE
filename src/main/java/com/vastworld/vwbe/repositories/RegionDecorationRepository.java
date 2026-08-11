package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.RegionDecoration;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RegionDecorationRepository extends JpaRepository<RegionDecoration, Integer> {
    @EntityGraph(attributePaths = "map")
    List<RegionDecoration> findByRegion_RegionId(Integer regionId);
    Optional<RegionDecoration> findByMap_MapId(Integer mapId);
}
