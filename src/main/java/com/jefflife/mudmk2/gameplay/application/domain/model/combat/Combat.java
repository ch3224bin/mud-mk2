package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import java.util.UUID;

public class Combat {
    private static final int TICKS_PER_TURN = 10;
    private final UUID id;
    private final CombatGroup allyGroup;
    private final CombatGroup enemyGroup;
    private CombatState combatState;
    private CombatGroupType initiativeGroup;
    private int currentTurn;

    public Combat(UUID id, CombatGroup allyGroup, CombatGroup enemyGroup) {
        this.id = id;
        this.allyGroup = allyGroup;
        this.enemyGroup = enemyGroup;
        this.combatState = CombatState.WAITING;
        this.currentTurn = 1;
    }
}
