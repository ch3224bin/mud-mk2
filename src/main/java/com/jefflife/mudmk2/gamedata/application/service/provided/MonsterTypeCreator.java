package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;

public interface MonsterTypeCreator {
    MonsterType createMonsterType(CreateMonsterTypeRequest createMonsterTypeRequest);
}
