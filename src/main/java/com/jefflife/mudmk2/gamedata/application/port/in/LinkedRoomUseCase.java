package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.service.model.request.LinkRoomRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.LinkedRoomResponse;

public interface LinkedRoomUseCase {
    LinkedRoomResponse linkAnotherRoom(LinkRoomRequest linkRoomRequest);
}
