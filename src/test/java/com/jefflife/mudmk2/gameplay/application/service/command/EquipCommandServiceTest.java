package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipCommand;
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

class EquipCommandServiceTest {

    private ActivePlayerRepository players;
    private SendMessageToUserPort sender;
    private EquipCommandService service;
    private PlayerCharacter player;
    private Inventory inventory;
    private EquippedItems equipped;

    private WeaponTemplate swordTemplate;
    private EquipmentTemplate helmetTemplate;
    private AccessoryTemplate ringTemplate;
    private AccessoryTemplate necklaceTemplate;
    private FoodTemplate foodTemplate;

    @BeforeEach
    void setUp() {
        players = mock(ActivePlayerRepository.class);
        sender = mock(SendMessageToUserPort.class);
        service = new EquipCommandService(players, sender);

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
                CharacterClass.WARRIOR, true, LocalDateTime.now(), inventory, equipped);

        when(players.findByUserId(1L)).thenReturn(Optional.of(player));

        swordTemplate = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 2)))
                .build();
        helmetTemplate = EquipmentTemplate.builder()
                .name("야구모자").description("d").weight(1).stackable(false)
                .equipmentSlot(EquipmentSlot.HELMET)
                .statModifiers(List.of()).build();
        ringTemplate = AccessoryTemplate.builder()
                .name("금반지").description("d").weight(1).stackable(false)
                .accessoryType(AccessoryType.RING)
                .statModifiers(List.of()).build();
        necklaceTemplate = AccessoryTemplate.builder()
                .name("목걸이").description("d").weight(1).stackable(false)
                .accessoryType(AccessoryType.NECKLACE)
                .statModifiers(List.of()).build();
        foodTemplate = FoodTemplate.builder()
                .name("만두").description("d").weight(1).stackable(true)
                .hpRecovery(0).mpRecovery(0).apRecovery(0).build();
    }

    @Test
    void equip_weaponFromInventory_movesToWeaponSlot() {
        ItemInstance sword = new ItemInstance(swordTemplate, 1);
        inventory.addItem(sword);

        service.equip(new EquipCommand(1L, "철검", 1));

        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).contains(sword);
        assertThat(inventory.getItems()).isEmpty();
        verify(sender).messageToUser(eq(1L), contains("철검"));
    }

    @Test
    void equip_helmet_movesToHelmetSlot() {
        ItemInstance helmet = new ItemInstance(helmetTemplate, 1);
        inventory.addItem(helmet);

        service.equip(new EquipCommand(1L, "야구모자", 1));

        assertThat(equipped.getSlot(EquipmentSlot.HELMET)).contains(helmet);
    }

    @Test
    void equip_necklace_movesToNecklaceSlot() {
        ItemInstance necklace = new ItemInstance(necklaceTemplate, 1);
        inventory.addItem(necklace);

        service.equip(new EquipCommand(1L, "목걸이", 1));

        assertThat(equipped.getSlot(EquipmentSlot.NECKLACE)).contains(necklace);
    }

    @Test
    void equip_firstRing_goesToRingLeft() {
        ItemInstance ring = new ItemInstance(ringTemplate, 1);
        inventory.addItem(ring);

        service.equip(new EquipCommand(1L, "금반지", 1));

        assertThat(equipped.getSlot(EquipmentSlot.RING_LEFT)).contains(ring);
        assertThat(equipped.getSlot(EquipmentSlot.RING_RIGHT)).isEmpty();
    }

    @Test
    void equip_secondRing_goesToRingRight_whenLeftOccupied() {
        ItemInstance ring1 = new ItemInstance(ringTemplate, 1);
        ItemInstance ring2 = new ItemInstance(ringTemplate, 1);
        inventory.addItem(ring1);
        inventory.addItem(ring2);

        service.equip(new EquipCommand(1L, "금반지", 1));
        service.equip(new EquipCommand(1L, "금반지", 1));

        assertThat(equipped.getSlot(EquipmentSlot.RING_LEFT)).isPresent();
        assertThat(equipped.getSlot(EquipmentSlot.RING_RIGHT)).isPresent();
    }

    @Test
    void equip_thirdRing_swapsLeft_andReturnsOldToInventory() {
        ItemInstance ring1 = new ItemInstance(ringTemplate, 1);
        ItemInstance ring2 = new ItemInstance(ringTemplate, 1);
        ItemInstance ring3 = new ItemInstance(ringTemplate, 1);
        inventory.addItem(ring1);
        inventory.addItem(ring2);
        inventory.addItem(ring3);

        service.equip(new EquipCommand(1L, "금반지", 1)); // left
        service.equip(new EquipCommand(1L, "금반지", 1)); // right
        service.equip(new EquipCommand(1L, "금반지", 1)); // swap left

        assertThat(equipped.getSlot(EquipmentSlot.RING_LEFT)).isPresent();
        assertThat(equipped.getSlot(EquipmentSlot.RING_RIGHT)).isPresent();
        assertThat(inventory.getItems()).hasSize(1);
    }

    @Test
    void equip_occupiedSlot_swapsAndReturnsOldToInventory() {
        ItemInstance helmet1 = new ItemInstance(helmetTemplate, 1);
        ItemInstance helmet2 = new ItemInstance(helmetTemplate, 1);
        inventory.addItem(helmet1);
        inventory.addItem(helmet2);

        service.equip(new EquipCommand(1L, "야구모자", 1));
        service.equip(new EquipCommand(1L, "야구모자", 1));

        assertThat(equipped.getSlot(EquipmentSlot.HELMET)).isPresent();
        assertThat(inventory.getItems()).hasSize(1);
        verify(sender, atLeastOnce()).messageToUser(eq(1L), contains("해제"));
    }

    @Test
    void equip_itemNotInInventory_sendsErrorMessage() {
        service.equip(new EquipCommand(1L, "없는검", 1));

        verify(sender).messageToUser(eq(1L), contains("가지고 있지 않습니다"));
        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).isEmpty();
    }

    @Test
    void equip_nonEquippableItem_sendsErrorMessage() {
        ItemInstance food = new ItemInstance(foodTemplate, 1);
        inventory.addItem(food);

        service.equip(new EquipCommand(1L, "만두", 1));

        verify(sender).messageToUser(eq(1L), contains("장착할 수 없습니다"));
        assertThat(inventory.getItems()).hasSize(1);
    }

    @Test
    void equip_swapWouldExceedInventoryWeight_rejected() {
        WeaponTemplate lightSwordTemplate = WeaponTemplate.builder()
                .name("목검").description("d").weight(1).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of()).build();
        WeaponTemplate heavySwordTemplate = WeaponTemplate.builder()
                .name("대검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of()).build();

        Inventory small = Inventory.create(5);
        equipped = EquippedItems.create();
        player = new PlayerCharacter(null, player.getBaseCharacterInfo(), player.getPlayableCharacterInfo(), 1L,
                "철수", CharacterClass.WARRIOR, true, LocalDateTime.now(), small, equipped);
        when(players.findByUserId(1L)).thenReturn(Optional.of(player));

        // 슬롯에 5kg 대검 장착
        ItemInstance heavy = new ItemInstance(heavySwordTemplate, 1);
        small.addItem(heavy);
        service.equip(new EquipCommand(1L, "대검", 1));  // 슬롯 = 대검, 인벤 = 0

        // 인벤토리에 1kg 목검 + 4kg 중검 채워서 5/5
        ItemInstance light = new ItemInstance(lightSwordTemplate, 1);
        small.addItem(light); // 인벤 = 1kg
        WeaponTemplate midSwordTemplate = WeaponTemplate.builder()
                .name("중검").description("d").weight(4).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of()).build();
        ItemInstance mid = new ItemInstance(midSwordTemplate, 1);
        small.addItem(mid); // 인벤 = 1+4 = 5kg (꽉)

        // 목검 장착 시도 → 슬롯의 대검(5kg)이 인벤으로 와야 함. 인벤 무게: 5 - 1 + 5 = 9 > 5 → 거부
        service.equip(new EquipCommand(1L, "목검", 1));

        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).contains(heavy);  // 여전히 대검
        verify(sender, atLeastOnce()).messageToUser(eq(1L), contains("무게가 부족"));
    }
}
