package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionary;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EatCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EatCommandParser extends AbstractCommandParser {
    private static final Pattern EAT_PATTERN =
            Pattern.compile("(\\S+?)(?:\\s+(\\d+))?\\s+(" + CommandDictionary.EAT.toRegex() + ")");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = EAT_PATTERN.matcher(content);
        if (matcher.matches()) {
            String itemName = matcher.group(1);
            int index = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 1;
            return new EatCommand(userId, itemName, index);
        }
        return null;
    }
}
