package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;

import java.util.Optional;
import java.util.UUID;

public interface ActivePartyRepository {
    Optional<Party> findById(UUID id);
    Iterable<Party> findAll();
    void add(Party party);
    void remove(UUID id);
}
