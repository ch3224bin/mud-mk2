package com.jefflife.mudmk2.game.application.port.in;

import com.jefflife.mudmk2.game.application.service.model.request.UpdateRoomRequest;
import com.jefflife.mudmk2.game.application.service.model.response.RoomResponse;

public interface UpdateRoomUseCase {
    RoomResponse updateRoom(long id, UpdateRoomRequest updateRoomRequest);
}
