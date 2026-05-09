package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;

public final class CombatFormulas {

    private CombatFormulas() {}

    public static double initiativeSpeed(CharacterStats stats) {
        return stats.agility() * 1.0 + stats.lightStep() * 0.5;
    }

    public static double accuracy(CharacterStats stats, int weaponSkill, int equipAccuracy) {
        return stats.intellect() * 0.8 + weaponSkill * 0.5 + equipAccuracy + 50;
    }

    public static double evasion(CharacterStats stats) {
        return stats.agility() * 0.8 + stats.lightStep() * 0.4;
    }

    public static int evasionRate(double evasion, double accuracy) {
        return Math.max(0, Math.min(75, (int)(evasion - accuracy)));
    }

    public static int baseDamage(CharacterStats stats, int weaponBase, int weaponSkill, RandomGenerator rng) {
        double weaponMultiplier = 1.0 + weaponSkill * 0.008;
        double base = weaponBase * weaponMultiplier + stats.vigor() * 0.3;
        double randomFactor = 0.9 + rng.nextInt(21) / 100.0;
        return (int)(base * randomFactor);
    }

    public static int critRate(CharacterStats stats) {
        return (int)(stats.vigor() * 0.3);
    }

    public static int armor(CharacterStats stats, int equipArmor) {
        return equipArmor + (int)(stats.will() * 0.4);
    }

    public static int armorPct(CharacterStats stats, int equipArmorPct, int weaponSkill) {
        return Math.min(75, (int)(equipArmorPct + stats.will() * 0.2 + weaponSkill * 0.15));
    }

    public static int applyDefense(int rawDamage, int armor, int armorPct) {
        double reduced = rawDamage * (1.0 - armorPct / 100.0);
        return Math.max(1, (int)(reduced - armor));
    }
}
