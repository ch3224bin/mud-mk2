package com.jefflife.mudmk2.gameplay.application.port.in;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.LookCommand;

/**
 * Use case for handling look commands in the game.
 */
public interface LookUseCase {
    /**
     * Processes a look command.
     * 
     * @param command the look command to process
     */
    void look(LookCommand command);
}