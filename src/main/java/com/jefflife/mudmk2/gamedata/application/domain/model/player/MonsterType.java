package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
public class MonsterType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    
    // 기본 스탯
    private int baseHp;
    private int baseMp;
    private int baseStr;
    private int baseDex;
    private int baseCon;
    private int baseIntelligence;
    private int basePow;
    private int baseCha;
    
    // 레벨별 스탯 증가치
    private float hpPerLevel;
    private float strPerLevel;
    // ... 기타 스탯
    
    // 기본 경험치 보상
    private long baseExperience;
    
    // 스폰 정보
    @ElementCollection
    @CollectionTable(name = "monster_spawn_rooms")
    private Set<Long> spawnRoomIds = new HashSet<>();
    
    private int maxSpawnCount;
    private int currentSpawnCount;
}