package com.jefflife.mudmk2.gameplay.application.service.sync;

import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import com.jefflife.mudmk2.gamedata.application.service.required.PartyRepository;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PartySyncService implements BatchSyncable {

    private final PartyRepository partyRepository;
    private final GameWorldService gameWorldService;

    public PartySyncService(
            final PartyRepository partyRepository,
            final GameWorldService gameWorldService
    ) {
        this.partyRepository = partyRepository;
        this.gameWorldService = gameWorldService;
    }

    @Override
    @Transactional
    public void syncToDb() {
        partyRepository.saveAll(gameWorldService.getActiveParties());
    }

    @EventListener
    @Transactional
    public void onPartyCreated(PartyCreatedEvent event) {
        partyRepository.save(event.getParty());
    }

    @EventListener
    @Transactional
    public void onPartyDisbanded(PartyDisbandedEvent event) {
        partyRepository.deleteById(event.getPartyId());
    }
}
