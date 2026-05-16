package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedExternalArt;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedMentalMethod;
import com.jefflife.mudmk2.gamedata.application.service.exception.AlreadyLearnedException;
import com.jefflife.mudmk2.gamedata.application.service.provided.MartialArtLearner;
import com.jefflife.mudmk2.gamedata.application.service.required.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class MartialArtLearningService implements MartialArtLearner {

    private final LearnedMentalMethodRepository mentalRepo;
    private final LearnedExternalArtRepository externalRepo;
    private final MentalMethodTemplateRepository mentalTplRepo;
    private final ExternalArtTemplateRepository externalTplRepo;
    private final PlayerCharacterRepository pcRepo;

    public MartialArtLearningService(LearnedMentalMethodRepository mentalRepo,
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
    public LearnedMentalMethod learnMentalMethod(UUID pcId, Long templateId) {
        requirePc(pcId);
        if (!mentalTplRepo.existsById(templateId)) {
            throw new NoSuchElementException("MentalMethodTemplate not found: " + templateId);
        }
        if (mentalRepo.existsByPlayerCharacterIdAndMentalMethodTemplateId(pcId, templateId)) {
            throw new AlreadyLearnedException(
                    "already learned MentalMethodTemplate " + templateId + " for character " + pcId);
        }
        return mentalRepo.save(LearnedMentalMethod.create(pcId, templateId));
    }

    @Override
    public LearnedExternalArt learnExternalArt(UUID pcId, Long templateId) {
        requirePc(pcId);
        if (!externalTplRepo.existsById(templateId)) {
            throw new NoSuchElementException("ExternalArtTemplate not found: " + templateId);
        }
        if (externalRepo.existsByPlayerCharacterIdAndExternalArtTemplateId(pcId, templateId)) {
            throw new AlreadyLearnedException(
                    "already learned ExternalArtTemplate " + templateId + " for character " + pcId);
        }
        return externalRepo.save(LearnedExternalArt.create(pcId, templateId));
    }

    private void requirePc(UUID pcId) {
        if (!pcRepo.existsById(pcId)) {
            throw new NoSuchElementException("PlayerCharacter not found: " + pcId);
        }
    }
}
