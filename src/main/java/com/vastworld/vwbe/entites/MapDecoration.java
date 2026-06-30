package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MapDecorations")
@Getter
@Setter
public class MapDecoration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DecorationID")
    private Integer decorationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MapID", nullable = false)
    private Map map;

    @Column(name = "X", nullable = false)
    private Integer x;

    @Column(name = "Y", nullable = false)
    private Integer y;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssetID", nullable = false)
    private GameAsset gameAsset;

    @Column(name = "SpriteFrame")
    private Integer spriteFrame;
}