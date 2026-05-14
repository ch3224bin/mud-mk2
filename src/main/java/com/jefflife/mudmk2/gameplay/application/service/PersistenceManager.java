package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.service.required.PartyRepository;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PersistenceManager {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PersistenceManager.class);

    private final GameWorldService gameWorldService;
    private final PartyRepository partyRepository;

    public PersistenceManager(
            final GameWorldService gameWorldService,
            final PartyRepository partyRepository
    ) {
        this.gameWorldService = gameWorldService;
        this.partyRepository = partyRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void loadGameState() {
        gameWorldService.loadParties(partyRepository.findAll());

        logger.info("loadGameState finished");
    }
}
