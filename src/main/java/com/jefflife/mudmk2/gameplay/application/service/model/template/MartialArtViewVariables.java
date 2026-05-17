package com.jefflife.mudmk2.gameplay.application.service.model.template;

import java.util.List;

public record MartialArtViewVariables(
        Long userId,
        List<MentalGroup> mentalGroups,
        List<ExternalGroup> externalGroups
) {
    public record MentalGroup(
            String kindLabel,
            List<LearnedMentalLine> items
    ) {}

    public record LearnedMentalLine(
            String name,
            int currentLevel,
            int maxLevel,
            long currentExp,
            boolean atMax,
            boolean equipped,
            List<StatModLine> effects
    ) {}

    public record ExternalGroup(
            String weaponLabel,
            List<LearnedExternalLine> items
    ) {}

    public record LearnedExternalLine(
            String name,
            int currentLevel,
            int maxLevel,
            long currentExp,
            boolean atMax,
            boolean equipped,
            double damageMultiplier,
            int cooldownSeconds,
            int apCost,
            int mpCost
    ) {}

    public record StatModLine(String label, int value) {}
}
