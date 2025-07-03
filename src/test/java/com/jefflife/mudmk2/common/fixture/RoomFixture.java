package com.jefflife.mudmk2.common.fixture;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomRegisterRequest;

public class RoomFixture {
    public static RoomRegisterRequest createRoomRegisterRequest() {
        return new RoomRegisterRequest(1L, "name", "summary", "description");
    }
}
