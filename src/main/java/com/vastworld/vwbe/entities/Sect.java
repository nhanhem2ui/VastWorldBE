package com.vastworld.vwbe.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Sects")
public class Sect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RegionId")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MapID")
    private Map map;

    @Column(name = "X", nullable = false)
    private Integer x;

    @Column(name = "Y", nullable = false)
    private Integer y;

    @Column(name = "ImageUrl")
    private String imageUrl;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "sect", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SectRank> ranks = new ArrayList<>();
}
