package com.jefflife.mudmk2.game.application.service.model.request;

import com.jefflife.mudmk2.game.application.domain.model.map.Room;

public record CreateRoomRequest(String summary, String description) {
    public Room toDomain() {
        return Room.builder()
                .summary(summary)
                .description(description)
                .build();
    }
}
