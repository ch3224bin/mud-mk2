package com.jefflife.mudmk2.gamedata.application.service.model.request;

public record MonsterSpawnRoomRequest(
    Long roomId,
    int spawnCount
) {}
