package com.jefflife.mudmk2.gameplay.application.service.command.look;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;

public final class ItemDisplayLabels {

    private ItemDisplayLabels() {}

    public static String of(ItemType type) {
        return switch (type) {
            case FOOD -> "음식";
            case WEAPON -> "무기";
            case EQUIPMENT -> "장비";
            case ACCESSORY -> "악세서리";
            case MARTIAL_ARTS_BOOK -> "무공서";
            case MISSION -> "임무 아이템";
        };
    }

    public static String of(WeaponType type) {
        return switch (type) {
            case SWORD -> "검";
            case BLADE -> "도";
            case FIST -> "권";
            case ARCHERY -> "활";
            case ESOTERIC -> "암기";
            case LONG_WEAPON -> "장병기";
        };
    }

    public static String of(EquipmentSlot slot) {
        return switch (slot) {
            case HELMET -> "투구";
            case UPPER_ARMOR -> "상의";
            case LOWER_ARMOR -> "하의";
            case GLOVES -> "장갑";
            case BOOTS -> "신발";
            case BELT -> "허리띠";
            case WEAPON -> "무기";
            case NECKLACE -> "목걸이";
            case RING_LEFT -> "왼손 반지";
            case RING_RIGHT -> "오른손 반지";
        };
    }

    public static String of(AccessoryType type) {
        return switch (type) {
            case NECKLACE -> "목걸이";
            case RING -> "반지";
        };
    }

    public static String of(MissionItemType type) {
        return switch (type) {
            case KEY -> "열쇠";
            case QUEST_COMPLETION -> "퀘스트 완료품";
        };
    }

    public static String of(MentalMethodKind kind) {
        return switch (kind) {
            case INNER_POWER       -> "내공";
            case LIGHT_STEP        -> "경공";
            case SPECIAL_TECHNIQUE -> "특기";
        };
    }

    public static String of(StatType type) {
        return switch (type) {
            case VIGOR -> "활력";
            case PHYSIQUE -> "체력";
            case AGILITY -> "민첩";
            case INTELLECT -> "지력";
            case WILL -> "의지";
            case MERIDIAN -> "기맥";
            case INNER_POWER -> "내공";
            case SPECIAL_TECHNIQUE -> "특기";
            case LIGHT_STEP -> "경공";
            case FISTS_AND_PALMS -> "권장";
            case SWORD_METHOD -> "검술";
            case BLADE_METHOD -> "도법";
            case LONG_WEAPON -> "장병기술";
            case ESOTERIC_WEAPON -> "암기술";
            case ARCHERY -> "궁술";
        };
    }
}
