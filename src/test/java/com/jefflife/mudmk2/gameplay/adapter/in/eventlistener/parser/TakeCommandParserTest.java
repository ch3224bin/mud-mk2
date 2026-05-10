package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.TakeCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TakeCommandParserTest {

    private TakeCommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new TakeCommandParser();
    }

    @Test
    void parse_itemNameOnly_returnsFirstIndex() {
        Command command = parser.parse(1L, "철검 주워");
        assertThat(command).isInstanceOf(TakeCommand.class);
        TakeCommand take = (TakeCommand) command;
        assertThat(take.itemName()).isEqualTo("철검");
        assertThat(take.index()).isEqualTo(1);
        assertThat(take.userId()).isEqualTo(1L);
    }

    @Test
    void parse_itemNameWithIndex_returnsCorrectIndex() {
        Command command = parser.parse(1L, "철검 2 주워");
        assertThat(command).isInstanceOf(TakeCommand.class);
        TakeCommand take = (TakeCommand) command;
        assertThat(take.itemName()).isEqualTo("철검");
        assertThat(take.index()).isEqualTo(2);
    }

    @Test
    void parse_verb_줍다_returnsCommand() {
        Command command = parser.parse(1L, "만두 줍다");
        assertThat(command).isInstanceOf(TakeCommand.class);
        TakeCommand take = (TakeCommand) command;
        assertThat(take.itemName()).isEqualTo("만두");
        assertThat(take.index()).isEqualTo(1);
    }

    @Test
    void parse_verb_집어_returnsCommand() {
        Command command = parser.parse(1L, "사과 집어");
        assertThat(command).isInstanceOf(TakeCommand.class);
        TakeCommand take = (TakeCommand) command;
        assertThat(take.itemName()).isEqualTo("사과");
    }

    @Test
    void parse_verb_집어_withIndex_returnsCorrectIndex() {
        Command command = parser.parse(1L, "사과 3 집어");
        assertThat(command).isInstanceOf(TakeCommand.class);
        TakeCommand take = (TakeCommand) command;
        assertThat(take.itemName()).isEqualTo("사과");
        assertThat(take.index()).isEqualTo(3);
    }

    @Test
    void parse_nonTakeCommand_returnsNull() {
        Command command = parser.parse(1L, "동");
        assertThat(command).isNull();
    }
}
