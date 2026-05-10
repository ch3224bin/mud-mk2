package com.jefflife.mudmk2.gamedata.application.domain.model.item;

// 스탯 타입: 속성(屬性) 6개 + 무예(武藝) 9개. LONG_WEAPON은 장병(長兵) 무예 스탯으로 WeaponType.LONG_WEAPON(장병기)과 별개 개념.
public enum StatType {
    VIGOR, PHYSIQUE, AGILITY, INTELLECT, WILL, MERIDIAN,
    INNER_POWER, SPECIAL_TECHNIQUE, LIGHT_STEP,
    FISTS_AND_PALMS, SWORD_METHOD, BLADE_METHOD,
    LONG_WEAPON, ESOTERIC_WEAPON, ARCHERY
}
