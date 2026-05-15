package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EquipCommandParserTest {

    private EquipCommandParser parser;

    @BeforeEach
    void setUp() { parser = new EquipCommandParser(); }

    @Test
    void parse_swordWith_장착_returnsEquipCommand() {
        Command c = parser.parse(1L, "철검 장착");
        assertThat(c).isInstanceOf(EquipCommand.class);
        EquipCommand e = (EquipCommand) c;
        assertThat(e.itemName()).isEqualTo("철검");
        assertThat(e.index()).isEqualTo(1);
        assertThat(e.userId()).isEqualTo(1L);
    }

    @Test
    void parse_helmetWith_입어_returnsEquipCommand() {
        EquipCommand e = (EquipCommand) parser.parse(1L, "야구모자 입어");
        assertThat(e.itemName()).isEqualTo("야구모자");
    }

    @Test
    void parse_ringWith_끼다_returnsEquipCommand() {
        EquipCommand e = (EquipCommand) parser.parse(1L, "금반지 끼다");
        assertThat(e.itemName()).isEqualTo("금반지");
    }

    @Test
    void parse_withIndex_returnsCorrectIndex() {
        EquipCommand e = (EquipCommand) parser.parse(1L, "철검 2 장착");
        assertThat(e.index()).isEqualTo(2);
    }

    @Test
    void parse_nonEquipText_returnsNull() {
        assertThat(parser.parse(1L, "철검 주워")).isNull();
    }
}
