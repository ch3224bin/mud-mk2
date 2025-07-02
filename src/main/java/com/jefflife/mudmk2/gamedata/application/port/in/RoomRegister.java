package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomRegisterRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.RoomResponse;

public interface RoomRegister {
    RoomResponse register(RoomRegisterRequest roomRegisterRequest);
}
