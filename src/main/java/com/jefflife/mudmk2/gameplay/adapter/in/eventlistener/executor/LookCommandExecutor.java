package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.LookCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.LookUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Executor for look commands.
 */
@Component
public class LookCommandExecutor implements CommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(LookCommandExecutor.class);
    private final LookUseCase lookUseCase;

    public LookCommandExecutor(LookUseCase lookUseCase) {
        this.lookUseCase = lookUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof LookCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof LookCommand lookCommand)) {
            throw new IllegalArgumentException("Command must be a LookCommand");
        }

        if (lookUseCase != null) {
            lookUseCase.look(lookCommand);
        } else {
            logger.info("Executed LookCommand: {}", lookCommand);
        }
    }
}