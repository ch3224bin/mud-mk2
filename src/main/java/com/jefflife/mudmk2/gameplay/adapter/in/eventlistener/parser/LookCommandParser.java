package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.LookCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for look commands.
 * Example: "몬스터 봐" (Look at monster)
 */
@Component
public class LookCommandParser extends AbstractCommandParser {
    private static final Pattern LOOK_PATTERN = Pattern.compile("(\\S+) 봐");
    
    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = LOOK_PATTERN.matcher(content);
        if (matcher.matches()) {
            String target = matcher.group(1);
            LookCommand command = new LookCommand(userId, target);
            logger.debug("Parsed LookCommand: {}", command);
            return command;
        }
        return null;
    }
}