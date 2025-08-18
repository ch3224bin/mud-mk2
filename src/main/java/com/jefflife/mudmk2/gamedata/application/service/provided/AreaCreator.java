package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Area;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.CreateAreaRequest;
import jakarta.validation.Valid;

public interface AreaCreator {
    Area createArea(@Valid CreateAreaRequest createAreaRequest);
}
