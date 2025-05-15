package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.user.service.UserSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWorldService {
    private final static Logger logger = LoggerFactory.getLogger(GameWorldService.class);

    private final Map<Long, PlayerCharacter> activePlayers = new ConcurrentHashMap<>();
    private final Map<Long, PlayerCharacter> activePlayersByUserId = new ConcurrentHashMap<>();
    private final Map<Long, Room> rooms = new ConcurrentHashMap<>();

    public void loadRooms(final Iterable<Room> rooms) {
        rooms.forEach(room -> {
            room.initializeAssociatedEntities(); // Room 객체 내부에서 연관 객체 초기화
            this.rooms.put(room.getId(), room);
        });
        logger.info("Loaded {} rooms", this.rooms.size());
    }

    public void loadPlayers(final Iterable<PlayerCharacter> players) {
        players.forEach(playerCharacter -> {
            playerCharacter.initializeAssociatedEntities(); // PlayerCharacter 객체 내부에서 연관 객체 초기화
            activePlayers.put(playerCharacter.getId(), playerCharacter);
            activePlayersByUserId.put(playerCharacter.getUserId(), playerCharacter);
        });
        logger.info("Loaded {} players", this.activePlayers.size());
    }

    /**
     * 새로 생성된 플레이어 캐릭터를 인메모리 캐시에 추가합니다.
     * @param playerCharacter 추가할 플레이어 캐릭터
     */
    public void addPlayer(final PlayerCharacter playerCharacter) {
        activePlayers.put(playerCharacter.getId(), playerCharacter);
        activePlayersByUserId.put(playerCharacter.getUserId(), playerCharacter);
        logger.debug("Player added to game world: {} (ID: {}, User ID: {})",
                playerCharacter.getNickname(), playerCharacter.getId(), playerCharacter.getUserId());
    }

    public void movePlayer(Long playerId, Long roomId) {
        final PlayerCharacter player = activePlayers.get(playerId);
        player.setCurrentRoomId(roomId);
    }

    public Room getRoom(Long id) {
        final Room room = rooms.get(id);
        if (room == null) {
            throw new IllegalArgumentException("Room not found for ID: " + id);
        }
        return room;
    }

    public PlayerCharacter getPlayerByUserId(Long userId) {
        final PlayerCharacter playerCharacter = activePlayersByUserId.get(userId);
        if (playerCharacter == null) {
            throw new IllegalArgumentException("Player character not found for user ID: " + userId);
        }
        return playerCharacter;
    }

    public PlayerCharacter getPlayerByUsername(String username) {
        return UserSessionManager.getConnectedUser(username)
                .map(user -> getPlayerByUserId(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
}

