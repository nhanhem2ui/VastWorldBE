package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entities.PlayerSect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerSectRepository extends JpaRepository<PlayerSect, UUID> {
}
