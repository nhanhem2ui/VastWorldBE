package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "RegionDecorations")
@Getter
@Setter
public class RegionDecoration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RegionDecorationID")
    private Integer regionDecorationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RegionID")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MapID")
    private Map map;

    @Column(name = "Label")
    private String Label;

    @Column(name = "X", nullable = false)
    private Integer x;

    @Column(name = "Y", nullable = false)
    private Integer y;

    @Column(name = "Width", nullable = false)
    private Integer width;

    @Column(name = "Height", nullable = false)
    private Integer height;

    @Column(name = "TopHeight", nullable = false)
    private Integer topHeight;

    @Column(name = "RightWidth", nullable = false)
    private Integer rightWidth;

    @Column(name = "BottomHeight", nullable = false)
    private Integer bottomHeight;

    @Column(name = "LeftWidth", nullable = false)
    private Integer leftWidth;

    @Column(name = "BackgroundTexture", nullable = false)
    private String backgroundTexture;

    @Column(name = "TextColor", nullable = false, length = 7)
    private String textColor;
}