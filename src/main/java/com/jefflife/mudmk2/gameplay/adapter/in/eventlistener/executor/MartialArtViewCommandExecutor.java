package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.MartialArtViewCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.MartialArtViewUseCase;
import org.springframework.stereotype.Component;

@Component
public class MartialArtViewCommandExecutor implements CommandExecutor {
    private final MartialArtViewUseCase useCase;

    public MartialArtViewCommandExecutor(MartialArtViewUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof MartialArtViewCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof MartialArtViewCommand viewCommand)) {
            throw new IllegalArgumentException("Command must be a MartialArtViewCommand");
        }
        useCase.showMartialArts(viewCommand);
    }
}
