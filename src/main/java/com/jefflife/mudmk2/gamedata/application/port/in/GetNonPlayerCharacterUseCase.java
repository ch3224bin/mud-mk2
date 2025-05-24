package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.service.model.response.NonPlayerCharacterResponse;

import java.util.List;
import java.util.UUID;

public interface GetNonPlayerCharacterUseCase {
    NonPlayerCharacterResponse getNonPlayerCharacter(UUID id);
    List<NonPlayerCharacterResponse> getAllNonPlayerCharacters();
}