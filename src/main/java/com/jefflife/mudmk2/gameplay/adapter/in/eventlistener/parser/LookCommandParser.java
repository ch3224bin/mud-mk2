package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.LookCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for look commands.
 * Examples: 
 * - "몬스터 봐" (Look at monster)
 * - "봐" (Look around)
 */
@Component
public class LookCommandParser extends AbstractCommandParser {
    private static final Pattern LOOK_WITH_TARGET_PATTERN = Pattern.compile("(\\S+) 봐");
    private static final Pattern LOOK_WITHOUT_TARGET_PATTERN = Pattern.compile("봐");

    @Override
    protected Command parseCommand(Long userId, String content) {
        // Check for look with target pattern
        Matcher targetMatcher = LOOK_WITH_TARGET_PATTERN.matcher(content);
        if (targetMatcher.matches()) {
            String target = targetMatcher.group(1);
            LookCommand command = new LookCommand(userId, target);
            logger.debug("Parsed LookCommand with target: {}", command);
            return command;
        }

        // Check for simple look pattern
        Matcher simpleMatcher = LOOK_WITHOUT_TARGET_PATTERN.matcher(content);
        if (simpleMatcher.matches()) {
            LookCommand command = new LookCommand(userId, null);
            logger.debug("Parsed simple LookCommand: {}", command);
            return command;
        }

        return null;
    }
}
