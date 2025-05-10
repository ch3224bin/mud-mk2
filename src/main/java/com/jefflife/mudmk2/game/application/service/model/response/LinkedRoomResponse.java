package com.jefflife.mudmk2.game.application.service.model.response;

import com.jefflife.mudmk2.game.application.domain.model.map.Room;

import java.util.List;

public record LinkedRoomResponse(List<RoomResponse> linkedRooms) {

    public static LinkedRoomResponse of(Room room1, Room room2) {
        return new LinkedRoomResponse(List.of(RoomResponse.of(room1), RoomResponse.of(room2)));
    }
}
