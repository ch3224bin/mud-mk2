package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.NonPlayerCharacterResponse;

public interface UpdateNonPlayerCharacterUseCase {
    NonPlayerCharacterResponse updateNonPlayerCharacter(Long id, UpdateNonPlayerCharacterRequest updateNonPlayerCharacterRequest);
}