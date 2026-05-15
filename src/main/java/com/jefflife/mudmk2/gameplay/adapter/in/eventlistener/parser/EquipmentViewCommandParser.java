package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionary;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EquipmentViewCommandParser extends AbstractCommandParser {
    private static final Pattern VIEW_PATTERN =
            Pattern.compile("(" + CommandDictionary.EQUIPMENT_VIEW.toRegex() + ")");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = VIEW_PATTERN.matcher(content);
        if (matcher.matches()) {
            return new EquipmentViewCommand(userId);
        }
        return null;
    }
}
