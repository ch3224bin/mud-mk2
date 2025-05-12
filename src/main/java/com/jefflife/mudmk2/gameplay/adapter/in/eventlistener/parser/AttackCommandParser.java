package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.AttackCommand;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for attack commands.
 * Example: "몬스터 때려" (Attack monster)
 */
@Component
public class AttackCommandParser extends AbstractCommandParser {
    private static final Pattern ATTACK_PATTERN = Pattern.compile("(\\S+) 때려");
    
    @Override
    protected Command parseCommand(String sender, String content) {
        Matcher matcher = ATTACK_PATTERN.matcher(content);
        if (matcher.matches()) {
            String target = matcher.group(1);
            AttackCommand command = new AttackCommand(sender, target);
            logger.debug("Parsed AttackCommand: {}", command);
            return command;
        }
        return null;
    }
}