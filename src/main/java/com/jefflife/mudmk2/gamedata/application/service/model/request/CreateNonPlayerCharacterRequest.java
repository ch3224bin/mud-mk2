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
    int hp,
    int maxHp,
    int mp,
    int maxMp,
    int str,
    int dex,
    int con,
    int intelligence,
    int pow,
    int cha,
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
                        .maxHp(maxHp)
                        .mp(mp)
                        .maxMp(maxMp)
                        .str(str)
                        .dex(dex)
                        .con(con)
                        .intelligence(intelligence)
                        .pow(pow)
                        .cha(cha)
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
