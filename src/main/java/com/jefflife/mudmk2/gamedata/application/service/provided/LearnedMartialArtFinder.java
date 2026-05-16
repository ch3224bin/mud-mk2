package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.EquippedMartialArts;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedExternalArt;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedMentalMethod;

import java.util.List;
import java.util.UUID;

public interface LearnedMartialArtFinder {

    record CharacterMartialArtView(
            UUID playerCharacterId,
            List<LearnedMentalMethod> learnedMentalMethods,
            List<LearnedExternalArt> learnedExternalArts,
            EquippedMartialArts equipped
    ) {}

    CharacterMartialArtView findByCharacter(UUID playerCharacterId);
}
