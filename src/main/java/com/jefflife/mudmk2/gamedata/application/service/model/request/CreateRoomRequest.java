package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.WayOuts;

public record CreateRoomRequest(long areaId, String name, String summary, String description) {
    public Room toDomain() {
        return Room.builder()
                .areaId(areaId)
                .name(name)
                .summary(summary)
                .description(description)
                .wayOuts(new WayOuts())
                .build();
    }
}
