package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record MonsterTypeResponse(
    Long id,
    String name,
    String description,
    Gender gender,
    int baseHp,
    int baseMp,
    // 속성 기본값
    int baseVigor,
    int basePhysique,
    int baseAgility,
    int baseIntellect,
    int baseWill,
    int baseMeridian,
    long baseExperience,
    int hpPerLevel,
    // 속성 레벨당 증가치
    int vigorPerLevel,
    int physiquePerLevel,
    int agilityPerLevel,
    int intellectPerLevel,
    int willPerLevel,
    int meridianPerLevel,
    // 무예 기본값
    int baseInnerPower,
    int baseSpecialTechnique,
    int baseLightStep,
    int baseFistsAndPalms,
    int baseSwordMethod,
    int baseBladeMethod,
    int baseLongWeapon,
    int baseEsotericWeapon,
    int baseArchery,
    int expPerLevel,
    List<MonsterSpawnRoomResponse> spawnRooms,
    int aggressiveness,
    int respawnTime
) {
    public static MonsterTypeResponse from(MonsterType monsterType) {
        List<MonsterSpawnRoomResponse> spawnRoomResponses = new ArrayList<>();
        if (monsterType.getMonsterSpawnRooms() != null && monsterType.getMonsterSpawnRooms().getSpawnRooms() != null) {
            spawnRoomResponses = monsterType.getMonsterSpawnRooms().getSpawnRooms().stream()
                    .map(MonsterSpawnRoomResponse::from)
                    .collect(Collectors.toList());
        }

        return new MonsterTypeResponse(
                monsterType.getId(),
                monsterType.getName(),
                monsterType.getDescription(),
                monsterType.getGender(),
                monsterType.getBaseHp(),
                monsterType.getBaseMp(),
                monsterType.getBaseVigor(),
                monsterType.getBasePhysique(),
                monsterType.getBaseAgility(),
                monsterType.getBaseIntellect(),
                monsterType.getBaseWill(),
                monsterType.getBaseMeridian(),
                monsterType.getBaseExperience(),
                monsterType.getHpPerLevel(),
                monsterType.getVigorPerLevel(),
                monsterType.getPhysiquePerLevel(),
                monsterType.getAgilityPerLevel(),
                monsterType.getIntellectPerLevel(),
                monsterType.getWillPerLevel(),
                monsterType.getMeridianPerLevel(),
                monsterType.getBaseInnerPower(),
                monsterType.getBaseSpecialTechnique(),
                monsterType.getBaseLightStep(),
                monsterType.getBaseFistsAndPalms(),
                monsterType.getBaseSwordMethod(),
                monsterType.getBaseBladeMethod(),
                monsterType.getBaseLongWeapon(),
                monsterType.getBaseEsotericWeapon(),
                monsterType.getBaseArchery(),
                monsterType.getExpPerLevel(),
                spawnRoomResponses,
                monsterType.getAggressiveness(),
                monsterType.getRespawnTime()
        );
    }
}
