package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.StatusCommand;
import org.springframework.stereotype.Component;

/**
 * Parser for status commands.
 * Example: "상태창" (Status window)
 */
@Component
public class StatusCommandParser extends AbstractCommandParser {

    @Override
    protected Command parseCommand(Long userId, String content) {
        if (content.equals("상태창")) {
            StatusCommand command = new StatusCommand(userId);
            logger.debug("Parsed StatusCommand: {}", command);
            return command;
        }
        return null;
    }
}
