package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_template")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "item_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ItemTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    private int weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", insertable = false, updatable = false)
    private ItemType itemType;

    private boolean stackable;

    protected ItemTemplate(String name, String description, int weight, ItemType itemType, boolean stackable) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.itemType = itemType;
        this.stackable = stackable;
    }

    protected void updateCommon(String name, String description, int weight, boolean stackable) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.stackable = stackable;
    }
}
