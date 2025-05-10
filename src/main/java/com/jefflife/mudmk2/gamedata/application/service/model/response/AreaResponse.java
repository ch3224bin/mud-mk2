package com.jefflife.mudmk2.gamedata.application.service.model.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Area;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.AreaType;
import lombok.Getter;

@Getter
public class AreaResponse {
    private final Long id;
    private final String name;
    private final AreaType type;

    private AreaResponse(Long id, String name, AreaType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public static AreaResponse of(Area area) {
        return new AreaResponse(area.getId(), area.getName(), area.getType());
    }
}
