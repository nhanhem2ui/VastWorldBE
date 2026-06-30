package com.vastworld.vwbe.entites;

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

    @Column(name = "CreatedAt", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
