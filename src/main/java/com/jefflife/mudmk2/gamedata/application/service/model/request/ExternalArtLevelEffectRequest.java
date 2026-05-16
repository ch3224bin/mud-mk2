package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtLevelEffect;

public record ExternalArtLevelEffectRequest(
        int level,
        double damageMultiplier,
        int cooldownSeconds,
        int apCost,
        int mpCost
) {
    public ExternalArtLevelEffect toDomain() {
        return new ExternalArtLevelEffect(level, damageMultiplier, cooldownSeconds, apCost, mpCost);
    }
}
