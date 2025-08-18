package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Area;

import java.util.List;

public interface AreaFinder {
    Area getArea(Long id);
    List<Area> getAreas();
}
