package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EquipmentSlotTest {

    @Test
    void enum_containsAllTenSlots() {
        assertThat(EquipmentSlot.values()).containsExactlyInAnyOrder(
                EquipmentSlot.HELMET, EquipmentSlot.UPPER_ARMOR, EquipmentSlot.LOWER_ARMOR,
                EquipmentSlot.GLOVES, EquipmentSlot.BOOTS, EquipmentSlot.BELT,
                EquipmentSlot.WEAPON, EquipmentSlot.NECKLACE,
                EquipmentSlot.RING_LEFT, EquipmentSlot.RING_RIGHT
        );
    }

    @Test
    void displayName_returnsKoreanLabel() {
        assertThat(EquipmentSlot.HELMET.displayName()).isEqualTo("머리");
        assertThat(EquipmentSlot.UPPER_ARMOR.displayName()).isEqualTo("상의");
        assertThat(EquipmentSlot.LOWER_ARMOR.displayName()).isEqualTo("하의");
        assertThat(EquipmentSlot.GLOVES.displayName()).isEqualTo("장갑");
        assertThat(EquipmentSlot.BOOTS.displayName()).isEqualTo("신발");
        assertThat(EquipmentSlot.BELT.displayName()).isEqualTo("허리띠");
        assertThat(EquipmentSlot.WEAPON.displayName()).isEqualTo("무기");
        assertThat(EquipmentSlot.NECKLACE.displayName()).isEqualTo("목걸이");
        assertThat(EquipmentSlot.RING_LEFT.displayName()).isEqualTo("왼손 반지");
        assertThat(EquipmentSlot.RING_RIGHT.displayName()).isEqualTo("오른손 반지");
    }
}
