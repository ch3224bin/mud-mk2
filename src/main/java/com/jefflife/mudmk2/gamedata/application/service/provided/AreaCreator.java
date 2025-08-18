package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Area;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateAreaRequest;

public interface AreaCreator {
    Area createArea(CreateAreaRequest createAreaRequest);
}
