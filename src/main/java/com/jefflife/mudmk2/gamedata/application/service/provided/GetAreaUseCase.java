package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.response.AreaResponse;

import java.util.List;

public interface GetAreaUseCase {
    AreaResponse getArea(Long id);
    List<AreaResponse> getAreas();
}
