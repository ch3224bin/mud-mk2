package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;

public interface NonPlayerCharacterCreator {
    NonPlayerCharacter createNonPlayerCharacter(CreateNonPlayerCharacterRequest createNonPlayerCharacterRequest);
}
