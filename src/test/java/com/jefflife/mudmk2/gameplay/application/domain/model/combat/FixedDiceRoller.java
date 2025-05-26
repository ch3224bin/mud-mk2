package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

/**
 * A test implementation of DiceRoller that returns fixed values.
 * This allows for deterministic testing of combat scenarios.
 */
public class FixedDiceRoller implements DiceRoller {
    private final int[] values;
    private int currentIndex = 0;

    /**
     * Creates a FixedDiceRoller with the specified sequence of values.
     * Each call to roll() will return the next value in the sequence.
     *
     * @param values The sequence of values to return
     */
    public FixedDiceRoller(int... values) {
        this.values = values;
    }

    @Override
    public int roll(int numDice, int diceSides) {
        // Ignore the parameters and return the next fixed value
        if (currentIndex >= values.length) {
            // If we've used all values, start over
            currentIndex = 0;
        }
        return values[currentIndex++];
    }
}