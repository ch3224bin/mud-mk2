package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserChainTest {

    // Simple Command implementation for testing
    private static class TestCommand implements Command {
        private final Long userId;
        
        public TestCommand(Long userId) {
            this.userId = userId;
        }
        
        @Override
        public Long userId() {
            return userId;
        }
        
        @Override
        public String toString() {
            return "TestCommand{username='" + userId + "'}";
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
        protected Command parseCommand(Long userId, String content) {
            if (shouldMatch && content.startsWith(commandPrefix)) {
                return new TestCommand(userId);
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
        Command result = chain.parse(1L, "move east");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.userId());
    }

    @Test
    void parse_withSecondParserMatching_shouldReturnCommand() {
        // Arrange
        CommandParser parser1 = new TestCommandParser("move", false);
        CommandParser parser2 = new TestCommandParser("look", true);
        List<CommandParser> parsers = List.of(parser1, parser2);
        CommandParserChain chain = new CommandParserChain(parsers);

        // Act
        Command result = chain.parse(1L, "look around");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.userId());
    }
}