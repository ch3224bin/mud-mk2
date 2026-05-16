package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.exception.NotLearnedException;
import com.jefflife.mudmk2.gamedata.application.service.provided.LearnedMartialArtFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.MartialArtEquipper;
import com.jefflife.mudmk2.gamedata.application.service.required.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class MartialArtEquipService implements MartialArtEquipper, LearnedMartialArtFinder {

    private final LearnedMentalMethodRepository mentalRepo;
    private final LearnedExternalArtRepository externalRepo;
    private final MentalMethodTemplateRepository mentalTplRepo;
    private final ExternalArtTemplateRepository externalTplRepo;
    private final PlayerCharacterRepository pcRepo;

    public MartialArtEquipService(LearnedMentalMethodRepository mentalRepo,
                                  LearnedExternalArtRepository externalRepo,
                                  MentalMethodTemplateRepository mentalTplRepo,
                                  ExternalArtTemplateRepository externalTplRepo,
                                  PlayerCharacterRepository pcRepo) {
        this.mentalRepo = mentalRepo;
        this.externalRepo = externalRepo;
        this.mentalTplRepo = mentalTplRepo;
        this.externalTplRepo = externalTplRepo;
        this.pcRepo = pcRepo;
    }

    @Override
    public void equipMentalMethod(UUID pcId, UUID learnedId) {
        PlayerCharacter pc = requirePc(pcId);
        LearnedMentalMethod learned = mentalRepo.findByIdAndPlayerCharacterId(learnedId, pcId)
                .orElseThrow(() -> new NotLearnedException(
                        "LearnedMentalMethod " + learnedId + " not learned by character " + pcId));
        MentalMethodTemplate tpl = mentalTplRepo.findById(learned.getMentalMethodTemplateId())
                .orElseThrow(() -> new NoSuchElementException(
                        "MentalMethodTemplate not found: " + learned.getMentalMethodTemplateId()));
        pc.getEquippedMartialArts().equipMental(tpl.getKind(), learned.getId());
    }

    @Override
    public void unequipMentalMethod(UUID pcId, MentalMethodKind kind) {
        PlayerCharacter pc = requirePc(pcId);
        pc.getEquippedMartialArts().unequipMental(kind);
    }

    @Override
    public void equipExternalArt(UUID pcId, UUID learnedId) {
        PlayerCharacter pc = requirePc(pcId);
        LearnedExternalArt learned = externalRepo.findByIdAndPlayerCharacterId(learnedId, pcId)
                .orElseThrow(() -> new NotLearnedException(
                        "LearnedExternalArt " + learnedId + " not learned by character " + pcId));
        pc.getEquippedMartialArts().equipExternal(learned.getId());
    }

    @Override
    public void unequipExternalArt(UUID pcId, UUID learnedId) {
        PlayerCharacter pc = requirePc(pcId);
        pc.getEquippedMartialArts().unequipExternal(learnedId);
    }

    @Override
    @Transactional(readOnly = true)
    public CharacterMartialArtView findByCharacter(UUID pcId) {
        PlayerCharacter pc = requirePc(pcId);
        return new CharacterMartialArtView(
                pcId,
                mentalRepo.findAllByPlayerCharacterId(pcId),
                externalRepo.findAllByPlayerCharacterId(pcId),
                pc.getEquippedMartialArts()
        );
    }

    private PlayerCharacter requirePc(UUID pcId) {
        return pcRepo.findById(pcId)
                .orElseThrow(() -> new NoSuchElementException("PlayerCharacter not found: " + pcId));
    }
}
