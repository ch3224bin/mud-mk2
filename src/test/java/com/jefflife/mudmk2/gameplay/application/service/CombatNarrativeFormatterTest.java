package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.domain.model.combat.CombatLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CombatNarrativeFormatterTest {

    private CombatNarrativeFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new CombatNarrativeFormatter();
    }

    private CombatLog.CombatLogBuilder baseLog() {
        return CombatLog.builder()
            .attackerId(UUID.randomUUID()).attackerName("홍길동")
            .targetId(UUID.randomUUID()).targetName("산적두목")
            .attackRoll(0).attackModifier(0).attackTotal(0)
            .defenseRoll(0).defenseModifier(0).defenseTotal(0)
            .damageModifier(0).damageTotal(0).defenseValue(0);
    }

    @Test
    void 일반명중_메시지() {
        CombatLog log = baseLog()
            .hitSuccess(true).evaded(false).isCrit(false)
            .baseDamage(30).finalDamage(22)
            .targetRemainingHp(78).targetDefeated(false)
            .attackerApAfter(80).targetApAfter(112)
            .weaponTypeName("검법")
            .build();

        String message = formatter.format(log);

        assertThat(message).contains("홍길동").contains("검법").contains("산적두목");
        assertThat(message).contains("22").contains("78");
        assertThat(message).doesNotContain("치명타").doesNotContain("피했다");
    }

    @Test
    void 치명타_메시지() {
        CombatLog log = baseLog()
            .hitSuccess(true).evaded(false).isCrit(true)
            .baseDamage(30).finalDamage(33)
            .targetRemainingHp(45).targetDefeated(false)
            .attackerApAfter(80).targetApAfter(112)
            .weaponTypeName("검법")
            .build();

        assertThat(formatter.format(log)).contains("치명타");
    }

    @Test
    void 회피_메시지() {
        CombatLog log = baseLog()
            .hitSuccess(false).evaded(true).isCrit(false)
            .baseDamage(0).finalDamage(0)
            .targetRemainingHp(150).targetDefeated(false)
            .attackerApAfter(80).targetApAfter(107)
            .weaponTypeName("검법")
            .build();

        String message = formatter.format(log);
        assertThat(message).contains("피했다");
        assertThat(message).doesNotContain("데미지");
    }

    @Test
    void 사망_메시지가_포함된다() {
        CombatLog log = baseLog()
            .hitSuccess(true).evaded(false).isCrit(false)
            .baseDamage(100).finalDamage(90)
            .targetRemainingHp(0).targetDefeated(true)
            .attackerApAfter(80).targetApAfter(0)
            .weaponTypeName("도법")
            .build();

        String message = formatter.format(log);
        assertThat(message).contains("쓰러졌다");
    }
}
