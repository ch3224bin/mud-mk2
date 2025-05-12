package com.jefflife.mudmk2.gameplay.application.port.in;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.BuyCommand;

/**
 * Use case for handling buy commands in the game.
 */
public interface BuyUseCase {
    /**
     * Processes a buy command.
     * 
     * @param command the buy command to process
     */
    void buy(BuyCommand command);
}