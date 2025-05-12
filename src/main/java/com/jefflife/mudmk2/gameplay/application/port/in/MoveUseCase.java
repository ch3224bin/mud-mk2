package com.jefflife.mudmk2.gameplay.application.port.in;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.MoveCommand;

/**
 * Use case for handling move commands in the game.
 */
public interface MoveUseCase {
    /**
     * Processes a move command.
     * 
     * @param command the move command to process
     */
    void move(MoveCommand command);
}