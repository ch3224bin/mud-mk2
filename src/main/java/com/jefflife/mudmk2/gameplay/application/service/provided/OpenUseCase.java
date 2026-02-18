package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.OpenCommand;

/**
 * Use case for handling open commands in the game.
 */
public interface OpenUseCase {
    /**
     * Processes an open command.
     * 
     * @param command the open command to process
     */
    void open(OpenCommand command);
}