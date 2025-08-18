package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomRegisterRequest;
import jakarta.validation.Valid;

public interface RoomCreator {
    Room register(@Valid RoomRegisterRequest roomRegisterRequest);
}
