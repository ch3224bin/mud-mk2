package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;
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
public class WeaponTemplate extends ItemTemplate implements EquippableItemTemplate {

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

    public void update(ItemTemplateRequest request) {
        updateCommon(request.name(), request.description(), request.weight(), request.stackable());
        this.weaponType = request.weaponType();
        this.statModifiers.clear();
        if (request.statModifiers() != null) {
            request.statModifiers().forEach(sm -> this.statModifiers.add(sm.toDomain()));
        }
    }

    @Override
    public void initializeAssociatedEntities() {
        this.statModifiers.size();
    }

    @Override
    public boolean requiresImmediateDeletion() {
        return false;
    }
}
