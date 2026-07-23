package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.MapInteractable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MapInteractableRepository extends JpaRepository<MapInteractable, Integer> {
    List<MapInteractable> findByMap_MapId(Integer mapId);
}
