package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.EquippedMartialArts;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.UnequipCommand;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UnequipCommandServiceTest {

    private ActivePlayerRepository players;
    private SendMessageToUserPort sendMessageToUserPort;
    private UnequipCommandService service;
    private PlayerCharacter player;
    private Inventory inventory;
    private EquippedItems equipped;

    private WeaponTemplate swordTemplate;

    @BeforeEach
    void setUp() {
        players = mock(ActivePlayerRepository.class);
        sendMessageToUserPort = mock(SendMessageToUserPort.class);
        service = new UnequipCommandService(players, sendMessageToUserPort);

        inventory = Inventory.create(100);
        equipped = EquippedItems.create();
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
        player = new PlayerCharacter(null, base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), inventory, equipped, EquippedMartialArts.create());
        when(players.findByUserId(1L)).thenReturn(Optional.of(player));

        swordTemplate = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of()).build();
    }

    @Test
    void unequip_equippedWeapon_returnsToInventory() {
        ItemInstance sword = new ItemInstance(swordTemplate, 1);
        equipped.equip(EquipmentSlot.WEAPON, sword);

        service.unequip(new UnequipCommand(1L, "철검"));

        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).isEmpty();
        assertThat(inventory.getItems()).containsExactly(sword);
        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("해제"));
    }

    @Test
    void unequip_notEquipped_sendsErrorMessage() {
        service.unequip(new UnequipCommand(1L, "없는검"));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("장착하고 있지 않습니다"));
    }

    @Test
    void unequip_inventoryWeightExceeded_rejected() {
        Inventory small = Inventory.create(5);
        equipped = EquippedItems.create();
        player = new PlayerCharacter(null, player.getBaseCharacterInfo(), player.getPlayableCharacterInfo(),
                1L, "철수", CharacterClass.WARRIOR, true, LocalDateTime.now(), small, equipped, EquippedMartialArts.create());
        when(players.findByUserId(1L)).thenReturn(Optional.of(player));

        ItemInstance sword = new ItemInstance(swordTemplate, 1); // 5kg
        equipped.equip(EquipmentSlot.WEAPON, sword);
        FoodTemplate food = FoodTemplate.builder()
                .name("만두").description("d").weight(1).stackable(false)
                .hpRecovery(0).mpRecovery(0).apRecovery(0).build();
        for (int i = 0; i < 5; i++) small.addItem(new ItemInstance(food, 1));

        service.unequip(new UnequipCommand(1L, "철검"));

        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).contains(sword);
        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("무게가 부족"));
    }
}
