package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class GameWorldService {
    private final static Logger logger = LoggerFactory.getLogger(GameWorldService.class);

    private final Map<UUID, PlayerCharacter> activePlayers = new ConcurrentHashMap<>();
    private final Map<Long, PlayerCharacter> activePlayersByUserId = new ConcurrentHashMap<>();
    private final Map<Long, Room> rooms = new ConcurrentHashMap<>();
    private final Map<UUID, NonPlayerCharacter> activeNpcs = new ConcurrentHashMap<>();
    private final Map<UUID, Monster> activeMonsters = new ConcurrentHashMap<>(); // Monster ID(UUID) -> Monster
    private final Map<UUID, Party> parties = new ConcurrentHashMap<>();

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

    /**
     * 게임에 NPC를 로드합니다.
     * @param npcs 로드할 NPC 목록
     */
    public void loadNpcs(final Iterable<NonPlayerCharacter> npcs) {
        npcs.forEach(npc -> {
            npc.initializeAssociatedEntities();
            activeNpcs.put(npc.getId(), npc);
        });
        logger.info("Loaded {} NPCs", this.activeNpcs.size());
    }

    /**
     * 게임에 몬스터를 로드합니다.
     * @param monsters 로드할 몬스터 목록
     */
    public void loadMonsters(final List<Monster> monsters) {
        monsters.forEach(monster -> {
            activeMonsters.put(monster.getId(), monster);
        });
        logger.info("Loaded {} monsters", this.activeMonsters.size());
    }

    /**
     * 새로운 몬스터를 인메모리 캐시에 추가합니다.
     * @param monster 추가할 몬스터
     */
    public void addMonster(final Monster monster) {
        activeMonsters.put(monster.getId(), monster);
        logger.debug("Monster added to game world: {} (ID: {}, Type: {}, Room: {})",
                monster.getName(), monster.getId(), monster.getMonsterTypeId(), monster.getCurrentRoomId());
    }

    /**
     * 특정 방에 있는 모든 몬스터를 조회합니다.
     * @param roomId 방 ID
     * @return 방에 있는 몬스터 목록
     */
    public List<Monster> getMonstersInRoom(Long roomId) {
        return activeMonsters.values().stream()
                .filter(monster -> monster.isAlive() && monster.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    /**
     * 특정 몬스터 타입으로 생성된 모든 몬스터를 조회합니다.
     * @param monsterTypeId 몬스터 타입 ID
     * @return 해당 타입의 몬스터 목록
     */
    public List<Monster> getMonstersByType(Long monsterTypeId) {
        return activeMonsters.values().stream()
                .filter(monster -> monster.getMonsterTypeId().equals(monsterTypeId))
                .collect(Collectors.toList());
    }

    /**
     * ID로 몬스터를 찾습니다.
     * @param monsterId 몬스터 ID
     * @return 찾은 몬스터, 없으면 null
     */
    public Monster getMonsterById(String monsterId) {
        return activeMonsters.get(monsterId);
    }

    /**
     * 모든 몬스터를 조회합니다.
     * @return 모든 몬스터 목록
     */
    public List<Monster> getAllMonsters() {
        return new ArrayList<>(activeMonsters.values());
    }

    /**
     * 몬스터를 제거합니다.
     * @param monsterId 제거할 몬스터 ID
     */
    public void removeMonster(String monsterId) {
        Monster removed = activeMonsters.remove(monsterId);
        if (removed != null) {
            logger.debug("Monster removed from game world: {} (ID: {})", removed.getName(), monsterId);
        }
    }

    /**
     * 죽은 몬스터 중 리스폰 가능한 몬스터를 리스폰합니다.
     * @return 리스폰된 몬스터 수
     */
    public int respawnMonsters() {
        int respawnCount = 0;

        for (Monster monster : activeMonsters.values()) {
            if (!monster.isAlive() && monster.canRespawn()) {
                monster.respawn();
                respawnCount++;
                logger.debug("Monster respawned: {} (ID: {}, Room: {})",
                        monster.getName(), monster.getId(), monster.getCurrentRoomId());
            }
        }

        if (respawnCount > 0) {
            logger.info("Respawned {} monsters", respawnCount);
        }

        return respawnCount;
    }

    /**
     * 새로운 NPC를 인메모리 캐시에 추가합니다.
     * @param npc 추가할 NPC
     */
    public void addNpc(final NonPlayerCharacter npc) {
        activeNpcs.put(npc.getId(), npc);
        logger.debug("NPC added to game world: {} (ID: {})", npc.getName(), npc.getId());
    }

    /**
     * 특정 방에 있는 모든 NPC를 조회합니다.
     * @param roomId 방 ID
     * @return 방에 있는 NPC 목록
     */
    public List<NonPlayerCharacter> getNpcsInRoom(Long roomId) {
        return activeNpcs.values().stream()
                .filter(npc -> npc.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    /**
     * 특정 방에 있는 모든 플레이어 캐릭터를 조회합니다.
     * @param roomId 방 ID
     * @return 방에 있는 플레이어 캐릭터 목록
     */
    public List<PlayerCharacter> getPlayersInRoom(Long roomId) {
        return activePlayers.values().stream()
                .filter(pc -> pc.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
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

    public Iterable<PlayerCharacter> getActivePlayers() {
        return activePlayers.values();
    }

    /**
     * ID로 NPC를 찾습니다.
     * @param npcId NPC ID
     * @return 찾은 NPC, 없으면 null
     */
    public NonPlayerCharacter getNpcById(UUID npcId) {
        return activeNpcs.get(npcId);
    }

    /**
     * 이름으로 NPC를 찾습니다.
     * @param name NPC 이름
     * @return 찾은 NPC, 없으면 null
     */
    public NonPlayerCharacter getNpcByName(String name) {
        return activeNpcs.values().stream()
                .filter(npc -> npc.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * NPC를 특정 방으로 이동시킵니다.
     * @param npcId NPC ID
     * @param roomId 이동할 방 ID
     * @return 이동 성공 여부
     */
    public boolean moveNpcToRoom(UUID npcId, Long roomId) {
        NonPlayerCharacter npc = activeNpcs.get(npcId);
        if (npc == null || rooms.get(roomId) == null) {
            return false;
        }

        // NPC의 방 ID 변경 (BaseCharacter의 roomId 필드 접근)
        try {
            java.lang.reflect.Field roomIdField = npc.getBaseCharacterInfo().getClass().getDeclaredField("roomId");
            roomIdField.setAccessible(true);
            roomIdField.set(npc.getBaseCharacterInfo(), roomId);
            logger.debug("Moved NPC {} to room {}", npc.getName(), roomId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to move NPC to room: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 캐릭터 ID로 사용자 ID를 찾습니다.
     * @param characterId 캐릭터 ID
     * @return 사용자 ID
     */
    public Long getUserIdByCharacterId(UUID characterId) {
        PlayerCharacter pc = activePlayers.get(characterId);
        if (pc == null) {
            throw new IllegalArgumentException("Character not found for ID: " + characterId);
        }
        return pc.getUserId();
    }

    public boolean isInParty(UUID characterId) {
        return parties.values()
                .stream()
                .anyMatch(party -> party.contains(characterId));
    }

    public void addParty(final Party party) {
        parties.put(party.getId(), party);
    }

    public Optional<Party> getPartyByPlayerId(UUID playerId) {
        return parties.values()
                .stream()
                .filter(party -> party.contains(playerId))
                .findFirst();
    }
}
