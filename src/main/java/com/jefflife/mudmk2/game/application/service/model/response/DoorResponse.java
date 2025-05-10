package com.jefflife.mudmk2.game.application.service.model.response;

import com.jefflife.mudmk2.game.application.domain.model.map.Door;

public record DoorResponse(long id, boolean isLocked) {

    public static DoorResponse of(final Door door) {
        return new DoorResponse(door.getId(), door.isLocked());
    }
}
