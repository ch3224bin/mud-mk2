package com.jefflife.mudmk2.common.fixture;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Direction;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.WayOuts;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.BaseCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayableCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 게임 테스트를 위한 Fixture 클래스
 * 테스트에 필요한 객체들을 생성하는 메서드를 제공합니다.
 */
public class GameTestFixture {

    /**
     * 테스트용 플레이어 캐릭터를 생성합니다.
     *
     * @param userId 사용자 ID
     * @param roomId 현재 방 ID
     * @return 생성된 PlayerCharacter 객체
     */
    public static PlayerCharacter createTestPlayer(Long userId, Long roomId) {
        BaseCharacter baseCharacter = BaseCharacter.builder()
                .name("TestPlayer")
                .roomId(roomId)
                .hp(100)
                .maxHp(100)
                .mp(100)
                .maxMp(100)
                .build();

        PlayableCharacter playableCharacter = PlayableCharacter.builder()
                .level(1)
                .experience(0)
                .nextLevelExp(100)
                .conversable(true)
                .build();

        return new PlayerCharacter(
                UUID.randomUUID(),
                baseCharacter,
                playableCharacter,
                userId,
                "TestPlayer",
                CharacterClass.WARRIOR,
                true,
                LocalDateTime.now());
    }

    /**
     * 테스트용 방을 생성합니다.
     *
     * @param roomId 방 ID
     * @param name 방 이름
     * @param summary 방 요약
     * @param description 방 설명
     * @return 생성된 Room 객체
     */
    public static Room createTestRoom(Long roomId, String name, String summary, String description) {
        return Room.builder()
                .id(roomId)
                .name(name)
                .summary(summary)
                .description(description)
                .wayOuts(new WayOuts())
                .build();
    }

    /**
     * 두 방을 지정된 방향으로 연결합니다.
     *
     * @param room1 첫 번째 방
     * @param room2 두 번째 방
     * @param directionFromRoom1 첫 번째 방에서 두 번째 방으로의 방향
     * @param directionFromRoom2 두 번째 방에서 첫 번째 방으로의 방향
     */
    public static void linkRooms(Room room1, Room room2, Direction directionFromRoom1, Direction directionFromRoom2) {
        room1.linkAnotherRoom(room2, directionFromRoom1, directionFromRoom2);
    }

    /**
     * 방향 검색 전략 테스트를 위한 기본 설정을 생성합니다.
     * 플레이어와 두 개의 방을 생성하고 동쪽과 서쪽으로 연결합니다.
     *
     * @param userId 사용자 ID
     * @param roomId 현재 방 ID
     * @param nextRoomId 다음 방 ID
     * @return 생성된 객체들을 담은 DirectionTestSetup 객체
     */
    public static DirectionTestSetup createDirectionTestSetup(Long userId, Long roomId, Long nextRoomId) {
        PlayerCharacter player = createTestPlayer(userId, roomId);
        
        Room currentRoom = createTestRoom(
                roomId, 
                "Current Room", 
                "A test room", 
                "This is a test room"
        );
        
        Room nextRoom = createTestRoom(
                nextRoomId, 
                "Next Room", 
                "Another test room", 
                "This is another test room"
        );
        
        linkRooms(currentRoom, nextRoom, Direction.EAST, Direction.WEST);
        
        return new DirectionTestSetup(player, currentRoom, nextRoom);
    }
    
    /**
     * 방향 검색 전략 테스트에 필요한 객체들을 담는 레코드
     */
    public record DirectionTestSetup(
            PlayerCharacter player,
            Room currentRoom,
            Room nextRoom
    ) {}
}