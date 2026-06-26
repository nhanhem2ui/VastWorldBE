package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "PlayerSkills")
@Getter
@Setter
public class PlayerSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "PlayerId", nullable = false)
    private UUID playerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SkillId", nullable = false, foreignKey = @ForeignKey(name = "FK_PS_Skill"))
    private Skill skill;

    @Column(name = "UnlockedAt", nullable = false, updatable = false)
    private LocalDateTime unlockedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.unlockedAt == null) this.unlockedAt = LocalDateTime.now();
    }
}