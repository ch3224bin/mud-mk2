package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "weapon_template")
@DiscriminatorValue("WEAPON")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeaponTemplate extends ItemTemplate {

    @Enumerated(EnumType.STRING)
    private WeaponType weaponType;

    @ElementCollection
    @CollectionTable(name = "weapon_stat_modifiers", joinColumns = @JoinColumn(name = "weapon_template_id"))
    private List<StatModifier> statModifiers = new ArrayList<>();

    @Builder
    public WeaponTemplate(String name, String description, int weight, boolean stackable,
                          WeaponType weaponType, List<StatModifier> statModifiers) {
        super(name, description, weight, ItemType.WEAPON, stackable);
        this.weaponType = weaponType;
        if (statModifiers != null) {
            this.statModifiers.addAll(statModifiers);
        }
    }
}
