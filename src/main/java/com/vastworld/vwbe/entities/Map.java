package com.vastworld.vwbe.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Maps")
public class Map {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MapID")
    private Integer mapId;

    @Column(name = "MapName", nullable = false, length = 100)
    private String mapName;

    @Column(name = "Width", nullable = false)
    private Integer width;

    @Column(name = "Height", nullable = false)
    private Integer height;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RegionId")
    private Region region;

    @Column(name = "CreatedAt", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
