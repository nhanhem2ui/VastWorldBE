package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.GameAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameAssetRepository extends JpaRepository<GameAsset, Integer> {
}
