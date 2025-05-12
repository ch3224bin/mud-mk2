package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserChainTest {

    // Simple Command implementation for testing
    private static class TestCommand implements Command {
        private final String player;
        
        public TestCommand(String player) {
            this.player = player;
        }
        
        @Override
        public String getPlayer() {
            return player;
        }
        
        @Override
        public String toString() {
            return "TestCommand{player='" + player + "'}";
        }
    }
    
    // Simple CommandParser implementation for testing
    private static class TestCommandParser extends AbstractCommandParser {
        private final String commandPrefix;
        private final boolean shouldMatch;
        
        public TestCommandParser(String commandPrefix, boolean shouldMatch) {
            this.commandPrefix = commandPrefix;
            this.shouldMatch = shouldMatch;
        }
        
        @Override
        protected Command parseCommand(String sender, String content) {
            if (shouldMatch && content.startsWith(commandPrefix)) {
                return new TestCommand(sender);
            }
            return null;
        }
    }

    @Test
    void constructor_withEmptyParsers_shouldThrowException() {
        // Arrange
        List<CommandParser> emptyParsers = Collections.emptyList();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new CommandParserChain(emptyParsers));
    }

    @Test
    void constructor_withValidParsers_shouldChainParsers() {
        // Arrange
        CommandParser parser1 = new TestCommandParser("move", true);
        CommandParser parser2 = new TestCommandParser("look", true);
        List<CommandParser> parsers = List.of(parser1, parser2);

        // Act
        CommandParserChain chain = new CommandParserChain(parsers);

        // Assert
        // If the chain is constructed correctly, the parse test will pass
        assertNotNull(chain);
    }

    @Test
    void parse_withMatchingCommand_shouldReturnCommand() {
        // Arrange
        CommandParser parser1 = new TestCommandParser("move", true);
        CommandParser parser2 = new TestCommandParser("look", true);
        List<CommandParser> parsers = List.of(parser1, parser2);
        CommandParserChain chain = new CommandParserChain(parsers);

        // Act
        Command result = chain.parse("player1", "move east");

        // Assert
        assertNotNull(result);
        assertEquals("player1", result.getPlayer());
    }

    @Test
    void parse_withNonMatchingCommand_shouldReturnNull() {
        // Arrange
        CommandParser parser1 = new TestCommandParser("move", false);
        CommandParser parser2 = new TestCommandParser("look", false);
        List<CommandParser> parsers = List.of(parser1, parser2);
        CommandParserChain chain = new CommandParserChain(parsers);

        // Act
        Command result = chain.parse("player1", "unknown command");

        // Assert
        assertNull(result);
    }

    @Test
    void parse_withSecondParserMatching_shouldReturnCommand() {
        // Arrange
        CommandParser parser1 = new TestCommandParser("move", false);
        CommandParser parser2 = new TestCommandParser("look", true);
        List<CommandParser> parsers = List.of(parser1, parser2);
        CommandParserChain chain = new CommandParserChain(parsers);

        // Act
        Command result = chain.parse("player1", "look around");

        // Assert
        assertNotNull(result);
        assertEquals("player1", result.getPlayer());
    }
}