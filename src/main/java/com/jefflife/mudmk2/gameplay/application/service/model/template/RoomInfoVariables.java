package com.jefflife.mudmk2.gameplay.application.service.model.template;

import java.util.List;

public record RoomInfoVariables(
        Long userId,
        String roomName,
        String roomDescription,
        String exits,
        List<CreatureInfo> npcsInRoom,
        List<CreatureInfo> otherPlayersInRoom,
        List<CreatureInfo> monstersInRoom
) {
}
