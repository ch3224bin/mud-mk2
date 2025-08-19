package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateNonPlayerCharacterRequest;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;

import java.util.UUID;

public interface NonPlayerCharacterModifier {
    NonPlayerCharacter updateNonPlayerCharacter(UUID id, UpdateNonPlayerCharacterRequest updateNonPlayerCharacterRequest);
}