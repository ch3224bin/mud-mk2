package com.jefflife.mudmk2.gamedata.application.domain.repository;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface NonPlayerCharacterRepository extends CrudRepository<NonPlayerCharacter, UUID> {
}
