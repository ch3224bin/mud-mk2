package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.NonPlayerCharacterResponse;

public interface NonPlayerCharacterCreator {
    NonPlayerCharacterResponse createNonPlayerCharacter(CreateNonPlayerCharacterRequest createNonPlayerCharacterRequest);
}
