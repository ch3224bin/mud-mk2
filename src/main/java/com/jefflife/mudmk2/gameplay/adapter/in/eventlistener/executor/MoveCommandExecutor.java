package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.MoveCommand;
import com.jefflife.mudmk2.gameplay.application.port.in.MoveUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Executor for move commands.
 */
@Component
public class MoveCommandExecutor implements CommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MoveCommandExecutor.class);
    private final MoveUseCase moveUseCase;

    @Autowired
    public MoveCommandExecutor(@Autowired(required = false) MoveUseCase moveUseCase) {
        this.moveUseCase = moveUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof MoveCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof MoveCommand moveCommand)) {
            throw new IllegalArgumentException("Command must be a MoveCommand");
        }

        if (moveUseCase != null) {
            moveUseCase.move(moveCommand);
        } else {
            logger.info("Executed MoveCommand: {}", moveCommand);
        }
    }
}