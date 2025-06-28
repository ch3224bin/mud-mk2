package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.common.fixture.GameTestFixture;
import com.jefflife.mudmk2.common.fixture.GameTestFixture.DirectionTestSetup;
import com.jefflife.mudmk2.gameplay.application.service.command.look.DirectionLookable;
import com.jefflife.mudmk2.gameplay.application.service.command.look.Lookable;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Direction;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("방향 검색 전략 테스트")
class DirectionSearchStrategyTest {

    @Mock
    private GameWorldService gameWorldService;

    @InjectMocks
    private DirectionSearchStrategy directionSearchStrategy;

    private final Long userId = 1L;
    private final Long roomId = 101L;
    private final Long nextRoomId = 102L;
    private PlayerCharacter player;
    private Room currentRoom;
    private Room nextRoom;

    @BeforeEach
    void setUp() {
        DirectionTestSetup testSetup = GameTestFixture.createDirectionTestSetup(userId, roomId, nextRoomId);
        player = testSetup.player();
        currentRoom = testSetup.currentRoom();
        nextRoom = testSetup.nextRoom();
    }

    @Test
    @DisplayName("유효한 방향과 존재하는 방이 주어졌을 때 DirectionLookable을 반환한다")
    void search_ValidDirectionWithExistingRoom_ReturnsDirectionLookable() {
        // Arrange
        when(gameWorldService.getPlayerByUserId(userId)).thenReturn(player);
        when(gameWorldService.getRoom(roomId)).thenReturn(currentRoom);

        // Act
        Optional<Lookable> result = directionSearchStrategy.search(userId, "동");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(DirectionLookable.class);
        DirectionLookable directionLookable = (DirectionLookable) result.get();
        assertThat(directionLookable.direction()).isEqualTo(Direction.EAST);
        assertThat(directionLookable.room()).isEqualTo(nextRoom);
    }

    @Test
    @DisplayName("유효한 방향이지만 연결된 방이 없을 때 빈 Optional을 반환한다")
    void search_ValidDirectionWithNoRoom_ReturnsEmptyOptional() {
        // Arrange
        when(gameWorldService.getPlayerByUserId(userId)).thenReturn(player);
        when(gameWorldService.getRoom(roomId)).thenReturn(currentRoom);

        // Act - Try to go NORTH where there's no room
        Optional<Lookable> result = directionSearchStrategy.search(userId, "북");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("유효하지 않은 방향이 주어졌을 때 빈 Optional을 반환한다")
    void search_InvalidDirection_ReturnsEmptyOptional() {
        // Act
        Optional<Lookable> result = directionSearchStrategy.search(userId, "invalid");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("우선순위 값으로 1을 반환한다")
    void getPriority_ReturnsOne() {
        // Act
        int priority = directionSearchStrategy.getPriority();

        // Assert
        assertThat(priority).isEqualTo(1);
    }
}
