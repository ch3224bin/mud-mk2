package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtTemplateInUseException;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodLevelEffectRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.StatModifierRequest;
import com.jefflife.mudmk2.gamedata.application.service.required.LearnedMentalMethodRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.MentalMethodTemplateRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MentalMethodTemplateServiceTest {

    private final MentalMethodTemplateRepository repo = mock(MentalMethodTemplateRepository.class);
    private final LearnedMentalMethodRepository learnedRepo = mock(LearnedMentalMethodRepository.class);
    private final MentalMethodTemplateService service = new MentalMethodTemplateService(repo, learnedRepo);

    private MentalMethodTemplateRequest req(int maxLevel) {
        return new MentalMethodTemplateRequest("천뢰신공", "d",
                MentalMethodKind.INNER_POWER, maxLevel,
                List.of(new MentalMethodLevelEffectRequest(1,
                        List.of(new StatModifierRequest(StatType.INNER_POWER, 3)))));
    }

    @Test
    void create_persistsTemplate() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(req(1));

        ArgumentCaptor<MentalMethodTemplate> captor = ArgumentCaptor.forClass(MentalMethodTemplate.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("천뢰신공");
    }

    @Test
    void update_modifiesExisting() {
        MentalMethodTemplate existing = MentalMethodTemplate.builder()
                .name("old").description("old").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(1).levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build();
        when(repo.findById(1L)).thenReturn(Optional.of(existing));

        service.update(1L, req(1));

        assertThat(existing.getName()).isEqualTo("천뢰신공");
    }

    @Test
    void findById_whenMissing_throwsNoSuchElement() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void delete_whenLearned_throws() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER).maxLevel(1)
                .levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build();
        when(repo.findById(7L)).thenReturn(Optional.of(t));
        when(learnedRepo.existsByMentalMethodTemplateId(7L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOf(MartialArtTemplateInUseException.class);
        verify(repo, never()).delete(any());
    }

    @Test
    void delete_whenNotLearned_deletes() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER).maxLevel(1)
                .levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build();
        when(repo.findById(7L)).thenReturn(Optional.of(t));
        when(learnedRepo.existsByMentalMethodTemplateId(7L)).thenReturn(false);

        service.delete(7L);

        verify(repo).delete(t);
    }
}
