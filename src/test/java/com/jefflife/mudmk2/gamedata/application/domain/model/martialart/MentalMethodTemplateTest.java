package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MentalMethodTemplateTest {

    @Test
    void create_buildsTemplate() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("천뢰신공")
                .description("내공 심법")
                .kind(MentalMethodKind.INNER_POWER)
                .maxLevel(2)
                .levelEffects(List.of(
                        new MentalMethodLevelEffect(1, List.of(new StatModifier(StatType.INNER_POWER, 3))),
                        new MentalMethodLevelEffect(2, List.of(new StatModifier(StatType.INNER_POWER, 6)))))
                .build();

        assertThat(t.getName()).isEqualTo("천뢰신공");
        assertThat(t.getKind()).isEqualTo(MentalMethodKind.INNER_POWER);
        assertThat(t.getMaxLevel()).isEqualTo(2);
        assertThat(t.getLevelEffects()).hasSize(2);
    }

    @Test
    void create_whenMaxLevelLessThan1_throws() {
        assertThatThrownBy(() -> MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(0).levelEffects(List.of()).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_whenLevelEffectsSizeNotEqMaxLevel_throws() {
        assertThatThrownBy(() -> MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(2)
                .levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_whenLevelsNotConsecutive_throws() {
        assertThatThrownBy(() -> MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(2)
                .levelEffects(List.of(
                        new MentalMethodLevelEffect(1, List.of()),
                        new MentalMethodLevelEffect(3, List.of())))
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_replacesAllFields() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("old").description("old").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(1).levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build();

        t.update("new", "new desc", MentalMethodKind.LIGHT_STEP, 1,
                List.of(new MentalMethodLevelEffect(1, List.of(new StatModifier(StatType.LIGHT_STEP, 4)))));

        assertThat(t.getName()).isEqualTo("new");
        assertThat(t.getKind()).isEqualTo(MentalMethodKind.LIGHT_STEP);
        assertThat(t.getLevelEffects().get(0).statModifiers()).hasSize(1);
    }

    @Test
    void effectAt_returnsEffectForGivenLevel() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("천뢰신공").description("d").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(3)
                .levelEffects(List.of(
                        new MentalMethodLevelEffect(1, List.of(new StatModifier(StatType.INNER_POWER, 1))),
                        new MentalMethodLevelEffect(2, List.of(new StatModifier(StatType.INNER_POWER, 3))),
                        new MentalMethodLevelEffect(3, List.of(new StatModifier(StatType.INNER_POWER, 6)))))
                .build();

        assertThat(t.effectAt(1).statModifiers().get(0).getValue()).isEqualTo(1);
        assertThat(t.effectAt(2).statModifiers().get(0).getValue()).isEqualTo(3);
        assertThat(t.effectAt(3).statModifiers().get(0).getValue()).isEqualTo(6);
    }

    @Test
    void effectAt_whenLevelBelow1_throws() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(1)
                .levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build();

        assertThatThrownBy(() -> t.effectAt(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> t.effectAt(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void effectAt_whenLevelAboveMaxLevel_throws() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(2)
                .levelEffects(List.of(
                        new MentalMethodLevelEffect(1, List.of()),
                        new MentalMethodLevelEffect(2, List.of())))
                .build();

        assertThatThrownBy(() -> t.effectAt(3)).isInstanceOf(IllegalArgumentException.class);
    }
}
