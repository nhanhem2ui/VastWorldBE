package com.vastworld.vwbe.entites;

import com.vastworld.vwbe.common.enums.CombatStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CombatSessions")
@Getter
@Setter
public class CombatSession {

    @Id
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ArenaId", nullable = false)
    private HexArena arena;

    // Assuming a Player entity exists
    @Column(name = "InitiatorId", nullable = false)
    private UUID initiatorId;

    @Column(name = "DefenderId", nullable = false)
    private UUID defenderId;

    @Column(name = "CurrentTurnPlayerId")
    private UUID currentTurnPlayerId;

    @Column(name = "RoundNumber", nullable = false)
    private Integer roundNumber = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private CombatStatus status = CombatStatus.WAITING;

    @Column(name = "WinnerId")
    private UUID winnerId;

    @Column(name = "StartedAt", nullable = false, updatable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "EndedAt")
    private LocalDateTime endedAt;
}
