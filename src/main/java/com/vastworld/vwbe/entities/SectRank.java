package com.vastworld.vwbe.entities;

import com.vastworld.vwbe.enums.MasterRank;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "SectRanks")
public class SectRank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SectId", nullable = false)
    private Sect sect;

    @Enumerated(EnumType.STRING)
    @Column(name = "MasterRankCode", nullable = false, length = 50)
    private MasterRank masterRankCode;

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "RankOrder", nullable = false)
    private Integer rankOrder;
}