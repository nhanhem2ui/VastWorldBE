package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
}