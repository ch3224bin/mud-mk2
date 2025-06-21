package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.StatusCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for status commands.
 * Example: "상태창" (Status window)
 */
@Component
public class StatusCommandParser extends AbstractCommandParser {
    private static final Pattern STATUS_PATTERN = Pattern.compile("상태창|상태");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = STATUS_PATTERN.matcher(content);
        if (matcher.matches()) {
            StatusCommand command = new StatusCommand(userId);
            logger.debug("Parsed StatusCommand: {}", command);
            return command;
        }
        return null;
    }
}
