package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;

import java.util.UUID;

public interface MartialArtEquipper {
    void equipMentalMethod(UUID playerCharacterId, UUID learnedId);
    void unequipMentalMethod(UUID playerCharacterId, MentalMethodKind kind);
    void equipExternalArt(UUID playerCharacterId, UUID learnedId);
    void unequipExternalArt(UUID playerCharacterId, UUID learnedId);
}
