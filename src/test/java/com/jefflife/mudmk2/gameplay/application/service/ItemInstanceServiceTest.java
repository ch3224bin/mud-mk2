package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemInstancePlaceRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemInstancePlaceRequest.LocationType;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemInstanceRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemTemplateRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemInstanceServiceTest {

    @Mock private ItemTemplateRepository itemTemplateRepository;
    @Mock private ItemInstanceRepository itemInstanceRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private PlayerCharacterRepository playerCharacterRepository;
    @Mock private GameWorldService gameWorldService;

    private ItemInstanceService service;

    @BeforeEach
    void setUp() {
        service = new ItemInstanceService(
            itemTemplateRepository, itemInstanceRepository,
            roomRepository, playerCharacterRepository, gameWorldService
        );
    }

    @Test
    void place_toRoom_shouldSaveInstanceAndAddToRoomAndInMemory() {
        FoodTemplate template = FoodTemplate.builder()
            .name("만두").description("찐만두").weight(1).stackable(true)
            .hpRecovery(10).mpRecovery(0).apRecovery(0).build();
        Room room = Room.builder().id(1L).areaId(1L).name("동방 입구")
            .summary("입구").description("입구입니다").build();

        when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(itemInstanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(gameWorldService.getRoomOptional(1L)).thenReturn(Optional.of(room));

        ItemInstancePlaceRequest request = new ItemInstancePlaceRequest(1L, 10, LocationType.ROOM, "1");
        ItemInstance result = service.place(request);

        assertThat(result.getQuantity()).isEqualTo(10);
        assertThat(room.getFloorItems()).contains(result);
        verify(roomRepository).save(room);
    }

    @Test
    void place_toRoom_whenRoomNotFound_shouldThrow() {
        FoodTemplate template = FoodTemplate.builder()
            .name("만두").description("찐만두").weight(1).stackable(true)
            .hpRecovery(10).mpRecovery(0).apRecovery(0).build();
        when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        ItemInstancePlaceRequest request = new ItemInstancePlaceRequest(1L, 1, LocationType.ROOM, "999");
        assertThatThrownBy(() -> service.place(request))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void place_toCharacter_whenCharacterNotFound_shouldThrow() {
        FoodTemplate template = FoodTemplate.builder()
            .name("만두").description("찐만두").weight(1).stackable(true)
            .hpRecovery(10).mpRecovery(0).apRecovery(0).build();
        UUID characterId = UUID.randomUUID();
        when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(playerCharacterRepository.findById(characterId)).thenReturn(Optional.empty());

        ItemInstancePlaceRequest request = new ItemInstancePlaceRequest(1L, 1, LocationType.CHARACTER, characterId.toString());
        assertThatThrownBy(() -> service.place(request))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void place_toCharacter_shouldSaveInstanceAndAddToInventoryAndInMemory() {
        FoodTemplate template = FoodTemplate.builder()
            .name("만두").description("찐만두").weight(1).stackable(true)
            .hpRecovery(10).mpRecovery(0).apRecovery(0).build();
        UUID characterId = UUID.randomUUID();
        PlayerCharacter character = mock(PlayerCharacter.class);
        Inventory inventory = mock(Inventory.class);
        when(character.getInventory()).thenReturn(inventory);

        when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(playerCharacterRepository.findById(characterId)).thenReturn(Optional.of(character));
        when(itemInstanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(playerCharacterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ItemInstancePlaceRequest request = new ItemInstancePlaceRequest(1L, 5, LocationType.CHARACTER, characterId.toString());
        ItemInstance result = service.place(request);

        assertThat(result.getQuantity()).isEqualTo(5);
        verify(inventory).addItem(result);
        verify(playerCharacterRepository).save(character);
    }

    @Test
    void place_whenTemplateNotFound_shouldThrow() {
        when(itemTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        ItemInstancePlaceRequest request = new ItemInstancePlaceRequest(999L, 1, LocationType.ROOM, "1");
        assertThatThrownBy(() -> service.place(request))
            .isInstanceOf(NoSuchElementException.class);
    }
}
