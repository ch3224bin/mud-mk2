package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaseCharacterMoveToTest {

    @Test
    void moveTo_updatesRoomId() {
        BaseCharacter character = BaseCharacter.builder().roomId(100L).build();

        character.moveTo(200L);

        assertThat(character.getRoomId()).isEqualTo(200L);
    }

    @Test
    void moveTo_throwsWhenRoomIdIsNull() {
        BaseCharacter character = BaseCharacter.builder().roomId(100L).build();

        assertThatThrownBy(() -> character.moveTo(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("roomId");
    }

    @Test
    void nonPlayerCharacter_moveTo_delegatesToBaseCharacter() {
        BaseCharacter base = BaseCharacter.builder().roomId(100L).build();
        NonPlayerCharacter npc = new NonPlayerCharacter(
                java.util.UUID.randomUUID(),
                base,
                PlayableCharacter.builder().build(),
                "persona",
                NPCType.MERCHANT,
                new java.util.HashMap<>(),
                100L,
                false
        );

        npc.moveTo(200L);

        assertThat(npc.getCurrentRoomId()).isEqualTo(200L);
    }
}
