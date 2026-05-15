package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionary;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.UnequipCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UnequipCommandParser extends AbstractCommandParser {
    private static final Pattern UNEQUIP_PATTERN =
            Pattern.compile("(\\S+?)\\s+(" + CommandDictionary.UNEQUIP.toRegex() + ")");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = UNEQUIP_PATTERN.matcher(content);
        if (matcher.matches()) {
            return new UnequipCommand(userId, matcher.group(1));
        }
        return null;
    }
}
