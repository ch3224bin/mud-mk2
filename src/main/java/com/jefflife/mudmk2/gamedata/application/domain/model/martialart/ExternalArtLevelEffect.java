package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

public record ExternalArtLevelEffect(
        int level,
        double damageMultiplier,
        int cooldownSeconds,
        int apCost,
        int mpCost
) {
    public ExternalArtLevelEffect {
        if (level < 1) throw new IllegalArgumentException("level must be >= 1: " + level);
        if (damageMultiplier < 0) throw new IllegalArgumentException("damageMultiplier must be >= 0: " + damageMultiplier);
        if (cooldownSeconds < 0) throw new IllegalArgumentException("cooldownSeconds must be >= 0: " + cooldownSeconds);
        if (apCost < 0) throw new IllegalArgumentException("apCost must be >= 0: " + apCost);
        if (mpCost < 0) throw new IllegalArgumentException("mpCost must be >= 0: " + mpCost);
    }
}
