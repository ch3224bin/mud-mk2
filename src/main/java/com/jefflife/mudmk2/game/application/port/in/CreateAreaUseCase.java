package com.jefflife.mudmk2.game.application.port.in;

import com.jefflife.mudmk2.game.application.service.model.request.CreateAreaRequest;
import com.jefflife.mudmk2.game.application.service.model.response.AreaResponse;

public interface CreateAreaUseCase {
    AreaResponse createArea(CreateAreaRequest createAreaRequest);
}
