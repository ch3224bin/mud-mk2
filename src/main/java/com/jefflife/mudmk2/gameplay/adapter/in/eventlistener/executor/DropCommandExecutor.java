package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.DropCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.DropUseCase;
import org.springframework.stereotype.Component;

@Component
public class DropCommandExecutor implements CommandExecutor {
    private final DropUseCase dropUseCase;

    public DropCommandExecutor(DropUseCase dropUseCase) {
        this.dropUseCase = dropUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof DropCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof DropCommand dropCommand)) {
            throw new IllegalArgumentException("Command must be a DropCommand");
        }
        dropUseCase.drop(dropCommand);
    }
}
