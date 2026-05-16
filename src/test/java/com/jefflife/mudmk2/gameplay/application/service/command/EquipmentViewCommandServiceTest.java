package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.EquippedMartialArts;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;
import com.jefflife.mudmk2.gameplay.application.service.model.template.EquipmentViewVariables;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendEquipmentViewMessagePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EquipmentViewCommandServiceTest {

    private ActivePlayerRepository players;
    private SendEquipmentViewMessagePort sender;
    private EquipmentViewCommandService service;
    private PlayerCharacter player;
    private EquippedItems equipped;

    @BeforeEach
    void setUp() {
        players = mock(ActivePlayerRepository.class);
        sender = mock(SendEquipmentViewMessagePort.class);
        service = new EquipmentViewCommandService(players, sender);

        equipped = EquippedItems.create();
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
        player = new PlayerCharacter(null, base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), Inventory.create(100), equipped, EquippedMartialArts.create());
        when(players.findByUserId(1L)).thenReturn(Optional.of(player));
    }

    private EquipmentViewVariables capturedVariables() {
        ArgumentCaptor<EquipmentViewVariables> cap = ArgumentCaptor.forClass(EquipmentViewVariables.class);
        verify(sender).sendMessage(cap.capture());
        return cap.getValue();
    }

    @Test
    void showEquipment_emptySlots_allSlotsHaveNullItemName() {
        service.showEquipment(new EquipmentViewCommand(1L));

        EquipmentViewVariables v = capturedVariables();
        assertThat(v.userId()).isEqualTo(1L);
        assertThat(v.slots()).hasSize(10);
        assertThat(v.slots()).allMatch(s -> s.itemName() == null);
        assertThat(v.statDiffs()).isEmpty();
    }

    @Test
    void showEquipment_withWeapon_weaponSlotHasItemNameAndKoreanModifiers() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(
                        new StatModifier(StatType.VIGOR, 2),
                        new StatModifier(StatType.SWORD_METHOD, 5)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        service.showEquipment(new EquipmentViewCommand(1L));

        EquipmentViewVariables v = capturedVariables();
        EquipmentViewVariables.SlotEntry weaponSlot = v.slots().stream()
                .filter(s -> "무기".equals(s.slotLabel())).findFirst().orElseThrow();
        assertThat(weaponSlot.itemName()).isEqualTo("철검");
        assertThat(weaponSlot.modifiers()).hasSize(2);
        assertThat(weaponSlot.modifiers().get(0).label()).isEqualTo("활력");
        assertThat(weaponSlot.modifiers().get(0).value()).isEqualTo(2);
        assertThat(weaponSlot.modifiers().get(1).label()).isEqualTo("검술");
        assertThat(weaponSlot.modifiers().get(1).value()).isEqualTo(5);
    }

    @Test
    void showEquipment_displaysOnlyChangedStatsWithKoreanLabels() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 2)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        service.showEquipment(new EquipmentViewCommand(1L));

        EquipmentViewVariables v = capturedVariables();
        assertThat(v.statDiffs()).hasSize(1);
        assertThat(v.statDiffs().get(0).label()).isEqualTo("활력");
        assertThat(v.statDiffs().get(0).base()).isEqualTo(10);
        assertThat(v.statDiffs().get(0).effective()).isEqualTo(12);
    }
}
