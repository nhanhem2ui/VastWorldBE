package com.vastworld.vwbe.entites;

import com.vastworld.vwbe.common.enums.AssetType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "GameAssets")
@Getter
@Setter
public class GameAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AssetID")
    private Integer assetId;

    @Column(name = "AssetName", nullable = false, length = 100)
    private String assetName;

    @Column(name = "IsAnimated")
    private Boolean isAnimated = false;

    @Column(name = "AssetUrl", nullable = false, length = 500)
    private String assetUrl;

    @Column(name = "FrameConfig", length = 255)
    private String frameConfig;

    @Column(name = "OffsetX", nullable = false)
    private Double offsetX = 0D;

    @Column(name = "OffsetY", nullable = false)
    private Double offsetY = 0D;

    @Column(name = "Width")
    private Double width = 0D;

    @Column(name = "Height")
    private Double height = 0D;

    @Enumerated(EnumType.STRING)
    @Column(name = "AssetType", nullable = false, length = 20)
    private AssetType assetType = AssetType.IMAGE;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}