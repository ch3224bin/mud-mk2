package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import java.util.List;
import java.util.UUID;

public class Combat {
    private static final int TICKS_PER_TURN = 20;

    private final UUID id;
    private final CombatGroup allyGroup;
    private final CombatGroup enemyGroup;
    private final InitiativeProvider initiativeProvider;
    private CombatState combatState;
    private CombatGroupType initiativeGroup;
    private int tickCount;
    private int currentTurn;

    public Combat(UUID id, CombatGroup allyGroup, CombatGroup enemyGroup, InitiativeProvider initiativeProvider) {
        this.id = id;
        this.allyGroup = allyGroup;
        this.enemyGroup = enemyGroup;
        this.initiativeProvider = initiativeProvider;
        this.combatState = CombatState.WAITING;
        this.currentTurn = 1;
    }

    public UUID getId() {
        return id;
    }

    public void action() {
        if (tickCount % TICKS_PER_TURN == 0) {
            currentTurn++;
        }
        tickCount++;
    }

    public CombatStartResult start() {
        combatState = CombatState.ACTIVE;
        allyGroup.enterCombatState();
        enemyGroup.enterCombatState();

        InitiativeRoll allyInitiative = allyGroup.getInitiativeRoll(initiativeProvider);
        InitiativeRoll enemyInitiative = enemyGroup.getInitiativeRoll(initiativeProvider);

        if (allyInitiative.total() >= enemyInitiative.total()) {
            initiativeGroup = CombatGroupType.ALLY;
        } else {
            initiativeGroup = CombatGroupType.ENEMY;
        }

        return new CombatStartResult(initiativeGroup, allyInitiative, enemyInitiative);
    }

    public boolean isFinished() {
        return combatState == CombatState.FINISHED;
    }

    public void close() {
        // GC를 위한 참조 해제
    }

    public List<Long> getAllyUserIds() {
        return allyGroup.getUserIds();
    }
}
