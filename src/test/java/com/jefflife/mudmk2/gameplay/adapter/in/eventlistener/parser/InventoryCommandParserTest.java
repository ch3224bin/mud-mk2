package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.InventoryCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InventoryCommandParserTest {

    private InventoryCommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new InventoryCommandParser();
    }

    @Test
    void parse_소지품_returnsInventoryCommand() {
        Command command = parser.parse(1L, "소지품");
        assertThat(command).isInstanceOf(InventoryCommand.class);
        assertThat(command.userId()).isEqualTo(1L);
    }

    @Test
    void parse_가방_returnsInventoryCommand() {
        Command command = parser.parse(1L, "가방");
        assertThat(command).isInstanceOf(InventoryCommand.class);
    }

    @Test
    void parse_인벤_returnsInventoryCommand() {
        Command command = parser.parse(1L, "인벤");
        assertThat(command).isInstanceOf(InventoryCommand.class);
    }

    @Test
    void parse_nonInventoryCommand_returnsNull() {
        Command command = parser.parse(1L, "동");
        assertThat(command).isNull();
    }
}
