package com.vastworld.vwbe.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "PlayerSects")
public class PlayerSect {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id")
    private UUID id;

    @Column(name = "PlayerId", nullable = false, unique = true)
    private UUID playerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SectId", nullable = false)
    private Sect sect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RankId", nullable = false)
    private SectRank rank;
}