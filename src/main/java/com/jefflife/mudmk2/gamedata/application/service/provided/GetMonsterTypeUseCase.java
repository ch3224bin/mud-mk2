package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.response.MonsterTypeResponse;

import java.util.List;

public interface GetMonsterTypeUseCase {
    MonsterTypeResponse getMonsterType(Long id);
    List<MonsterTypeResponse> getAllMonsterTypes();
}
