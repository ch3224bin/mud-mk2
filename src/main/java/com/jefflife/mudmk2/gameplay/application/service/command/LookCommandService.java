package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.LookCommand;
import com.jefflife.mudmk2.gameplay.application.port.in.DisplayRoomInfoUseCase;
import com.jefflife.mudmk2.gameplay.application.port.in.LookUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service implementation for the LookUseCase.
 * Handles look commands in the game.
 */
@Service
public class LookCommandService implements LookUseCase {
    private static final Logger logger = LoggerFactory.getLogger(LookCommandService.class);
    
    private final DisplayRoomInfoUseCase displayRoomInfoUseCase;
    
    public LookCommandService(DisplayRoomInfoUseCase displayRoomInfoUseCase) {
        this.displayRoomInfoUseCase = displayRoomInfoUseCase;
    }
    
    @Override
    public void look(LookCommand command) {
        logger.debug("Processing look command: {}", command);
        
        // If target is null or empty, display room info
        if (command.target() == null || command.target().isEmpty()) {
            logger.debug("No target specified, displaying room info for user: {}", command.userId());
            displayRoomInfoUseCase.displayRoomInfo(command.userId());
            return;
        }
        
        // For future implementation: handle specific targets like "도토리 봐", "사냥꾼 봐"
        logger.info("Look at target '{}' not implemented yet", command.target());
    }
}