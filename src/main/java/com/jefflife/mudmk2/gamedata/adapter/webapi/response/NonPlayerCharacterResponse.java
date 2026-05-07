package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NPCType;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;

import java.util.UUID;

public record NonPlayerCharacterResponse (
    UUID id,
    String name,
    String background,
    Gender gender,
    // 현재 자원
    int hp,
    int maxHp,
    int mp,
    int maxMp,
    int ap,
    int maxAp,
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
        var base = nonPlayerCharacter.getBaseCharacterInfo();
        var stats = base.getStats();
        return new NonPlayerCharacterResponse(
            nonPlayerCharacter.getId(),
            base.getName(),
            base.getBackground(),
            base.getGender(),
            stats.hp(),
            stats.maxHp(),
            stats.mp(),
            stats.maxMp(),
            stats.ap(),
            stats.maxAp(),
            stats.vigor(),
            stats.physique(),
            stats.agility(),
            stats.intellect(),
            stats.will(),
            stats.meridian(),
            stats.innerPower(),
            stats.specialTechnique(),
            stats.lightStep(),
            stats.fistsAndPalms(),
            stats.swordMethod(),
            stats.bladeMethod(),
            stats.longWeapon(),
            stats.esotericWeapon(),
            stats.archery(),
            base.getRoomId(),
            base.isAlive(),
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
