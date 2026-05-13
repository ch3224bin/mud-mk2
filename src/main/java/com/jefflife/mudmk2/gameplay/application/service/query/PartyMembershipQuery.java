package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;

import java.util.Optional;
import java.util.UUID;

public interface PartyMembershipQuery {
    Optional<Party> findByMemberId(UUID memberId);
    boolean         isInParty(UUID memberId);
}
