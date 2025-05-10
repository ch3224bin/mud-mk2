package com.jefflife.mudmk2.game.application.port.in;

import com.jefflife.mudmk2.game.application.service.model.request.CreateRoomRequest;
import com.jefflife.mudmk2.game.application.service.model.response.RoomResponse;

public interface CreateRoomUseCase {
    RoomResponse createRoom(CreateRoomRequest createRoomRequest);
}
