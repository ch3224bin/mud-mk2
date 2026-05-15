package com.jefflife.mudmk2.gamedata.application.domain.model.item;

public enum EquipmentSlot {
    HELMET("머리"),
    UPPER_ARMOR("상의"),
    LOWER_ARMOR("하의"),
    GLOVES("장갑"),
    BOOTS("신발"),
    BELT("허리띠"),
    WEAPON("무기"),
    NECKLACE("목걸이"),
    RING_LEFT("왼손 반지"),
    RING_RIGHT("오른손 반지");

    private final String displayName;

    EquipmentSlot(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
