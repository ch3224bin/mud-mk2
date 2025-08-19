package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterSpawnRoom;

public record MonsterSpawnRoomResponse(
    Long id,
    Long roomId,
    int spawnCount
) {
    public static MonsterSpawnRoomResponse from(MonsterSpawnRoom spawnRoom) {
        return new MonsterSpawnRoomResponse(
                spawnRoom.getId(),
                spawnRoom.getRoomId(),
                spawnRoom.getSpawnCount()
        );
    }
}
