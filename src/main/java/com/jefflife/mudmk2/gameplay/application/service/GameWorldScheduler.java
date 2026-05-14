package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.service.sync.BatchSyncable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GameWorldScheduler {
    private static final Logger logger = LoggerFactory.getLogger(GameWorldScheduler.class);

    private final List<BatchSyncable> syncables;
    private final MonsterRespawnService respawn;

    public GameWorldScheduler(List<BatchSyncable> syncables, MonsterRespawnService respawn) {
        this.syncables = syncables;
        this.respawn = respawn;
    }

    @Scheduled(fixedDelay = 60_000) // 1분마다 인메모리 상태 DB 동기화
    public void persist() {
        syncables.forEach(BatchSyncable::syncToDb);
    }

    @Scheduled(fixedDelay = 5_000) // 5초마다 몬스터 리스폰 처리
    public void respawnMonsters() {
        int count = respawn.respawnAll();
        if (count > 0) {
            logger.debug("Respawned {} monsters", count);
        }
    }
}
