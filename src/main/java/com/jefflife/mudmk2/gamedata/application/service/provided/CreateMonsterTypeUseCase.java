package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateMonsterTypeRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.MonsterTypeResponse;

public interface CreateMonsterTypeUseCase {
    MonsterTypeResponse createMonsterType(CreateMonsterTypeRequest createMonsterTypeRequest);
}
