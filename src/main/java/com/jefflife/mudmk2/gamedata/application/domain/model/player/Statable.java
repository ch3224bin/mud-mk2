package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import java.util.UUID;

public interface Statable {
    UUID getId();
    String getName();
    CharacterState getState();

    default boolean isNormal() {
        return getState() == CharacterState.NORMAL;
    }

    default boolean isAttackableTarget() {
        if (this instanceof PlayerCharacter) {
            return false;
        }
        if (this instanceof NonPlayerCharacter) {
            return false;
        }
        return true;
    }
}
