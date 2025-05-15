package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.InvalidCommand;
import com.jefflife.mudmk2.gameplay.application.port.in.InvalidCommandUseCase;
import org.springframework.stereotype.Component;

/**
 * CommandExecutor 구현체로 잘못된 명령어를 처리합니다.
 */
@Component
public class InvalidCommandExecutor implements CommandExecutor {

    private final InvalidCommandUseCase invalidCommandUseCase;

    public InvalidCommandExecutor(InvalidCommandUseCase invalidCommandUseCase) {
        this.invalidCommandUseCase = invalidCommandUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof InvalidCommand;
    }

    @Override
    public void execute(Command command) {
        InvalidCommand invalidCommand = (InvalidCommand) command;
        invalidCommandUseCase.notifyInvalidCommand(
            invalidCommand.username(),
            invalidCommand.originalContent()
        );
    }
}
