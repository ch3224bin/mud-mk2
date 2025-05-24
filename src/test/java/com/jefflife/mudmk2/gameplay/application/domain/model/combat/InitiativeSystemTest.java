package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InitiativeSystemTest {

    @Test
    @DisplayName("주어진 캐릭터 스탯으로 이니셔티브 롤 결과를 올바르게 계산한다")
    void rollInitiative_WithFixedDiceRoll_CalculatesCorrectTotal() {
        // given
        int fixedDiceRoll = 15; // 고정된 주사위 값
        CharacterStats stats = new CharacterStats(
                100, 100, // hp, maxHp
                50, 50,   // mp, maxMp
                10,       // str
                14,       // dex (민첩 수정치 +2)
                10,       // con
                10,       // int
                10,       // pow
                10        // cha
        );

        // 항상 15(0-19 기준에서 14)가 나오도록 고정된 값을 반환하는 RandomGenerator 구현
        RandomGenerator fixedRandomGenerator = getFixedRandomGenerator(fixedDiceRoll);
        InitiativeSystem initiativeSystem = new InitiativeSystem(fixedRandomGenerator);

        // when
        InitiativeRoll result = initiativeSystem.rollInitiative(stats);

        // then
        assertEquals(fixedDiceRoll, result.diceRoll());
        assertEquals(2, result.dexBonus()); // dex 14 -> 수정치 +2
        assertEquals(0, result.skillBonus());
        assertEquals(0, result.miscBonus());
        assertEquals(17, result.total()); // 15(주사위) + 2(덱스 보너스) + 0 + 0 = 17
    }

    @Test
    @DisplayName("음수 민첩 수정치를 가진 캐릭터의 이니셔티브 롤을 올바르게 계산한다")
    void rollInitiative_WithNegativeDexModifier_CalculatesCorrectTotal() {
        // given
        int fixedDiceRoll = 10; // 고정된 주사위 값
        CharacterStats stats = new CharacterStats(
                100, 100, // hp, maxHp
                50, 50,   // mp, maxMp
                10,       // str
                8,        // dex (민첩 수정치 -1)
                10,       // con
                10,       // int
                10,       // pow
                10        // cha
        );

        // 항상 10(0-19 기준에서 9)가 나오도록 고정된 값을 반환하는 RandomGenerator 구현
        RandomGenerator fixedRandomGenerator = getFixedRandomGenerator(fixedDiceRoll);
        InitiativeSystem initiativeSystem = new InitiativeSystem(fixedRandomGenerator);

        // when
        InitiativeRoll result = initiativeSystem.rollInitiative(stats);

        // then
        assertEquals(fixedDiceRoll, result.diceRoll());
        assertEquals(-1, result.dexBonus()); // dex 8 -> 수정치 -1
        assertEquals(0, result.skillBonus());
        assertEquals(0, result.miscBonus());
        assertEquals(9, result.total()); // 10(주사위) - 1(덱스 보너스) + 0 + 0 = 9
    }

    @Test
    @DisplayName("여러 이니셔티브 롤의 주사위 결과가 RandomGenerator에 의해 결정됨을 확인한다")
    void rollInitiative_WithDifferentRandomGenerators_ReturnsExpectedDiceRolls() {
        // given
        CharacterStats stats = new CharacterStats(
                100, 100, 50, 50, 10, 10, 10, 10, 10, 10
        );

        // 최소값(1)이 나오는 RandomGenerator
        RandomGenerator minRandomGenerator = getFixedRandomGenerator(1);
        InitiativeSystem minInitiativeSystem = new InitiativeSystem(minRandomGenerator);

        // 최대값(20)이 나오는 RandomGenerator
        RandomGenerator maxRandomGenerator = getFixedRandomGenerator(20);
        InitiativeSystem maxInitiativeSystem = new InitiativeSystem(maxRandomGenerator);

        // when
        InitiativeRoll minResult = minInitiativeSystem.rollInitiative(stats);
        InitiativeRoll maxResult = maxInitiativeSystem.rollInitiative(stats);

        // then
        assertEquals(1, minResult.diceRoll()); // 최소값 1
        assertEquals(20, maxResult.diceRoll()); // 최대값 20

        // dex 10은 수정치 0
        assertEquals(0, minResult.dexBonus());
        assertEquals(0, maxResult.dexBonus());

        // 총합 확인
        assertEquals(1, minResult.total()); // 1 + 0 + 0 + 0 = 1
        assertEquals(20, maxResult.total()); // 20 + 0 + 0 + 0 = 20
    }

    private static RandomGenerator getFixedRandomGenerator(final int fixedDiceRoll) {
        return bound -> {
            return fixedDiceRoll - 1; // 0부터 19 사이의 값으로 변환
        };
    }
}
