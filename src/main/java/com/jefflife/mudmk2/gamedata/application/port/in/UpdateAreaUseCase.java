package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateAreaRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.AreaResponse;

public interface UpdateAreaUseCase {
    AreaResponse updateArea(Long id, UpdateAreaRequest updateAreaRequest);
}
