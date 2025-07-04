package com.jefflife.mudmk2.gameplay.application.service.command.look;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Direction;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;

import java.util.Map;

public record DirectionLookable(Direction direction, Room room) implements Lookable {
    @Override
    public String getName() {
        return room.getName();
    }

    @Override
    public LookableType getType() {
        return LookableType.DIRECTION;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Map.of(
                "summary", room.getSummary()
        );
    }
}
