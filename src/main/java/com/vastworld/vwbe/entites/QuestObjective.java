package com.vastworld.vwbe.entites;

import com.vastworld.vwbe.enums.quests.QuestObjectiveTypes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "QuestObjectives")
@Getter
@Setter
public class QuestObjective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QuestId", nullable = false)
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(name = "ObjectiveType", nullable = false, length = 100)
    private QuestObjectiveTypes objectiveType;

    @Column(name = "Description")
    private String description;

    @Column(name = "RequiredCount")
    private Integer requiredCount;

    @Column(name = "MonsterId")
    private Integer monsterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TargetItemId")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TargetRealmId")
    private CultivationRealm targetRealm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TargetRealmStage")
    private RealmStage targetRealmStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TargetMapId")
    private Map targetMap;

    @Column(name = "TargetX")
    private Integer targetX;

    @Column(name = "TargetY")
    private Integer targetY;
}
