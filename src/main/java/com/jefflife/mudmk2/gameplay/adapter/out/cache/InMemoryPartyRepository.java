package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import com.jefflife.mudmk2.gamedata.application.service.required.PartyRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePartyRepository;
import com.jefflife.mudmk2.gameplay.application.service.sync.BatchSyncable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryPartyRepository implements ActivePartyRepository, BatchSyncable {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryPartyRepository.class);

    private final PartyRepository jpa;
    private final Map<UUID, Party> cache = new ConcurrentHashMap<>();

    public InMemoryPartyRepository(PartyRepository jpa) {
        this.jpa = jpa;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void bootstrap() {
        jpa.findAll().forEach(party -> cache.put(party.getId(), party));
        logger.info("Loaded {} parties", cache.size());
    }

    @Override
    public Optional<Party> findById(UUID id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public Iterable<Party> findAll() {
        return cache.values();
    }

    @Override
    public void add(Party party) {
        cache.put(party.getId(), party);
    }

    @Override
    public void remove(UUID id) {
        cache.remove(id);
    }

    @EventListener
    @Transactional
    public void onPartyCreated(PartyCreatedEvent event) {
        jpa.save(event.getParty());
    }

    @EventListener
    @Transactional
    public void onPartyDisbanded(PartyDisbandedEvent event) {
        jpa.deleteById(event.getPartyId());
    }

    @Override
    @Transactional
    public void syncToDb() {
        jpa.saveAll(cache.values());
    }
}
