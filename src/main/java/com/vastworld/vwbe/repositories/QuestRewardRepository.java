package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.QuestReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestRewardRepository extends JpaRepository<QuestReward, Integer> {
    Optional<List<QuestReward>> findByQuest_Id(Integer questId);
}
