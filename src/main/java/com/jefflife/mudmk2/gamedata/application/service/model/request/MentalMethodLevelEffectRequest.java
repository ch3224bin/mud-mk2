package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodLevelEffect;

import java.util.List;

public record MentalMethodLevelEffectRequest(int level, List<StatModifierRequest> statModifiers) {
    public MentalMethodLevelEffect toDomain() {
        List<StatModifier> mods = statModifiers == null
                ? List.of()
                : statModifiers.stream().map(StatModifierRequest::toDomain).toList();
        return new MentalMethodLevelEffect(level, mods);
    }
}
