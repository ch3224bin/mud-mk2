package com.jefflife.mudmk2.gamedata.application.domain.model.party;

public class DefaultPartyPolicy implements PartyPolicy {
    private static final int MAX_PARTY_SIZE = 6;

    @Override
    public boolean isValidPartySize(final PartyMembers partyMembers) {
        return !partyMembers.isEmpty() && partyMembers.size() <= MAX_PARTY_SIZE;
    }
}
