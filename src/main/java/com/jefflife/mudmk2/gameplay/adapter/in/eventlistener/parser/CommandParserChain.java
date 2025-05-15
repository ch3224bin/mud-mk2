package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.InvalidCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Manages a chain of command parsers and delegates parsing to them.
 */
@Component
public class CommandParserChain {
    private static final Logger logger = LoggerFactory.getLogger(CommandParserChain.class);
    private final CommandParser firstParser;

    /**
     * Creates a new CommandParserChain with the given parsers.
     * The parsers are chained in the order they are provided.
     *
     * @param parsers the parsers to chain
     */
    public CommandParserChain(List<CommandParser> parsers) {
        if (parsers.isEmpty()) {
            throw new IllegalArgumentException("At least one parser is required");
        }
        
        // Chain the parsers
        CommandParser first = parsers.getFirst();
        CommandParser current = first;
        
        for (int i = 1; i < parsers.size(); i++) {
            current = current.setNext(parsers.get(i));
        }
        
        this.firstParser = first;
    }

    /**
     * Parses a command from the given message.
     *
     * @param sender the sender of the message
     * @param content the content of the message
     * @return the parsed command, or null if no parser could handle the message
     */
    public Command parse(String sender, String content) {
        Command command = firstParser.parse(sender, content);
        if (command == null) {
            logger.info("No command matched for content: {}", content);
            return new InvalidCommand(sender, content);
        }
        return command;
    }
}