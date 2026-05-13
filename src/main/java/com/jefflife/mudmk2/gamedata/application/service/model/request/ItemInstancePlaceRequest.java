package com.jefflife.mudmk2.gamedata.application.service.model.request;

public record ItemInstancePlaceRequest(
    Long templateId,
    int quantity,
    LocationType locationType,
    String locationId
) {
    public enum LocationType { ROOM, CHARACTER }
}
