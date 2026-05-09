package com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SimulationSpawnRequest {
    private String name;
    private int vigor = 10;
    private int physique = 10;
    private int agility = 10;
    private int intellect = 10;
    private int will = 10;
    private int meridian = 10;
    private int innerPower = 0;
    private int specialTechnique = 0;
    private int lightStep = 0;
    private int fistsAndPalms = 0;
    private int swordMethod = 0;
    private int bladeMethod = 0;
    private int longWeapon = 0;
    private int esotericWeapon = 0;
    private int archery = 0;
    private int weaponBaseDamage = 10;
    private int equipmentArmor = 0;
    private int equipmentArmorPct = 0;
}
