package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.SpeakCommand;
import org.springframework.stereotype.Component;

/**
 * Parser for speak commands.
 * Example: "안녕하세요 말" (Hello speak)
 */
@Component
public class SpeakCommandParser extends AbstractCommandParser {
    
    @Override
    protected Command parseCommand(Long userId, String content) {
        if (content.endsWith(" 말")) {
            String message = content.substring(0, content.length() - 2);
            SpeakCommand command = new SpeakCommand(userId, message);
            logger.debug("Parsed SpeakCommand: {}", command);
            return command;
        }
        return null;
    }
}