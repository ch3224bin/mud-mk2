package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EatCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EatCommandParserTest {

    private EatCommandParser parser;

    @BeforeEach
    void setUp() { parser = new EatCommandParser(); }

    @Test
    void parse_appleWith_먹어_returnsEatCommand() {
        Command c = parser.parse(1L, "사과 먹어");
        assertThat(c).isInstanceOf(EatCommand.class);
        EatCommand e = (EatCommand) c;
        assertThat(e.itemName()).isEqualTo("사과");
        assertThat(e.index()).isEqualTo(1);
        assertThat(e.userId()).isEqualTo(1L);
    }

    @Test
    void parse_meatWith_먹다_returnsEatCommand() {
        EatCommand e = (EatCommand) parser.parse(1L, "고기 먹다");
        assertThat(e.itemName()).isEqualTo("고기");
    }

    @Test
    void parse_withIndex_returnsCorrectIndex() {
        EatCommand e = (EatCommand) parser.parse(1L, "고기 2 먹다");
        assertThat(e.index()).isEqualTo(2);
    }

    @Test
    void parse_nonEatText_returnsNull() {
        assertThat(parser.parse(1L, "철검 장착")).isNull();
    }
}
