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
    String roomName
) {
}