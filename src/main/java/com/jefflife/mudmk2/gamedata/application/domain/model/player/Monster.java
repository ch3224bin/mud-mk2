package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Monster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private BaseCharacter baseCharacterInfo;
    
    // 몬스터 타입 정보
    @ManyToOne
    @JoinColumn(name = "monster_type_id")
    private MonsterType monsterType;
    
    // 공격성
    private int aggressiveness = 50; // 0-100
    
    // 경험치 보상
    private long experienceReward;
    
    // 리스폰 시간 (초)
    private int respawnTime = 300;
    
    // 마지막 사망 시간
    private LocalDateTime lastDeathTime;
    
    // 소속 그룹 (무리를 지어 다니는 경우)
    private String groupId;
}