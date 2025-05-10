package com.jefflife.mudmk2.game.application.port.in;

import com.jefflife.mudmk2.game.application.service.model.request.LinkRoomRequest;
import com.jefflife.mudmk2.game.application.service.model.response.LinkedRoomResponse;

public interface LinkedRoomUseCase {
    LinkedRoomResponse linkAnotherRoom(LinkRoomRequest linkRoomRequest);
}
