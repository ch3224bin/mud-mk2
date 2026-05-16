package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.FoodTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemInstanceRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EatCommand;
import com.jefflife.mudmk2.gameplay.application.service.model.template.EatSuccessVariables;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendEatSuccessMessagePort;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EatCommandServiceTest {

    @Mock
    private ActivePlayerRepository players;

    @Mock
    private ItemInstanceRepository itemInstanceRepository;

    @Mock
    private PlayerCharacterRepository playerCharacterRepository;

    @Mock
    private SendMessageToUserPort sendMessageToUserPort;

    @Mock
    private SendEatSuccessMessagePort sendEatSuccessMessagePort;

    @InjectMocks
    private EatCommandService eatCommandService;

    private PlayerCharacter stubPlayer(Inventory inventory) {
        PlayerCharacter player = mock(PlayerCharacter.class);
        lenient().when(player.getInventory()).thenReturn(inventory);
        lenient().when(players.findByUserId(1L)).thenReturn(Optional.of(player));
        return player;
    }

    private FoodTemplate apple(int hp, int mp, int ap) {
        return FoodTemplate.builder()
                .name("사과").description("빨간 사과").weight(1).stackable(true)
                .hpRecovery(hp).mpRecovery(mp).apRecovery(ap)
                .build();
    }

    @Test
    void eat_invalidIndex_sendsIndexErrorMessage() {
        eatCommandService.eat(new EatCommand(1L, "사과", 0));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("올바른 번호"));
        verify(sendEatSuccessMessagePort, never()).sendMessage(any());
        verifyNoInteractions(players);
    }

    @Test
    void eat_itemNotFoundInInventory_sendsNotFoundMessage() {
        Inventory inventory = mock(Inventory.class);
        stubPlayer(inventory);
        when(inventory.findItemsByName("사과")).thenReturn(List.of());

        eatCommandService.eat(new EatCommand(1L, "사과", 1));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("가지고 있지 않습니다"));
        verify(sendEatSuccessMessagePort, never()).sendMessage(any());
        verify(inventory, never()).consumeOne(any());
        verifyNoInteractions(itemInstanceRepository);
    }

    @Test
    void eat_indexOutOfRange_sendsIndexErrorMessage() {
        ItemInstance item = new ItemInstance(apple(30, 0, 0), 1);
        Inventory inventory = mock(Inventory.class);
        stubPlayer(inventory);
        when(inventory.findItemsByName("사과")).thenReturn(List.of(item));

        eatCommandService.eat(new EatCommand(1L, "사과", 5));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("찾을 수 없습니다"));
        verify(sendEatSuccessMessagePort, never()).sendMessage(any());
        verify(inventory, never()).consumeOne(any());
    }

    @Test
    void eat_nonFoodItem_sendsRejectionMessage() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("평범한 철검").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD).statModifiers(List.of())
                .build();
        ItemInstance item = new ItemInstance(sword, 1);
        Inventory inventory = mock(Inventory.class);
        stubPlayer(inventory);
        when(inventory.findItemsByName("철검")).thenReturn(List.of(item));

        eatCommandService.eat(new EatCommand(1L, "철검", 1));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("먹을 수 없습니다"));
        verify(sendEatSuccessMessagePort, never()).sendMessage(any());
        verify(inventory, never()).consumeOne(any());
    }

    @Test
    void eat_quantityGreaterThanOne_appliesEffectsButDoesNotDeleteInstance() {
        ItemInstance item = new ItemInstance(apple(30, 10, 0), 5);
        Inventory inventory = mock(Inventory.class);
        PlayerCharacter player = stubPlayer(inventory);
        when(inventory.findItemsByName("사과")).thenReturn(List.of(item));
        when(inventory.consumeOne(item)).thenReturn(false);  // 수량 5→4, 제거 안 됨

        eatCommandService.eat(new EatCommand(1L, "사과", 1));

        verify(player).heal(30, 0, 0);
        verify(player).heal(0, 10, 0);
        verify(inventory).consumeOne(item);
        verify(itemInstanceRepository, never()).delete(any());
        verify(playerCharacterRepository, never()).save(any());

        ArgumentCaptor<EatSuccessVariables> captor = ArgumentCaptor.forClass(EatSuccessVariables.class);
        verify(sendEatSuccessMessagePort).sendMessage(captor.capture());
        EatSuccessVariables vars = captor.getValue();
        assertThat(vars.userId()).isEqualTo(1L);
        assertThat(vars.itemName()).isEqualTo("사과");
        assertThat(vars.hpRecovery()).isEqualTo(30);
        assertThat(vars.mpRecovery()).isEqualTo(10);
    }

    @Test
    void eat_quantityOne_deletesInstanceImmediately() {
        ItemInstance item = new ItemInstance(apple(30, 0, 0), 1);
        Inventory inventory = mock(Inventory.class);
        PlayerCharacter player = stubPlayer(inventory);
        when(inventory.findItemsByName("사과")).thenReturn(List.of(item));
        when(inventory.consumeOne(item)).thenReturn(true);  // 수량 1→0, 제거됨

        eatCommandService.eat(new EatCommand(1L, "사과", 1));

        verify(player).heal(30, 0, 0);
        verify(inventory).consumeOne(item);
        verify(itemInstanceRepository).delete(item);
        verify(playerCharacterRepository, never()).save(any());  // FoodTemplate 은 false
        verify(sendEatSuccessMessagePort).sendMessage(any());
    }

    @Test
    void eat_requiresImmediateDeletionTemplate_savesPlayerImmediately() {
        FoodTemplate persistentFood = new FoodTemplate(
                "선단", "내공 영구 증진 단약", 1, false, 0, 0, 0
        ) {
            @Override
            public boolean requiresImmediateDeletion() {
                return true;
            }
        };
        ItemInstance item = new ItemInstance(persistentFood, 1);

        Inventory inventory = mock(Inventory.class);
        PlayerCharacter player = stubPlayer(inventory);
        when(inventory.findItemsByName("선단")).thenReturn(List.of(item));
        when(inventory.consumeOne(item)).thenReturn(true);

        eatCommandService.eat(new EatCommand(1L, "선단", 1));

        verify(itemInstanceRepository).delete(item);
        verify(playerCharacterRepository).save(player);
        verify(sendEatSuccessMessagePort).sendMessage(any());
    }
}
