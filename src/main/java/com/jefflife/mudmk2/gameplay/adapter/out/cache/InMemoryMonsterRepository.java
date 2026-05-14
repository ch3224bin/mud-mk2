package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterSpawnRoom;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterSpawnRooms;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;
import com.jefflife.mudmk2.gamedata.application.service.required.MonsterTypeRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
import com.jefflife.mudmk2.gameplay.application.service.sync.BatchSyncable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryMonsterRepository implements ActiveMonsterRepository, BatchSyncable {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryMonsterRepository.class);
    private static final Random random = new Random();

    private final MonsterTypeRepository monsterTypes;
    private final Map<UUID, Monster> cache = new ConcurrentHashMap<>();

    public InMemoryMonsterRepository(MonsterTypeRepository monsterTypes) {
        this.monsterTypes = monsterTypes;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void bootstrap() {
        long typeCount = 0;
        for (MonsterType type : monsterTypes.findAll()) {
            typeCount++;
            MonsterSpawnRooms spawnRooms = type.getMonsterSpawnRooms();
            if (spawnRooms == null || spawnRooms.getSpawnRooms() == null || spawnRooms.getSpawnRooms().isEmpty()) {
                continue;
            }
            for (MonsterSpawnRoom spawnRoom : spawnRooms.getSpawnRooms()) {
                for (int i = 0; i < spawnRoom.getSpawnCount(); i++) {
                    int level = 1 + random.nextInt(5);
                    Monster monster = Monster.createFromType(type, level, spawnRoom.getRoomId());
                    cache.put(monster.getId(), monster);
                }
            }
        }
        logger.info("Loaded {} monsters from {} monster types", cache.size(), typeCount);
    }

    @Override
    public Optional<Monster> findById(UUID id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public Iterable<Monster> findAll() {
        return cache.values();
    }

    @Override
    public void add(Monster monster) {
        cache.put(monster.getId(), monster);
    }

    @Override
    public void remove(UUID id) {
        cache.remove(id);
    }

    @Override
    public void syncToDb() {
        // Monster 는 JPA 영속 대상이 아님 (MonsterRepository 미존재). BatchSyncable 일관성을 위해 구현만 두고 noop.
    }
}
