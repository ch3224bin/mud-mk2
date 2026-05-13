package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.FoodTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.DropCommand;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DropCommandServiceTest {

    @Mock
    private GameWorldService gameWorldService;

    @Mock
    private ActiveRoomRepository rooms;

    @Mock
    private SendMessageToUserPort sendMessageToUserPort;

    @InjectMocks
    private DropCommandService dropCommandService;

    private PlayerCharacter stubPlayer(Inventory inventory) {
        PlayerCharacter player = mock(PlayerCharacter.class);
        lenient().when(player.getCurrentRoomId()).thenReturn(100L);
        when(player.getInventory()).thenReturn(inventory);
        when(gameWorldService.getPlayerByUserId(1L)).thenReturn(player);
        return player;
    }

    @Test
    void drop_invalidIndex_sendsIndexErrorMessage() {
        dropCommandService.drop(new DropCommand(1L, "철검", 0));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("올바른 번호"));
        verifyNoInteractions(gameWorldService);
    }

    @Test
    void drop_itemNotFoundInInventory_sendsNotFoundMessage() {
        Inventory inventory = mock(Inventory.class);
        stubPlayer(inventory);
        when(inventory.findItemsByName("철검")).thenReturn(List.of());

        dropCommandService.drop(new DropCommand(1L, "철검", 1));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("소지하고 있지 않습니다"));
        verify(inventory, never()).removeItem(any());
    }

    @Test
    void drop_indexOutOfRange_sendsIndexErrorMessage() {
        FoodTemplate template = FoodTemplate.builder()
                .name("만두").description("찐만두").weight(1).stackable(true)
                .hpRecovery(10).mpRecovery(0).apRecovery(0)
                .build();
        ItemInstance item = new ItemInstance(template, 1);

        Inventory inventory = mock(Inventory.class);
        stubPlayer(inventory);
        when(inventory.findItemsByName("만두")).thenReturn(List.of(item));

        dropCommandService.drop(new DropCommand(1L, "만두", 5));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("찾을 수 없습니다"));
        verify(inventory, never()).removeItem(any());
    }

    @Test
    void drop_success_movesItemToFloorAndSendsMessage() {
        FoodTemplate template = FoodTemplate.builder()
                .name("사과").description("빨간 사과").weight(1).stackable(true)
                .hpRecovery(5).mpRecovery(0).apRecovery(0)
                .build();
        ItemInstance item = new ItemInstance(template, 1);

        Inventory inventory = mock(Inventory.class);
        PlayerCharacter player = stubPlayer(inventory);
        Room room = mock(Room.class);
        when(inventory.findItemsByName("사과")).thenReturn(List.of(item));
        when(rooms.findById(100L)).thenReturn(Optional.of(room));

        dropCommandService.drop(new DropCommand(1L, "사과", 1));

        verify(inventory).removeItem(item);
        verify(room).addFloorItem(item);
        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("버렸습니다"));
    }
}
