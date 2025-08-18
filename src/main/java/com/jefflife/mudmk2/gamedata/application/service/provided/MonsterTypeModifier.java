package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.MonsterTypeResponse;

public interface MonsterTypeModifier {
    MonsterTypeResponse updateMonsterType(Long id, UpdateMonsterTypeRequest updateMonsterTypeRequest);
}
