package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;

import java.util.List;

public record CreateMonsterTypeRequest(
    String name,
    String description,
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
    List<MonsterSpawnRoomRequest> spawnRooms,
    int aggressiveness,
    int respawnTime
) {
    public MonsterType toDomain() {
        return MonsterType.builder()
                .name(name)
                .description(description)
                .baseHp(baseHp)
                .baseMp(baseMp)
                .baseStr(baseStr)
                .baseDex(baseDex)
                .baseCon(baseCon)
                .baseIntelligence(baseIntelligence)
                .basePow(basePow)
                .baseCha(baseCha)
                .baseExperience(baseExperience)
                .hpPerLevel(hpPerLevel)
                .strPerLevel(strPerLevel)
                .dexPerLevel(dexPerLevel)
                .conPerLevel(conPerLevel)
                .intelligencePerLevel(intelligencePerLevel)
                .powPerLevel(powPerLevel)
                .chaPerLevel(chaPerLevel)
                .expPerLevel(expPerLevel)
                .aggressiveness(aggressiveness)
                .respawnTime(respawnTime)
                .build();
    }
}
