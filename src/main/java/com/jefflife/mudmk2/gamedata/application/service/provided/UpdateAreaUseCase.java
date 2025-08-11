package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Area;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateAreaRequest;

public interface UpdateAreaUseCase {
    Area updateArea(Long id, UpdateAreaRequest updateAreaRequest);
}
