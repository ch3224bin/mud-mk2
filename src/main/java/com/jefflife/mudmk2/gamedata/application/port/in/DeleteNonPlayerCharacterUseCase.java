package com.jefflife.mudmk2.gamedata.application.port.in;

import java.util.UUID;

public interface DeleteNonPlayerCharacterUseCase {
    void deleteNonPlayerCharacter(UUID id);
}