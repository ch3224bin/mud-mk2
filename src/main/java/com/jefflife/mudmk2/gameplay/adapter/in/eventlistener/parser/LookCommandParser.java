package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.LookCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for look commands.
 * Examples:
 * - "봐" (look around)
 * - "동 봐" (look east)
 * - "철검 봐" (look at item, index 1)
 * - "철검 2 봐" (look at 2nd 철검)
 */
@Component
public class LookCommandParser extends AbstractCommandParser {
    private static final Pattern LOOK_WITH_TARGET_PATTERN =
            Pattern.compile("(\\S+?)(?:\\s+(\\d+))?\\s+봐");
    private static final Pattern LOOK_WITHOUT_TARGET_PATTERN = Pattern.compile("봐");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher targetMatcher = LOOK_WITH_TARGET_PATTERN.matcher(content);
        if (targetMatcher.matches()) {
            String target = targetMatcher.group(1);
            int index = targetMatcher.group(2) != null ? Integer.parseInt(targetMatcher.group(2)) : 1;
            LookCommand command = new LookCommand(userId, target, index);
            logger.debug("Parsed LookCommand with target: {}", command);
            return command;
        }

        Matcher simpleMatcher = LOOK_WITHOUT_TARGET_PATTERN.matcher(content);
        if (simpleMatcher.matches()) {
            LookCommand command = new LookCommand(userId, null, 0);
            logger.debug("Parsed simple LookCommand: {}", command);
            return command;
        }

        return null;
    }
}
