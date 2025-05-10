package com.jefflife.mudmk2.game.application.port.in;

import com.jefflife.mudmk2.game.application.service.model.response.AreaResponse;

import java.util.List;

public interface GetAreaUseCase {
    AreaResponse getArea(Long id);
    List<AreaResponse> getAreas();
}
