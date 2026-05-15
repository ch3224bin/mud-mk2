package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.domain.model.combat.CombatLog;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CombatNarrativeFormatterTest {

    private final CombatNarrativeFormatter formatter = new CombatNarrativeFormatter();

    private CombatLog.CombatLogBuilder baseBuilder() {
        return CombatLog.builder()
                .attackerId(UUID.randomUUID()).attackerName("철수")
                .targetId(UUID.randomUUID()).targetName("다람쥐")
                .attackRoll(15).attackModifier(0).attackTotal(15)
                .defenseRoll(10).defenseModifier(0).defenseTotal(10)
                .hitSuccess(true)
                .baseDamage(5).damageModifier(0).damageTotal(5)
                .defenseValue(0).finalDamage(5)
                .targetRemainingHp(15).targetDefeated(false)
                .evaded(false).isCrit(false)
                .attackerApAfter(0).targetApAfter(0)
                .weaponTypeName("SWORD").weaponName("철검");
    }

    @Test
    void format_normalHit_swordWeapon_usesItemNameAndVerb베었다() {
        CombatLog log = baseBuilder().build();
        String result = formatter.format(log);
        assertThat(result).contains("철검").contains("베었다");
        assertThat(result).contains("다람쥐").contains("철수");
    }

    @Test
    void format_normalHit_bladeWeapon_usesVerb내려쳤다() {
        CombatLog log = baseBuilder().weaponTypeName("BLADE").weaponName("청룡도").build();
        assertThat(formatter.format(log)).contains("청룡도").contains("내려쳤다");
    }

    @Test
    void format_normalHit_fist_usesVerb내질렀다() {
        CombatLog log = baseBuilder().weaponTypeName("FIST").weaponName("맨손").build();
        assertThat(formatter.format(log)).contains("맨손").contains("내질렀다");
    }

    @Test
    void format_normalHit_archery_usesVerb쐈다() {
        CombatLog log = baseBuilder().weaponTypeName("ARCHERY").weaponName("나무활").build();
        assertThat(formatter.format(log)).contains("나무활").contains("쐈다");
    }

    @Test
    void format_normalHit_longWeapon_usesVerb휘둘렀다() {
        CombatLog log = baseBuilder().weaponTypeName("LONG_WEAPON").weaponName("장창").build();
        assertThat(formatter.format(log)).contains("장창").contains("휘둘렀다");
    }

    @Test
    void format_normalHit_esoteric_usesVerb휘둘렀다() {
        CombatLog log = baseBuilder().weaponTypeName("ESOTERIC").weaponName("암기").build();
        assertThat(formatter.format(log)).contains("암기").contains("휘둘렀다");
    }

    @Test
    void format_crit_usesWeaponNameAndVerb() {
        CombatLog log = baseBuilder().isCrit(true).build();
        String result = formatter.format(log);
        assertThat(result).contains("철검").contains("베었다").contains("치명");
    }

    @Test
    void format_evaded_usesWeaponNameAndVerb() {
        CombatLog log = baseBuilder().hitSuccess(false).evaded(true).build();
        String result = formatter.format(log);
        assertThat(result).contains("철검").contains("베었다").contains("피했다");
    }

    @Test
    void format_targetDefeated_appendsDefeatedLine() {
        CombatLog log = baseBuilder().targetDefeated(true).build();
        assertThat(formatter.format(log)).contains("쓰러졌다");
    }
}
