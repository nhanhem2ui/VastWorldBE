package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.PlayerSpiritRoot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerSpiritRootRepository extends JpaRepository<PlayerSpiritRoot, Integer> {
    boolean existsByPlayer_IdAndSpiritRoot_Id(UUID playerId, Integer spiritRootId);
    boolean existsByPlayer_IdAndSpiritRoot_IdAndIdNot(UUID playerId, Integer spiritRootId, Integer id);
}
