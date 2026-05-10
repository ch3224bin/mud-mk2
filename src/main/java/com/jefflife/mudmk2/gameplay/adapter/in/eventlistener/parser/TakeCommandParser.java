package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionary;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.TakeCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TakeCommandParser extends AbstractCommandParser {
    private static final Pattern TAKE_PATTERN =
            Pattern.compile("(\\S+?)(?:\\s+(\\d+))?\\s+(" + CommandDictionary.TAKE.toRegex() + ")");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = TAKE_PATTERN.matcher(content);
        if (matcher.matches()) {
            String itemName = matcher.group(1);
            int index = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 1;
            return new TakeCommand(userId, itemName, index);
        }
        return null;
    }
}
