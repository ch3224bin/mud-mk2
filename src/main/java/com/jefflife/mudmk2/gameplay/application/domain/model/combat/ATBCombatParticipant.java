package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Combatable;
import lombok.Getter;

@Getter
public class ATBCombatParticipant {

    private final Combatable combatable;
    private final CombatGroupType group;
    private final int weaponBaseDamage;
    private final int equipArmor;
    private final int equipArmorPct;
    private final int weaponSkill;
    private final String weaponTypeName;
    private double atbGauge;
    private int currentMp;
    private int currentAp;

    public ATBCombatParticipant(Combatable combatable, CombatGroupType group) {
        this(combatable, group, 10, 0, 0);
    }

    public ATBCombatParticipant(Combatable combatable, CombatGroupType group,
                                 int weaponBaseDamage, int equipArmor, int equipArmorPct) {
        this.combatable = combatable;
        this.group = group;
        this.weaponBaseDamage = weaponBaseDamage;
        this.equipArmor = equipArmor;
        this.equipArmorPct = equipArmorPct;
        CharacterStats stats = combatable.getStats();
        WeaponDerivation derived = deriveWeapon(stats);
        this.weaponSkill = derived.skill();
        this.weaponTypeName = derived.name();
        this.currentMp = stats.mp();
        this.currentAp = stats.ap();
        this.atbGauge = 0.0;
    }

    public void advanceAtb(int speedDivisor) {
        double speed = CombatFormulas.initiativeSpeed(combatable.getStats());
        this.atbGauge += speed / speedDivisor;
    }

    public boolean isReadyToAct() {
        return atbGauge >= 100.0;
    }

    public void resetAtb() {
        this.atbGauge -= 100.0;
    }

    public void recoverResourcesOnAction() {
        CharacterStats stats = combatable.getStats();
        int apRecovery = (int)(stats.agility() * 0.5);
        int mpRecovery = (int)(stats.meridian() * 0.3);
        this.currentAp = Math.min(stats.maxAp(), this.currentAp + apRecovery);
        this.currentMp = Math.min(stats.maxMp(), this.currentMp + mpRecovery);
    }

    public void spendAp(int amount) {
        this.currentAp = Math.max(0, this.currentAp - amount);
    }

    public boolean isDefeated() {
        return !combatable.isAlive();
    }

    public int getCurrentHp() {
        return combatable.getStats().hp();
    }

    public void applyDamage(int damage) {
        combatable.damaged(damage);
    }

    private record WeaponDerivation(int skill, String name) {}

    private static WeaponDerivation deriveWeapon(CharacterStats stats) {
        int max = stats.fistsAndPalms();
        String name = "권장";
        if (stats.swordMethod() > max)    { max = stats.swordMethod();    name = "검법"; }
        if (stats.bladeMethod() > max)    { max = stats.bladeMethod();    name = "도법"; }
        if (stats.longWeapon() > max)     { max = stats.longWeapon();     name = "장병"; }
        if (stats.esotericWeapon() > max) { max = stats.esotericWeapon(); name = "기문"; }
        if (stats.archery() > max)        { max = stats.archery();        name = "사술"; }
        return new WeaponDerivation(max, name);
    }
}
