package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;

/**
 * Interface for command parsers that follow the Chain of Responsibility pattern.
 * Each parser attempts to parse a command from a message, and if it can't,
 * passes the responsibility to the next parser in the chain.
 */
public interface CommandParser {
    /**
     * Sets the next parser in the chain.
     *
     * @param next the next parser to try if this one can't parse the command
     * @return the next parser, for method chaining
     */
    CommandParser setNext(CommandParser next);

    /**
     * Gets the next parser in the chain.
     *
     * @return the next parser
     */
    CommandParser getNext();

    /**
     * Attempts to parse a command from the given message.
     * If this parser can't handle the message, it passes the request to the next parser.
     *
     * @param sender the sender of the message
     * @param content the content of the message
     * @return the parsed command, or null if no parser in the chain could handle the message
     */
    Command parse(String sender, String content);
}