package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Combatable;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
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

    public void addParticipant(CombatParticipant participant) {
        participants.add(participant);
    }

    public CombatParticipant getTarget() {
        return participants.stream()
                .filter(participant -> !participant.isDefeated())
                .max(Comparator.comparingLong(CombatParticipant::getAggroScore))
                .orElse(null);
    }

    public InitiativeRoll getInitiativeRoll(InitiativeProvider initiativeProvider) {
        return participants.stream()
                .filter(participant -> !participant.isDefeated())
                .map(participant -> initiativeProvider.calculate(participant.getParticipant().getStats()))
                .max(Comparator.comparingInt(InitiativeRoll::total))
                .orElse(new InitiativeRoll(0, 0, 0, 0));
    }

    public List<Long> getUserIds() {
        return participants.stream()
                .map(CombatParticipant::getParticipant)
                .filter(combatable -> combatable instanceof PlayerCharacter)
                .map(combatable -> ((PlayerCharacter) combatable).getUserId())
                .toList();
    }

    public void enterCombatState() {
        participants.forEach(CombatParticipant::enterCombatState);
    }

    public List<PlayerCharacter> getUsers() {
        return participants.stream()
                .map(CombatParticipant::getParticipant)
                .filter(combatable -> combatable instanceof PlayerCharacter)
                .map(combatable -> (PlayerCharacter) combatable)
                .toList();
    }
}
