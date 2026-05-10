package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.FoodTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.InventoryCommand;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryCommandServiceTest {

    @Mock
    private GameWorldService gameWorldService;

    @Mock
    private SendMessageToUserPort sendMessageToUserPort;

    @InjectMocks
    private InventoryCommandService inventoryCommandService;

    private PlayerCharacter stubPlayerWithInventory(Inventory inventory) {
        PlayerCharacter player = mock(PlayerCharacter.class);
        when(player.getInventory()).thenReturn(inventory);
        when(gameWorldService.getPlayerByUserId(1L)).thenReturn(player);
        return player;
    }

    @Test
    void showInventory_emptyInventory_sendsEmptyMessage() {
        Inventory inventory = mock(Inventory.class);
        when(inventory.getItems()).thenReturn(List.of());
        when(inventory.currentWeight()).thenReturn(0);
        when(inventory.getMaxWeightCapacity()).thenReturn(100);
        stubPlayerWithInventory(inventory);

        inventoryCommandService.showInventory(new InventoryCommand(1L));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("소지품이 없습니다"));
    }

    @Test
    void showInventory_emptyInventory_alwaysAppendsWeightSummary() {
        Inventory inventory = mock(Inventory.class);
        when(inventory.getItems()).thenReturn(List.of());
        when(inventory.currentWeight()).thenReturn(0);
        when(inventory.getMaxWeightCapacity()).thenReturn(100);
        stubPlayerWithInventory(inventory);

        inventoryCommandService.showInventory(new InventoryCommand(1L));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("무게:"));
    }

    @Test
    void showInventory_nonStackableItem_containsItemNameAndWeight() {
        FoodTemplate template = FoodTemplate.builder()
                .name("철검").description("날카로운 검").weight(5).stackable(false)
                .hpRecovery(0).mpRecovery(0).apRecovery(0)
                .build();
        ItemInstance item = new ItemInstance(template, 1);

        Inventory inventory = mock(Inventory.class);
        when(inventory.getItems()).thenReturn(List.of(item));
        when(inventory.currentWeight()).thenReturn(5);
        when(inventory.getMaxWeightCapacity()).thenReturn(100);
        stubPlayerWithInventory(inventory);

        inventoryCommandService.showInventory(new InventoryCommand(1L));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("철검"));
        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("5kg"));
    }

    @Test
    void showInventory_stackableItem_containsItemNameQuantityAndWeight() {
        FoodTemplate template = FoodTemplate.builder()
                .name("사과").description("빨간 사과").weight(1).stackable(true)
                .hpRecovery(5).mpRecovery(0).apRecovery(0)
                .build();
        ItemInstance item = new ItemInstance(template, 3);

        Inventory inventory = mock(Inventory.class);
        when(inventory.getItems()).thenReturn(List.of(item));
        when(inventory.currentWeight()).thenReturn(3);
        when(inventory.getMaxWeightCapacity()).thenReturn(100);
        stubPlayerWithInventory(inventory);

        inventoryCommandService.showInventory(new InventoryCommand(1L));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("사과"));
        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("x3"));
        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("3kg"));
    }

    @Test
    void showInventory_withItems_alwaysAppendsWeightSummary() {
        FoodTemplate template = FoodTemplate.builder()
                .name("만두").description("찐만두").weight(1).stackable(true)
                .hpRecovery(10).mpRecovery(0).apRecovery(0)
                .build();
        ItemInstance item = new ItemInstance(template, 2);

        Inventory inventory = mock(Inventory.class);
        when(inventory.getItems()).thenReturn(List.of(item));
        when(inventory.currentWeight()).thenReturn(2);
        when(inventory.getMaxWeightCapacity()).thenReturn(100);
        stubPlayerWithInventory(inventory);

        inventoryCommandService.showInventory(new InventoryCommand(1L));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("무게:"));
        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("2/100kg"));
    }
}
