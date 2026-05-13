package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;

import java.util.Optional;
import java.util.UUID;

public interface ActiveMonsterRepository {
    Optional<Monster> findById(UUID id);
    Iterable<Monster> findAll();
    void add(Monster monster);
    void remove(UUID id);
}
