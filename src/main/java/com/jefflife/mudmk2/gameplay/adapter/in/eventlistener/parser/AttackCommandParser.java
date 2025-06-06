package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.AttackCommand;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionary;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for attack commands.
 * Example: "몬스터 때려" (Attack monster)
 */
@Component
public class AttackCommandParser extends AbstractCommandParser {
    private static final Pattern ATTACK_PATTERN = Pattern.compile("(\\S+) (" + CommandDictionary.ATTACK.toRegex() + ")");
    
    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = ATTACK_PATTERN.matcher(content);
        if (matcher.matches()) {
            String target = matcher.group(1);
            AttackCommand command = new AttackCommand(userId, target);
            logger.debug("Parsed AttackCommand: {}", command);
            return command;
        }
        return null;
    }
}