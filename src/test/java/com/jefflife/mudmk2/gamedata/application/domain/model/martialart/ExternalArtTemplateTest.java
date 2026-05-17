package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalArtTemplateTest {

    @Test
    void create_buildsTemplate() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("발검술")
                .description("기초 검법")
                .weaponType(WeaponType.SWORD)
                .maxLevel(2)
                .levelEffects(List.of(
                        new ExternalArtLevelEffect(1, 1.1, 6, 5, 0),
                        new ExternalArtLevelEffect(2, 1.2, 5, 5, 0)))
                .build();

        assertThat(t.getWeaponType()).isEqualTo(WeaponType.SWORD);
        assertThat(t.getLevelEffects()).hasSize(2);
    }

    @Test
    void create_whenLevelEffectsSizeNotEqMaxLevel_throws() {
        assertThatThrownBy(() -> ExternalArtTemplate.builder()
                .name("x").description("x").weaponType(WeaponType.SWORD)
                .maxLevel(2)
                .levelEffects(List.of(new ExternalArtLevelEffect(1, 1.0, 1, 1, 0)))
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_replacesAllFields() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("old").description("old").weaponType(WeaponType.SWORD)
                .maxLevel(1).levelEffects(List.of(new ExternalArtLevelEffect(1, 1.0, 1, 1, 0)))
                .build();

        t.update("new", "new desc", WeaponType.FIST, 1,
                List.of(new ExternalArtLevelEffect(1, 1.5, 2, 2, 1)));

        assertThat(t.getName()).isEqualTo("new");
        assertThat(t.getWeaponType()).isEqualTo(WeaponType.FIST);
        assertThat(t.getLevelEffects().get(0).damageMultiplier()).isEqualTo(1.5);
    }

    @Test
    void effectAt_returnsEffectForGivenLevel() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("천뢰검법").description("d").weaponType(WeaponType.SWORD)
                .maxLevel(3)
                .levelEffects(List.of(
                        new ExternalArtLevelEffect(1, 1.0, 5, 3, 2),
                        new ExternalArtLevelEffect(2, 1.5, 4, 3, 2),
                        new ExternalArtLevelEffect(3, 2.0, 3, 3, 2)))
                .build();

        assertThat(t.effectAt(1).damageMultiplier()).isEqualTo(1.0);
        assertThat(t.effectAt(2).damageMultiplier()).isEqualTo(1.5);
        assertThat(t.effectAt(3).damageMultiplier()).isEqualTo(2.0);
    }

    @Test
    void effectAt_whenLevelBelow1_throws() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("x").description("x").weaponType(WeaponType.SWORD)
                .maxLevel(1)
                .levelEffects(List.of(new ExternalArtLevelEffect(1, 1.0, 1, 1, 1)))
                .build();

        assertThatThrownBy(() -> t.effectAt(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void effectAt_whenLevelAboveMaxLevel_throws() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("x").description("x").weaponType(WeaponType.SWORD)
                .maxLevel(1)
                .levelEffects(List.of(new ExternalArtLevelEffect(1, 1.0, 1, 1, 1)))
                .build();

        assertThatThrownBy(() -> t.effectAt(2)).isInstanceOf(IllegalArgumentException.class);
    }
}
