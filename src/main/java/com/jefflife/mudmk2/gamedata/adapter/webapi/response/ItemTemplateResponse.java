package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;

import java.util.List;

public record ItemTemplateResponse(
    Long id,
    ItemType itemType,
    String name,
    String description,
    int weight,
    boolean stackable,
    Integer hpRecovery,
    Integer mpRecovery,
    Integer apRecovery,
    WeaponType weaponType,
    EquipmentSlot equipmentSlot,
    AccessoryType accessoryType,
    List<StatModifierResponse> statModifiers,
    String skillRef,
    MissionItemType missionItemType,
    String targetRef
) {
    public static ItemTemplateResponse from(ItemTemplate template) {
        if (template instanceof WeaponTemplate w) {
            return new ItemTemplateResponse(
                w.getId(), ItemType.WEAPON, w.getName(), w.getDescription(), w.getWeight(), w.isStackable(),
                null, null, null, w.getWeaponType(), null, null,
                w.getStatModifiers().stream().map(StatModifierResponse::from).toList(),
                null, null, null
            );
        } else if (template instanceof EquipmentTemplate e) {
            return new ItemTemplateResponse(
                e.getId(), ItemType.EQUIPMENT, e.getName(), e.getDescription(), e.getWeight(), e.isStackable(),
                null, null, null, null, e.getEquipmentSlot(), null,
                e.getStatModifiers().stream().map(StatModifierResponse::from).toList(),
                null, null, null
            );
        } else if (template instanceof AccessoryTemplate a) {
            return new ItemTemplateResponse(
                a.getId(), ItemType.ACCESSORY, a.getName(), a.getDescription(), a.getWeight(), a.isStackable(),
                null, null, null, null, null, a.getAccessoryType(),
                a.getStatModifiers().stream().map(StatModifierResponse::from).toList(),
                null, null, null
            );
        } else if (template instanceof FoodTemplate f) {
            return new ItemTemplateResponse(
                f.getId(), ItemType.FOOD, f.getName(), f.getDescription(), f.getWeight(), f.isStackable(),
                f.getHpRecovery(), f.getMpRecovery(), f.getApRecovery(),
                null, null, null, List.of(), null, null, null
            );
        } else if (template instanceof MartialArtsBookTemplate m) {
            return new ItemTemplateResponse(
                m.getId(), ItemType.MARTIAL_ARTS_BOOK, m.getName(), m.getDescription(), m.getWeight(), m.isStackable(),
                null, null, null, null, null, null, List.of(), m.getSkillRef(), null, null
            );
        } else if (template instanceof MissionItemTemplate mi) {
            return new ItemTemplateResponse(
                mi.getId(), ItemType.MISSION, mi.getName(), mi.getDescription(), mi.getWeight(), mi.isStackable(),
                null, null, null, null, null, null, List.of(), null, mi.getMissionItemType(), mi.getTargetRef()
            );
        }
        throw new IllegalArgumentException("Unknown ItemTemplate type: " + template.getClass());
    }
}
