package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateAreaRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.AreaResponse;

public interface CreateAreaUseCase {
    AreaResponse createArea(CreateAreaRequest createAreaRequest);
}
