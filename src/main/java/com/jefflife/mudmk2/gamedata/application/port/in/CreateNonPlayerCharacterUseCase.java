package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.NonPlayerCharacterResponse;

public interface CreateNonPlayerCharacterUseCase {
    NonPlayerCharacterResponse createNonPlayerCharacter(CreateNonPlayerCharacterRequest createNonPlayerCharacterRequest);
}
