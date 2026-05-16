package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodLevelEffect;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplate;

import java.util.List;

public record MentalMethodTemplateResponse(
        Long id, String name, String description,
        MentalMethodKind kind, int maxLevel,
        List<MentalMethodLevelEffect> levelEffects
) {
    public static MentalMethodTemplateResponse from(MentalMethodTemplate t) {
        return new MentalMethodTemplateResponse(
                t.getId(), t.getName(), t.getDescription(),
                t.getKind(), t.getMaxLevel(), t.getLevelEffects());
    }
}
