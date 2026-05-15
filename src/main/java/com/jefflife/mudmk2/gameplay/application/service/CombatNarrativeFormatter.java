package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponTypeMapping;
import com.jefflife.mudmk2.gameplay.application.domain.model.combat.CombatLog;
import org.springframework.stereotype.Component;

@Component
public class CombatNarrativeFormatter {

    public String format(CombatLog log) {
        String verb = verbFor(log.weaponTypeName());
        String weapon = log.weaponName() != null ? log.weaponName() : "맨손";

        StringBuilder sb = new StringBuilder();

        if (log.evaded()) {
            sb.append(String.format("%s이(가) %s을(를) %s(으)로 %s지만 피했다!",
                    log.attackerName(), log.targetName(), weapon, verb));
        } else if (log.isCrit()) {
            sb.append(String.format("%s이(가) %s을(를) %s(으)로 치명적으로 %s!",
                    log.attackerName(), log.targetName(), weapon, verb));
            sb.append(String.format("\n  → %d 데미지! (남은 HP: %d)",
                    log.finalDamage(), log.targetRemainingHp()));
        } else {
            sb.append(String.format("%s이(가) %s을(를) %s(으)로 힘껏 %s!",
                    log.attackerName(), log.targetName(), weapon, verb));
            sb.append(String.format("\n  → %d 데미지! (남은 HP: %d)",
                    log.finalDamage(), log.targetRemainingHp()));
        }

        if (log.targetDefeated()) {
            sb.append(String.format("\n%s이(가) 쓰러졌다!", log.targetName()));
        }

        return sb.toString();
    }

    private String verbFor(String weaponTypeName) {
        if (weaponTypeName == null) {
            return WeaponTypeMapping.attackVerb(WeaponType.FIST);
        }
        try {
            return WeaponTypeMapping.attackVerb(WeaponType.valueOf(weaponTypeName));
        } catch (IllegalArgumentException e) {
            return WeaponTypeMapping.attackVerb(WeaponType.FIST);
        }
    }
}
