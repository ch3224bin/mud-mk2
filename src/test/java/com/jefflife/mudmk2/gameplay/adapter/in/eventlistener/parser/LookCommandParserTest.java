package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.LookCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LookCommandParserTest {

    private LookCommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new LookCommandParser();
    }

    @Test
    void parse_lookAlone_returnsCommandWithNullTarget() {
        Command command = parser.parse(1L, "봐");
        assertThat(command).isInstanceOf(LookCommand.class);
        LookCommand look = (LookCommand) command;
        assertThat(look.target()).isNull();
        assertThat(look.userId()).isEqualTo(1L);
    }

    @Test
    void parse_targetOnly_returnsCommandWithIndex1() {
        Command command = parser.parse(1L, "철검 봐");
        assertThat(command).isInstanceOf(LookCommand.class);
        LookCommand look = (LookCommand) command;
        assertThat(look.target()).isEqualTo("철검");
        assertThat(look.index()).isEqualTo(1);
    }

    @Test
    void parse_targetWithIndex_returnsCorrectIndex() {
        Command command = parser.parse(1L, "철검 2 봐");
        assertThat(command).isInstanceOf(LookCommand.class);
        LookCommand look = (LookCommand) command;
        assertThat(look.target()).isEqualTo("철검");
        assertThat(look.index()).isEqualTo(2);
    }

    @Test
    void parse_direction_returnsCommandWithIndex1() {
        Command command = parser.parse(1L, "동 봐");
        assertThat(command).isInstanceOf(LookCommand.class);
        LookCommand look = (LookCommand) command;
        assertThat(look.target()).isEqualTo("동");
        assertThat(look.index()).isEqualTo(1);
    }

    @Test
    void parse_nonLook_returnsNull() {
        Command command = parser.parse(1L, "철검 줍기");
        assertThat(command).isNull();
    }
}
