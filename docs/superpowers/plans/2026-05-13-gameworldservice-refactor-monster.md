# GameWorldService 리팩토링 구현 계획 — Phase 5 (Monster 도메인 + GameWorldScheduler 도입)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Monster 도메인의 부트스트랩 + 캐시를 `InMemoryMonsterRepository`(`MonsterType`에서 생성하는 흡수 로직 포함)로 완전 이전하고, 리스폰 도메인 행위를 `DefaultMonsterRespawnService`로 분리한다. 동시에 `@Scheduled` 진입점을 모은 `GameWorldScheduler` 구체 `@Component`를 신설해 `PersistenceManager`의 `persistGameState()` / `checkMonsterRespawn()` 두 스케줄러 책임을 이관하고, `PersistenceManager.loadMonsters()` private 메서드까지 제거한다. 호출처 마이그레이션은 `SimulationService`의 `addMonster`/`removeMonster` 두 곳만 대상이며(`AttackCommandService.getMonstersInRoom`/`RoomInfoService.getMonstersInRoom`은 phase 6 영역이라 본 phase에서는 `GameWorldService`에 임시 위임 형태로 유지), `GameWorldService`에서 `activeMonsters` 필드 + `loadMonsters` / `addMonster` / `getMonsterById` / `getAllMonsters` / `removeMonster` / `respawnMonsters` 6개 멤버를 제거한다. `getMonstersInRoom` / `getMonstersByType`은 `ActiveMonsterRepository.findAll()` 위에서 임시 위임으로 유지(phase 6에서 Query로 이관).

**Architecture:** `Monster`는 JPA `@Entity`가 아닌 도메인 클래스이고 영속 대상이 아니므로(현재 `PersistenceManager.persistGameState`는 `batchSyncables.forEach(syncToDb)`만 수행해 Monster를 직접 저장하지 않음), `InMemoryMonsterRepository`는 `ActiveMonsterRepository, BatchSyncable`을 구현하되 `syncToDb()`는 **noop**으로 둔다(spec 5.1 / 5.2 일관성 유지 + `GameWorldScheduler.persist`의 `List<BatchSyncable>` 자동 주입 명단에 포함). 부트스트랩은 spec 5.2 명세대로 `MonsterTypeRepository.findAll()` → `MonsterType.getMonsterSpawnRooms().getSpawnRooms()` 순회 → 각 `spawnCount` 만큼 `Monster.createFromType(monsterType, level, roomId)`로 생성 후 캐시 적재(현재 `PersistenceManager.loadMonsters` 로직을 그대로 흡수, `Random` 의존성 어댑터로 이관). `DefaultMonsterRespawnService.respawnAll()`은 `ActiveMonsterRepository.findAll()` 위에서 `!isAlive() && canRespawn()`인 몬스터를 `monster.respawn()` 호출해 부활시키고 부활 수를 반환(현재 `GameWorldService.respawnMonsters` 로직 흡수, `MonsterNotFoundException` 없음). `GameWorldScheduler`는 spec 7 명세대로 구체 `@Component` 클래스로 두고 `List<BatchSyncable>` + `MonsterRespawnService`를 생성자 주입 받아 `@Scheduled(fixedDelay = 60_000)` persist + `@Scheduled(fixedDelay = 5_000)` respawnMonsters 두 진입점을 갖는다(트랜잭션은 어댑터의 `syncToDb()`에서 끊기므로 스케줄러 자체는 무관). `GameWorldScheduler` 추가와 `PersistenceManager`의 두 `@Scheduled` 메서드 제거는 동일 commit으로 묶어 이중 실행 위험(60초/5초마다 양쪽 호출)을 회피.

**Tech Stack:** Java 17, Spring Boot 3.x, JPA, JUnit 5, Mockito, AssertJ

**Spec:** `docs/superpowers/specs/2026-05-13-gameworldservice-refactor-design.md` (섹션 4.3, 5.1, 5.2, 7, 8.3 step 5)

