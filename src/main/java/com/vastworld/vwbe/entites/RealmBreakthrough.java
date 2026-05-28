package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "RealmBreakthroughs")
public class RealmBreakthrough {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RealmId", nullable = false)
    private CultivationRealm realm;

    @Column(name = "StageLevel", nullable = false)
    private Integer stageLevel;

    @Column(name = "RequiredCultivationPoint", nullable = false)
    private Long requiredCultivationPoint;

    @Column(name = "RequiresTribulation", nullable = false)
    private Boolean requiresTribulation = false;

    @Column(name = "SuccessRate", nullable = false)
    private Double successRate = 100D;
}