package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.SpeakCommand;

/**
 * Use case for handling speak commands in the game.
 */
public interface SpeakUseCase {
    /**
     * Processes a speak command.
     * 
     * @param command the speak command to process
     */
    void speak(SpeakCommand command);
}