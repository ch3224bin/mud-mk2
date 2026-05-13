package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

import java.util.Optional;
import java.util.UUID;

public interface ActivePlayerRepository {
    Optional<PlayerCharacter> findById(UUID id);
    Optional<PlayerCharacter> findByUserId(Long userId);
    Iterable<PlayerCharacter> findAll();
    void add(PlayerCharacter player);
    void removeByUserId(Long userId);
}
