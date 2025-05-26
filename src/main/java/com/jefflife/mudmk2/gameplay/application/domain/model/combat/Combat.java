package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Combatable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Combat {
    private static final int TICKS_PER_TURN = 20;

    private final UUID id;
    private final CombatGroup allyGroup;
    private final CombatGroup enemyGroup;
    private final InitiativeProvider initiativeProvider;
    private final DiceRoller diceRoller;
    private CombatState combatState;
    private CombatGroupType initiativeGroup;
    private int tickCount;
    private int currentTurn;

    public Combat(UUID id, CombatGroup allyGroup, CombatGroup enemyGroup, InitiativeProvider initiativeProvider) {
        this(id, allyGroup, enemyGroup, initiativeProvider, new RandomDiceRoller());
    }

    public Combat(UUID id, CombatGroup allyGroup, CombatGroup enemyGroup, InitiativeProvider initiativeProvider, DiceRoller diceRoller) {
        this.id = id;
        this.allyGroup = allyGroup;
        this.enemyGroup = enemyGroup;
        this.initiativeProvider = initiativeProvider;
        this.diceRoller = diceRoller;
        this.combatState = CombatState.WAITING;
        this.currentTurn = 1;
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

    public CombatActionResult action() {
        if (combatState != CombatState.ACTIVE) {
            return CombatActionResult.NOT_ACTED_RESULT;
        }

        if (tickCount++ % TICKS_PER_TURN == 0) {
            CombatActionResult actionResult = new CombatActionResult();
            CombatGroup currentActiveGroup = getInitiativeCombatGroup();
            for (int i = 0; i < 2; i++) {
                actionResult.addLogs(executeGroupAction(currentActiveGroup));
                currentActiveGroup = switchCurrentActiveGroup(currentActiveGroup);
            }

            currentTurn++;
            return actionResult;
        }

        return CombatActionResult.NOT_ACTED_RESULT;
    }

    private List<CombatLog> executeGroupAction(CombatGroup currentActiveGroup) {
        // Initialize a list to store combat logs
        List<CombatLog> combatLogs = new ArrayList<>();

        // Get the opposing group
        CombatGroup opposingGroup = (currentActiveGroup == allyGroup) ? enemyGroup : allyGroup;

        // Iterate through each participant in the active group
        for (CombatParticipant attacker : currentActiveGroup.getParticipants()) {
            // Skip defeated participants
            if (attacker.isDefeated()) {
                continue;
            }

            // 1. Target Selection
            CombatParticipant target = opposingGroup.getTarget();

            // If no valid target exists, end combat
            if (target == null) {
                combatState = CombatState.FINISHED;
                break;
            }

            // Get attacker and target information
            Combatable attackerCombatable = attacker.getParticipant();
            Combatable targetCombatable = target.getParticipant();

            // 2. Attack Execution
            // 2-1. Hit Check
            int attackRoll = diceRoller.roll(1, 20);
            int attackModifier = attackerCombatable.getStats().getDexterityModifier();
            int attackTotal = attackRoll + attackModifier;

            int defenseRoll = diceRoller.roll(1, 20);
            int defenseModifier = targetCombatable.getStats().getDexterityModifier();
            int defenseTotal = defenseRoll + defenseModifier;

            boolean hitSuccess = attackTotal >= defenseTotal;

            // If hit is successful, calculate and apply damage
            int baseDamage = 0;
            int damageModifier = 0;
            int damageTotal = 0;
            int defenseValue = 0;
            int finalDamage = 0;
            int targetRemainingHp = targetCombatable.getStats().hp();
            boolean targetDefeated = false;

            if (hitSuccess) {
                // 2-2. Damage Calculation
                baseDamage = diceRoller.roll(1, 6); // Simulate weapon damage (1d6)
                damageModifier = getStrengthModifier(attackerCombatable.getStats().str());
                damageTotal = baseDamage + damageModifier;

                // 2-3. Defense Application
                defenseValue = 10; // Base defense value

                // 2-4. Final Damage Calculation
                finalDamage = Math.max(1, damageTotal - defenseValue);

                // 2-5. Damage Application
                targetCombatable.damaged(finalDamage);
                targetDefeated = !targetCombatable.isAlive();
            }

            // Record the combat log
            CombatLog combatLog = recordCombatLog(
                attackerCombatable, targetCombatable,
                attackRoll, attackModifier, attackTotal,
                defenseRoll, defenseModifier, defenseTotal,
                hitSuccess,
                baseDamage, damageModifier, damageTotal,
                defenseValue, finalDamage,
                targetRemainingHp, targetDefeated
            );

            // Add the combat log to the list
            combatLogs.add(combatLog);

            // 3. Check if combat should end
            if (isGroupDefeated(opposingGroup)) {
                combatState = CombatState.FINISHED;
                break;
            }
        }

        return combatLogs;
    }

    /**
     * Records a combat log with all the details of an attack.
     * Includes explicit miss information for display to users.
     */
    private CombatLog recordCombatLog(
            Combatable attacker, Combatable target,
            int attackRoll, int attackModifier, int attackTotal,
            int defenseRoll, int defenseModifier, int defenseTotal,
            boolean hitSuccess,
            int baseDamage, int damageModifier, int damageTotal,
            int defenseValue, int finalDamage,
            int targetRemainingHp, boolean targetDefeated) {

        CombatLog.CombatLogBuilder logBuilder = CombatLog.builder()
                .attackerId(attacker.getId())
                .attackerName(attacker.getName())
                .targetId(target.getId())
                .targetName(target.getName())
                .attackRoll(attackRoll)
                .attackModifier(attackModifier)
                .attackTotal(attackTotal)
                .defenseRoll(defenseRoll)
                .defenseModifier(defenseModifier)
                .defenseTotal(defenseTotal)
                .hitSuccess(hitSuccess)
                .baseDamage(baseDamage)
                .damageModifier(damageModifier)
                .damageTotal(damageTotal)
                .defenseValue(defenseValue)
                .finalDamage(finalDamage)
                .targetRemainingHp(targetRemainingHp)
                .targetDefeated(targetDefeated);

        return logBuilder.build();
    }


    // Utility method to get strength modifier
    private int getStrengthModifier(int strength) {
        return (strength - 10) / 2;
    }

    // Utility method to check if a group is completely defeated
    private boolean isGroupDefeated(CombatGroup group) {
        return group.getParticipants().stream()
                .allMatch(CombatParticipant::isDefeated);
    }

    private CombatGroup getInitiativeCombatGroup() {
        return initiativeGroup == CombatGroupType.ALLY ? allyGroup : enemyGroup;
    }

    private CombatGroup switchCurrentActiveGroup(CombatGroup currentActiveGroup) {
        return (currentActiveGroup.getCombatGroupType() == CombatGroupType.ALLY) ? enemyGroup : allyGroup;
    }

    public boolean isFinished() {
        return combatState == CombatState.FINISHED;
    }

    public void close() {
        // GC를 위한 참조 해제
    }

    public UUID getId() {
        return id;
    }

    public List<Long> getAllyUserIds() {
        return allyGroup.getUserIds();
    }
}
