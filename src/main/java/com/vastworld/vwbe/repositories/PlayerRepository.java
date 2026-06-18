package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.lang.management.PlatformLoggingMXBean;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<Player> findPlayerByAccount_Id(UUID accountId);
}