package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePartyRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultPartyService implements PartyService {

    private final ActivePartyRepository parties;
    private final ApplicationEventPublisher eventPublisher;

    public DefaultPartyService(ActivePartyRepository parties, ApplicationEventPublisher eventPublisher) {
        this.parties = parties;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void create(Party party) {
        parties.add(party);
        eventPublisher.publishEvent(new PartyCreatedEvent(this, party));
    }

    @Override
    public void disband(UUID partyId) {
        parties.findById(partyId).ifPresent(party -> {
            parties.remove(partyId);
            eventPublisher.publishEvent(new PartyDisbandedEvent(this, partyId));
        });
    }
}
