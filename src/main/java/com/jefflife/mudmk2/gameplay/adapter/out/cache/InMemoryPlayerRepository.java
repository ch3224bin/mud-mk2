package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.event.PlayerCharacterCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
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
public class InMemoryPlayerRepository implements ActivePlayerRepository, BatchSyncable {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryPlayerRepository.class);

    private final PlayerCharacterRepository jpa;
    private final Map<UUID, PlayerCharacter> byId = new ConcurrentHashMap<>();
    private final Map<Long, PlayerCharacter> byUserId = new ConcurrentHashMap<>();
    private final Object indexLock = new Object();

    public InMemoryPlayerRepository(PlayerCharacterRepository jpa) {
        this.jpa = jpa;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void bootstrap() {
        jpa.findAll().forEach(p -> {
            p.initializeAssociatedEntities();
            putIntoIndexes(p);
        });
        logger.info("Loaded {} players", byId.size());
    }

    @EventListener
    @Transactional(readOnly = true)
    public void onCreated(PlayerCharacterCreatedEvent event) {
        PlayerCharacter pc = event.getPlayerCharacter();
        pc.initializeAssociatedEntities();
        add(pc);
        logger.info("New player added to cache: {}", pc.getId());
    }

    @Override
    public Optional<PlayerCharacter> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<PlayerCharacter> findByUserId(Long userId) {
        return Optional.ofNullable(byUserId.get(userId));
    }

    @Override
    public Iterable<PlayerCharacter> findAll() {
        return byId.values();
    }

    @Override
    public void add(PlayerCharacter player) {
        putIntoIndexes(player);
    }

    @Override
    public void removeByUserId(Long userId) {
        synchronized (indexLock) {
            PlayerCharacter removed = byUserId.remove(userId);
            if (removed != null) {
                byId.remove(removed.getId());
            }
        }
    }

    @Override
    @Transactional
    public void syncToDb() {
        jpa.saveAll(byId.values());
    }

    private void putIntoIndexes(PlayerCharacter p) {
        synchronized (indexLock) {
            byId.put(p.getId(), p);
            byUserId.put(p.getUserId(), p);
        }
    }
}
