package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.service.required.RoomRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
import com.jefflife.mudmk2.gameplay.application.service.sync.BatchSyncable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRoomRepository implements ActiveRoomRepository, BatchSyncable {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryRoomRepository.class);

    private final RoomRepository jpa;
    private final Map<Long, Room> cache = new ConcurrentHashMap<>();

    public InMemoryRoomRepository(RoomRepository jpa) {
        this.jpa = jpa;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void bootstrap() {
        jpa.findAll().forEach(r -> {
            r.initializeAssociatedEntities();
            cache.put(r.getId(), r);
        });
        logger.info("Loaded {} rooms", cache.size());
    }

    @Override
    public Optional<Room> findById(Long id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public Iterable<Room> findAll() {
        return cache.values();
    }

    @Override
    public void add(Room room) {
        cache.put(room.getId(), room);
    }

    @Override
    public void remove(Long id) {
        cache.remove(id);
    }

    @Override
    @Transactional
    public void syncToDb() {
        jpa.saveAll(cache.values());
    }
}
