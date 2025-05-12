package com.jefflife.mudmk2.gameplay.application.port.in;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.CombineCommand;

/**
 * Use case for handling combine commands in the game.
 */
public interface CombineUseCase {
    /**
     * Processes a combine command.
     * 
     * @param command the combine command to process
     */
    void combine(CombineCommand command);
}