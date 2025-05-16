package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for command parsers that implements the Chain of Responsibility pattern.
 * Provides common functionality for all command parsers.
 */
public abstract class AbstractCommandParser implements CommandParser {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private CommandParser next;

    @Override
    public CommandParser setNext(CommandParser next) {
        this.next = next;
        return next;
    }

    @Override
    public CommandParser getNext() {
        return next;
    }

    @Override
    public Command parse(Long userId, String content) {
        Command command = parseCommand(userId, content);
        if (command != null) {
            return command;
        }
        
        if (next != null) {
            return next.parse(userId, content);
        }
        
        return null;
    }

    /**
     * Template method that concrete parsers must implement to parse a specific command type.
     *
     * @param userId the ID of the user sending the message
     * @param content the content of the message
     * @return the parsed command, or null if this parser can't handle the message
     */
    protected abstract Command parseCommand(Long userId, String content);
}