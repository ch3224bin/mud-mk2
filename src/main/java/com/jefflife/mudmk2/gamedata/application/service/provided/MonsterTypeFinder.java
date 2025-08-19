package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;

import java.util.List;

public interface MonsterTypeFinder {
    MonsterType getMonsterType(Long id);
    List<MonsterType> getAllMonsterTypes();
}
