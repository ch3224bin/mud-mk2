package com.jefflife.mudmk2.gamedata.application.event;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import org.springframework.context.ApplicationEvent;

public class PlayerCharacterCreatedEvent extends ApplicationEvent {
    private final PlayerCharacter playerCharacter;

    public PlayerCharacterCreatedEvent(Object source, PlayerCharacter playerCharacter) {
        super(source);
        this.playerCharacter = playerCharacter;
    }

    public PlayerCharacter getPlayerCharacter() {
        return playerCharacter;
    }
}
