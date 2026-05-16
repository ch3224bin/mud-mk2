package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerCharacterStatModifiersTest {

    private PlayerCharacter newPlayer(EquippedItems equipped) {
        BaseCharacter base = BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(10).physique(10).agility(10).intellect(10).will(10).meridian(10)
                .innerPower(0).specialTechnique(0).lightStep(0)
                .fistsAndPalms(0).swordMethod(0).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(1L).alive(true).build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(1).experience(0).nextLevelExp(100).conversable(true).build();
        return new PlayerCharacter(null, base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), Inventory.create(100), equipped);
    }

    @Test
    void getStatModifiers_emptyEquipment_returnsEmptyMap() {
        PlayerCharacter player = newPlayer(EquippedItems.create());

        Map<StatType, Integer> mods = player.getStatModifiers();

        assertThat(mods).isEmpty();
    }

    @Test
    void getStatModifiers_withWeapon_returnsAggregatedModifiers() {
        EquippedItems equipped = EquippedItems.create();
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(
                        new StatModifier(StatType.VIGOR, 2),
                        new StatModifier(StatType.SWORD_METHOD, 5)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));
        PlayerCharacter player = newPlayer(equipped);

        Map<StatType, Integer> mods = player.getStatModifiers();

        assertThat(mods).containsEntry(StatType.VIGOR, 2);
        assertThat(mods).containsEntry(StatType.SWORD_METHOD, 5);
    }
}
