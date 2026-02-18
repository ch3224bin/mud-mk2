package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.LookCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.RoomDescriber;
import com.jefflife.mudmk2.gameplay.application.service.provided.LookUseCase;
import com.jefflife.mudmk2.gameplay.application.service.command.look.LookTargetProcessor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service implementation for the LookUseCase.
 * Handles look commands in the game.
 */
@RequiredArgsConstructor
@Service
public class LookCommandService implements LookUseCase {
    private static final Logger logger = LoggerFactory.getLogger(LookCommandService.class);
    
    private final RoomDescriber roomDescriber;
    private final LookTargetProcessor lookTargetProcessor;
    
    @Override
    public void look(LookCommand command) {
        logger.debug("Processing look command: {}", command);
        
        // If target is null or empty, display room info
        if (command.target() == null || command.target().isEmpty()) {
            logger.debug("No target specified, displaying room info for user: {}", command.userId());
            roomDescriber.describe(command.userId());
            return;
        }

        lookTargetProcessor.processLookTarget(command.userId(), command.target());
    }
}