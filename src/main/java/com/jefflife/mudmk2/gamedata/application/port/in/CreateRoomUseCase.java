package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateRoomRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.RoomResponse;

public interface CreateRoomUseCase {
    RoomResponse createRoom(CreateRoomRequest createRoomRequest);
}
