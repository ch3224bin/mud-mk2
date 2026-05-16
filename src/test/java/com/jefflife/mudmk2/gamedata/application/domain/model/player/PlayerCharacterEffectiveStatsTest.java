package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.EquippedMartialArts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerCharacterEffectiveStatsTest {

    private PlayerCharacter player;
    private EquippedItems equipped;

    @BeforeEach
    void setUp() {
        BaseCharacter base = BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(10).physique(10).agility(10).intellect(10).will(10).meridian(10)
                .innerPower(0).specialTechnique(0).lightStep(0)
                .fistsAndPalms(0).swordMethod(3).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(1L).alive(true).build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(1).experience(0).nextLevelExp(100).conversable(true).build();
        Inventory inventory = Inventory.create(100);
        equipped = EquippedItems.create();
        player = new PlayerCharacter(null, base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), inventory, equipped, EquippedMartialArts.create());
    }

    @Test
    void getStats_noEquipment_returnsBaseValues() {
        CharacterStats stats = player.getStats();
        assertThat(stats.vigor()).isEqualTo(10);
        assertThat(stats.swordMethod()).isEqualTo(3);
    }

    @Test
    void getStats_withEquippedWeapon_addsModifiers() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(
                        new StatModifier(StatType.VIGOR, 2),
                        new StatModifier(StatType.SWORD_METHOD, 5)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        CharacterStats stats = player.getStats();
        assertThat(stats.vigor()).isEqualTo(12);          // 10 + 2
        assertThat(stats.swordMethod()).isEqualTo(8);     // 3 + 5
    }

    @Test
    void getBaseStats_alwaysReturnsBase_ignoresEquipment() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 2)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        assertThat(player.getBaseStats().vigor()).isEqualTo(10);
    }

    @Test
    void getStats_keepsCurrentResources_fromBase() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.PHYSIQUE, 5)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        CharacterStats stats = player.getStats();
        assertThat(stats.hp()).isEqualTo(100);
        assertThat(stats.physique()).isEqualTo(15);
    }
}
