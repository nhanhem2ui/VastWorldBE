package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Skills")
@Getter
@Setter
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "EffectType", nullable = false, length = 20)
    private String effectType = "ATTACK";

    @Column(name = "Range", nullable = false)
    private Integer range = 1;

    @Column(name = "ActionPointCost", nullable = false)
    private Integer actionPointCost = 2;

    @Column(name = "PowerMultiplier", nullable = false)
    private Double powerMultiplier = 1.0;

    @Column(name = "RequiredRealmId")
    private Integer requiredRealmId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VfxAssetId", foreignKey = @ForeignKey(name = "FK_Skills_Assets"))
    private GameAsset vfxAsset;
}