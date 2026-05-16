package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.EquippedMartialArts;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.StatusCommand;
import com.jefflife.mudmk2.gameplay.application.service.model.template.StatusVariables;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendStatusMessagePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StatusCommandServiceTest {

    private ActivePlayerRepository players;
    private ActiveRoomRepository rooms;
    private SendStatusMessagePort sender;
    private StatusCommandService service;
    private PlayerCharacter player;
    private EquippedItems equipped;

    @BeforeEach
    void setUp() {
        players = mock(ActivePlayerRepository.class);
        rooms = mock(ActiveRoomRepository.class);
        sender = mock(SendStatusMessagePort.class);
        service = new StatusCommandService(rooms, players, sender);

        equipped = EquippedItems.create();
        BaseCharacter base = BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(10).physique(12).agility(11).intellect(9).will(8).meridian(7)
                .innerPower(3).specialTechnique(2).lightStep(1)
                .fistsAndPalms(0).swordMethod(4).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(1L).alive(true).build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(5).experience(120).nextLevelExp(500).conversable(true).build();
        player = new PlayerCharacter(null, base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), Inventory.create(100), equipped, EquippedMartialArts.create());

        Room room = mock(Room.class);
        when(room.getName()).thenReturn("훈련장");
        when(rooms.findById(1L)).thenReturn(Optional.of(room));
        when(players.findByUserId(1L)).thenReturn(Optional.of(player));
    }

    private StatusVariables capturedVariables() {
        ArgumentCaptor<StatusVariables> cap = ArgumentCaptor.forClass(StatusVariables.class);
        verify(sender).sendMessage(cap.capture());
        return cap.getValue();
    }

    @Test
    void showStatus_noEquipment_allStatsHaveZeroBonus() {
        service.showStatus(new StatusCommand(1L));

        StatusVariables v = capturedVariables();
        assertThat(v.userId()).isEqualTo(1L);
        assertThat(v.playerName()).isEqualTo("철수");
        assertThat(v.roomName()).isEqualTo("훈련장");
        assertThat(v.vigor().base()).isEqualTo(10);
        assertThat(v.vigor().bonus()).isEqualTo(0);
        assertThat(v.physique().base()).isEqualTo(12);
        assertThat(v.physique().bonus()).isEqualTo(0);
        assertThat(v.swordMethod().base()).isEqualTo(4);
        assertThat(v.swordMethod().bonus()).isEqualTo(0);
        assertThat(v.archery().base()).isEqualTo(0);
        assertThat(v.archery().bonus()).isEqualTo(0);
    }

    @Test
    void showStatus_withEquippedWeapon_appliesBonusToCorrectStats() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(
                        new StatModifier(StatType.VIGOR, 2),
                        new StatModifier(StatType.SWORD_METHOD, 5)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        service.showStatus(new StatusCommand(1L));

        StatusVariables v = capturedVariables();
        assertThat(v.vigor().base()).isEqualTo(10);
        assertThat(v.vigor().bonus()).isEqualTo(2);
        assertThat(v.vigor().total()).isEqualTo(12);
        assertThat(v.swordMethod().base()).isEqualTo(4);
        assertThat(v.swordMethod().bonus()).isEqualTo(5);
        assertThat(v.swordMethod().total()).isEqualTo(9);
        assertThat(v.physique().bonus()).isEqualTo(0);
        assertThat(v.archery().bonus()).isEqualTo(0);
    }

    @Test
    void showStatus_resourcesFromBaseStats_notFromTotal() {
        service.showStatus(new StatusCommand(1L));

        StatusVariables v = capturedVariables();
        assertThat(v.hp()).isEqualTo(100);
        assertThat(v.mp()).isEqualTo(50);
        assertThat(v.ap()).isEqualTo(80);
        // maxHp = physique * 10 + specialTechnique * 3 = 12 * 10 + 2 * 3 = 126
        assertThat(v.maxHp()).isEqualTo(126);
        // maxMp = meridian * 5 + innerPower * 3 = 7 * 5 + 3 * 3 = 44
        assertThat(v.maxMp()).isEqualTo(44);
        // maxAp = agility * 8 = 11 * 8 = 88
        assertThat(v.maxAp()).isEqualTo(88);
    }
}
