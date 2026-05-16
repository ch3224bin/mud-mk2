package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher.chat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterState;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gameplay.application.service.model.template.StatusVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.springframework.context.support.StaticApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StatusMessageSenderTemplateTest {

    @Mock
    private ChatEventPublisher chatEventPublisher;

    private StatusMessageSender sender;

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

        sender = new StatusMessageSender(engine, chatEventPublisher);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static final StatusVariables.StatValue DEFAULT_STAT = new StatusVariables.StatValue(10, 0);

    /**
     * Builds a default StatusVariables with all stats at StatValue(10, 0).
     * Individual fields can be overridden by passing a custom StatusVariables directly.
     */
    private StatusVariables defaultVariables() {
        return new StatusVariables(
                1L,             // userId
                "테스터",        // playerName
                CharacterClass.WARRIOR,
                Gender.MALE,
                CharacterState.NORMAL,
                1,              // level
                0L,             // experience
                1000L,          // nextLevelExp
                100,            // hp
                100,            // maxHp
                50,             // mp
                50,             // maxMp
                30,             // ap
                30,             // maxAp
                // 속성 6개
                DEFAULT_STAT,   // vigor
                DEFAULT_STAT,   // physique
                DEFAULT_STAT,   // agility
                DEFAULT_STAT,   // intellect
                DEFAULT_STAT,   // will
                DEFAULT_STAT,   // meridian
                // 무예 9개
                DEFAULT_STAT,   // innerPower
                DEFAULT_STAT,   // specialTechnique
                DEFAULT_STAT,   // lightStep
                DEFAULT_STAT,   // fistsAndPalms
                DEFAULT_STAT,   // swordMethod
                DEFAULT_STAT,   // bladeMethod
                DEFAULT_STAT,   // longWeapon
                DEFAULT_STAT,   // esotericWeapon
                DEFAULT_STAT,   // archery
                "수련장"         // roomName
        );
    }

    private StatusVariables withVigor(StatusVariables.StatValue vigor) {
        StatusVariables d = defaultVariables();
        return new StatusVariables(
                d.userId(), d.playerName(), d.characterClass(), d.gender(), d.state(),
                d.level(), d.experience(), d.nextLevelExp(),
                d.hp(), d.maxHp(), d.mp(), d.maxMp(), d.ap(), d.maxAp(),
                vigor,
                d.physique(), d.agility(), d.intellect(), d.will(), d.meridian(),
                d.innerPower(), d.specialTechnique(), d.lightStep(), d.fistsAndPalms(),
                d.swordMethod(), d.bladeMethod(), d.longWeapon(), d.esotericWeapon(),
                d.archery(), d.roomName()
        );
    }

    private String captureHtml(StatusVariables vars) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        sender.sendMessage(vars);
        verify(chatEventPublisher).messageToUser(anyLong(), captor.capture());
        return captor.getValue();
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * Smoke test: all 15 StatValue(N, 0) must render without SpEL null exception.
     * This is the regression guard for the original bug where D&D variable names
     * (str/dex/...) were referenced instead of wuxia names (vigor/physique/...).
     */
    @Test
    void render_doesNotThrow_whenAllBonusesAreZero() {
        assertThatCode(() -> {
            String html = captureHtml(defaultVariables());
            assertThat(html).isNotEmpty();
        }).doesNotThrowAnyException();
    }

    /**
     * Bonus span must be suppressed when bonus == 0.
     * Guards the th:if="${vigor.bonus != 0}" condition.
     */
    @Test
    void render_omitsBonusSpan_whenBonusIsZero() {
        String html = captureHtml(withVigor(new StatusVariables.StatValue(10, 0)));

        // The bonus span should not appear at all — no "(+" or "(-" in the output
        assertThat(html).doesNotContain("(+").doesNotContain("(-");
    }

    /**
     * A positive bonus must render as "(+5)" — not "(5)" or "(+-5)".
     */
    @Test
    void render_includesPositiveBonus_whenBonusIsPositive() {
        String html = captureHtml(withVigor(new StatusVariables.StatValue(10, 5)));

        assertThat(html).contains("(+5)");
    }

    /**
     * A negative bonus must render as "(-3)" — not "(+-3)" which would be a
     * sign-prefix bug ('+' prepended unconditionally before a negative number).
     */
    @Test
    void render_includesNegativeBonus_whenBonusIsNegative() {
        String html = captureHtml(withVigor(new StatusVariables.StatValue(10, -3)));

        assertThat(html).contains("(-3)");
        assertThat(html).doesNotContain("(+-3)");
    }

    /**
     * All 15 Korean stat labels must appear in the rendered HTML.
     */
    @Test
    void render_containsAllStatLabels() {
        String html = captureHtml(defaultVariables());

        // 속성 6개
        assertThat(html).contains("활력");
        assertThat(html).contains("체력");
        assertThat(html).contains("민첩");
        assertThat(html).contains("지력");
        assertThat(html).contains("의지");
        assertThat(html).contains("기맥");
        // 무예 9개
        assertThat(html).contains("내공");
        assertThat(html).contains("특기");
        assertThat(html).contains("경공");
        assertThat(html).contains("권장");
        assertThat(html).contains("검술");
        assertThat(html).contains("도법");
        assertThat(html).contains("장병기술");
        assertThat(html).contains("암기술");
        assertThat(html).contains("궁술");
    }
}
