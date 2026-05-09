package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CombatFormulasTest {

    private CharacterStats makeStats(int vigor, int physique, int agility, int intellect, int will, int meridian,
                                      int lightStep, int swordMethod) {
        return new CharacterStats(100, 50, 80, vigor, physique, agility, intellect, will, meridian,
                0, 0, lightStep, 0, swordMethod, 0, 0, 0, 0);
    }

    @Test
    void initiativeSpeed_민첩14_경공5() {
        CharacterStats stats = makeStats(12, 10, 14, 10, 8, 10, 5, 8);
        assertThat(CombatFormulas.initiativeSpeed(stats)).isCloseTo(16.5, within(0.001));
    }

    @Test
    void accuracy_지력10_검법8_장비0() {
        CharacterStats stats = makeStats(12, 10, 14, 10, 8, 10, 5, 8);
        assertThat(CombatFormulas.accuracy(stats, 8, 0)).isCloseTo(62.0, within(0.001));
    }

    @Test
    void evasion_민첩14_경공5() {
        CharacterStats stats = makeStats(12, 10, 14, 10, 8, 10, 5, 8);
        assertThat(CombatFormulas.evasion(stats)).isCloseTo(13.2, within(0.001));
    }

    @Test
    void evasionRate_회피가낮으면0() {
        assertThat(CombatFormulas.evasionRate(13.2, 62.0)).isEqualTo(0);
    }

    @Test
    void evasionRate_회피가높으면최대75() {
        assertThat(CombatFormulas.evasionRate(200.0, 50.0)).isEqualTo(75);
    }

    @Test
    void evasionRate_중간값() {
        assertThat(CombatFormulas.evasionRate(80.0, 50.0)).isEqualTo(30);
    }

    @Test
    void baseDamage_무기20_검법8_근력15_랜덤고정10() {
        CharacterStats stats = makeStats(15, 10, 14, 10, 8, 10, 5, 8);
        // base = 20 × (1 + 8×0.008) + 15×0.3 = 25.78
        // randomFactor = 0.9 + 10/100 = 1.0 → (int)(25.78 × 1.0) = 25
        FixedRandomGenerator rng = new FixedRandomGenerator(10);
        assertThat(CombatFormulas.baseDamage(stats, 20, 8, rng)).isEqualTo(25);
    }

    @Test
    void critRate_근력15() {
        CharacterStats stats = makeStats(15, 10, 14, 10, 8, 10, 5, 8);
        assertThat(CombatFormulas.critRate(stats)).isEqualTo(4);
    }

    @Test
    void armor_장비5_의지8() {
        CharacterStats stats = makeStats(12, 10, 14, 10, 8, 10, 5, 8);
        assertThat(CombatFormulas.armor(stats, 5)).isEqualTo(8); // 5 + (int)(8×0.4)=8
    }

    @Test
    void armorPct_의지8_검법8_장비0_상한미달() {
        CharacterStats stats = makeStats(12, 10, 14, 10, 8, 10, 5, 8);
        assertThat(CombatFormulas.armorPct(stats, 0, 8)).isEqualTo(2);
        // (int)(0 + 8×0.2 + 8×0.15) = (int)2.8 = 2
    }

    @Test
    void armorPct_상한75초과시75() {
        CharacterStats stats = makeStats(12, 10, 14, 10, 300, 10, 5, 200);
        assertThat(CombatFormulas.armorPct(stats, 50, 200)).isEqualTo(75);
    }

    @Test
    void applyDefense_피해30_방어8_방어율10() {
        // 30 × (1 - 0.10) = 27.0 → 27 - 8 = 19
        assertThat(CombatFormulas.applyDefense(30, 8, 10)).isEqualTo(19);
    }

    @Test
    void applyDefense_최솟값1() {
        assertThat(CombatFormulas.applyDefense(1, 100, 75)).isEqualTo(1);
    }
}
