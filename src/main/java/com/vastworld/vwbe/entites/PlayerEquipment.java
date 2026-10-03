package com.vastworld.vwbe.entites;

import com.vastworld.vwbe.enums.EquipmentSlotTypes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "PlayerEquipment")
public class PlayerEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PlayerId", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ItemId")
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(name = "EquipmentSlotTypes", nullable = false, length = 50)
    private EquipmentSlotTypes equipmentSlotType;
}