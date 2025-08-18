package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Area;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.AreaModifyRequest;
import jakarta.validation.Valid;

public interface AreaModifier {
    Area updateArea(Long id, @Valid AreaModifyRequest areaModifyRequest);
}
