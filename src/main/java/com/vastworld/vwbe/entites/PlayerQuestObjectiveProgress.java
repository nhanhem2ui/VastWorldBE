package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "PlayerQuestObjectiveProgress")
@Getter
@Setter
public class PlayerQuestObjectiveProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PlayerQuestId", nullable = false)
    private PlayerQuest playerQuest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QuestObjectiveId", nullable = false)
    private QuestObjective questObjective;

    @Column(name = "CurrentCount", nullable = false)
    private Integer currentCount = 0;

    @Column(name = "IsCompleted", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "CompletedAt")
    private LocalDateTime completedAt;
}