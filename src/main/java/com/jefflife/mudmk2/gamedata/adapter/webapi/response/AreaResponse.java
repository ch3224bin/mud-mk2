package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Area;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.AreaType;
import lombok.Getter;

@Getter
public class AreaResponse {
    private final Long id;
    private final String name;
    private final AreaType type;

    @JsonCreator
    private AreaResponse(@JsonProperty("id") Long id, @JsonProperty("name") String name, @JsonProperty("type") AreaType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public static AreaResponse of(Area area) {
        return new AreaResponse(area.getId(), area.getName(), area.getType());
    }
}
