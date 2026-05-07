package com.jefflife.mudmk2.gameplay.application.service.model.template;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterState;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;

/**
 * Variables for the status message template.
 */
public record StatusVariables(
    Long userId,
    String playerName,
    CharacterClass characterClass,
    Gender gender,
    CharacterState state,
    int level,
    long experience,
    long nextLevelExp,
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
    String roomName
) {
}
