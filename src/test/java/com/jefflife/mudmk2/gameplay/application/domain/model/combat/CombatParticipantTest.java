package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class CombatParticipantTest {
    @DisplayName("PlayerCharacter 참가자 객체를 생성할 수 있다")
    @Test
    void createParticipant() {
        PlayerCharacter playerCharacter = new PlayerCharacter(UUID.randomUUID(),
                BaseCharacter.builder().build(),
                PlayableCharacter.builder().build(),
                1L,
                "testUser",
                CharacterClass.WARRIOR,
                true,
                LocalDateTime.now());
        CombatParticipant participant = new CombatParticipant(playerCharacter);
        assertThat(participant.getParticipant()).isEqualTo(playerCharacter);
        assertThat(participant.getAggroScore()).isEqualTo(0);
        assertThat(participant.isDefeated()).isFalse();
    }

    @DisplayName("NPC 참가자 객체를 생성할 수 있다")
    @Test
    void createNpcParticipant() {
        NonPlayerCharacter npc = new NonPlayerCharacter(
                UUID.randomUUID(),
                BaseCharacter.builder().build(),
                PlayableCharacter.builder().build(),
                "testPersona",
                NPCType.MERCHANT,
                Map.of("greeting", "Hello!"),
                1L,
                false
        );
        CombatParticipant participant = new CombatParticipant(npc);
        assertThat(participant.getParticipant()).isEqualTo(npc);
        assertThat(participant.getAggroScore()).isEqualTo(0);
        assertThat(participant.isDefeated()).isFalse();
    }

    @DisplayName("Monster 참가자 객체를 생성할 수 있다")
    @Test
    void createMonsterParticipant() {
        Monster monster = Monster.createFromType(MonsterType.builder().build(), 1, 1L);
        CombatParticipant participant = new CombatParticipant(monster);
        assertThat(participant.getParticipant()).isEqualTo(monster);
        assertThat(participant.getAggroScore()).isEqualTo(0);
        assertThat(participant.isDefeated()).isFalse();
    }
}