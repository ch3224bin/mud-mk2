package com.jefflife.mudmk2.gamedata.application.domain.model.item;

public final class WeaponTypeMapping {

    private WeaponTypeMapping() {}

    public static StatType weaponSkillFor(WeaponType type) {
        return switch (type) {
            case SWORD -> StatType.SWORD_METHOD;
            case BLADE -> StatType.BLADE_METHOD;
            case FIST -> StatType.FISTS_AND_PALMS;
            case ARCHERY -> StatType.ARCHERY;
            case ESOTERIC -> StatType.ESOTERIC_WEAPON;
            case LONG_WEAPON -> StatType.LONG_WEAPON;
        };
    }

    public static String attackVerb(WeaponType type) {
        return switch (type) {
            case SWORD -> "베었다";
            case BLADE -> "내려쳤다";
            case FIST -> "내질렀다";
            case ARCHERY -> "쐈다";
            case LONG_WEAPON, ESOTERIC -> "휘둘렀다";
        };
    }
}
