package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.UnequipCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.UnequipUseCase;
import org.springframework.stereotype.Component;

@Component
public class UnequipCommandExecutor implements CommandExecutor {
    private final UnequipUseCase unequipUseCase;

    public UnequipCommandExecutor(UnequipUseCase unequipUseCase) {
        this.unequipUseCase = unequipUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof UnequipCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof UnequipCommand unequipCommand)) {
            throw new IllegalArgumentException("Command must be an UnequipCommand");
        }
        unequipUseCase.unequip(unequipCommand);
    }
}
