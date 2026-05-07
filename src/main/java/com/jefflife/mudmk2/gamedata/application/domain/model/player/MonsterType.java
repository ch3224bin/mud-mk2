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

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Gender gender = Gender.MALE;

    // 기본 스탯
    private int baseHp;
    private int baseMp;

    // 속성 기본값
    private int baseVigor;
    private int basePhysique;
    private int baseAgility;
    private int baseIntellect;
    private int baseWill;
    private int baseMeridian;

    // 기본 경험치 보상
    private long baseExperience;

    // 레벨별 스탯 증가치
    private int hpPerLevel;

    // 속성 레벨당 증가치
    private int vigorPerLevel;
    private int physiquePerLevel;
    private int agilityPerLevel;
    private int intellectPerLevel;
    private int willPerLevel;
    private int meridianPerLevel;

    // 무예 기본값
    private int baseInnerPower;
    private int baseSpecialTechnique;
    private int baseLightStep;
    private int baseFistsAndPalms;
    private int baseSwordMethod;
    private int baseBladeMethod;
    private int baseLongWeapon;
    private int baseEsotericWeapon;
    private int baseArchery;

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
