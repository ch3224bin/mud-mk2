package com.jefflife.mudmk2.gamedata.application.service.model.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;

import java.util.List;

public record RoomResponse(
        long id,
        long areaId,
        String name,
        String summary,
        String description,
        List<WayOutResponse> wayOuts,
        String exitString
) {

    public static RoomResponse of(final Room room) {
        return new RoomResponse(
                room.getId(),
                room.getAreaId(),
                room.getName(),
                room.getSummary(),
                room.getDescription(),
                WayOutResponse.of(room.getWayOuts()),
                room.getExitString()
        );
    }
}
