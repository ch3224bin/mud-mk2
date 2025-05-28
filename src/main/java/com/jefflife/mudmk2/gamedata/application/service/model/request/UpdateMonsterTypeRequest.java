package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import java.util.List;

public record UpdateMonsterTypeRequest(
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
    List<MonsterSpawnRoomRequest> spawnRooms,
    int aggressiveness,
    int respawnTime
) {}
