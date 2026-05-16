package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtTemplateInUseException;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtLevelEffectRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.required.ExternalArtTemplateRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.LearnedExternalArtRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ExternalArtTemplateServiceTest {

    private final ExternalArtTemplateRepository repo = mock(ExternalArtTemplateRepository.class);
    private final LearnedExternalArtRepository learnedRepo = mock(LearnedExternalArtRepository.class);
    private final ExternalArtTemplateService service = new ExternalArtTemplateService(repo, learnedRepo);

    private ExternalArtTemplateRequest req() {
        return new ExternalArtTemplateRequest("발검술", "d", WeaponType.SWORD, 1,
                List.of(new ExternalArtLevelEffectRequest(1, 1.1, 5, 5, 0)));
    }

    @Test
    void create_persists() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        service.create(req());
        verify(repo).save(any());
    }

    @Test
    void delete_whenLearned_throws() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("x").description("x").weaponType(WeaponType.SWORD).maxLevel(1)
                .levelEffects(List.of(new ExternalArtLevelEffect(1, 1.0, 1, 1, 0)))
                .build();
        when(repo.findById(5L)).thenReturn(Optional.of(t));
        when(learnedRepo.existsByExternalArtTemplateId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(5L))
                .isInstanceOf(MartialArtTemplateInUseException.class);
    }

    @Test
    void findById_whenMissing_throws() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(NoSuchElementException.class);
    }
}
