package com.vastworld.vwbe.repositories;

import com.vastworld.vwbe.entites.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemTypeRepository extends JpaRepository<ItemType, Integer> {
}