package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Combatable;
import lombok.Getter;

@Getter
public class CombatParticipant {
    private final Combatable participant;
    private long aggroScore;
    private boolean defeated;

    public CombatParticipant(Combatable participant) {
        this.participant = participant;
        this.aggroScore = 0;
        this.defeated = false;
    }

    public CombatParticipant(final Combatable participant, final long aggroScore, final boolean defeated) {
        this.participant = participant;
        this.aggroScore = aggroScore;
        this.defeated = defeated;
    }
}
