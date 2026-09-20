package com.vastworld.vwbe.entites;

import com.vastworld.vwbe.enums.quests.QuestRewardTypes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "QuestRewards")
@Getter
@Setter
public class QuestReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QuestId", nullable = false)
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(name = "RewardType", nullable = false, length = 100)
    private QuestRewardTypes rewardType;

    @Column(name = "Amount")
    private Long amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ItemId")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SkillId")
    private Skill skill;
}
