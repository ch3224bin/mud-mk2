package com.jefflife.mudmk2.gamedata.application.event;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class PartyDisbandedEvent extends ApplicationEvent {
    private final UUID partyId;

    public PartyDisbandedEvent(Object source, UUID partyId) {
        super(source);
        this.partyId = partyId;
    }

    public UUID getPartyId() {
        return partyId;
    }
}
