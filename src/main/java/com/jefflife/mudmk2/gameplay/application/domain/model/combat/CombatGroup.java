package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class CombatGroup {
    private final CombatGroupType combatGroupType;
    private final List<CombatParticipant> participants = new ArrayList<>();

    public CombatGroup(CombatGroupType combatGroupType) {
        this.combatGroupType = combatGroupType;
    }

    public CombatParticipant getTarget() {
        return participants.stream()
                .filter(participant -> !participant.isDefeated())
                .max(Comparator.comparingLong(CombatParticipant::getAggroScore))
                .orElse(null);
    }
}
