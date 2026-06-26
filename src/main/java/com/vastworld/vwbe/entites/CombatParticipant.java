package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "CombatParticipants")
@Getter
@Setter
public class CombatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SessionId", nullable = false)
    private CombatSession combatSession;

    @Column(name = "PlayerId", nullable = false)
    private UUID playerId;

    @Column(name = "HexCol", nullable = false)
    private Integer hexCol;

    @Column(name = "HexRow", nullable = false)
    private Integer hexRow;

    @Column(name = "CurrentHP", nullable = false)
    private Long currentHP;

    @Column(name = "CurrentAP", nullable = false)
    private Integer currentAP = 10;

    @Column(name = "HasMovedThisTurn", nullable = false)
    private Boolean hasMovedThisTurn = false;

    @Column(name = "HasActedThisTurn", nullable = false)
    private Boolean hasActedThisTurn = false;
}
