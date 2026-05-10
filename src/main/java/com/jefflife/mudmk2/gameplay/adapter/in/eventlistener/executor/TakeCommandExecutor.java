package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.TakeCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.TakeUseCase;
import org.springframework.stereotype.Component;

@Component
public class TakeCommandExecutor implements CommandExecutor {
    private final TakeUseCase takeUseCase;

    public TakeCommandExecutor(TakeUseCase takeUseCase) {
        this.takeUseCase = takeUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof TakeCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof TakeCommand takeCommand)) {
            throw new IllegalArgumentException("Command must be a TakeCommand");
        }
        takeUseCase.take(takeCommand);
    }
}
