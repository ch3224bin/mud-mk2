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
@Table(name = "equipment_template")
@DiscriminatorValue("EQUIPMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquipmentTemplate extends ItemTemplate {

    @Enumerated(EnumType.STRING)
    private EquipmentSlot equipmentSlot;

    @ElementCollection
    @CollectionTable(name = "equipment_stat_modifiers", joinColumns = @JoinColumn(name = "equipment_template_id"))
    private List<StatModifier> statModifiers = new ArrayList<>();

    @Builder
    public EquipmentTemplate(String name, String description, int weight, boolean stackable,
                             EquipmentSlot equipmentSlot, List<StatModifier> statModifiers) {
        super(name, description, weight, ItemType.EQUIPMENT, stackable);
        this.equipmentSlot = equipmentSlot;
        if (statModifiers != null) {
            this.statModifiers.addAll(statModifiers);
        }
    }

    public void update(ItemTemplateRequest request) {
        updateCommon(request.name(), request.description(), request.weight(), request.stackable());
        this.equipmentSlot = request.equipmentSlot();
        this.statModifiers.clear();
        if (request.statModifiers() != null) {
            request.statModifiers().forEach(sm -> this.statModifiers.add(sm.toDomain()));
        }
    }

    @Override
    public void initializeAssociatedEntities() {
        this.statModifiers.size();
    }
}
