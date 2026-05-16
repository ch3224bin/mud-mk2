package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedExternalArt;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedMentalMethod;

import java.util.UUID;

public interface MartialArtLearner {
    LearnedMentalMethod learnMentalMethod(UUID playerCharacterId, Long templateId);
    LearnedExternalArt learnExternalArt(UUID playerCharacterId, Long templateId);
}
