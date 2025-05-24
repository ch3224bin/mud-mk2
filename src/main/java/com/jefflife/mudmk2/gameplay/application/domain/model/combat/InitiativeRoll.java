package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

public record InitiativeRoll(
        int diceRoll,
        int dexBonus,
        int skillBonus,
        int miscBonus
) {
    public int total() {
        return diceRoll + dexBonus + skillBonus + miscBonus;
    }
}
