package com.jefflife.mudmk2.gameplay.application.domain.model.look;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Direction;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;

import java.util.Map;

public record DirectionLookable(Direction direction, Room room) implements Lookable {
    @Override
    public String getName() {
        return "";
    }

    @Override
    public Map<String, Object> getProperties() {
        return Map.of();
    }
}
