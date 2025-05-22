package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.RecruitCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for recruit commands.
 * Example: "소연 합류"
 */
@Component
public class RecruitCommandParser extends AbstractCommandParser {
    private static final Pattern RECRUIT_PATTERN = Pattern.compile("(\\S+) 합류");

    @Override
    protected Command parseCommand(final Long userId, final String content) {
        Matcher matcher = RECRUIT_PATTERN.matcher(content);
        if (matcher.matches()) {
            String target = matcher.group(1);
            RecruitCommand command = new RecruitCommand(userId, target);
            logger.debug("Parsed RecruitCommand: {}", command);
            return command;
        }
        return null;
    }
}
