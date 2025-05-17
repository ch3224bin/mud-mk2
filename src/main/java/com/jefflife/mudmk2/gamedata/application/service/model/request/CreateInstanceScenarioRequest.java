package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.instance.InstanceScenario;

public record CreateInstanceScenarioRequest(String title, String description, long areaId, long entranceRoomId) {
    public InstanceScenario toDomain() {
        return InstanceScenario.builder()
                .title(title)
                .description(description)
                .areaId(areaId)
                .entranceRoomId(entranceRoomId)
                .build();
    }
}
