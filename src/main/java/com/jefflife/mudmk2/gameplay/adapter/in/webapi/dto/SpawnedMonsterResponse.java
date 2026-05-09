package com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;

import java.util.UUID;

public record SpawnedMonsterResponse(
    UUID id,
    String name,
    int vigor,
    int physique,
    int agility,
    int maxHp,
    int weaponBaseDamage,
    int equipmentArmor,
    int equipmentArmorPct
) {
    public static SpawnedMonsterResponse from(Monster monster) {
        var stats = monster.getStats();
        return new SpawnedMonsterResponse(
            monster.getId(), monster.getName(),
            stats.vigor(), stats.physique(), stats.agility(),
            stats.maxHp(),
            monster.getWeaponBaseDamage(),
            monster.getEquipmentArmor(),
            monster.getEquipmentArmorPct()
        );
    }
}
