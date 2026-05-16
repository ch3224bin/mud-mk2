package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.EquipmentSlot;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponTypeMapping;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Combatable;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import lombok.Getter;

import java.util.Optional;

@Getter
public class ATBCombatParticipant {

    private final Combatable combatable;
    private final CombatGroupType group;
    private final int weaponBaseDamage;
    private final int equipArmor;
    private final int equipArmorPct;
    private final int weaponSkill;
    private final String weaponTypeName;
    private final String weaponName;
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
        WeaponDerivation derived = deriveWeapon(combatable, stats);
        this.weaponSkill = derived.skill();
        this.weaponTypeName = derived.typeName();
        this.weaponName = derived.displayName();

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

    private record WeaponDerivation(int skill, String typeName, String displayName) {}

    /**
     * PlayerCharacter는 무기 슬롯 기반 (장착 무기의 type + name).
     * 그 외 Combatable(Monster/NPC)은 보유한 무공 stat 중 가장 높은 것을 자동 선택.
     */
    private static WeaponDerivation deriveWeapon(Combatable combatable, CharacterStats stats) {
        if (combatable instanceof PlayerCharacter pc) {
            Optional<ItemInstance> weaponOpt = pc.getEquippedItems().getSlot(EquipmentSlot.WEAPON);
            if (weaponOpt.isPresent() && weaponOpt.get().getTemplate() instanceof WeaponTemplate wt) {
                WeaponType type = wt.getWeaponType();
                StatType skillType = WeaponTypeMapping.weaponSkillFor(type);
                return new WeaponDerivation(readSkillStat(stats, skillType), type.name(), wt.getName());
            }
            // 맨손
            return new WeaponDerivation(stats.fistsAndPalms(), WeaponType.FIST.name(), "맨손");
        }

        // Monster / NPC: 최대 무공 stat 자동 선택
        return autoSelectFromStats(stats);
    }

    private static WeaponDerivation autoSelectFromStats(CharacterStats stats) {
        int max = stats.fistsAndPalms();
        WeaponType type = WeaponType.FIST;
        String name = "권장";
        if (stats.swordMethod() > max)    { max = stats.swordMethod();    type = WeaponType.SWORD;       name = "검법"; }
        if (stats.bladeMethod() > max)    { max = stats.bladeMethod();    type = WeaponType.BLADE;       name = "도법"; }
        if (stats.longWeapon() > max)     { max = stats.longWeapon();     type = WeaponType.LONG_WEAPON; name = "장병"; }
        if (stats.esotericWeapon() > max) { max = stats.esotericWeapon(); type = WeaponType.ESOTERIC;    name = "기문"; }
        if (stats.archery() > max)        { max = stats.archery();        type = WeaponType.ARCHERY;     name = "사술"; }
        return new WeaponDerivation(max, type.name(), name);
    }

    private static int readSkillStat(CharacterStats stats, StatType type) {
        return switch (type) {
            case VIGOR -> stats.vigor();
            case PHYSIQUE -> stats.physique();
            case AGILITY -> stats.agility();
            case INTELLECT -> stats.intellect();
            case WILL -> stats.will();
            case MERIDIAN -> stats.meridian();
            case INNER_POWER -> stats.innerPower();
            case SPECIAL_TECHNIQUE -> stats.specialTechnique();
            case LIGHT_STEP -> stats.lightStep();
            case FISTS_AND_PALMS -> stats.fistsAndPalms();
            case SWORD_METHOD -> stats.swordMethod();
            case BLADE_METHOD -> stats.bladeMethod();
            case LONG_WEAPON -> stats.longWeapon();
            case ESOTERIC_WEAPON -> stats.esotericWeapon();
            case ARCHERY -> stats.archery();
        };
    }
}
