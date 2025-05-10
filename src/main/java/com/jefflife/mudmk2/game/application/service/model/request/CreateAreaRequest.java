package com.jefflife.mudmk2.game.application.service.model.request;

import com.jefflife.mudmk2.game.application.domain.model.map.Area;
import com.jefflife.mudmk2.game.application.domain.model.map.AreaType;

public class CreateAreaRequest {
    private final String name;
    private final AreaType type;

    public CreateAreaRequest(String name, String type) {
        this.name = name;
        this.type = AreaType.valueOf(type);
    }

    public Area toDomain() {
        return Area.builder()
                .name(name)
                .type(type)
                .build();
    }
}
