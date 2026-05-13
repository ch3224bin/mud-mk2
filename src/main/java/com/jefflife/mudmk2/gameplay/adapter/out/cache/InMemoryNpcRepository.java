package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.NonPlayerCharacterRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
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
public class InMemoryNpcRepository implements ActiveNpcRepository, BatchSyncable {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryNpcRepository.class);

    private final NonPlayerCharacterRepository jpa;
    private final Map<UUID, NonPlayerCharacter> cache = new ConcurrentHashMap<>();

    public InMemoryNpcRepository(NonPlayerCharacterRepository jpa) {
        this.jpa = jpa;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void bootstrap() {
        jpa.findAll().forEach(npc -> {
            npc.initializeAssociatedEntities();
            cache.put(npc.getId(), npc);
        });
        logger.info("Loaded {} NPCs", cache.size());
    }

    @Override
    public Optional<NonPlayerCharacter> findById(UUID id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public Iterable<NonPlayerCharacter> findAll() {
        return cache.values();
    }

    @Override
    public void add(NonPlayerCharacter npc) {
        cache.put(npc.getId(), npc);
    }

    @Override
    public void remove(UUID id) {
        cache.remove(id);
    }

    @Override
    @Transactional
    public void syncToDb() {
        jpa.saveAll(cache.values());
    }
}
