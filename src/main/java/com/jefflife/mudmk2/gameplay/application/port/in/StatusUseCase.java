package com.jefflife.mudmk2.gameplay.application.port.in;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.StatusCommand;

/**
 * Use case for handling status commands in the game.
 */
public interface StatusUseCase {
    /**
     * Processes a status command.
     * 
     * @param command the status command to process
     */
    void showStatus(StatusCommand command);
}