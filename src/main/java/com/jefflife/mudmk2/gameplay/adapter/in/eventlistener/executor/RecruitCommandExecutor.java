package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.RecruitCommand;
import com.jefflife.mudmk2.gameplay.application.port.in.RecruitUseCase;
import org.springframework.stereotype.Component;

@Component
public class RecruitCommandExecutor implements CommandExecutor {
    private final RecruitUseCase recruitUseCase;

    public RecruitCommandExecutor(final RecruitUseCase recruitUseCase) {
        this.recruitUseCase = recruitUseCase;
    }

    @Override
    public boolean canExecute(final Command command) {
        return command instanceof RecruitCommand;
    }

    @Override
    public void execute(final Command command) {
        if (!(command instanceof RecruitCommand recruitCommand)) {
            throw new IllegalArgumentException("Command must be a RecruitCommand");
        }

        recruitUseCase.recruit(recruitCommand);
    }
}
