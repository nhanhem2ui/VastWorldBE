package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.PlayerMeditation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerMeditationRepository extends JpaRepository<PlayerMeditation, Long> {
    List<PlayerMeditation> findByPlayer_Id(UUID playerId);

    Optional<PlayerMeditation> findByPlayer_IdAndIsClaimed(UUID playerId, Boolean isClaimed);
}