package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.service.model.response.NonPlayerCharacterResponse;

import java.util.List;

public interface GetNonPlayerCharacterUseCase {
    NonPlayerCharacterResponse getNonPlayerCharacter(Long id);
    List<NonPlayerCharacterResponse> getAllNonPlayerCharacters();
}