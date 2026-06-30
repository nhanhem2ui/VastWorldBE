package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.PlayerLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlayerLocationRepository extends JpaRepository<PlayerLocation, UUID> {
    Optional<PlayerLocation> findByPlayer_Id(UUID id);
}
