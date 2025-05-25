package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Direction;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@EqualsAndHashCode(of = "id")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PlayerCharacter implements Combatable, Statable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    private BaseCharacter baseCharacterInfo;

    @Embedded
    private PlayableCharacter playableCharacterInfo;

    private Long userId;
    
    // 플레이어 고유 속성
    private String nickname;
    
    // 직업/클래스 (추후 확장 가능)
    @Enumerated(EnumType.STRING)
    private CharacterClass characterClass;
    
    // 온라인 상태
    private boolean online = false;
    
    // 최근 접속 시간
    private LocalDateTime lastActiveAt;

    public PlayerCharacter(
            final UUID id,
            final BaseCharacter baseCharacterInfo,
            final PlayableCharacter playableCharacterInfo,
            final Long userId,
            final String nickname,
            final CharacterClass characterClass,
            final boolean online,
            final LocalDateTime lastActiveAt
    ) {
        this.id = id;
        this.baseCharacterInfo = baseCharacterInfo;
        this.playableCharacterInfo = playableCharacterInfo;
        this.userId = userId;
        this.nickname = nickname;
        this.characterClass = characterClass;
        this.online = online;
        this.lastActiveAt = lastActiveAt;
    }

    public void initializeAssociatedEntities() {

    }

    public MoveResult move(final Room currentRoom, final Direction direction) {
        if (!currentRoom.hasWay(direction)) {
            return MoveResult.NO_WAY;
        }
        if (currentRoom.isLocked(direction)) {
            return MoveResult.LOCKED;
        }
        Optional<Room> nextRoomByDirection = currentRoom.getNextRoomByDirection(direction);
        if (nextRoomByDirection.isPresent()) {
            Room nextRoom = nextRoomByDirection.get();
            this.setCurrentRoomId(nextRoom.getId());
            return MoveResult.SUCCESS;
        } else {
            return MoveResult.FAILED;
        }
    }

    public void setCurrentRoomId(final Long roomId) {
        this.baseCharacterInfo.setRoomId(roomId);
    }

    public Long getCurrentRoomId() {
        return this.baseCharacterInfo.getRoomId();
    }

    @Override
    public String getName() {
        return this.nickname;
    }

    @Override
    public CharacterStats getStats() {
        return this.baseCharacterInfo.getStats();
    }

    @Override
    public CharacterState getState() {
        return this.baseCharacterInfo.getState();
    }

    public enum MoveResult {
        NO_WAY,
        LOCKED,
        SUCCESS,
        FAILED
    }
}

