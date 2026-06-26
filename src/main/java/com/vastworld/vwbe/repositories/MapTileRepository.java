package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.MapTile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MapTileRepository extends JpaRepository<MapTile, Integer> {
    List<MapTile> findByGameMap_MapId(Integer mapId);
}
