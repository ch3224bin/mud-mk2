package com.jefflife.mudmk2.gameplay.application.service.command.look;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemDisplayLabelsTest {

    @Test
    void of_itemType() {
        assertThat(ItemDisplayLabels.of(ItemType.FOOD)).isEqualTo("음식");
        assertThat(ItemDisplayLabels.of(ItemType.WEAPON)).isEqualTo("무기");
        assertThat(ItemDisplayLabels.of(ItemType.EQUIPMENT)).isEqualTo("장비");
        assertThat(ItemDisplayLabels.of(ItemType.ACCESSORY)).isEqualTo("악세서리");
        assertThat(ItemDisplayLabels.of(ItemType.MARTIAL_ARTS_BOOK)).isEqualTo("무공서");
        assertThat(ItemDisplayLabels.of(ItemType.MISSION)).isEqualTo("임무 아이템");
    }

    @Test
    void of_weaponType() {
        assertThat(ItemDisplayLabels.of(WeaponType.SWORD)).isEqualTo("검");
        assertThat(ItemDisplayLabels.of(WeaponType.BLADE)).isEqualTo("도");
        assertThat(ItemDisplayLabels.of(WeaponType.FIST)).isEqualTo("권");
        assertThat(ItemDisplayLabels.of(WeaponType.ARCHERY)).isEqualTo("활");
        assertThat(ItemDisplayLabels.of(WeaponType.ESOTERIC)).isEqualTo("암기");
        assertThat(ItemDisplayLabels.of(WeaponType.LONG_WEAPON)).isEqualTo("장병기");
    }

    @Test
    void of_equipmentSlot() {
        assertThat(ItemDisplayLabels.of(EquipmentSlot.HELMET)).isEqualTo("투구");
        assertThat(ItemDisplayLabels.of(EquipmentSlot.UPPER_ARMOR)).isEqualTo("상의");
        assertThat(ItemDisplayLabels.of(EquipmentSlot.LOWER_ARMOR)).isEqualTo("하의");
        assertThat(ItemDisplayLabels.of(EquipmentSlot.GLOVES)).isEqualTo("장갑");
        assertThat(ItemDisplayLabels.of(EquipmentSlot.BOOTS)).isEqualTo("신발");
        assertThat(ItemDisplayLabels.of(EquipmentSlot.BELT)).isEqualTo("허리띠");
    }

    @Test
    void of_accessoryType() {
        assertThat(ItemDisplayLabels.of(AccessoryType.NECKLACE)).isEqualTo("목걸이");
        assertThat(ItemDisplayLabels.of(AccessoryType.RING)).isEqualTo("반지");
    }

    @Test
    void of_missionItemType() {
        assertThat(ItemDisplayLabels.of(MissionItemType.KEY)).isEqualTo("열쇠");
        assertThat(ItemDisplayLabels.of(MissionItemType.QUEST_COMPLETION)).isEqualTo("퀘스트 완료품");
    }

    @Test
    void of_statType() {
        assertThat(ItemDisplayLabels.of(StatType.VIGOR)).isEqualTo("활력");
        assertThat(ItemDisplayLabels.of(StatType.SWORD_METHOD)).isEqualTo("검술");
        assertThat(ItemDisplayLabels.of(StatType.ARCHERY)).isEqualTo("궁술");
    }
}
