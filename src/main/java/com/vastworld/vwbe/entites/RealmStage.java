package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "RealmStages")
public class RealmStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "StageLevel", nullable = false)
    private Integer stageLevel;

    @Column(name = "StageName", nullable = false, length = 50)
    private String stageName;
}