package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.FoodTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.TakeCommand;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
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
class TakeCommandServiceTest {

    @Mock
    private ActivePlayerRepository players;

    @Mock
    private ActiveRoomRepository rooms;

    @Mock
    private SendMessageToUserPort sendMessageToUserPort;

    @InjectMocks
    private TakeCommandService takeCommandService;

    private PlayerCharacter stubPlayer(Room room) {
        PlayerCharacter player = mock(PlayerCharacter.class);
        when(player.getCurrentRoomId()).thenReturn(100L);
        when(players.findByUserId(1L)).thenReturn(Optional.of(player));
        when(rooms.findById(100L)).thenReturn(Optional.of(room));
        return player;
    }

    @Test
    void take_invalidIndex_sendsIndexErrorMessage() {
        takeCommandService.take(new TakeCommand(1L, "철검", 0));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("올바른 번호"));
        verifyNoInteractions(players);
    }

    @Test
    void take_itemNotFound_sendsNotFoundMessage() {
        Room room = mock(Room.class);
        stubPlayer(room);
        when(room.findFloorItemsByName("철검")).thenReturn(List.of());

        takeCommandService.take(new TakeCommand(1L, "철검", 1));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("찾을 수 없습니다"));
    }

    @Test
    void take_indexOutOfRange_sendsIndexErrorMessage() {
        FoodTemplate template = FoodTemplate.builder()
                .name("만두").description("찐만두").weight(1).stackable(true)
                .hpRecovery(10).mpRecovery(0).apRecovery(0)
                .build();
        ItemInstance item = new ItemInstance(template, 1);

        Room room = mock(Room.class);
        stubPlayer(room);
        when(room.findFloorItemsByName("만두")).thenReturn(List.of(item));

        takeCommandService.take(new TakeCommand(1L, "만두", 5));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("찾을 수 없습니다"));
    }

    @Test
    void take_weightExceeded_sendsWeightErrorMessage() {
        FoodTemplate template = FoodTemplate.builder()
                .name("돌").description("무거운 돌").weight(99).stackable(false)
                .hpRecovery(0).mpRecovery(0).apRecovery(0)
                .build();
        ItemInstance item = new ItemInstance(template, 1);

        Room room = mock(Room.class);
        PlayerCharacter player = stubPlayer(room);
        Inventory inventory = mock(Inventory.class);
        when(room.findFloorItemsByName("돌")).thenReturn(List.of(item));
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.canAdd(template, 1)).thenReturn(false);

        takeCommandService.take(new TakeCommand(1L, "돌", 1));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("무게 초과"));
        verify(inventory, never()).addItem(any());
    }

    @Test
    void take_success_movesItemAndSendsMessage() {
        FoodTemplate template = FoodTemplate.builder()
                .name("사과").description("빨간 사과").weight(1).stackable(true)
                .hpRecovery(5).mpRecovery(0).apRecovery(0)
                .build();
        ItemInstance item = new ItemInstance(template, 1);

        Room room = mock(Room.class);
        PlayerCharacter player = stubPlayer(room);
        Inventory inventory = mock(Inventory.class);
        when(room.findFloorItemsByName("사과")).thenReturn(List.of(item));
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.canAdd(template, 1)).thenReturn(true);

        takeCommandService.take(new TakeCommand(1L, "사과", 1));

        verify(room).removeFloorItem(item);
        verify(inventory).addItem(item);
        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("주웠습니다"));
    }
}
