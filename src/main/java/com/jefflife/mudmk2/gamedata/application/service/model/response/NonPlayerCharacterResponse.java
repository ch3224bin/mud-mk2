package com.jefflife.mudmk2.gamedata.application.service.model.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NPCType;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;

public record NonPlayerCharacterResponse (
    Long id,
    String name,
    String background,
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
    boolean alive,
    int level,
    long experience,
    long nextLevelExp,
    boolean conversable,
    String persona,
    NPCType npcType,
    Long spawnRoomId,
    boolean essential
) {
    public static NonPlayerCharacterResponse of(NonPlayerCharacter nonPlayerCharacter) {
        return new NonPlayerCharacterResponse(
            nonPlayerCharacter.getId(),
            nonPlayerCharacter.getBaseCharacterInfo().getName(),
            nonPlayerCharacter.getBaseCharacterInfo().getBackground(),
            nonPlayerCharacter.getBaseCharacterInfo().getHp(),
            nonPlayerCharacter.getBaseCharacterInfo().getMaxHp(),
            nonPlayerCharacter.getBaseCharacterInfo().getMp(),
            nonPlayerCharacter.getBaseCharacterInfo().getMaxMp(),
            nonPlayerCharacter.getBaseCharacterInfo().getStr(),
            nonPlayerCharacter.getBaseCharacterInfo().getDex(),
            nonPlayerCharacter.getBaseCharacterInfo().getCon(),
            nonPlayerCharacter.getBaseCharacterInfo().getIntelligence(),
            nonPlayerCharacter.getBaseCharacterInfo().getPow(),
            nonPlayerCharacter.getBaseCharacterInfo().getCha(),
            nonPlayerCharacter.getBaseCharacterInfo().getRoomId(),
            nonPlayerCharacter.getBaseCharacterInfo().isAlive(),
            nonPlayerCharacter.getPlayableCharacterInfo().getLevel(),
            nonPlayerCharacter.getPlayableCharacterInfo().getExperience(),
            nonPlayerCharacter.getPlayableCharacterInfo().getNextLevelExp(),
            nonPlayerCharacter.getPlayableCharacterInfo().isConversable(),
            nonPlayerCharacter.getPersona(),
            nonPlayerCharacter.getNpcType(),
            nonPlayerCharacter.getSpawnRoomId(),
            nonPlayerCharacter.isEssential()
        );
    }
}