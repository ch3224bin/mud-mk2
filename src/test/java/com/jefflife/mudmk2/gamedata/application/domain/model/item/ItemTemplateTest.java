package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ItemTemplateTest {

    @Test
    void foodTemplate_storesRecoveryValues() {
        FoodTemplate food = FoodTemplate.builder()
                .name("만두").description("맛있는 만두").weight(1).stackable(true)
                .hpRecovery(0).mpRecovery(0).apRecovery(50)
                .build();
        assertThat(food.getName()).isEqualTo("만두");
        assertThat(food.getApRecovery()).isEqualTo(50);
        assertThat(food.isStackable()).isTrue();
        assertThat(food.getItemType()).isEqualTo(ItemType.FOOD);
    }

    @Test
    void weaponTemplate_storesWeaponTypeAndStatModifiers() {
        StatModifier modifier = new StatModifier(StatType.SWORD_METHOD, 15);
        WeaponTemplate weapon = WeaponTemplate.builder()
                .name("철검").description("평범한 철검").weight(3).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(modifier))
                .build();
        assertThat(weapon.getWeaponType()).isEqualTo(WeaponType.SWORD);
        assertThat(weapon.getStatModifiers()).hasSize(1);
        assertThat(weapon.getStatModifiers().get(0).getValue()).isEqualTo(15);
        assertThat(weapon.getItemType()).isEqualTo(ItemType.WEAPON);
    }

    @Test
    void equipmentTemplate_storesSlotAndStatModifiers() {
        EquipmentTemplate equipment = EquipmentTemplate.builder()
                .name("철투구").description("평범한 투구").weight(2).stackable(false)
                .equipmentSlot(EquipmentSlot.HELMET)
                .statModifiers(List.of(new StatModifier(StatType.PHYSIQUE, 5)))
                .build();
        assertThat(equipment.getEquipmentSlot()).isEqualTo(EquipmentSlot.HELMET);
        assertThat(equipment.getItemType()).isEqualTo(ItemType.EQUIPMENT);
    }

    @Test
    void missionItemTemplate_storesTypeAndTargetRef() {
        MissionItemTemplate key = MissionItemTemplate.builder()
                .name("금빛 열쇠").description("금고를 여는 열쇠").weight(0).stackable(false)
                .missionItemType(MissionItemType.KEY)
                .targetRef("door-123")
                .build();
        assertThat(key.getMissionItemType()).isEqualTo(MissionItemType.KEY);
        assertThat(key.getTargetRef()).isEqualTo("door-123");
        assertThat(key.getItemType()).isEqualTo(ItemType.MISSION);
    }
}