**Prior phases:**
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-room.md` (Phase 1+2 완료)
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-player.md` (Phase 3 완료)
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-npc.md` (Phase 4 완료)

---

## 파일 구조

**생성**
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryMonsterRepository.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/MonsterRespawnService.java` (interface)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/DefaultMonsterRespawnService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldScheduler.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryMonsterRepositoryTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/DefaultMonsterRespawnServiceTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldSchedulerTest.java`

**수정 (main)**
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java`
  - `persistGameState()`, `checkMonsterRespawn()` 두 `@Scheduled` 메서드 제거 (Task 3)
  - `loadMonsters()` private 메서드 + `loadGameState`에서 `loadMonsters()` 호출 제거 (Task 4)
  - 미사용 의존성 (`MonsterTypeRepository`, `batchSyncables`, `Random`) 제거 (Task 3 + 4 분산)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/SimulationService.java`
  - `GameWorldService` 의존을 `ActiveMonsterRepository`로 교체 (`addMonster`/`removeMonster` 두 호출)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java`
  - `activeMonsters` 필드 제거
  - `loadMonsters` / `addMonster` / `getMonsterById` / `getAllMonsters` / `removeMonster` / `respawnMonsters` 6개 메서드 제거
  - `getMonstersInRoom` / `getMonstersByType`은 `ActiveMonsterRepository.findAll()` 임시 위임 (TODO(phase6))
  - 생성자에 `ActiveMonsterRepository` 추가
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/BatchSyncable.java`
  - 클래스 Javadoc 마이그레이션 노트 갱신 (Task 3에서 `PersistenceManager.persistGameState` 제거 → `GameWorldScheduler.persist`로 이관됨)

**수정 (test)**
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java`
  - `FakeGameWorldService` 부모 생성자 호출에 `ActiveMonsterRepository` 인자 `null` 추가

---

## Task 1: `InMemoryMonsterRepository` — TDD로 구현

