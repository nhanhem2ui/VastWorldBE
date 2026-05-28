package com.vastworld.vwbe.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "CultivationRealms")
public class CultivationRealm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "RealmOrder", nullable = false)
    private Integer realmOrder;

    @Column(name = "IsImmortal", nullable = false)
    private Boolean isImmortal = false;
}