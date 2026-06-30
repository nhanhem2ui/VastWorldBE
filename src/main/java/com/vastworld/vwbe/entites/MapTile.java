package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MapTiles")
@Getter
@Setter
public class MapTile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TileID")
    private Integer tileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MapID", nullable = false)
    private Map gameMap;

    @Column(name = "X", nullable = false)
    private Integer x;

    @Column(name = "Y", nullable = false)
    private Integer y;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssetID", nullable = false)
    private GameAsset asset;

    @Column(name = "SpriteFrame")
    private Integer spriteFrame;
}