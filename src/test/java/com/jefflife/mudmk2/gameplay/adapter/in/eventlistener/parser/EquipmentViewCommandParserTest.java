package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EquipmentViewCommandParserTest {

    private EquipmentViewCommandParser parser;

    @BeforeEach
    void setUp() { parser = new EquipmentViewCommandParser(); }

    @Test
    void parse_장비_returnsCommand() {
        Command c = parser.parse(1L, "장비");
        assertThat(c).isInstanceOf(EquipmentViewCommand.class);
    }

    @Test
    void parse_장비창_returnsCommand() {
        assertThat(parser.parse(1L, "장비창")).isInstanceOf(EquipmentViewCommand.class);
    }

    @Test
    void parse_other_returnsNull() {
        assertThat(parser.parse(1L, "소지품")).isNull();
        assertThat(parser.parse(1L, "철검 장착")).isNull();
    }
}
