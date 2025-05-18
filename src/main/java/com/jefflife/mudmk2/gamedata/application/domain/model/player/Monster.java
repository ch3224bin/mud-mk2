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

    // 경험치 보상
    private long experienceReward;
    
    // 마지막 사망 시간
    private LocalDateTime lastDeathTime;
}