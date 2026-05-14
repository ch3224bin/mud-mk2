package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.service.sync.BatchSyncable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameWorldSchedulerTest {

    @Test
    void persist_callsSyncToDb_onEachBatchSyncable() {
        BatchSyncable a = mock(BatchSyncable.class);
        BatchSyncable b = mock(BatchSyncable.class);
        MonsterRespawnService respawn = mock(MonsterRespawnService.class);
        GameWorldScheduler scheduler = new GameWorldScheduler(List.of(a, b), respawn);

        scheduler.persist();

        verify(a).syncToDb();
        verify(b).syncToDb();
        verifyNoInteractions(respawn);
    }

    @Test
    void respawnMonsters_delegatesToRespawnService() {
        BatchSyncable a = mock(BatchSyncable.class);
        MonsterRespawnService respawn = mock(MonsterRespawnService.class);
        when(respawn.respawnAll()).thenReturn(3);
        GameWorldScheduler scheduler = new GameWorldScheduler(List.of(a), respawn);

        scheduler.respawnMonsters();

        verify(respawn).respawnAll();
        verifyNoInteractions(a);
    }

    @Test
    void respawnMonsters_doesNotThrow_whenRespawnReturnsZero() {
        MonsterRespawnService respawn = mock(MonsterRespawnService.class);
        when(respawn.respawnAll()).thenReturn(0);
        GameWorldScheduler scheduler = new GameWorldScheduler(List.of(), respawn);

        scheduler.respawnMonsters(); // 예외 없이 통과

        verify(respawn).respawnAll();
    }
}
