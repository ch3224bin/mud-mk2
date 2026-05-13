package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;

import java.util.List;

public record ItemTemplateRequest(
    ItemType itemType,
    String name,
    String description,
    int weight,
    boolean stackable,
    // FOOD
    Integer hpRecovery,
    Integer mpRecovery,
    Integer apRecovery,
    // WEAPON
    WeaponType weaponType,
    // EQUIPMENT
    EquipmentSlot equipmentSlot,
    // ACCESSORY
    AccessoryType accessoryType,
    // WEAPON / EQUIPMENT / ACCESSORY
    List<StatModifierRequest> statModifiers,
    // MARTIAL_ARTS_BOOK
    String skillRef,
    // MISSION
    MissionItemType missionItemType,
    String targetRef
) {}
