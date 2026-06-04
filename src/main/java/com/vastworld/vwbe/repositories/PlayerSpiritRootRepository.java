package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.PlayerSpiritRoot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerSpiritRootRepository extends JpaRepository<PlayerSpiritRoot, Integer> {
    List<PlayerSpiritRoot> findByPlayer_Id(UUID playerId);
    void deleteByPlayer_Id(UUID playerId);
    boolean existsByPlayer_IdAndSpiritRoot_Id(UUID playerId, Integer spiritRootId);
    boolean existsByPlayer_IdAndSpiritRoot_IdAndIdNot(UUID playerId, Integer spiritRootId, Integer id);
}
