package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;

public record StatModifierRequest(StatType statType, int value) {
    public StatModifier toDomain() {
        return new StatModifier(statType, value);
    }
}
