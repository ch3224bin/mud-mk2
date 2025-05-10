package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateRoomRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.RoomResponse;

public interface UpdateRoomUseCase {
    RoomResponse updateRoom(long id, UpdateRoomRequest updateRoomRequest);
}
