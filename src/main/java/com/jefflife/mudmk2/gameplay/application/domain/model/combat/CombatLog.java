package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import lombok.Builder;

import java.util.UUID;

public record CombatLog(
    // Attacker information
    UUID attackerId,
    String attackerName,

    // Target information
    UUID targetId,
    String targetName,

    // Attack roll details
    int attackRoll,
    int attackModifier,
    int attackTotal,

    // Defense roll details
    int defenseRoll,
    int defenseModifier,
    int defenseTotal,

    // Hit result
    boolean hitSuccess,

    // Damage details
    int baseDamage,
    int damageModifier,
    int damageTotal,

    // Defense reduction
    int defenseValue,

    // Final damage
    int finalDamage,

    // Target status after attack
    int targetRemainingHp,
    boolean targetDefeated
) {
    @Builder
    public CombatLog {

    }
}
