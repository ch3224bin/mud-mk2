package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.SpeakCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpeakCommandParserTest {

    private final SpeakCommandParser parser = new SpeakCommandParser();
    private final Long userId = 1L;

    @Test
    @DisplayName("단일 단어 메시지는 target이 null로 파싱되어야 한다")
    void parseCommand_singleWordMessage_shouldParseWithNullTarget() {
        // given
        String content = "안녕하세요 말";

        // when
        Command command = parser.parse(userId, content);

        // then
        assertThat(command).isInstanceOf(SpeakCommand.class);
        SpeakCommand speakCommand = (SpeakCommand) command;
        assertThat(speakCommand.userId()).isEqualTo(userId);
        assertThat(speakCommand.target()).isNull();
        assertThat(speakCommand.message()).isEqualTo("안녕하세요");
    }

    @Test
    @DisplayName("여러 단어 메시지는 첫 단어가 target으로 파싱되어야 한다")
    void parseCommand_multiWordMessage_shouldParseWithFirstWordAsTarget() {
        // given
        String content = "좋은 아침입니다 말";

        // when
        Command command = parser.parse(userId, content);

        // then
        assertThat(command).isInstanceOf(SpeakCommand.class);
        SpeakCommand speakCommand = (SpeakCommand) command;
        assertThat(speakCommand.userId()).isEqualTo(userId);
        assertThat(speakCommand.target()).isEqualTo("좋은");
        assertThat(speakCommand.message()).isEqualTo("좋은 아침입니다");
    }

    @Test
    @DisplayName("'말'로 끝나지 않는 메시지는 null을 반환해야 한다")
    void parseCommand_nonSpeakCommand_shouldReturnNull() {
        // given
        String content = "안녕하세요";

        // when
        Command command = parser.parse(userId, content);

        // then
        assertThat(command).isNull();
    }
}