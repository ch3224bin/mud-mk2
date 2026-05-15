package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.UnequipCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UnequipCommandParserTest {

    private UnequipCommandParser parser;

    @BeforeEach
    void setUp() { parser = new UnequipCommandParser(); }

    @Test
    void parse_swordWith_벗어_returnsUnequipCommand() {
        Command c = parser.parse(1L, "철검 벗어");
        assertThat(c).isInstanceOf(UnequipCommand.class);
        assertThat(((UnequipCommand) c).itemName()).isEqualTo("철검");
    }

    @Test
    void parse_pantsWith_벗어_returnsUnequipCommand() {
        UnequipCommand u = (UnequipCommand) parser.parse(1L, "반바지 벗어");
        assertThat(u.itemName()).isEqualTo("반바지");
    }

    @Test
    void parse_with_해제_returnsUnequipCommand() {
        UnequipCommand u = (UnequipCommand) parser.parse(1L, "철검 해제");
        assertThat(u.itemName()).isEqualTo("철검");
    }

    @Test
    void parse_with_빼다_returnsUnequipCommand() {
        UnequipCommand u = (UnequipCommand) parser.parse(1L, "금반지 빼다");
        assertThat(u.itemName()).isEqualTo("금반지");
    }

    @Test
    void parse_irrelevant_returnsNull() {
        assertThat(parser.parse(1L, "철검 장착")).isNull();
    }
}
