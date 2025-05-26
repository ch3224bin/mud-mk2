package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import java.util.Random;

/**
 * Default implementation of DiceRoller that uses java.util.Random
 * to generate random dice rolls.
 */
public class RandomDiceRoller implements DiceRoller {
    private final Random random;

    public RandomDiceRoller() {
        this.random = new Random();
    }

    /**
     * Constructor that allows injecting a specific Random instance
     * (useful for seeded testing)
     */
    public RandomDiceRoller(Random random) {
        this.random = random;
    }

    @Override
    public int roll(int numDice, int diceSides) {
        int total = 0;
        for (int i = 0; i < numDice; i++) {
            total += random.nextInt(diceSides) + 1;
        }
        return total;
    }
}