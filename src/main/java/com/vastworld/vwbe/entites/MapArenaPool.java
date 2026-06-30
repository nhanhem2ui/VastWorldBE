package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MapArenaPools")
@Getter
@Setter
public class MapArenaPool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MapArenaPoolId")
    private Integer id;

    @Column(name = "Weight", nullable = false)
    private Integer weight = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MapId", foreignKey = @ForeignKey(name = "FK_MAP_Pool"))
    private Map gameMap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HexArenaId", foreignKey = @ForeignKey(name = "FK_ARENA_Pool"))
    private HexArena hexArena;
}