package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodLevelEffect;

import java.util.List;

public record MentalMethodTemplateRequest(
        String name,
        String description,
        MentalMethodKind kind,
        int maxLevel,
        List<MentalMethodLevelEffectRequest> levelEffects
) {
    public List<MentalMethodLevelEffect> levelEffectsDomain() {
        return levelEffects == null ? List.of()
                : levelEffects.stream().map(MentalMethodLevelEffectRequest::toDomain).toList();
    }
}
