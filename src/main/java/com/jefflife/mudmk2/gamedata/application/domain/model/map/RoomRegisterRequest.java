package com.jefflife.mudmk2.gamedata.application.domain.model.map;

public record RoomRegisterRequest(long areaId, String name, String summary, String description) {
    public Room toDomain() {
        return Room.builder()
                .areaId(areaId)
                .name(name)
                .summary(summary)
                .description(description)
                .wayOuts(new WayOuts())
                .build();
    }
}
