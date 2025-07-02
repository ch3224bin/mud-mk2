package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomUpdateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.RoomResponse;
import jakarta.validation.Valid;

public interface RoomUpdater {
    RoomResponse update(long id, @Valid RoomUpdateRequest roomUpdateRequest);
}
