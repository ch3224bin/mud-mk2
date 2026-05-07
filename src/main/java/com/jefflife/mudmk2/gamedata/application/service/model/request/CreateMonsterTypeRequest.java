package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;

import java.util.List;

public record CreateMonsterTypeRequest(
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
    List<MonsterSpawnRoomRequest> spawnRooms,
    int aggressiveness,
    int respawnTime
) {
    public MonsterType toDomain() {
        return MonsterType.builder()
                .name(name)
                .description(description)
                .gender(gender)
                .baseHp(baseHp)
                .baseMp(baseMp)
                .baseVigor(baseVigor)
                .basePhysique(basePhysique)
                .baseAgility(baseAgility)
                .baseIntellect(baseIntellect)
                .baseWill(baseWill)
                .baseMeridian(baseMeridian)
                .baseExperience(baseExperience)
                .hpPerLevel(hpPerLevel)
                .vigorPerLevel(vigorPerLevel)
                .physiquePerLevel(physiquePerLevel)
                .agilityPerLevel(agilityPerLevel)
                .intellectPerLevel(intellectPerLevel)
                .willPerLevel(willPerLevel)
                .meridianPerLevel(meridianPerLevel)
                .baseInnerPower(baseInnerPower)
                .baseSpecialTechnique(baseSpecialTechnique)
                .baseLightStep(baseLightStep)
                .baseFistsAndPalms(baseFistsAndPalms)
                .baseSwordMethod(baseSwordMethod)
                .baseBladeMethod(baseBladeMethod)
                .baseLongWeapon(baseLongWeapon)
                .baseEsotericWeapon(baseEsotericWeapon)
                .baseArchery(baseArchery)
                .expPerLevel(expPerLevel)
                .aggressiveness(aggressiveness)
                .respawnTime(respawnTime)
                .build();
    }
}
