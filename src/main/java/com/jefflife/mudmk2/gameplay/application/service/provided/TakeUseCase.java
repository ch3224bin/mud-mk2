package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.TakeCommand;

/**
 * Use case for handling take commands in the game.
 */
public interface TakeUseCase {
    /**
     * Processes a take command.
     * 
     * @param command the take command to process
     */
    void take(TakeCommand command);
}