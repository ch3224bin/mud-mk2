package com.jefflife.mudmk2.gameplay.application.service.model.template;

import java.util.List;

public record ItemInfoVariables(
        Long userId,
        String name,
        String description,
        String location,
        String typeLabel,
        int weight,
        int quantity,
        boolean stackable,
        boolean hasRecovery,
        int hpRecovery,
        int mpRecovery,
        int apRecovery,
        List<StatModifierLine> statModifiers,
        String skillRef,
        String missionInfo
) {
    public record StatModifierLine(String label, int value) {}
}
