package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "Players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountId", nullable = false)
    private Account account;

    @Column(name = "RealmId", nullable = false)
    private Integer realmId;

    @Column(name = "Gender")
    private Boolean gender;

    @Column(name = "RollNum")
    private Integer rollNum = 10;

    @Column(name = "RealmStage", nullable = false)
    private Integer realmStage;

    @Column(name = "HP", nullable = false)
    private Long hp = 100L;

    @Column(name = "Attack", nullable = false)
    private Long attack = 10L;

    @Column(name = "Defense", nullable = false)
    private Long defense = 5L;

    @Column(name = "CritRate", nullable = false)
    private Double critRate = 0D;

    @Column(name = "CritDamage", nullable = false)
    private Double critDamage = 150D;

    @Column(name = "Speed", nullable = false)
    private Double speed = 1D;

    @Column(name = "LifeSteal", nullable = false)
    private Double lifeSteal = 0D;

    @Column(name = "CultivationSpeed", nullable = false)
    private Double cultivationSpeed = 1D;

    @Column(name = "CultivationPoint", nullable = false)
    private Long cultivationPoint = 0L;

    @Column(name = "Reputation", nullable = false)
    private Long reputation = 0L;

    @Column(name = "SpiritStone", nullable = false)
    private Long spiritStone = 0L;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}