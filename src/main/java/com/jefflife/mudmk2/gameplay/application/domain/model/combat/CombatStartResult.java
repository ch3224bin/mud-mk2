package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

public record CombatStartResult(
        CombatGroupType initiativeGroup,
        InitiativeRoll allyInitiative,
        InitiativeRoll enemyInitiative
) {
}
