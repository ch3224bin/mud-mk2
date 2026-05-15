package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EquippedItemsTest {

    private EquippedItems equipped;
    private WeaponTemplate swordTemplate;
    private EquipmentTemplate helmetTemplate;
    private AccessoryTemplate ringTemplate;

    @BeforeEach
    void setUp() {
        equipped = EquippedItems.create();
        swordTemplate = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 2)))
                .build();
        helmetTemplate = EquipmentTemplate.builder()
                .name("야구모자").description("d").weight(1).stackable(false)
                .equipmentSlot(EquipmentSlot.HELMET)
                .statModifiers(List.of(new StatModifier(StatType.AGILITY, 1)))
                .build();
        ringTemplate = AccessoryTemplate.builder()
                .name("금반지").description("d").weight(1).stackable(false)
                .accessoryType(AccessoryType.RING)
                .statModifiers(List.of(new StatModifier(StatType.INNER_POWER, 3)))
                .build();
    }

    @Test
    void equip_emptySlot_storesInstance_returnsEmpty() {
        ItemInstance sword = new ItemInstance(swordTemplate, 1);
        Optional<ItemInstance> prev = equipped.equip(EquipmentSlot.WEAPON, sword);
        assertThat(prev).isEmpty();
        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).contains(sword);
    }

    @Test
    void equip_occupiedSlot_swapsAndReturnsPrevious() {
        ItemInstance sword1 = new ItemInstance(swordTemplate, 1);
        ItemInstance sword2 = new ItemInstance(swordTemplate, 1);
        equipped.equip(EquipmentSlot.WEAPON, sword1);
        Optional<ItemInstance> prev = equipped.equip(EquipmentSlot.WEAPON, sword2);
        assertThat(prev).contains(sword1);
        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).contains(sword2);
    }

    @Test
    void unequip_occupiedSlot_returnsInstance_andClears() {
        ItemInstance helmet = new ItemInstance(helmetTemplate, 1);
        equipped.equip(EquipmentSlot.HELMET, helmet);
        Optional<ItemInstance> removed = equipped.unequip(EquipmentSlot.HELMET);
        assertThat(removed).contains(helmet);
        assertThat(equipped.getSlot(EquipmentSlot.HELMET)).isEmpty();
    }

    @Test
    void unequip_emptySlot_returnsEmpty() {
        assertThat(equipped.unequip(EquipmentSlot.HELMET)).isEmpty();
    }

    @Test
    void findByItemName_returnsSlotAndInstance() {
        ItemInstance helmet = new ItemInstance(helmetTemplate, 1);
        ItemInstance sword = new ItemInstance(swordTemplate, 1);
        equipped.equip(EquipmentSlot.HELMET, helmet);
        equipped.equip(EquipmentSlot.WEAPON, sword);

        Optional<Map.Entry<EquipmentSlot, ItemInstance>> found = equipped.findByItemName("철검");
        assertThat(found).isPresent();
        assertThat(found.get().getKey()).isEqualTo(EquipmentSlot.WEAPON);
        assertThat(found.get().getValue()).isSameAs(sword);
    }

    @Test
    void findByItemName_notEquipped_returnsEmpty() {
        assertThat(equipped.findByItemName("철검")).isEmpty();
    }

    @Test
    void sumStatModifiers_aggregatesAcrossSlots() {
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(swordTemplate, 1));   // +2 VIGOR
        equipped.equip(EquipmentSlot.HELMET, new ItemInstance(helmetTemplate, 1));  // +1 AGILITY
        equipped.equip(EquipmentSlot.RING_LEFT, new ItemInstance(ringTemplate, 1)); // +3 INNER_POWER

        Map<StatType, Integer> sums = equipped.sumStatModifiers();
        assertThat(sums.get(StatType.VIGOR)).isEqualTo(2);
        assertThat(sums.get(StatType.AGILITY)).isEqualTo(1);
        assertThat(sums.get(StatType.INNER_POWER)).isEqualTo(3);
    }

    @Test
    void sumStatModifiers_stacksSameStatTypeAcrossSlots() {
        WeaponTemplate doubleVigorSword = WeaponTemplate.builder()
                .name("쌍철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 3)))
                .build();
        EquipmentTemplate vigorHelmet = EquipmentTemplate.builder()
                .name("힘투구").description("d").weight(1).stackable(false)
                .equipmentSlot(EquipmentSlot.HELMET)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 5)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(doubleVigorSword, 1));
        equipped.equip(EquipmentSlot.HELMET, new ItemInstance(vigorHelmet, 1));

        assertThat(equipped.sumStatModifiers().get(StatType.VIGOR)).isEqualTo(8);
    }

    @Test
    void sumStatModifiers_emptyEquipped_returnsEmptyMap() {
        assertThat(equipped.sumStatModifiers()).isEmpty();
    }

    @Test
    void initializeAssociatedEntities_callsOnEachInstance() {
        ItemInstance sword = org.mockito.Mockito.spy(new ItemInstance(swordTemplate, 1));
        ItemInstance helmet = org.mockito.Mockito.spy(new ItemInstance(helmetTemplate, 1));
        equipped.equip(EquipmentSlot.WEAPON, sword);
        equipped.equip(EquipmentSlot.HELMET, helmet);

        equipped.initializeAssociatedEntities();

        org.mockito.Mockito.verify(sword).initializeAssociatedEntities();
        org.mockito.Mockito.verify(helmet).initializeAssociatedEntities();
    }
}
