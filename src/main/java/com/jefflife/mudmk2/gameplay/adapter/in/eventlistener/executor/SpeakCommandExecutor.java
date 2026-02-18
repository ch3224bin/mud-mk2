package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.SpeakCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.SpeakUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Executor for speak commands.
 */
@Component
public class SpeakCommandExecutor implements CommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SpeakCommandExecutor.class);
    private final SpeakUseCase speakUseCase;

    @Autowired
    public SpeakCommandExecutor(SpeakUseCase speakUseCase) {
        this.speakUseCase = speakUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof SpeakCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof SpeakCommand speakCommand)) {
            throw new IllegalArgumentException("Command must be a SpeakCommand");
        }

        if (speakUseCase != null) {
            speakUseCase.speak(speakCommand);
        } else {
            logger.info("Executed SpeakCommand: {}", speakCommand);
        }
    }
}