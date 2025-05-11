package com.jefflife.mudmk2.gamedata.application.domain.repository;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import org.springframework.data.repository.CrudRepository;

public interface NonPlayerCharacterRepository extends CrudRepository<NonPlayerCharacter, Long> {
}
