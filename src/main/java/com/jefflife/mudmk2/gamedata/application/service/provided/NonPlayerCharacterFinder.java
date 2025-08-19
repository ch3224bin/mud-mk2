package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;

import java.util.List;
import java.util.UUID;

public interface NonPlayerCharacterFinder {
    NonPlayerCharacter getNonPlayerCharacter(UUID id);
    List<NonPlayerCharacter> getAllNonPlayerCharacters();
}