부트스트랩은 다른 어댑터들과 다르게 `MonsterTypeRepository.findAll()`을 읽고 각 `MonsterType`의 `MonsterSpawnRooms.spawnRooms`를 순회해 `Monster.createFromType(monsterType, level, roomId)`로 생성한다(현재 `PersistenceManager.loadMonsters` 로직 그대로). `Random`은 어댑터로 이관해 `1 + random.nextInt(5)`로 레벨 결정. `Monster`는 JPA Entity가 아니므로 `syncToDb()`는 noop(MonsterRepository 없음).

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryMonsterRepository.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryMonsterRepositoryTest.java`

- [ ] **Step 1: 테스트 클래스 작성**

`InMemoryMonsterRepositoryTest.java`:
```java
package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterSpawnRoom;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;
import com.jefflife.mudmk2.gamedata.application.service.required.MonsterTypeRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InMemoryMonsterRepositoryTest {

    @Mock private MonsterTypeRepository monsterTypes;
    private InMemoryMonsterRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMonsterRepository(monsterTypes);
    }

    @Test
    void bootstrap_createsMonstersForEachSpawnRoomTimesSpawnCount() {
        MonsterType type = monsterTypeWithSpawnRooms(
                spawnRoom(100L, 2),
                spawnRoom(200L, 3)
        );
        when(monsterTypes.findAll()).thenReturn(List.of(type));

        repository.bootstrap();

        List<Monster> loaded = toList(repository.findAll());
        assertThat(loaded).hasSize(5);
        long room100 = loaded.stream().filter(m -> m.getCurrentRoomId().equals(100L)).count();
        long room200 = loaded.stream().filter(m -> m.getCurrentRoomId().equals(200L)).count();
        assertThat(room100).isEqualTo(2);
        assertThat(room200).isEqualTo(3);
    }

    @Test
    void bootstrap_skipsMonsterTypeWhenMonsterSpawnRoomsContainerIsNull() {
        MonsterType type = MonsterType.builder().name("noSpawn").build();
        type.setMonsterSpawnRooms(null);
        when(monsterTypes.findAll()).thenReturn(List.of(type));

        repository.bootstrap();

        assertThat(toList(repository.findAll())).isEmpty();
    }

    @Test
    void bootstrap_skipsMonsterTypeWhenSpawnRoomsListIsEmpty() {
        MonsterType type = MonsterType.builder().name("emptySpawn").build();
        // monsterSpawnRooms 는 @Builder.Default 로 빈 컬렉션 초기화됨
        when(monsterTypes.findAll()).thenReturn(List.of(type));

        repository.bootstrap();

        assertThat(toList(repository.findAll())).isEmpty();
    }

    @Test
    void findById_returnsEmpty_whenNotCached() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findAll_returnsAllAddedMonsters() {
        Monster m1 = simpleMonster(10L);
        Monster m2 = simpleMonster(20L);
        repository.add(m1);
        repository.add(m2);

        assertThat(repository.findAll()).containsExactlyInAnyOrder(m1, m2);
    }

    @Test
    void add_putsMonsterIntoCache_andFindByIdReturnsIt() {
        Monster monster = simpleMonster(10L);

        repository.add(monster);

        assertThat(repository.findById(monster.getId())).contains(monster);
    }

    @Test
    void remove_dropsMonsterFromCache() {
        Monster monster = simpleMonster(10L);
        repository.add(monster);

        repository.remove(monster.getId());

        assertThat(repository.findById(monster.getId())).isEmpty();
    }

    @Test
    void remove_isNoOp_whenIdNotPresent() {
        repository.remove(UUID.randomUUID());

        assertThat(toList(repository.findAll())).isEmpty();
    }

    @Test
    void syncToDb_isNoOp_andDoesNotThrow() {
        repository.add(simpleMonster(10L));

        Assertions.assertThatNoException().isThrownBy(() -> repository.syncToDb());

        // 캐시는 변경되지 않음 (Monster는 JPA 영속 대상이 아님)
        assertThat(toList(repository.findAll())).hasSize(1);
    }

    // --- 테스트 헬퍼 ---

    private static Monster simpleMonster(Long roomId) {
        MonsterType type = MonsterType.builder().name("dummy").baseHp(10).build();
        return Monster.createFromType(type, 1, roomId);
    }

    private static MonsterType monsterTypeWithSpawnRooms(MonsterSpawnRoom... rooms) {
        MonsterType type = MonsterType.builder().name("spawning").baseHp(10).build();
        for (MonsterSpawnRoom room : rooms) {
            type.addSpawnRoom(room);
        }
        return type;
    }

    private static MonsterSpawnRoom spawnRoom(Long roomId, int spawnCount) {
        return MonsterSpawnRoom.builder()
                .roomId(roomId)
                .spawnCount(spawnCount)
                .build();
    }

    private static <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new java.util.ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }
}
```

- [ ] **Step 2: 컴파일 실패 확인**

Run: `./gradlew test --tests InMemoryMonsterRepositoryTest`
Expected: 컴파일 실패 (`InMemoryMonsterRepository` 미존재)

- [ ] **Step 3: `InMemoryMonsterRepository` 구현체 작성**

`InMemoryMonsterRepository.java`:
```java
package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterSpawnRoom;
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
            if (type.getMonsterSpawnRooms() == null || type.getMonsterSpawnRooms().getSpawnRooms() == null) {
                continue;
            }
            for (MonsterSpawnRoom spawnRoom : type.getMonsterSpawnRooms().getSpawnRooms()) {
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
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests InMemoryMonsterRepositoryTest`
Expected: 9개 테스트 모두 PASS

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryMonsterRepository.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryMonsterRepositoryTest.java
git commit -m "feat(cache): add InMemoryMonsterRepository with MonsterType-based self-bootstrap"
```

---

## Task 2: `MonsterRespawnService` + `DefaultMonsterRespawnService` — TDD

리스폰 도메인 행위를 application service로 분리. 현재 `GameWorldService.respawnMonsters` 로직(`!isAlive() && canRespawn()` 체크 + `monster.respawn()` 호출 + 부활 수 카운트)을 그대로 흡수. `ActiveMonsterRepository.findAll()` 위에서 동작.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/MonsterRespawnService.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/DefaultMonsterRespawnService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/DefaultMonsterRespawnServiceTest.java`

- [ ] **Step 1: 인터페이스 작성**

`MonsterRespawnService.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service;

public interface MonsterRespawnService {
    /**
     * 죽었고 리스폰 가능한 모든 몬스터를 일괄 부활시킨다.
     * @return 부활시킨 몬스터 수
     */
    int respawnAll();
}
```

- [ ] **Step 2: 실패 테스트 작성**

`DefaultMonsterRespawnServiceTest.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultMonsterRespawnServiceTest {

    @Mock private ActiveMonsterRepository monsters;
    private DefaultMonsterRespawnService service;

    @BeforeEach
    void setUp() {
        service = new DefaultMonsterRespawnService(monsters);
    }

    @Test
    void respawnAll_invokesRespawn_onDeadRespawnableMonsters_andReturnsCount() {
        Monster dead1 = mock(Monster.class);
        Monster dead2 = mock(Monster.class);
        when(dead1.isAlive()).thenReturn(false);
        when(dead2.isAlive()).thenReturn(false);
        when(dead1.canRespawn()).thenReturn(true);
        when(dead2.canRespawn()).thenReturn(true);
        when(monsters.findAll()).thenReturn(List.of(dead1, dead2));

        int count = service.respawnAll();

        verify(dead1).respawn();
        verify(dead2).respawn();
        assertThat(count).isEqualTo(2);
    }

    @Test
    void respawnAll_skipsAliveMonsters() {
        Monster alive = mock(Monster.class);
        when(alive.isAlive()).thenReturn(true);
        when(monsters.findAll()).thenReturn(List.of(alive));

        int count = service.respawnAll();

        verify(alive, never()).respawn();
        assertThat(count).isZero();
    }

    @Test
    void respawnAll_skipsDeadButNotRespawnableMonsters() {
        Monster dead = mock(Monster.class);
        when(dead.isAlive()).thenReturn(false);
        when(dead.canRespawn()).thenReturn(false);
        when(monsters.findAll()).thenReturn(List.of(dead));

        int count = service.respawnAll();

        verify(dead, never()).respawn();
        assertThat(count).isZero();
    }

    @Test
    void respawnAll_returnsZero_whenNoMonsters() {
        when(monsters.findAll()).thenReturn(List.of());

        int count = service.respawnAll();

        assertThat(count).isZero();
    }
}
```

- [ ] **Step 3: 컴파일 실패 확인**

Run: `./gradlew test --tests DefaultMonsterRespawnServiceTest`
Expected: 컴파일 실패 (`DefaultMonsterRespawnService` 미존재)

- [ ] **Step 4: `DefaultMonsterRespawnService` 구현**

`DefaultMonsterRespawnService.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DefaultMonsterRespawnService implements MonsterRespawnService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultMonsterRespawnService.class);

    private final ActiveMonsterRepository monsters;

    public DefaultMonsterRespawnService(ActiveMonsterRepository monsters) {
        this.monsters = monsters;
    }

    @Override
    public int respawnAll() {
        int count = 0;
        for (Monster monster : monsters.findAll()) {
            if (!monster.isAlive() && monster.canRespawn()) {
                monster.respawn();
                count++;
                logger.debug("Monster respawned: {} (ID: {}, Room: {})",
                        monster.getName(), monster.getId(), monster.getCurrentRoomId());
            }
        }
        return count;
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests DefaultMonsterRespawnServiceTest`
Expected: 4개 테스트 모두 PASS

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/MonsterRespawnService.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/DefaultMonsterRespawnService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/DefaultMonsterRespawnServiceTest.java
git commit -m "feat(gameplay): add MonsterRespawnService + DefaultMonsterRespawnService"
```

---

## Task 3: `GameWorldScheduler` 추가 + `PersistenceManager`의 두 `@Scheduled` 메서드 제거 (단일 commit)

spec 7 명세대로 `GameWorldScheduler`를 구체 `@Component` 클래스로 신설. `List<BatchSyncable>` (스프링이 5개 빈 자동 주입: Room/Player/Npc/Monster/PartySync) + `MonsterRespawnService`를 받아 60초 persist + 5초 respawnMonsters 두 `@Scheduled` 진입점을 갖는다. **동시에** `PersistenceManager`의 `persistGameState()` / `checkMonsterRespawn()` 두 `@Scheduled` 메서드를 함께 제거해 이중 실행을 회피한다(사용자 권고: GameWorldScheduler 추가 시점에 즉시 PersistenceManager 메서드 제거). `BatchSyncable` Javadoc의 "PersistenceManager.persistGameState()의 외부 @Transactional이 임시로..." 문구도 본 task에서 갱신.

`batchSyncables` 의존성은 `PersistenceManager`에서 제거 가능(persistGameState만 사용). `MonsterTypeRepository`/`Random`은 아직 `loadMonsters()` private 메서드가 사용 중이므로 본 task에서는 유지 → Task 4에서 정리.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldScheduler.java`
- Create: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldSchedulerTest.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/BatchSyncable.java`

- [ ] **Step 1: 실패 테스트 작성**

`GameWorldSchedulerTest.java`:
```java
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
```

- [ ] **Step 2: 컴파일 실패 확인**

Run: `./gradlew test --tests GameWorldSchedulerTest`
Expected: 컴파일 실패 (`GameWorldScheduler` 미존재)

- [ ] **Step 3: `GameWorldScheduler` 구현**

`GameWorldScheduler.java`:
```java
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
```

- [ ] **Step 4: `PersistenceManager`에서 `persistGameState()` / `checkMonsterRespawn()` 두 `@Scheduled` 메서드 제거**

현재 `PersistenceManager.java` (line 78-93)의 다음 두 메서드를 통째로 삭제:
```java
@Transactional
@Scheduled(fixedDelay = 60_000) // 1분마다 저장
public void persistGameState() {
    batchSyncables.forEach(BatchSyncable::syncToDb);
}

/**
 * 몬스터 리스폰 처리
 */
@Scheduled(fixedDelay = 5_000) // 5초마다 실행
public void checkMonsterRespawn() {
    int respawnCount = gameWorldService.respawnMonsters();
    if (respawnCount > 0) {
        logger.debug("Respawned {} monsters", respawnCount);
    }
}
```

이어서 `batchSyncables` 의존성도 제거 (persistGameState만 사용했음):
- 필드 제거: `private final List<BatchSyncable> batchSyncables;` (line 28)
- 생성자 파라미터 제거: `final List<BatchSyncable> batchSyncables`
- 생성자 본문 제거: `this.batchSyncables = batchSyncables;`
- 미사용 import 제거:
  - `import com.jefflife.mudmk2.gameplay.application.service.sync.BatchSyncable;`
  - `import org.springframework.scheduling.annotation.Scheduled;`

변경 후 `PersistenceManager.java` (Task 3 종료 시점 — `loadMonsters` private 메서드는 Task 4에서 제거 예정이라 본 task에서는 유지):
```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterSpawnRoom;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;
import com.jefflife.mudmk2.gamedata.application.service.required.MonsterTypeRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PartyRepository;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class PersistenceManager {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PersistenceManager.class);
    private static final Random random = new Random();

    private final MonsterTypeRepository monsterTypeRepository;
    private final GameWorldService gameWorldService;
    private final PartyRepository partyRepository;

    public PersistenceManager(
            final MonsterTypeRepository monsterTypeRepository,
            final GameWorldService gameWorldService,
            final PartyRepository partyRepository
    ) {
        this.monsterTypeRepository = monsterTypeRepository;
        this.gameWorldService = gameWorldService;
        this.partyRepository = partyRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void loadGameState() {
        gameWorldService.loadParties(partyRepository.findAll());

        // MonsterType을 기반으로 Monster 생성 및 로드
        loadMonsters();

        logger.info("loadGameState finished");
    }

    /**
     * MonsterType을 기반으로 Monster를 생성하고 GameWorldService에 로드합니다.
     */
    private void loadMonsters() {
        List<Monster> monsters = new ArrayList<>();
        Iterable<MonsterType> monsterTypes = monsterTypeRepository.findAll();

        for (MonsterType monsterType : monsterTypes) {
            if (monsterType.getMonsterSpawnRooms() != null && monsterType.getMonsterSpawnRooms().getSpawnRooms() != null) {
                for (MonsterSpawnRoom spawnRoom : monsterType.getMonsterSpawnRooms().getSpawnRooms()) {
                    // 각 스폰 룸에서 지정된 수만큼 몬스터 생성
                    for (int i = 0; i < spawnRoom.getSpawnCount(); i++) {
                        // 몬스터 레벨 무작위 설정 (1~5 사이)
                        int monsterLevel = 1 + random.nextInt(5);
                        Monster monster = Monster.createFromType(monsterType, monsterLevel, spawnRoom.getRoomId());
                        monsters.add(monster);
                    }
                }
            }
        }

        gameWorldService.loadMonsters(monsters);
        logger.info("Created and loaded {} monsters from {} monster types", monsters.size(), monsterTypes.spliterator().estimateSize());
    }
}
```

- [ ] **Step 5: `BatchSyncable` Javadoc 갱신**

`BatchSyncable.java` 현재 Javadoc:
```java
/**
 * 인메모리 상태를 DB에 일괄 저장하는 컨트랙트.
 * 구현체는 자신의 syncToDb()에 @Transactional을 직접 부여한다.
 * 한 구현체의 실패가 다른 구현체로 전파되지 않도록 트랜잭션은 구현체별로 끊긴다.
 * <p>
 * 마이그레이션 노트: 기존 *SyncService 구현체는 후속 단계에서 InMemory*Repository로 흡수되며,
 * 그 시점에 본 계약을 완전히 따른다. 그 전까지는 PersistenceManager.persistGameState()의
 * 외부 @Transactional이 임시로 트랜잭션 경계를 제공한다.
 */
```

변경 후:
```java
/**
 * 인메모리 상태를 DB에 일괄 저장하는 컨트랙트.
 * 구현체는 자신의 syncToDb()에 @Transactional을 직접 부여한다.
 * 한 구현체의 실패가 다른 구현체로 전파되지 않도록 트랜잭션은 구현체별로 끊긴다.
 * <p>
 * GameWorldScheduler.persist() 가 60초마다 모든 BatchSyncable 빈을 호출한다.
 */
```

- [ ] **Step 6: 빌드 + 테스트 그린 확인**

Run: `./gradlew test --tests GameWorldSchedulerTest`
Expected: 3개 테스트 모두 PASS

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL (전체 회귀 확인)

- [ ] **Step 7: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldScheduler.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldSchedulerTest.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/BatchSyncable.java
git commit -m "refactor(gameplay): add GameWorldScheduler — move @Scheduled persist/respawn from PersistenceManager"
```

---

## Task 4: `PersistenceManager`에서 `loadMonsters()` 제거 + 의존성 정리

`InMemoryMonsterRepository`가 자체 부트스트랩으로 Monster를 적재하므로 `PersistenceManager.loadMonsters()`와 `loadGameState`에서 그 호출 라인을 제거. `MonsterTypeRepository`, `Random`, 관련 import도 제거. `loadGameState`는 `loadParties` 한 줄만 남아 phase 6에서 사라지기 직전 상태가 된다.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java`

- [ ] **Step 1: `PersistenceManager.java` 정리**

다음을 모두 제거:
- `loadGameState` 내 `loadMonsters()` 호출 라인:
  ```java
  // MonsterType을 기반으로 Monster 생성 및 로드
  loadMonsters();
  ```
- `private void loadMonsters() { ... }` 메서드 통째로 제거 (현재 Task 3 종료 시점의 line 53-76)
- 필드 제거: `private final MonsterTypeRepository monsterTypeRepository;`
- 필드 제거: `private static final Random random = new Random();`
- 생성자 파라미터 제거: `final MonsterTypeRepository monsterTypeRepository`
- 생성자 본문 제거: `this.monsterTypeRepository = monsterTypeRepository;`
- 미사용 import 제거:
  - `import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;`
  - `import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterSpawnRoom;`
  - `import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;`
  - `import com.jefflife.mudmk2.gamedata.application.service.required.MonsterTypeRepository;`
  - `import java.util.ArrayList;`
  - `import java.util.List;`
  - `import java.util.Random;`

변경 후 `PersistenceManager.java` 전체:
```java
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
```

- [ ] **Step 2: 빌드 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java
git commit -m "refactor(gameplay): move Monster cache bootstrap into InMemoryMonsterRepository"
```

---

## Task 5: `SimulationService` 호출처 마이그레이션

`gameWorldService.addMonster(...)`/`gameWorldService.removeMonster(...)` 두 호출을 `ActiveMonsterRepository.add(...)`/`ActiveMonsterRepository.remove(...)`로 교체. SimulationService는 Monster 외 다른 도메인을 사용하지 않으므로 `GameWorldService` 의존을 완전히 제거하고 `ActiveMonsterRepository`로 대체한다.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/SimulationService.java`

- [ ] **Step 1: `SimulationService.java` 변환**

import 변경:
```java
// 제거
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;  // (그대로 두지 말고 삭제)

// 추가
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
```

`GameWorldService` 의존을 `ActiveMonsterRepository`로 교체. 변경 후 전체:
```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto.SimulationSpawnRequest;
import com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto.SpawnedMonsterResponse;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimulationService {

    private final ActiveMonsterRepository monsters;
    private final Map<UUID, Monster> spawnedMonsters = new ConcurrentHashMap<>();

    @Value("${simulation.room-id:1}")
    private Long simulationRoomId;

    public SimulationService(ActiveMonsterRepository monsters) {
        this.monsters = monsters;
    }

    public SpawnedMonsterResponse spawn(SimulationSpawnRequest request) {
        Monster monster = Monster.createSimulation(
            request.getName(),
            request.getVigor(), request.getPhysique(), request.getAgility(),
            request.getIntellect(), request.getWill(), request.getMeridian(),
            request.getInnerPower(), request.getSpecialTechnique(), request.getLightStep(),
            request.getFistsAndPalms(), request.getSwordMethod(), request.getBladeMethod(),
            request.getLongWeapon(), request.getEsotericWeapon(), request.getArchery(),
            request.getWeaponBaseDamage(), request.getEquipmentArmor(), request.getEquipmentArmorPct(),
            simulationRoomId
        );
        spawnedMonsters.put(monster.getId(), monster);
        monsters.add(monster);
        return SpawnedMonsterResponse.from(monster);
    }

    public List<SpawnedMonsterResponse> listSpawned() {
        return spawnedMonsters.values().stream()
            .map(SpawnedMonsterResponse::from)
            .toList();
    }

    public boolean remove(UUID monsterId) {
        Monster removed = spawnedMonsters.remove(monsterId);
        if (removed != null) {
            monsters.remove(monsterId);
            return true;
        }
        return false;
    }
}
```

- [ ] **Step 2: 빌드 + 테스트 그린 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL (SimulationService 단위 테스트 없음 — 컴파일 + 전체 회귀 확인)

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/SimulationService.java
git commit -m "refactor(gameplay): migrate SimulationService monster add/remove to ActiveMonsterRepository"
```

---

## Task 6: `GameWorldService`에서 Monster 멤버 제거 + 임시 위임 + FakeGameWorldService 갱신

Monster 호출처가 모두 마이그레이션 완료(`SimulationService` task 5, `PersistenceManager` task 3+4). `GameWorldService`에서 Monster 캐시 필드 + 6개 메서드를 제거하고, `getMonstersInRoom`/`getMonstersByType`은 phase 6에서 Query로 이전할 때까지 `ActiveMonsterRepository.findAll()` 위의 임시 위임으로 유지(기존 필터 조건: `getMonstersInRoom`은 `isAlive() && roomId.equals(...)`, `getMonstersByType`은 `monsterTypeId.equals(...)`). `SpeakCommandServiceTest.FakeGameWorldService`의 부모 생성자 호출에 `ActiveMonsterRepository` 인자를 `null`로 추가(오버라이드된 메서드만 호출되므로 안전).

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java`

- [ ] **Step 1: 잔여 호출처 부재 재확인**

Run:
```bash
grep -rn "gameWorldService\.\(addMonster\|getMonsterById\|getAllMonsters\|removeMonster\|loadMonsters\|respawnMonsters\)" src/main/ --include="*.java"
```
Expected: 출력 없음

Run:
```bash
grep -n "activeMonsters\|loadMonsters\|addMonster\|getMonsterById\|getAllMonsters\|removeMonster\|respawnMonsters" src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java
```
Expected: `GameWorldService` 내부 정의만 출력됨 (활성 호출처 없음 확인)

- [ ] **Step 2: `GameWorldService.java`에서 Monster 캐시 멤버 모두 제거**

`GameWorldService.java` (현재 line 40-135) 다음 멤버 모두 제거:
- `private final Map<UUID, Monster> activeMonsters = new ConcurrentHashMap<>();` (line 40)
- `loadMonsters(List<Monster>)` 메서드 (line 43-52)
- `addMonster(Monster)` 메서드 (line 54-62)
- `getMonsterById(UUID)` 메서드 (line 86-93)
- `getAllMonsters()` 메서드 (line 95-101)
- `removeMonster(UUID)` 메서드 (line 103-112)
- `respawnMonsters()` 메서드 (line 114-135)

남길 메서드(phase 6에서 처리, 본 task에서 `ActiveMonsterRepository.findAll()` 위 임시 위임으로 변경):
- `getMonstersInRoom(Long roomId)` (현재 line 64-73)
- `getMonstersByType(Long monsterTypeId)` (현재 line 75-84)

생성자/필드 추가:
- 필드 추가: `private final ActiveMonsterRepository monsters;`
- 생성자에 `ActiveMonsterRepository monsters` 파라미터 추가 + 본문 `this.monsters = monsters;`

import 추가:
```java
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
```

`getMonstersInRoom` 변경 (기존 필터 조건 `isAlive() && roomId.equals(...)` 유지):
```java
    // TODO(phase6): RoomOccupancyQuery.monstersIn(roomId)으로 이전 — 위치 기반 조회는 Read Model로 분리
    public List<Monster> getMonstersInRoom(Long roomId) {
        return StreamSupport.stream(monsters.findAll().spliterator(), false)
                .filter(monster -> monster.isAlive() && monster.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
    }
```

`getMonstersByType` 변경 (기존 필터 조건 `monsterTypeId.equals(...)` 유지):
```java
    // TODO(phase6): CreatureLookupQuery.findMonstersByType(monsterTypeId)으로 이전 — 타입 기반 조회는 Read Model로 분리
    public List<Monster> getMonstersByType(Long monsterTypeId) {
        return StreamSupport.stream(monsters.findAll().spliterator(), false)
                .filter(monster -> monster.getMonsterTypeId().equals(monsterTypeId))
                .collect(Collectors.toList());
    }
```

미사용 import 검증/정리 (수동 확인 필요):
- `ConcurrentHashMap` — `parties` 필드가 여전히 사용 → 유지
- `UUID` — `isInParty`/`getPartyByPlayerId`/`addParty`/`removeParty` 사용 → 유지
- `java.util.*` 와일드카드는 그대로 유지(`ArrayList` 이전에 없었으면 무관 — 현재 import는 `java.util.*` 와일드카드)

변경 후 `GameWorldService.java` 클래스 헤더/생성자/필드 부분:
```java
@Component
public class GameWorldService {
    private final static Logger logger = LoggerFactory.getLogger(GameWorldService.class);

    private final ApplicationEventPublisher eventPublisher;
    private final ActivePlayerRepository players;
    private final ActiveNpcRepository npcs;
    private final ActiveMonsterRepository monsters;

    public GameWorldService(
            final ApplicationEventPublisher eventPublisher,
            final ActivePlayerRepository players,
            final ActiveNpcRepository npcs,
            final ActiveMonsterRepository monsters
    ) {
        this.eventPublisher = eventPublisher;
        this.players = players;
        this.npcs = npcs;
        this.monsters = monsters;
    }

    private final Map<UUID, Party> parties = new ConcurrentHashMap<>();

    // ... (Monster 관련 멤버는 모두 삭제됨, getMonstersInRoom/getMonstersByType만 임시 위임으로 유지)
```

- [ ] **Step 3: `SpeakCommandServiceTest.FakeGameWorldService` 부모 생성자 호출 갱신**

현재 `SpeakCommandServiceTest.java` (line 215-217):
```java
public FakeGameWorldService() {
    super(event -> {}, new FakeActivePlayerRepository(), null);
}
```

변경:
```java
public FakeGameWorldService() {
    super(event -> {}, new FakeActivePlayerRepository(), null, null);
}
```

`null`을 넘기는 이유: `FakeGameWorldService`는 `getPlayerByName`/`getNpcByName`/`getPlayersInRoom`을 오버라이드해 사용 중이며(현재 line 232-245), SpeakCommandService 실행 흐름에서 `getMonstersInRoom`/`getMonstersByType`을 호출하지 않으므로 부모 `monsters` 필드는 도달되지 않는다. Phase 6에서 `GameWorldService`가 삭제될 때 함께 정리될 임시 코드.

- [ ] **Step 4: 빌드 + 테스트 그린 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL (Phase 4 그린 226+12개 테스트 + 본 phase 신규 16개 = 254개 그린)

- [ ] **Step 5: `gameplay` 모듈 잔여 GameWorldService Monster API 사용 검증**

Run:
```bash
grep -rn "gameWorldService\.\(addMonster\|getMonsterById\|getAllMonsters\|removeMonster\|loadMonsters\|respawnMonsters\)" src/ --include="*.java"
```
Expected: 출력 없음

Run:
```bash
grep -n "activeMonsters" src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java
```
Expected: 출력 없음 (필드 제거 확인)

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java
git commit -m "refactor(gameplay): remove Monster cache from GameWorldService — delegated to InMemoryMonsterRepository"
```

---

## 완료 기준

- 신규 파일 7개:
  - `InMemoryMonsterRepository` + 단위 테스트 (9종 그린: bootstrap 정상 / null spawn rooms / 빈 spawn rooms / findById empty / findAll / add / remove / remove noop / syncToDb noop)
  - `MonsterRespawnService` 인터페이스
  - `DefaultMonsterRespawnService` + 단위 테스트 (4종 그린: 죽고 부활 가능 → respawn 호출 + 카운트 / 살아있음 skip / 부활 불가 skip / 빈 리스트 0)
  - `GameWorldScheduler` + 단위 테스트 (3종 그린: persist forEach / respawnMonsters 위임 / respawn=0 무예외)
- `PersistenceManager`:
  - `persistGameState()`, `checkMonsterRespawn()`, `loadMonsters()` 3개 메서드 제거
  - 의존성 (`MonsterTypeRepository`, `batchSyncables`, `Random`) 제거
  - `loadGameState` 본문이 `loadParties` 한 줄 + 로그 한 줄만 남음 (phase 6에서 완전 삭제)
- `GameWorldService`:
  - 제거: `activeMonsters` 필드 + `loadMonsters` / `addMonster` / `getMonsterById` / `getAllMonsters` / `removeMonster` / `respawnMonsters`
  - 임시 위임으로 유지: `getMonstersInRoom`, `getMonstersByType` (phase 6에서 Query로 이관 후 완전 제거)
  - 생성자에 `ActiveMonsterRepository` 추가 (`ApplicationEventPublisher`, `ActivePlayerRepository`, `ActiveNpcRepository`, `ActiveMonsterRepository`)
- `SimulationService`가 `ActiveMonsterRepository`만 의존, `GameWorldService` 의존 완전 제거.
- `BatchSyncable.java` Javadoc에서 "PersistenceManager.persistGameState()의 외부 @Transactional" 표현 제거 → "GameWorldScheduler.persist()가 60초마다 호출"로 갱신.
- `SpeakCommandServiceTest.FakeGameWorldService` 부모 생성자 인자 4개 (`ActiveMonsterRepository` 자리에 `null`).
- 60초 persist / 5초 respawn은 `GameWorldScheduler` 단일 진입점에서만 실행 (이중 실행 없음).
- `./gradlew test` 그린 (전체 회귀 확인 — 본 phase 신규 16개 그린).
- 애플리케이션 부트 시 로그: "Loaded N monsters from M monster types" (`InMemoryMonsterRepository`).

## 다음 phase

본 plan 완료 후 같은 패턴으로:
- `2026-XX-XX-gameworldservice-refactor-party.md` (Party 도메인 + `DefaultPartyService` 이벤트 책임 이관 + 3개 Query 구현체 — `PartySyncService` 삭제)
- 이후 잔여 정리(도메인 모델 주석, `GameWorldService.java`, `PersistenceManager.java` 완전 삭제) 순.
