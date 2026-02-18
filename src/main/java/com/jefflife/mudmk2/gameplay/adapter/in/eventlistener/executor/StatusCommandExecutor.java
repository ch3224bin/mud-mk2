package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.StatusCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.StatusUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Executor for status commands.
 */
@Component
public class StatusCommandExecutor implements CommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(StatusCommandExecutor.class);
    private final StatusUseCase statusUseCase;

    @Autowired
    public StatusCommandExecutor(@Autowired(required = false) StatusUseCase statusUseCase) {
        this.statusUseCase = statusUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof StatusCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof StatusCommand statusCommand)) {
            throw new IllegalArgumentException("Command must be a StatusCommand");
        }

        if (statusUseCase != null) {
            statusUseCase.showStatus(statusCommand);
        } else {
            logger.info("Executed StatusCommand: {}", statusCommand);
        }
    }
}