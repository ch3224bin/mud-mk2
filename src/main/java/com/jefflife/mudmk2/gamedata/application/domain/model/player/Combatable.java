package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import java.util.UUID;

public interface Combatable {
    UUID getId();
    Long getCurrentRoomId();
    String getName();
    CharacterStats getStats();
}
