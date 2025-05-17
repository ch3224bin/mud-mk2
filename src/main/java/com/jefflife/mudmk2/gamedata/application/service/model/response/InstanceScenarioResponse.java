package com.jefflife.mudmk2.gamedata.application.service.model.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.instance.InstanceScenario;

public record InstanceScenarioResponse(
        long id,
        String title,
        String description,
        long areaId,
        long entranceRoomId
) {
    public static InstanceScenarioResponse of(InstanceScenario scenario) {
        return new InstanceScenarioResponse(
                scenario.getId(),
                scenario.getTitle(),
                scenario.getDescription(),
                scenario.getAreaId(),
                scenario.getEntranceRoomId()
        );
    }
}
