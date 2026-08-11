package com.vastworld.vwbe.entites;

import com.vastworld.vwbe.enums.quests.PlayerQuestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DateTimeException;
import java.time.LocalDateTime;

@Entity
@Table(name = "PlayerQuests")
@Getter
@Setter
public class PlayerQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PlayerId", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QuestId", nullable = false)
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private PlayerQuestStatus status;

    @Column(name = "StartedAt", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "CompletedAt")
    private LocalDateTime completedAt;

    @Column(name = "ClaimedAt")
    private LocalDateTime claimedAt;

    // bumps each time claimed, for repeatable
    @Column(name = "CompletionCount", nullable = false)
    private Integer completionCount = 0;
}