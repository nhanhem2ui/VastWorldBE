package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Regions")
@Getter
@Setter
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RegionID")
    private Integer regionId;

    @Column(name = "RegionName")
    private String regionName;

    @Column(name = "Width")
    private Integer width;

    @Column(name = "Height")
    private Integer height;

    @Column(name = "BackgroundImage")
    private String backgroundImage;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
}