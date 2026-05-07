package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.BaseCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NPCType;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayableCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.service.NonPlayerCharacterFactory;


public record CreateNonPlayerCharacterRequest (
    // BaseCharacter fields
    String name,
    String background,
    Gender gender,
    // 현재 자원
    int hp,
    int mp,
    int ap,
    // 속성
    int vigor,
    int physique,
    int agility,
    int intellect,
    int will,
    int meridian,
    // 무예
    int innerPower,
    int specialTechnique,
    int lightStep,
    int fistsAndPalms,
    int swordMethod,
    int bladeMethod,
    int longWeapon,
    int esotericWeapon,
    int archery,
    Long roomId,

    // PlayableCharacter fields
    int level,
    long experience,
    long nextLevelExp,
    boolean conversable,

    // NonPlayerCharacter specific fields
    String persona,
    NPCType npcType,
    Long spawnRoomId,
    boolean essential
) {
    public NonPlayerCharacter toDomain() {
        return NonPlayerCharacterFactory.builder()
                .baseCharacter(
                        BaseCharacter.builder()
                        .name(name)
                        .background(background)
                        .hp(hp)
                        .mp(mp)
                        .ap(ap)
                        .vigor(vigor)
                        .physique(physique)
                        .agility(agility)
                        .intellect(intellect)
                        .will(will)
                        .meridian(meridian)
                        .innerPower(innerPower)
                        .specialTechnique(specialTechnique)
                        .lightStep(lightStep)
                        .fistsAndPalms(fistsAndPalms)
                        .swordMethod(swordMethod)
                        .bladeMethod(bladeMethod)
                        .longWeapon(longWeapon)
                        .esotericWeapon(esotericWeapon)
                        .archery(archery)
                        .roomId(roomId)
                        .alive(true)
                        .gender(gender)
                        .build())
                .playableCharacter(
                        PlayableCharacter.builder()
                        .level(level)
                        .experience(experience)
                        .nextLevelExp(nextLevelExp)
                        .conversable(conversable)
                        .build())
                .persona(persona)
                .npcType(npcType)
                .spawnRoomId(spawnRoomId)
                .essential(essential)
                .build();
    }
}
