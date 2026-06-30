package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "HexArenas")
@Getter
@Setter
public class HexArena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "Cols", nullable = false)
    private Integer cols = 9;

    @Column(name = "Rows", nullable = false)
    private Integer rows = 7;

    @Column(name = "BaseMovementAPCost", nullable = false)
    private Integer baseMovementAPCost = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BackgroundAssetId")
    private GameAsset backgroundAsset;
}
