package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ATBCombatTest {

    private ATBCombatParticipant attacker;
    private ATBCombatParticipant defender;

    @BeforeEach
    void setUp() {
        attacker = new ATBCombatParticipant(
            createMonster("공격자", 15, 10, 14, 10, 8, 10, 8, 20),
            CombatGroupType.ALLY, 20, 0, 0
        );
        defender = new ATBCombatParticipant(
            createMonster("방어자", 10, 12, 8, 8, 12, 8, 0, 15),
            CombatGroupType.ENEMY, 15, 5, 5
        );
    }

    @DisplayName("SPEED_DIVISOR=10, 틱을 반복하면 빠른 참가자가 먼저 행동한다")
    @Test
    void fastParticipantActsFirst() {
        ATBCombat combat = new ATBCombat(UUID.randomUUID(), List.of(attacker, defender),
            new FixedRandomGenerator(10));
        combat.start();

        CombatActionResult result = CombatActionResult.NOT_ACTED;
        for (int i = 0; i < 200 && !result.isActed(); i++) {
            result = combat.tick();
        }

        assertThat(result.isActed()).isTrue();
        assertThat(result.getLogs()).isNotEmpty();
        assertThat(result.getLogs().get(0).attackerName()).isEqualTo("공격자");
    }

    @DisplayName("전투 종료 — 방어자 HP가 0이 되면 FINISHED")
    @Test
    void combatFinishesWhenDefenderDefeated() {
        ATBCombat combat = new ATBCombat(UUID.randomUUID(), List.of(attacker, defender),
            new FixedRandomGenerator(10));
        combat.start();

        for (int i = 0; i < ATBCombat.MAX_TICKS && !combat.isFinished(); i++) {
            combat.tick();
        }

        assertThat(combat.isFinished()).isTrue();
    }

    @DisplayName("MAX_TICKS 초과 시 강제 종료")
    @Test
    void forcesFinishAtMaxTicks() {
        ATBCombatParticipant weakAttacker = new ATBCombatParticipant(
            createMonster("약한공격자", 1, 100, 10, 1, 1, 1, 0, 1),
            CombatGroupType.ALLY, 1, 0, 0
        );
        ATBCombatParticipant toughDefender = new ATBCombatParticipant(
            createMonster("강한방어자", 1, 100, 10, 1, 1, 1, 0, 1),
            CombatGroupType.ENEMY, 1, 0, 0
        );
        ATBCombat combat = new ATBCombat(UUID.randomUUID(),
            List.of(weakAttacker, toughDefender), new FixedRandomGenerator(10));
        combat.start();

        for (int i = 0; i < ATBCombat.MAX_TICKS + 10; i++) {
            combat.tick();
        }

        assertThat(combat.isFinished()).isTrue();
    }

    private Monster createMonster(String name, int vigor, int physique, int agility,
                                   int intellect, int will, int meridian, int swordMethod,
                                   int weaponBase) {
        MonsterType type = MonsterType.builder()
            .name(name).description("테스트").baseHp(physique * 10)
            .baseMp(meridian * 5).baseVigor(vigor).basePhysique(physique)
            .baseAgility(agility).baseIntellect(intellect).baseWill(will)
            .baseMeridian(meridian).baseSwordMethod(swordMethod).build();
        return Monster.createFromType(type, 1, 1L);
    }
}
