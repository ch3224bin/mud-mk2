package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.LockCommand;

/**
 * Use case for handling lock commands in the game.
 */
public interface LockUseCase {
    /**
     * Processes a lock command.
     * 
     * @param command the lock command to process
     */
    void lock(LockCommand command);
}