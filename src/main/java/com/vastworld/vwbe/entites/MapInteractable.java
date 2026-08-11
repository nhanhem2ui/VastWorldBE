package com.vastworld.vwbe.entites;

import com.vastworld.vwbe.enums.MapInteractableTypes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MapInteractables")
@Getter
@Setter
public class MapInteractable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InteractableID")
    private Integer interactableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MapID", nullable = false)
    private Map map;

    @Column(name = "X", nullable = false)
    private Integer x;

    @Column(name = "Y", nullable = false)
    private Integer y;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false, length = 50)
    private MapInteractableTypes type;

    @Column(name = "Data", columnDefinition = "nvarchar(max)")
    private String data;
}
