package com.jefflife.mudmk2.gameplay.application.service.model.template;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterState;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;

/**
 * Variables for the status message template.
 * 각 능력치/무예는 base 값과 장비/심법으로부터 누적된 bonus 값을 함께 보유한다.
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
        // 속성 6개
        StatValue vigor,
        StatValue physique,
        StatValue agility,
        StatValue intellect,
        StatValue will,
        StatValue meridian,
        // 무예 9개
        StatValue innerPower,
        StatValue specialTechnique,
        StatValue lightStep,
        StatValue fistsAndPalms,
        StatValue swordMethod,
        StatValue bladeMethod,
        StatValue longWeapon,
        StatValue esotericWeapon,
        StatValue archery,
        String roomName
) {
    public record StatValue(int base, int bonus) {
        public int total() {
            return base + bonus;
        }
    }
}
