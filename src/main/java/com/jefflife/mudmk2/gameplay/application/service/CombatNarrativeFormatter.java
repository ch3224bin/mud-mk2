package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.domain.model.combat.CombatLog;
import org.springframework.stereotype.Component;

@Component
public class CombatNarrativeFormatter {

    public String format(CombatLog log) {
        StringBuilder sb = new StringBuilder();

        if (log.evaded()) {
            sb.append(String.format("%s이(가) %s으로 공격했지만 %s이(가) 피했다!",
                log.attackerName(), log.weaponTypeName(), log.targetName()));
        } else if (log.isCrit()) {
            sb.append(String.format("%s이(가) %s으로 %s에게 치명타를 날렸다!",
                log.attackerName(), log.weaponTypeName(), log.targetName()));
            sb.append(String.format("\n  → %d 데미지! (남은 HP: %d)",
                log.finalDamage(), log.targetRemainingHp()));
        } else {
            sb.append(String.format("%s이(가) %s으로 %s을(를) 공격했다!",
                log.attackerName(), log.weaponTypeName(), log.targetName()));
            sb.append(String.format("\n  → %d 데미지! (남은 HP: %d)",
                log.finalDamage(), log.targetRemainingHp()));
        }

        if (log.targetDefeated()) {
            sb.append(String.format("\n%s이(가) 쓰러졌다!", log.targetName()));
        }

        return sb.toString();
    }
}
