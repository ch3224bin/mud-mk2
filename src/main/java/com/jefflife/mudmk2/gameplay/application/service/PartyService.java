package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;

import java.util.UUID;

public interface PartyService {
    /**
     * 새 파티를 활성 캐시에 추가하고 PartyCreatedEvent 를 발행한다.
     */
    void create(Party party);

    /**
     * 파티를 활성 캐시에서 제거하고 PartyDisbandedEvent 를 발행한다. 없으면 무시.
     */
    void disband(UUID partyId);
}
