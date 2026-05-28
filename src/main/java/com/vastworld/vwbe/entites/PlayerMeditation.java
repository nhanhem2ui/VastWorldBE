package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "PlayerMeditations")
public class PlayerMeditation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PlayerId", nullable = false)
    private Player player;

    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime = LocalDateTime.now();

    @Column(name = "DurationMinutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "EndTime", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "CultivationPerMinute", nullable = false)
    private Long cultivationPerMinute;

    @Column(name = "TotalCultivationReward", nullable = false)
    private Long totalCultivationReward = 0L;

    @Column(name = "IsCompleted", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "IsClaimed", nullable = false)
    private Boolean isClaimed = false;
}