package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomUpdateRequest;
import jakarta.validation.Valid;

public interface RoomUpdater {
    Room update(long id, @Valid RoomUpdateRequest roomUpdateRequest);
}
