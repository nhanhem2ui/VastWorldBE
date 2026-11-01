package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entities.QuestReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestRewardRepository extends JpaRepository<QuestReward, Integer> {
    List<QuestReward> findByQuest_Id(Integer questId);
}
