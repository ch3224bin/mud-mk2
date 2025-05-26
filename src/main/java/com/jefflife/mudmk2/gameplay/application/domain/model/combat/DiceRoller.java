package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

/**
 * Interface for rolling dice in the combat system.
 * This allows for dependency injection and easier testing.
 */
public interface DiceRoller {
    /**
     * Rolls a specified number of dice with the given number of sides.
     *
     * @param numDice The number of dice to roll
     * @param diceSides The number of sides on each die
     * @return The total of all dice rolls
     */
    int roll(int numDice, int diceSides);
}