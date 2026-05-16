package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EatCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.EatUseCase;
import org.springframework.stereotype.Component;

@Component
public class EatCommandExecutor implements CommandExecutor {
    private final EatUseCase eatUseCase;

    public EatCommandExecutor(EatUseCase eatUseCase) {
        this.eatUseCase = eatUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof EatCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof EatCommand eatCommand)) {
            throw new IllegalArgumentException("Command must be an EatCommand");
        }
        eatUseCase.eat(eatCommand);
    }
}
