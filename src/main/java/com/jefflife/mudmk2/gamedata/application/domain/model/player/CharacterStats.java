package com.jefflife.mudmk2.gamedata.application.domain.model.player;

public record CharacterStats(
        // 현재 자원
        int hp,
        int mp,
        int ap,
        // 속성 (屬性) — 6개
        int vigor,            // 근력(筋力)
        int physique,         // 체질(體質)
        int agility,          // 민첩(敏捷)
        int intellect,        // 지력(智力)
        int will,             // 의지(意志)
        int meridian,         // 경맥(經脈)
        // 무예 (武藝) — 9개
        int innerPower,       // 내공(內功)
        int specialTechnique, // 절기(絶技)
        int lightStep,        // 경공(輕功)
        int fistsAndPalms,    // 권장(拳掌)
        int swordMethod,      // 검법(劍法)
        int bladeMethod,      // 도법(刀法)
        int longWeapon,       // 장병(長兵)
        int esotericWeapon,   // 기문(奇門)
        int archery           // 사술(射術)
) {
    // 파생 스탯 공식 상수 — 밸런싱 시 이 값만 수정
    public static final int HP_PER_PHYSIQUE = 10;
    public static final int HP_PER_SPECIAL_TECHNIQUE = 3;
    public static final int MP_PER_MERIDIAN = 5;
    public static final int MP_PER_INNER_POWER = 3;
    public static final int AP_PER_AGILITY = 8;

    public int maxHp() {
        return physique * HP_PER_PHYSIQUE + specialTechnique * HP_PER_SPECIAL_TECHNIQUE;
    }

    public int maxMp() {
        return meridian * MP_PER_MERIDIAN + innerPower * MP_PER_INNER_POWER;
    }

    public int maxAp() {
        return agility * AP_PER_AGILITY;
    }
}
