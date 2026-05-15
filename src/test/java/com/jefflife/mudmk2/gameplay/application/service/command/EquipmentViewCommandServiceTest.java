package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EquipmentViewCommandServiceTest {

    private ActivePlayerRepository players;
    private SendMessageToUserPort sendMessageToUserPort;
    private EquipmentViewCommandService service;
    private PlayerCharacter player;
    private EquippedItems equipped;

    @BeforeEach
    void setUp() {
        players = mock(ActivePlayerRepository.class);
        sendMessageToUserPort = mock(SendMessageToUserPort.class);
        service = new EquipmentViewCommandService(players, sendMessageToUserPort);

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
                CharacterClass.WARRIOR, true, LocalDateTime.now(), Inventory.create(100), equipped);
        when(players.findByUserId(1L)).thenReturn(Optional.of(player));
    }

    @Test
    void showEquipment_emptySlots_listsAllAsEmpty() {
        service.showEquipment(new EquipmentViewCommand(1L));

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(sendMessageToUserPort).messageToUser(eq(1L), cap.capture());
        String msg = cap.getValue();
        assertThat(msg).contains("[ 장비 ]");
        assertThat(msg).contains("머리").contains("상의").contains("하의").contains("장갑")
                       .contains("신발").contains("허리띠").contains("목걸이")
                       .contains("왼손 반지").contains("오른손 반지").contains("무기");
        assertThat(msg).contains("(없음)");
    }

    @Test
    void showEquipment_withWeapon_listsItemNameAndModifiers() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(
                        new StatModifier(StatType.VIGOR, 2),
                        new StatModifier(StatType.SWORD_METHOD, 5)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        service.showEquipment(new EquipmentViewCommand(1L));

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(sendMessageToUserPort).messageToUser(eq(1L), cap.capture());
        String msg = cap.getValue();
        assertThat(msg).contains("철검");
        assertThat(msg).contains("VIGOR").contains("SWORD_METHOD");
    }

    @Test
    void showEquipment_displaysBaseToEffectiveDiffOnly() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 2)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        service.showEquipment(new EquipmentViewCommand(1L));

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(sendMessageToUserPort).messageToUser(eq(1L), cap.capture());
        String msg = cap.getValue();
        assertThat(msg).contains("[ 적용 스탯 ]");
        assertThat(msg).contains("10").contains("12");
        assertThat(msg).contains("VIGOR");
        assertThat(msg).doesNotContain("PHYSIQUE");
    }
}
