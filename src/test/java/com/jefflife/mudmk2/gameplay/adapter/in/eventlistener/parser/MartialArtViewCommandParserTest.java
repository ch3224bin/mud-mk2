package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.MartialArtViewCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MartialArtViewCommandParserTest {

    private MartialArtViewCommandParser parser;

    @BeforeEach
    void setUp() { parser = new MartialArtViewCommandParser(); }

    @Test
    void parse_무공_returnsCommand() {
        Command c = parser.parse(1L, "무공");
        assertThat(c).isInstanceOf(MartialArtViewCommand.class);
    }

    @Test
    void parse_무공창_returnsCommand() {
        assertThat(parser.parse(1L, "무공창")).isInstanceOf(MartialArtViewCommand.class);
    }

    @Test
    void parse_other_returnsNull() {
        assertThat(parser.parse(1L, "장비")).isNull();
        assertThat(parser.parse(1L, "무공창고")).isNull();
        assertThat(parser.parse(1L, "철검 장착")).isNull();
    }
}
