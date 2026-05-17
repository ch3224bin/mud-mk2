package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher.chat;

import com.jefflife.mudmk2.gameplay.application.service.model.template.MartialArtViewVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticApplicationContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MartialArtViewMessageSenderTemplateTest {

    @Mock
    private ChatEventPublisher chatEventPublisher;

    private MartialArtViewMessageSender sender;

    @BeforeEach
    void setUp() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(new StaticApplicationContext());
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        doNothing().when(chatEventPublisher).messageToUser(anyLong(), anyString());

        sender = new MartialArtViewMessageSender(engine, chatEventPublisher);
    }

    private String captureHtml(MartialArtViewVariables vars) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        sender.sendMessage(vars);
        verify(chatEventPublisher).messageToUser(anyLong(), captor.capture());
        return captor.getValue();
    }

    @Test
    void render_noLearned_emitsEmptyMessage() {
        String html = captureHtml(new MartialArtViewVariables(1L, List.of(), List.of()));
        assertThat(html).contains("배운 무공이 없습니다.");
        assertThat(html).doesNotContain("[ 심법 ]");
        assertThat(html).doesNotContain("[ 외공 ]");
    }

    @Test
    void render_withMentalAndExternal_emitsBothSections() {
        MartialArtViewVariables.MentalGroup mental = new MartialArtViewVariables.MentalGroup(
                "내공",
                List.of(new MartialArtViewVariables.LearnedMentalLine(
                        "천뢰신공", 3, 3, 0L, true, true,
                        List.of(new MartialArtViewVariables.StatModLine("내공", 6)))));

        MartialArtViewVariables.ExternalGroup external = new MartialArtViewVariables.ExternalGroup(
                "검",
                List.of(new MartialArtViewVariables.LearnedExternalLine(
                        "천뢰검법", 1, 2, 0L, false, false, 1.5, 4, 3, 2)));

        String html = captureHtml(new MartialArtViewVariables(
                1L, List.of(mental), List.of(external)));

        assertThat(html).contains("[ 심법 ]");
        assertThat(html).contains("〈").contains("내공").contains("〉");
        assertThat(html).contains("천뢰신공");
        assertThat(html).contains("Lv.").contains(">3<").contains(">3<");
        assertThat(html).contains("[MAX]");
        assertThat(html).contains("☆");
        assertThat(html).contains("+6 내공");
        assertThat(html).contains("Exp").contains(">0<");

        assertThat(html).contains("[ 외공 ]");
        assertThat(html).contains("〈").contains("검").contains("〉");
        assertThat(html).contains("천뢰검법");
        assertThat(html).contains(">1<").contains(">2<");
        assertThat(html).contains("×").contains("1.50");
        assertThat(html).contains("쿨").contains(">4<").contains("s");
        assertThat(html).contains("AP").contains(">3<");
        assertThat(html).contains("MP").contains(">2<");
    }

    @Test
    void render_negativeStatMod_rendersMinusPrefix() {
        MartialArtViewVariables.MentalGroup mental = new MartialArtViewVariables.MentalGroup(
                "내공",
                List.of(new MartialArtViewVariables.LearnedMentalLine(
                        "이름", 1, 1, 0L, true, false,
                        List.of(new MartialArtViewVariables.StatModLine("활력", -2)))));

        String html = captureHtml(new MartialArtViewVariables(
                1L, List.of(mental), List.of()));

        assertThat(html).contains("-2 활력");
        assertThat(html).doesNotContain("+-2");
    }
}
