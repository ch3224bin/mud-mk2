package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accessory_template")
@DiscriminatorValue("ACCESSORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccessoryTemplate extends ItemTemplate {

    @Enumerated(EnumType.STRING)
    private AccessoryType accessoryType;

    @ElementCollection
    @CollectionTable(name = "accessory_stat_modifiers", joinColumns = @JoinColumn(name = "accessory_template_id"))
    private List<StatModifier> statModifiers = new ArrayList<>();

    @Builder
    public AccessoryTemplate(String name, String description, int weight, boolean stackable,
                             AccessoryType accessoryType, List<StatModifier> statModifiers) {
        super(name, description, weight, ItemType.ACCESSORY, stackable);
        this.accessoryType = accessoryType;
        if (statModifiers != null) {
            this.statModifiers.addAll(statModifiers);
        }
    }
}
