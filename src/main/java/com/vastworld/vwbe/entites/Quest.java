package com.vastworld.vwbe.entites;

import com.vastworld.vwbe.services.CultivationRealmService;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Table(name = "Quests")
@Entity
public class Quest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Column(name = "Name", unique = true, length = 100)
    private String name;

    @Column(name = "Description")
    @JdbcTypeCode(SqlTypes.LONGNVARCHAR)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RequiredRealmId", nullable = false)
    private CultivationRealm requiredRealm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PrerequisiteQuestId")
    private Quest prerequisiteQuest;

    @Column(name = "IsRepeatable", nullable = false)
    private Boolean isRepeatable = false;

    @Column(name = "CooldownMinutes")
    private Integer cooldownMinutes;

    @Column(name = "MaxCompletions")
    private Integer maxCompletions;
}
