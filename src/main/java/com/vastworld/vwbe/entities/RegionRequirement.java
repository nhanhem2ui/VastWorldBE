package com.vastworld.vwbe.entities;

import com.vastworld.vwbe.enums.RequirementType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "RegionRequirements")
@Getter
@Setter
public class RegionRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RegionID", nullable = false)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(name = "RequirementType", nullable = false, length = 50)
    private RequirementType requirementType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RequiredRealmId")
    private CultivationRealm requiredRealm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RequiredQuestId")
    private Quest requiredQuest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RequiredSpiritRootId")
    private SpiritRoot requiredSpiritRoot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RequiredItemId")
    private Item requiredItem;

    @Column(name = "RequiredReputation")
    private Long requiredReputation;

    //Display if this not satisfied
    @Column(name = "ErrorMessage", nullable = false)
    private String errorMessage;
}
