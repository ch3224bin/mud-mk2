package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;

import java.util.List;

public record MentalMethodLevelEffect(
        int level,
        List<StatModifier> statModifiers
) {
    public MentalMethodLevelEffect {
        if (level < 1) throw new IllegalArgumentException("level must be >= 1: " + level);
        statModifiers = statModifiers == null ? List.of() : List.copyOf(statModifiers);
    }
}
