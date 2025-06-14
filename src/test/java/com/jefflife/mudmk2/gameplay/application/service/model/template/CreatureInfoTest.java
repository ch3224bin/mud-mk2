package com.jefflife.mudmk2.gameplay.application.service.model.template;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreatureInfoTest {

    @Test
    void getDescription_shouldReturnDeadDescription_whenStateIsDead() {
        // Given
        CreatureInfo creatureInfo = new CreatureInfo("테스트 몬스터", CharacterState.DEAD);

        // When
        String description = creatureInfo.getDescription();

        // Then
        assertEquals("테스트 몬스터의 시체가 있습니다", description);
    }

    @Test
    void getDescription_shouldReturnCombatDescription_whenStateIsCombat() {
        // Given
        CreatureInfo creatureInfo = new CreatureInfo("테스트 몬스터", CharacterState.COMBAT);

        // When
        String description = creatureInfo.getDescription();

        // Then
        assertEquals("테스트 몬스터이 싸우고 있습니다", description);
    }

    @Test
    void getDescription_shouldReturnNormalDescription_whenStateIsNormal() {
        // Given
        CreatureInfo creatureInfo = new CreatureInfo("테스트 몬스터", CharacterState.NORMAL);

        // When
        String description = creatureInfo.getDescription();

        // Then
        assertEquals("테스트 몬스터이 서있습니다", description);
    }
}