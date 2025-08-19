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
    int baseStr,
    int baseDex,
    int baseCon,
    int baseIntelligence,
    int basePow,
    int baseCha,
    long baseExperience,
    int hpPerLevel,
    int strPerLevel,
    int dexPerLevel,
    int conPerLevel,
    int intelligencePerLevel,
    int powPerLevel,
    int chaPerLevel,
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
                monsterType.getBaseStr(),
                monsterType.getBaseDex(),
                monsterType.getBaseCon(),
                monsterType.getBaseIntelligence(),
                monsterType.getBasePow(),
                monsterType.getBaseCha(),
                monsterType.getBaseExperience(),
                monsterType.getHpPerLevel(),
                monsterType.getStrPerLevel(),
                monsterType.getDexPerLevel(),
                monsterType.getConPerLevel(),
                monsterType.getIntelligencePerLevel(),
                monsterType.getPowPerLevel(),
                monsterType.getChaPerLevel(),
                monsterType.getExpPerLevel(),
                spawnRoomResponses,
                monsterType.getAggressiveness(),
                monsterType.getRespawnTime()
        );
    }
}
