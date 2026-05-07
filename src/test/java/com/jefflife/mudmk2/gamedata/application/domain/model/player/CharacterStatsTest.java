package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CharacterStatsTest {

    private CharacterStats statsWithPhysique(int physique, int specialTechnique) {
        return new CharacterStats(
            100, 50, 80,                              // hp, mp, ap
            0, physique, 0, 0, 0, 0,                 // vigor, physique, agility, intellect, will, meridian
            0, specialTechnique, 0, 0, 0, 0, 0, 0, 0 // innerPower, specialTechnique, lightStep, fistsAndPalms, swordMethod, bladeMethod, longWeapon, esotericWeapon, archery
        );
    }

    private CharacterStats statsWithMeridian(int meridian, int innerPower) {
        return new CharacterStats(
            100, 50, 80,
            0, 0, 0, 0, 0, meridian,
            innerPower, 0, 0, 0, 0, 0, 0, 0, 0
        );
    }

    private CharacterStats statsWithAgility(int agility) {
        return new CharacterStats(
            100, 50, 80,
            0, 0, agility, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0
        );
    }

    @Test
    void maxHp_isComputedFromPhysiqueAndSpecialTechnique() {
        CharacterStats stats = statsWithPhysique(50, 20);
        // 50 × 10 + 20 × 3 = 560
        assertThat(stats.maxHp()).isEqualTo(560);
    }

    @Test
    void maxHp_withZeroSpecialTechnique_isPhysiqueOnly() {
        CharacterStats stats = statsWithPhysique(30, 0);
        assertThat(stats.maxHp()).isEqualTo(300);
    }

    @Test
    void maxMp_isComputedFromMeridianAndInnerPower() {
        CharacterStats stats = statsWithMeridian(40, 30);
        // 40 × 5 + 30 × 3 = 290
        assertThat(stats.maxMp()).isEqualTo(290);
    }

    @Test
    void maxAp_isComputedFromAgility() {
        CharacterStats stats = statsWithAgility(60);
        // 60 × 8 = 480
        assertThat(stats.maxAp()).isEqualTo(480);
    }
}
