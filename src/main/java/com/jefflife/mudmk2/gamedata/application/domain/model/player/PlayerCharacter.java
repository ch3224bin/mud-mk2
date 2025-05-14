package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerCharacter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
            final Long id,
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

    public Long getCurrentRoomId() {
        return this.baseCharacterInfo.getRoomId();
    }
}

