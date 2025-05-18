package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder @AllArgsConstructor
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

    // 기본 경험치 보상
    private long baseExperience;
    
    // 레벨별 스탯 증가치
    private int hpPerLevel;
    private int strPerLevel;
    private int dexPerLevel;
    private int conPerLevel;
    private int intelligencePerLevel;
    private int powPerLevel;
    private int chaPerLevel;

    // 레벨당 경험치 증가치
    private int expPerLevel;

    // 스폰 정보
    @Builder.Default
    @Embedded
    private MonsterSpawnRooms monsterSpawnRooms = new MonsterSpawnRooms();

    // 공격성
    private int aggressiveness = 50; // 0-100

    // 리스폰 시간 (ticks)
    private int respawnTime = 300;

    public void addSpawnRoom(final MonsterSpawnRoom spawnRoom) {
        if (monsterSpawnRooms == null) {
            monsterSpawnRooms = new MonsterSpawnRooms();
        }
        monsterSpawnRooms.addSpawnRoom(spawnRoom);
    }

    public void clearAndAddAll(final List<MonsterSpawnRoom> newSpawnRooms) {
        if (monsterSpawnRooms == null) {
            monsterSpawnRooms = new MonsterSpawnRooms();
        } else {
            monsterSpawnRooms.clearAndAddAll(newSpawnRooms);
        }
    }
}

