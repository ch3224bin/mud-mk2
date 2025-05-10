package com.jefflife.mudmk2.game.application.service.model.response;

import com.jefflife.mudmk2.game.application.domain.model.map.Room;

import java.util.List;

public record RoomResponse(
        long id,
        String summary,
        String description,
        List<WayOutResponse> wayOuts,
        String exitString
) {

    public static RoomResponse of(final Room room) {
        return new RoomResponse(
                room.getId(),
                room.getSummary(),
                room.getDescription(),
                WayOutResponse.of(room.getWayOuts()),
                room.getExitString()
        );
    }
}
