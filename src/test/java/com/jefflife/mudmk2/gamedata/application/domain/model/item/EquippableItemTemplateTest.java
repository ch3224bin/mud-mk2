package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class EquippableItemTemplateTest {

    @Test
    void weaponTemplate_isEquippable() {
        WeaponTemplate w = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 2)))
                .build();
        assertThat(w).isInstanceOf(EquippableItemTemplate.class);
        assertThat(((EquippableItemTemplate) w).getStatModifiers())
                .containsExactly(new StatModifier(StatType.VIGOR, 2));
    }

    @Test
    void equipmentTemplate_isEquippable() {
        EquipmentTemplate e = EquipmentTemplate.builder()
                .name("야구모자").description("d").weight(1).stackable(false)
                .equipmentSlot(EquipmentSlot.HELMET)
                .statModifiers(List.of(new StatModifier(StatType.AGILITY, 1)))
                .build();
        assertThat(e).isInstanceOf(EquippableItemTemplate.class);
        assertThat(((EquippableItemTemplate) e).getStatModifiers())
                .containsExactly(new StatModifier(StatType.AGILITY, 1));
    }

    @Test
    void accessoryTemplate_isEquippable() {
        AccessoryTemplate a = AccessoryTemplate.builder()
                .name("금반지").description("d").weight(1).stackable(false)
                .accessoryType(AccessoryType.RING)
                .statModifiers(List.of(new StatModifier(StatType.INNER_POWER, 3)))
                .build();
        assertThat(a).isInstanceOf(EquippableItemTemplate.class);
        assertThat(((EquippableItemTemplate) a).getStatModifiers())
                .containsExactly(new StatModifier(StatType.INNER_POWER, 3));
    }

    @Test
    void foodTemplate_isNotEquippable() {
        FoodTemplate f = FoodTemplate.builder()
                .name("만두").description("d").weight(1).stackable(true)
                .hpRecovery(0).mpRecovery(0).apRecovery(50).build();
        assertThat(f).isNotInstanceOf(EquippableItemTemplate.class);
    }
}
