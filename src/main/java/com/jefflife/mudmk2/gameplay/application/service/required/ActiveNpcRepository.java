package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;

import java.util.Optional;
import java.util.UUID;

public interface ActiveNpcRepository {
    Optional<NonPlayerCharacter> findById(UUID id);
    Iterable<NonPlayerCharacter> findAll();
    void add(NonPlayerCharacter npc);
    void remove(UUID id);
}
