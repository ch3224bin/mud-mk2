package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.NonPlayerCharacterResponse;

import java.util.UUID;

public interface UpdateNonPlayerCharacterUseCase {
    NonPlayerCharacterResponse updateNonPlayerCharacter(UUID id, UpdateNonPlayerCharacterRequest updateNonPlayerCharacterRequest);
}