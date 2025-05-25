package com.jefflife.mudmk2.gameplay.application.service.model.template;

import com.jefflife.mudmk2.gameplay.application.domain.model.combat.CombatStartResult;

public record CombatStartVariables(
        Long userId,
        CombatStartResult startResult) {
}
