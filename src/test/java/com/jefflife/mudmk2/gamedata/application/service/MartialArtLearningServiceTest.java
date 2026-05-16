package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.service.exception.AlreadyLearnedException;
import com.jefflife.mudmk2.gamedata.application.service.required.*;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MartialArtLearningServiceTest {

    private final LearnedMentalMethodRepository mentalRepo = mock(LearnedMentalMethodRepository.class);
    private final LearnedExternalArtRepository externalRepo = mock(LearnedExternalArtRepository.class);
    private final MentalMethodTemplateRepository mentalTplRepo = mock(MentalMethodTemplateRepository.class);
    private final ExternalArtTemplateRepository externalTplRepo = mock(ExternalArtTemplateRepository.class);
    private final PlayerCharacterRepository pcRepo = mock(PlayerCharacterRepository.class);

    private final MartialArtLearningService service = new MartialArtLearningService(
            mentalRepo, externalRepo, mentalTplRepo, externalTplRepo, pcRepo);

    @Test
    void learnMentalMethod_savesNew() {
        UUID pc = UUID.randomUUID();
        when(pcRepo.existsById(pc)).thenReturn(true);
        when(mentalTplRepo.existsById(1L)).thenReturn(true);
        when(mentalRepo.existsByPlayerCharacterIdAndMentalMethodTemplateId(pc, 1L)).thenReturn(false);
        when(mentalRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.learnMentalMethod(pc, 1L);

        verify(mentalRepo).save(any(LearnedMentalMethod.class));
    }

    @Test
    void learnMentalMethod_whenDuplicate_throws() {
        UUID pc = UUID.randomUUID();
        when(pcRepo.existsById(pc)).thenReturn(true);
        when(mentalTplRepo.existsById(1L)).thenReturn(true);
        when(mentalRepo.existsByPlayerCharacterIdAndMentalMethodTemplateId(pc, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.learnMentalMethod(pc, 1L))
                .isInstanceOf(AlreadyLearnedException.class);
    }

    @Test
    void learnMentalMethod_whenPcMissing_throwsNoSuchElement() {
        UUID pc = UUID.randomUUID();
        when(pcRepo.existsById(pc)).thenReturn(false);
        assertThatThrownBy(() -> service.learnMentalMethod(pc, 1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void learnMentalMethod_whenTemplateMissing_throwsNoSuchElement() {
        UUID pc = UUID.randomUUID();
        when(pcRepo.existsById(pc)).thenReturn(true);
        when(mentalTplRepo.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.learnMentalMethod(pc, 99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void learnExternalArt_duplicate_throws() {
        UUID pc = UUID.randomUUID();
        when(pcRepo.existsById(pc)).thenReturn(true);
        when(externalTplRepo.existsById(2L)).thenReturn(true);
        when(externalRepo.existsByPlayerCharacterIdAndExternalArtTemplateId(pc, 2L)).thenReturn(true);

        assertThatThrownBy(() -> service.learnExternalArt(pc, 2L))
                .isInstanceOf(AlreadyLearnedException.class);
    }
}
