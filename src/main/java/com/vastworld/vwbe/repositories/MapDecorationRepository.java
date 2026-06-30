package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.MapDecoration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MapDecorationRepository extends JpaRepository<MapDecoration, Integer> {
    List<MapDecoration> findByMap_MapId(Integer mapId);
}
