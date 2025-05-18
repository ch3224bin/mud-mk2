package com.jefflife.mudmk2.gameplay.application.service.model.template;

import java.util.List;

public record RoomInfoVariables(
        Long userId,
        String roomName,
        String roomDescription,
        String exits,
        List<String> npcsInRoom,
        List<String> otherPlayersInRoom
) {
}
