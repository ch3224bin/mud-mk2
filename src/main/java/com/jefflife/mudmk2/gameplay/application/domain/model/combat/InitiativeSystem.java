package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;

public class InitiativeSystem {
    private final RandomGenerator randomGenerator;

    public InitiativeSystem(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public InitiativeRoll rollInitiative(CharacterStats stats) {
        int diceRoll = rollD20();
        int dexBonus = stats.getDexterityModifier();
        int skillBonus = 0;
        int miscBonus = 0;
        return new InitiativeRoll(
                diceRoll,
                dexBonus,
                skillBonus,
                miscBonus
        );
    }

    // 1d20 굴리기
    private int rollD20() {
        return randomGenerator.nextInt(20) + 1;
    }
}
