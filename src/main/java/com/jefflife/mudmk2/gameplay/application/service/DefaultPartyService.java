package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePartyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultPartyService implements PartyService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultPartyService.class);

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
        logger.debug("Party created: {} (leader: {})", party.getId(), party.getLeaderId());
    }

    @Override
    public void disband(UUID partyId) {
        parties.findById(partyId).ifPresent(party -> {
            parties.remove(partyId);
            eventPublisher.publishEvent(new PartyDisbandedEvent(this, partyId));
            logger.debug("Party disbanded: {}", partyId);
        });
    }
}
