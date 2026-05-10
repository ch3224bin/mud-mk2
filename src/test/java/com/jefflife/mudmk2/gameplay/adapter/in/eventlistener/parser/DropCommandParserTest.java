package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.DropCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DropCommandParserTest {

    private DropCommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new DropCommandParser();
    }

    @Test
    void parse_itemNameOnly_returnsFirstIndex() {
        Command command = parser.parse(1L, "철검 버려");
        assertThat(command).isInstanceOf(DropCommand.class);
        DropCommand drop = (DropCommand) command;
        assertThat(drop.itemName()).isEqualTo("철검");
        assertThat(drop.index()).isEqualTo(1);
        assertThat(drop.userId()).isEqualTo(1L);
    }

    @Test
    void parse_itemNameWithIndex_returnsCorrectIndex() {
        Command command = parser.parse(1L, "철검 2 버려");
        assertThat(command).isInstanceOf(DropCommand.class);
        DropCommand drop = (DropCommand) command;
        assertThat(drop.itemName()).isEqualTo("철검");
        assertThat(drop.index()).isEqualTo(2);
    }

    @Test
    void parse_verb_버리다_returnsCommand() {
        Command command = parser.parse(1L, "만두 버리다");
        assertThat(command).isInstanceOf(DropCommand.class);
        DropCommand drop = (DropCommand) command;
        assertThat(drop.itemName()).isEqualTo("만두");
        assertThat(drop.index()).isEqualTo(1);
    }

    @Test
    void parse_verb_놓다_returnsCommand() {
        Command command = parser.parse(1L, "사과 놓다");
        assertThat(command).isInstanceOf(DropCommand.class);
    }

    @Test
    void parse_nonDropCommand_returnsNull() {
        Command command = parser.parse(1L, "동");
        assertThat(command).isNull();
    }
}
