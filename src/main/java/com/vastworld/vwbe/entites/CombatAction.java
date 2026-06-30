package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CombatActions")
@Getter
@Setter
public class CombatAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SessionId", nullable = false)
    private CombatSession combatSession;

    @Column(name = "PlayerId", nullable = false)
    private UUID playerId;

    @Column(name = "RoundNumber", nullable = false)
    private Integer roundNumber;

    @Column(name = "ActionType", nullable = false, length = 20)
    private String actionType; // MOVE | SKILL | ITEM | FLEE | END_TURN

    @Column(name = "FromCol")
    private Integer fromCol;

    @Column(name = "FromRow")
    private Integer fromRow;

    @Column(name = "ToCol")
    private Integer toCol;

    @Column(name = "ToRow")
    private Integer toRow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SkillId", foreignKey = @ForeignKey(name = "FK_CA_Skill"))
    private Skill skill;

    @Column(name = "ItemInventoryId")
    private Long itemInventoryId;

    @Column(name = "TargetPlayerId")
    private UUID targetPlayerId;

    @Column(name = "DamageDealt")
    private Long damageDealt;

    @Column(name = "HealingDone")
    private Long healingDone;

    @Column(name = "APConsumed", nullable = false)
    private Integer apConsumed = 0;

    @Column(name = "IsCritical", nullable = false)
    private Boolean isCritical = false;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}