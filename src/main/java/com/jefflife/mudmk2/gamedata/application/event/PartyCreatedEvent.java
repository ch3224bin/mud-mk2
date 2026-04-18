package com.jefflife.mudmk2.gamedata.application.event;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import org.springframework.context.ApplicationEvent;

public class PartyCreatedEvent extends ApplicationEvent {
    private final Party party;

    public PartyCreatedEvent(Object source, Party party) {
        super(source);
        this.party = party;
    }

    public Party getParty() {
        return party;
    }
}
