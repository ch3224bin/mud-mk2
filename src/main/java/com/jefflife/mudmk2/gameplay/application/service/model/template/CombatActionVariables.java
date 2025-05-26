package com.jefflife.mudmk2.gameplay.application.service.model.template;

import com.jefflife.mudmk2.gameplay.application.domain.model.combat.CombatActionResult;

import java.util.UUID;

public record CombatActionVariables(
        Long userId,
        UUID userUuid,
        CombatActionResult combatActionResult) {
}
