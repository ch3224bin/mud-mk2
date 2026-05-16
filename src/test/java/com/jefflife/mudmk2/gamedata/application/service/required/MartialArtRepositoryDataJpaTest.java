package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({MentalMethodLevelEffectsConverter.class, ExternalArtLevelEffectsConverter.class})
class MartialArtRepositoryDataJpaTest {

    @Autowired MentalMethodTemplateRepository mentalRepo;
    @Autowired ExternalArtTemplateRepository externalRepo;
    @Autowired LearnedMentalMethodRepository learnedMentalRepo;
    @Autowired LearnedExternalArtRepository learnedExternalRepo;

    @Test
    void mentalMethodTemplate_persistsLevelEffectsRoundTrip() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("천뢰신공").description("d").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(2)
                .levelEffects(List.of(
                        new MentalMethodLevelEffect(1, List.of(new StatModifier(StatType.INNER_POWER, 3))),
                        new MentalMethodLevelEffect(2, List.of(new StatModifier(StatType.INNER_POWER, 7)))))
                .build();
        Long id = mentalRepo.save(t).getId();

        MentalMethodTemplate loaded = mentalRepo.findById(id).orElseThrow();
        assertThat(loaded.getLevelEffects()).hasSize(2);
        assertThat(loaded.getLevelEffects().get(1).statModifiers().get(0).getValue()).isEqualTo(7);
    }

    @Test
    void externalArtTemplate_persistsLevelEffectsRoundTrip() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("발검술").description("d").weaponType(WeaponType.SWORD)
                .maxLevel(1)
                .levelEffects(List.of(new ExternalArtLevelEffect(1, 1.2, 5, 5, 0)))
                .build();
        Long id = externalRepo.save(t).getId();

        ExternalArtTemplate loaded = externalRepo.findById(id).orElseThrow();
        assertThat(loaded.getLevelEffects().get(0).damageMultiplier()).isEqualTo(1.2);
    }

    @Test
    void learnedMentalMethod_existsByPlayerAndTemplate() {
        UUID pcId = UUID.randomUUID();
        learnedMentalRepo.save(LearnedMentalMethod.create(pcId, 100L));

        assertThat(learnedMentalRepo.existsByPlayerCharacterIdAndMentalMethodTemplateId(pcId, 100L)).isTrue();
        assertThat(learnedMentalRepo.existsByPlayerCharacterIdAndMentalMethodTemplateId(pcId, 200L)).isFalse();
        assertThat(learnedMentalRepo.existsByMentalMethodTemplateId(100L)).isTrue();
    }

    @Test
    void learnedExternalArt_existsByPlayerAndTemplate() {
        UUID pcId = UUID.randomUUID();
        learnedExternalRepo.save(LearnedExternalArt.create(pcId, 555L));

        assertThat(learnedExternalRepo.existsByPlayerCharacterIdAndExternalArtTemplateId(pcId, 555L)).isTrue();
        assertThat(learnedExternalRepo.existsByExternalArtTemplateId(555L)).isTrue();
    }
}
