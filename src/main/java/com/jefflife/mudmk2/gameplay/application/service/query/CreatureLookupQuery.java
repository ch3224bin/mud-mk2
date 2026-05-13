package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

import java.util.List;
import java.util.Optional;

public interface CreatureLookupQuery {
    Optional<PlayerCharacter>    findPlayerByName(String name);
    Optional<NonPlayerCharacter> findNpcByName(String name);
    List<Monster>                findMonstersByType(Long monsterTypeId);
}
