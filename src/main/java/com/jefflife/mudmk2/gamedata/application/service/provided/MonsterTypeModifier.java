package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;

public interface MonsterTypeModifier {
    MonsterType updateMonsterType(Long id, UpdateMonsterTypeRequest updateMonsterTypeRequest);
}
