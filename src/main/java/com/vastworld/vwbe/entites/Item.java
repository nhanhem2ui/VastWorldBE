package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "Items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id")
    private UUID id;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ItemTypeId", nullable = false)
    private ItemType itemType;

    @Column(name = "MaxUsageCount")
    private Integer maxUsageCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RequiredRealmId")
    private CultivationRealm requiredRealm;

    @Column(name = "HP", nullable = false)
    private Long hp = 0L;

    @Column(name = "Attack", nullable = false)
    private Long attack = 0L;

    @Column(name = "Defense", nullable = false)
    private Long defense = 0L;

    @Column(name = "CritRate", nullable = false)
    private Double critRate = 0D;

    @Column(name = "CritDamage", nullable = false)
    private Double critDamage = 0D;

    @Column(name = "Speed", nullable = false)
    private Double speed = 0D;

    @Column(name = "LifeSteal", nullable = false)
    private Double lifeSteal = 0D;

    @Column(name = "CultivationSpeed", nullable = false)
    private Double cultivationSpeed = 0D;
}