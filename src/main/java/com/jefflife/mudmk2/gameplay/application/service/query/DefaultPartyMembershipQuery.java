package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePartyRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Component
public class DefaultPartyMembershipQuery implements PartyMembershipQuery {

    private final ActivePartyRepository parties;

    public DefaultPartyMembershipQuery(ActivePartyRepository parties) {
        this.parties = parties;
    }

    @Override
    public Optional<Party> findByMemberId(UUID memberId) {
        return StreamSupport.stream(parties.findAll().spliterator(), false)
                .filter(party -> party.contains(memberId))
                .findFirst();
    }

    @Override
    public boolean isInParty(UUID memberId) {
        return StreamSupport.stream(parties.findAll().spliterator(), false)
                .anyMatch(party -> party.contains(memberId));
    }
}
