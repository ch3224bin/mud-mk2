package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.MoveCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for move commands.
 * Example: "동" (East), "서" (West), "남" (South), "북" (North), "위" (Up), "아래" (Down)
 */
@Component
public class MoveCommandParser extends AbstractCommandParser {
    private static final Pattern MOVE_PATTERN = Pattern.compile("(동|서|남|북|위|아래)");
    
    @Override
    protected Command parseCommand(String sender, String content) {
        Matcher matcher = MOVE_PATTERN.matcher(content);
        if (matcher.matches()) {
            String direction = matcher.group(1);
            MoveCommand command = new MoveCommand(sender, direction);
            logger.debug("Parsed MoveCommand: {}", command);
            return command;
        }
        return null;
    }
}