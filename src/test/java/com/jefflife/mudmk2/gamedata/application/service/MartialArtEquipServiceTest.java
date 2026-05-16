package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.exception.NotLearnedException;
import com.jefflife.mudmk2.gamedata.application.service.provided.LearnedMartialArtFinder.CharacterMartialArtView;
import com.jefflife.mudmk2.gamedata.application.service.required.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MartialArtEquipServiceTest {

    private final LearnedMentalMethodRepository mentalRepo = mock(LearnedMentalMethodRepository.class);
    private final LearnedExternalArtRepository externalRepo = mock(LearnedExternalArtRepository.class);
    private final MentalMethodTemplateRepository mentalTplRepo = mock(MentalMethodTemplateRepository.class);
    private final ExternalArtTemplateRepository externalTplRepo = mock(ExternalArtTemplateRepository.class);
    private final PlayerCharacterRepository pcRepo = mock(PlayerCharacterRepository.class);

    private final MartialArtEquipService service = new MartialArtEquipService(
            mentalRepo, externalRepo, mentalTplRepo, externalTplRepo, pcRepo);

    private PlayerCharacter pcWithEquipped(UUID pcId, EquippedMartialArts equipped) {
        PlayerCharacter pc = mock(PlayerCharacter.class);
        doReturn(pcId).when(pc).getId();
        doReturn(equipped).when(pc).getEquippedMartialArts();
        return pc;
    }

    @Test
    void equipMentalMethod_lookUpKind_andCallsEquipMental() {
        UUID pc = UUID.randomUUID();
        UUID learnedId = UUID.randomUUID();
        EquippedMartialArts eq = EquippedMartialArts.create();
        PlayerCharacter pcMock = pcWithEquipped(pc, eq);
        when(pcRepo.findById(pc)).thenReturn(Optional.of(pcMock));

        LearnedMentalMethod learned = LearnedMentalMethod.create(pc, 10L);
        when(mentalRepo.findByIdAndPlayerCharacterId(learnedId, pc)).thenReturn(Optional.of(learned));
        MentalMethodTemplate tpl = MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.LIGHT_STEP).maxLevel(1)
                .levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build();
        when(mentalTplRepo.findById(10L)).thenReturn(Optional.of(tpl));

        service.equipMentalMethod(pc, learnedId);

        assertThat(eq.getMentalSlots()).containsKey(MentalMethodKind.LIGHT_STEP);
    }

    @Test
    void equipMentalMethod_notLearned_throws() {
        UUID pc = UUID.randomUUID();
        UUID learnedId = UUID.randomUUID();
        PlayerCharacter pcMock = pcWithEquipped(pc, EquippedMartialArts.create());
        when(pcRepo.findById(pc)).thenReturn(Optional.of(pcMock));
        when(mentalRepo.findByIdAndPlayerCharacterId(learnedId, pc)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.equipMentalMethod(pc, learnedId))
                .isInstanceOf(NotLearnedException.class);
    }

    @Test
    void equipExternalArt_appendsToSlots() {
        UUID pc = UUID.randomUUID();
        UUID learnedId = UUID.randomUUID();
        EquippedMartialArts eq = EquippedMartialArts.create();
        PlayerCharacter pcMock = pcWithEquipped(pc, eq);
        when(pcRepo.findById(pc)).thenReturn(Optional.of(pcMock));
        when(externalRepo.findByIdAndPlayerCharacterId(learnedId, pc))
                .thenReturn(Optional.of(LearnedExternalArt.create(pc, 1L)));

        service.equipExternalArt(pc, learnedId);

        assertThat(eq.getExternalSlots()).hasSize(1);
    }

    @Test
    void unequipMentalMethod_removesSlot() {
        UUID pc = UUID.randomUUID();
        EquippedMartialArts eq = EquippedMartialArts.create();
        UUID learned = UUID.randomUUID();
        eq.equipMental(MentalMethodKind.INNER_POWER, learned);

        PlayerCharacter pcMock = pcWithEquipped(pc, eq);
        when(pcRepo.findById(pc)).thenReturn(Optional.of(pcMock));

        service.unequipMentalMethod(pc, MentalMethodKind.INNER_POWER);

        assertThat(eq.getMentalSlots()).doesNotContainKey(MentalMethodKind.INNER_POWER);
    }

    @Test
    void findByCharacter_returnsBundle() {
        UUID pc = UUID.randomUUID();
        EquippedMartialArts eq = EquippedMartialArts.create();
        PlayerCharacter pcMock = pcWithEquipped(pc, eq);
        when(pcRepo.findById(pc)).thenReturn(Optional.of(pcMock));
        when(mentalRepo.findAllByPlayerCharacterId(pc)).thenReturn(List.of(LearnedMentalMethod.create(pc, 1L)));
        when(externalRepo.findAllByPlayerCharacterId(pc)).thenReturn(List.of());

        CharacterMartialArtView view = service.findByCharacter(pc);
        assertThat(view.learnedMentalMethods()).hasSize(1);
        assertThat(view.learnedExternalArts()).isEmpty();
        assertThat(view.equipped()).isEqualTo(eq);
    }

    @Test
    void equipMentalMethod_pcMissing_throwsNoSuchElement() {
        when(pcRepo.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.equipMentalMethod(UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(NoSuchElementException.class);
    }
}
