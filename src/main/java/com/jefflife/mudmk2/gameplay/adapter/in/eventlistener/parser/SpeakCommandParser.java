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
            String fullMessage = content.substring(0, content.length() - 2);
            String target = null;

            String[] words = fullMessage.split(" ");
            if (words.length > 1) {
                target = words[0];
            }

            SpeakCommand command = new SpeakCommand(userId, target, fullMessage);
            logger.debug("Parsed SpeakCommand: {}", command);
            return command;
        }
        return null;
    }
}
