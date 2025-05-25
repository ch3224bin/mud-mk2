package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.AttackCommand;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.port.in.AttackUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Executor for attack commands.
 */
@Component
public class AttackCommandExecutor implements CommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AttackCommandExecutor.class);
    private final AttackUseCase attackUseCase;

    public AttackCommandExecutor(AttackUseCase attackUseCase) {
        this.attackUseCase = attackUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof AttackCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof AttackCommand attackCommand)) {
            throw new IllegalArgumentException("Command must be an AttackCommand");
        }

        attackUseCase.attack(attackCommand);
        logger.info("Executed attack command: {}", attackCommand);
    }
}