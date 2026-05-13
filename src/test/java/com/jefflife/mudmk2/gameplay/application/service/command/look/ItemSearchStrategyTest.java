package com.jefflife.mudmk2.gameplay.application.service.command.look;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.FoodTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
import com.jefflife.mudmk2.common.fixture.GameTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("아이템 검색 전략 테스트")
class ItemSearchStrategyTest {

    @Mock
    private GameWorldService gameWorldService;

    @Mock
    private ActiveRoomRepository rooms;

    private ItemSearchStrategy strategy;

    private final Long userId = 1L;
    private final Long roomId = 100L;
    private PlayerCharacter player;
    private Room currentRoom;
    private FoodTemplate manduTemplate;
    private FoodTemplate appleTemplate;

    @BeforeEach
    void setUp() {
        strategy = new ItemSearchStrategy(gameWorldService, rooms);
        player = GameTestFixture.createTestPlayer(userId, roomId);
        currentRoom = GameTestFixture.createTestRoom(roomId, "테스트 방", "요약", "설명");
        manduTemplate = FoodTemplate.builder()
                .name("만두").description("찐만두").weight(1).stackable(true)
                .hpRecovery(10).mpRecovery(0).apRecovery(0).build();
        appleTemplate = FoodTemplate.builder()
                .name("사과").description("빨간 사과").weight(1).stackable(true)
                .hpRecovery(5).mpRecovery(0).apRecovery(0).build();
    }

    @Test
    @DisplayName("우선순위는 2")
    void priorityIsTwo() {
        assertThat(strategy.getPriority()).isEqualTo(2);
    }

    @Test
    @DisplayName("바닥에만 아이템 있을 때 index=1로 찾는다")
    void search_floorOnly_returnsRoomItem() {
        currentRoom.addFloorItem(new ItemInstance(manduTemplate, 1));

        when(gameWorldService.getPlayerByUserId(userId)).thenReturn(player);
        when(rooms.findById(roomId)).thenReturn(Optional.of(currentRoom));

        Optional<Lookable> result = strategy.search(userId, "만두", 1);

        assertThat(result).isPresent();
        ItemLookable il = (ItemLookable) result.get();
        assertThat(il.location()).isEqualTo(ItemLookable.ItemLocation.ROOM);
        assertThat(il.instance().getTemplate().getName()).isEqualTo("만두");
    }

    @Test
    @DisplayName("소지품에만 아이템 있을 때 index=1로 찾는다")
    void search_inventoryOnly_returnsInventoryItem() {
        player.getInventory().addItem(new ItemInstance(appleTemplate, 1));

        when(gameWorldService.getPlayerByUserId(userId)).thenReturn(player);
        when(rooms.findById(roomId)).thenReturn(Optional.of(currentRoom));

        Optional<Lookable> result = strategy.search(userId, "사과", 1);

        assertThat(result).isPresent();
        ItemLookable il = (ItemLookable) result.get();
        assertThat(il.location()).isEqualTo(ItemLookable.ItemLocation.INVENTORY);
        assertThat(il.instance().getTemplate().getName()).isEqualTo("사과");
    }

    @Test
    @DisplayName("바닥과 소지품에 같은 이름 있을 때 통합 인덱스 — 1번은 바닥, 2번은 소지품")
    void search_bothFloorAndInventory_unifiedIndex() {
        currentRoom.addFloorItem(new ItemInstance(manduTemplate, 1));
        Inventory inv = player.getInventory();
        ItemInstance inventoryMandu = new ItemInstance(manduTemplate, 1);
        inv.addItem(inventoryMandu);

        when(gameWorldService.getPlayerByUserId(userId)).thenReturn(player);
        when(rooms.findById(roomId)).thenReturn(Optional.of(currentRoom));

        Optional<Lookable> first = strategy.search(userId, "만두", 1);
        assertThat(first).isPresent();
        assertThat(((ItemLookable) first.get()).location()).isEqualTo(ItemLookable.ItemLocation.ROOM);

        Optional<Lookable> second = strategy.search(userId, "만두", 2);
        assertThat(second).isPresent();
        assertThat(((ItemLookable) second.get()).location()).isEqualTo(ItemLookable.ItemLocation.INVENTORY);
    }

    @Test
    @DisplayName("매치 없으면 empty")
    void search_noMatch_empty() {
        when(gameWorldService.getPlayerByUserId(userId)).thenReturn(player);
        when(rooms.findById(roomId)).thenReturn(Optional.of(currentRoom));

        Optional<Lookable> result = strategy.search(userId, "없는것", 1);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("인덱스 범위 밖이면 empty")
    void search_indexOutOfRange_empty() {
        currentRoom.addFloorItem(new ItemInstance(manduTemplate, 1));

        when(gameWorldService.getPlayerByUserId(userId)).thenReturn(player);
        when(rooms.findById(roomId)).thenReturn(Optional.of(currentRoom));

        Optional<Lookable> result = strategy.search(userId, "만두", 2);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("index < 1 이면 empty (방어적)")
    void search_indexZero_empty() {
        Optional<Lookable> result = strategy.search(userId, "만두", 0);
        assertThat(result).isEmpty();
    }
}
