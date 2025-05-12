package com.jefflife.mudmk2.gameplay.application.port.in;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.AttackCommand;

/**
 * Use case for handling attack commands in the game.
 */
public interface AttackUseCase {
    /**
     * Processes an attack command.
     * 
     * @param command the attack command to process
     */
    void attack(AttackCommand command);
}