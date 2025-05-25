package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class CombatTest {

    @DisplayName("전투 시작을 알린다")
    @Test
    void startCombat() {
        // given
        Combat combat = new Combat(UUID.randomUUID(), new CombatGroup(CombatGroupType.ALLY), new CombatGroup(CombatGroupType.ENEMY), stats -> null);

        // when
        combat.start();

        // then

    }
}