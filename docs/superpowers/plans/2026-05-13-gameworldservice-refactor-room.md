# GameWorldService 리팩토링 구현 계획 — 공통 기반 + Room 도메인

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `GameWorldService` 리팩토링의 첫 단계로 공통 포트/예외/Query 인터페이스를 추가하고, Room 도메인 한 가지를 `InMemoryRoomRepository`로 완전 이전한다. 빌드/테스트 그린 상태로 PR 가능한 자기완결 단위.

**Architecture:** `gameplay`에 `ActiveRoomRepository` 포트와 `InMemoryRoomRepository` 어댑터 신설. 어댑터는 `gamedata`의 JPA `RoomRepository`에 위임해 `ApplicationReadyEvent` 시점에 self-bootstrap하고, `BatchSyncable.syncToDb()`로 60초마다 메모리→DB 일괄 저장. 호출처 10개가 새 포트를 의존하도록 옮기고 `GameWorldService`에서 Room 관련 메서드를 제거한다.

**Tech Stack:** Java 17, Spring Boot 3.x, JPA, JUnit 5, Mockito, AssertJ

**Spec:** `docs/superpowers/specs/2026-05-13-gameworldservice-refactor-design.md`

---

## 파일 구조

**생성**
- `src/main/java/com/jefflife/mudmk2/gameplay/application/exception/RoomNotFoundException.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/exception/PlayerNotFoundException.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/exception/NpcNotFoundException.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/exception/MonsterNotFoundException.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/exception/PartyNotFoundException.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/required/ActiveRoomRepository.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/required/ActivePlayerRepository.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/required/ActiveNpcRepository.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/required/ActiveMonsterRepository.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/required/ActivePartyRepository.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/RoomOccupancyQuery.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/CreatureLookupQuery.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/PartyMembershipQuery.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryRoomRepository.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryRoomRepositoryTest.java`

**수정**
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java` (Room 관련 필드/메서드 제거)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java` (`loadRooms` 호출 제거)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/BatchSyncable.java` (주석 갱신: 트랜잭션 모델 변경)
- 호출처 9개 (Room 직접 사용 — `InventoryCommandService`는 Room 미사용으로 제외):
  - `src/main/java/com/jefflife/mudmk2/gameplay/application/service/RoomInfoService.java`
  - `src/main/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceService.java`
  - `src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatService.java`
  - `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/MoveCommandService.java`
  - `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandService.java`
  - `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/DropCommandService.java`
  - `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/TakeCommandService.java`
  - `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/DirectionSearchStrategy.java`
  - `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/look/ItemSearchStrategy.java`
- 영향받는 테스트 (각 Task에서 함께 수정):
  - `src/test/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceServiceTest.java`
  - `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/DropCommandServiceTest.java`
  - `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/TakeCommandServiceTest.java`
  - `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/DirectionSearchStrategyTest.java`
  - `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/look/ItemSearchStrategyTest.java`
  - `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/look/LookableTargetFinderTest.java`

---

## Task 1: 도메인 예외 5종

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/exception/RoomNotFoundException.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/exception/PlayerNotFoundException.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/exception/NpcNotFoundException.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/exception/MonsterNotFoundException.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/exception/PartyNotFoundException.java`

- [ ] **Step 1: `RoomNotFoundException` 생성**

```java
package com.jefflife.mudmk2.gameplay.application.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(Long roomId) {
        super("Room not found: " + roomId);
    }
}
```

- [ ] **Step 2: `PlayerNotFoundException` 생성**

```java
package com.jefflife.mudmk2.gameplay.application.exception;

import java.util.UUID;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(Long userId) {
        super("Player not found for userId: " + userId);
    }
    public PlayerNotFoundException(UUID characterId) {
        super("Player not found for characterId: " + characterId);
    }
}
```

- [ ] **Step 3: `NpcNotFoundException` 생성**

```java
package com.jefflife.mudmk2.gameplay.application.exception;

import java.util.UUID;

public class NpcNotFoundException extends RuntimeException {
    public NpcNotFoundException(UUID npcId) {
        super("NPC not found: " + npcId);
    }
}
```

- [ ] **Step 4: `MonsterNotFoundException` 생성**

```java
package com.jefflife.mudmk2.gameplay.application.exception;

import java.util.UUID;

public class MonsterNotFoundException extends RuntimeException {
    public MonsterNotFoundException(UUID monsterId) {
        super("Monster not found: " + monsterId);
    }
}
```

- [ ] **Step 5: `PartyNotFoundException` 생성**

```java
package com.jefflife.mudmk2.gameplay.application.exception;

import java.util.UUID;

public class PartyNotFoundException extends RuntimeException {
    public PartyNotFoundException(UUID partyId) {
        super("Party not found: " + partyId);
    }
}
```

- [ ] **Step 6: 빌드 그린 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/exception/
git commit -m "feat(gameplay): add domain not-found exceptions"
```

---

## Task 2: Active*Repository 포트 5종

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/required/ActiveRoomRepository.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/required/ActivePlayerRepository.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/required/ActiveNpcRepository.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/required/ActiveMonsterRepository.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/required/ActivePartyRepository.java`

- [ ] **Step 1: `ActiveRoomRepository` 생성**

```java
package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;

import java.util.Optional;

public interface ActiveRoomRepository {
    Optional<Room> findById(Long id);
    Iterable<Room> findAll();
    void add(Room room);
    void remove(Long id);
}
```

> 주: `add(Room)`은 admin API에서 신규 방 등록 시 캐시 동기화에 사용하거나, 통합 테스트의 데이터 시드에 사용한다 (`LookableTargetFinderTest` 등).

- [ ] **Step 2: `ActivePlayerRepository` 생성**

```java
package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

import java.util.Optional;
import java.util.UUID;

public interface ActivePlayerRepository {
    Optional<PlayerCharacter> findById(UUID id);
    Optional<PlayerCharacter> findByUserId(Long userId);
    Iterable<PlayerCharacter> findAll();
    void add(PlayerCharacter player);
    void removeByUserId(Long userId);
}
```

- [ ] **Step 3: `ActiveNpcRepository` 생성**

```java
package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;

import java.util.Optional;
import java.util.UUID;

public interface ActiveNpcRepository {
    Optional<NonPlayerCharacter> findById(UUID id);
    Iterable<NonPlayerCharacter> findAll();
    void add(NonPlayerCharacter npc);
    void remove(UUID id);
}
```

- [ ] **Step 4: `ActiveMonsterRepository` 생성**

```java
package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;

import java.util.Optional;
import java.util.UUID;

public interface ActiveMonsterRepository {
    Optional<Monster> findById(UUID id);
    Iterable<Monster> findAll();
    void add(Monster monster);
    void remove(UUID id);
}
```

- [ ] **Step 5: `ActivePartyRepository` 생성**

```java
package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;

import java.util.Optional;
import java.util.UUID;

public interface ActivePartyRepository {
    Optional<Party> findById(UUID id);
    Iterable<Party> findAll();
    void add(Party party);
    void remove(UUID id);
}
```

- [ ] **Step 6: 빌드 그린 확인 + 커밋**

```bash
./gradlew compileJava
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/required/
git commit -m "feat(gameplay): add Active*Repository out-ports for in-memory caches"
```

---

## Task 3: Query 인터페이스 3종

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/RoomOccupancyQuery.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/CreatureLookupQuery.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/PartyMembershipQuery.java`

- [ ] **Step 1: `RoomOccupancyQuery` 생성**

```java
package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

import java.util.List;

public interface RoomOccupancyQuery {
    List<PlayerCharacter>    playersIn(Long roomId);
    List<NonPlayerCharacter> npcsIn(Long roomId);
    List<Monster>            monstersIn(Long roomId);
}
```

- [ ] **Step 2: `CreatureLookupQuery` 생성**

```java
package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

import java.util.List;
import java.util.Optional;

public interface CreatureLookupQuery {
    Optional<PlayerCharacter>    findPlayerByName(String name);
    Optional<NonPlayerCharacter> findNpcByName(String name);
    List<Monster>                findMonstersByType(Long monsterTypeId);
}
```

- [ ] **Step 3: `PartyMembershipQuery` 생성**

```java
package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;

import java.util.Optional;
import java.util.UUID;

public interface PartyMembershipQuery {
    Optional<Party> findByMemberId(UUID memberId);
    boolean         isInParty(UUID memberId);
}
```

- [ ] **Step 4: 빌드 그린 확인 + 커밋**

```bash
./gradlew compileJava
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/
git commit -m "feat(gameplay): add read-model Query interfaces for cross-aggregate lookups"
```

---

## Task 4: `BatchSyncable` 주석 갱신

현재 `BatchSyncable`의 JavaDoc은 "PersistenceManager의 @Scheduled 배치에서 호출되므로 구현체는 별도 @Transactional 불필요"라고 명시하지만, 새 설계는 어댑터별 `@Transactional`로 격리한다 (spec 9.2). 주석을 새 의도에 맞게 갱신한다.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/BatchSyncable.java`

- [ ] **Step 1: 주석 갱신**

`BatchSyncable.java` 내용 전체를 다음으로 교체:

```java
package com.jefflife.mudmk2.gameplay.application.service.sync;

/**
 * 인메모리 상태를 DB에 일괄 저장하는 컨트랙트.
 * 구현체는 자신의 syncToDb()에 @Transactional을 직접 부여한다.
 * 한 구현체의 실패가 다른 구현체로 전파되지 않도록 트랜잭션은 구현체별로 끊긴다.
 */
public interface BatchSyncable {
    void syncToDb();
}
```

- [ ] **Step 2: 빌드 그린 확인 + 커밋**

```bash
./gradlew compileJava
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/BatchSyncable.java
git commit -m "docs(sync): update BatchSyncable contract to per-adapter transactions"
```

---

## Task 5: `InMemoryRoomRepository` — TDD로 구현

`gamedata`의 JPA `RoomRepository`에 위임해 부트스트랩하고, `BatchSyncable`로 동기화한다.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryRoomRepository.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryRoomRepositoryTest.java`

- [ ] **Step 1: 테스트 클래스 작성**

```java
package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.service.required.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryRoomRepositoryTest {

    @Mock private RoomRepository jpa;
    private InMemoryRoomRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRoomRepository(jpa);
    }

    @Test
    void bootstrap_loadsAllRoomsFromJpa_andInitializesAssociations() {
        Room room1 = mock(Room.class);
        Room room2 = mock(Room.class);
        when(room1.getId()).thenReturn(1L);
        when(room2.getId()).thenReturn(2L);
        when(jpa.findAll()).thenReturn(List.of(room1, room2));

        repository.bootstrap();

        verify(room1).initializeAssociatedEntities();
        verify(room2).initializeAssociatedEntities();
        assertThat(repository.findById(1L)).contains(room1);
        assertThat(repository.findById(2L)).contains(room2);
    }

    @Test
    void findById_returnsEmpty_whenNotCached() {
        assertThat(repository.findById(999L)).isEmpty();
    }

    @Test
    void findAll_returnsAllCachedRooms() {
        Room room1 = mock(Room.class);
        Room room2 = mock(Room.class);
        when(room1.getId()).thenReturn(1L);
        when(room2.getId()).thenReturn(2L);
        when(jpa.findAll()).thenReturn(List.of(room1, room2));
        repository.bootstrap();

        assertThat(repository.findAll()).containsExactlyInAnyOrder(room1, room2);
    }

    @Test
    void remove_dropsTheRoomFromCache() {
        Room room = mock(Room.class);
        when(room.getId()).thenReturn(1L);
        when(jpa.findAll()).thenReturn(List.of(room));
        repository.bootstrap();

        repository.remove(1L);

        assertThat(repository.findById(1L)).isEmpty();
    }

    @Test
    void add_putsRoomIntoCache() {
        Room room = mock(Room.class);
        when(room.getId()).thenReturn(42L);

        repository.add(room);

        assertThat(repository.findById(42L)).contains(room);
    }

    @Test
    void syncToDb_savesCachedRoomsViaJpa() {
        Room room1 = mock(Room.class);
        Room room2 = mock(Room.class);
        when(room1.getId()).thenReturn(1L);
        when(room2.getId()).thenReturn(2L);
        when(jpa.findAll()).thenReturn(List.of(room1, room2));
        repository.bootstrap();

        repository.syncToDb();

        verify(jpa).saveAll(argThat(rooms -> {
            List<Room> list = new java.util.ArrayList<>();
            rooms.forEach(list::add);
            return list.size() == 2 && list.contains(room1) && list.contains(room2);
        }));
    }
}
```

- [ ] **Step 2: 테스트 실행으로 컴파일 실패 확인**

Run: `./gradlew test --tests InMemoryRoomRepositoryTest`
Expected: 컴파일 실패 (`InMemoryRoomRepository` 미존재)

- [ ] **Step 3: `InMemoryRoomRepository` 구현체 작성**

```java
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
    private static final Logger log = LoggerFactory.getLogger(InMemoryRoomRepository.class);

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
        log.info("Loaded {} rooms", cache.size());
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
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests InMemoryRoomRepositoryTest`
Expected: 6개 테스트 모두 PASS

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryRoomRepository.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryRoomRepositoryTest.java
git commit -m "feat(cache): add InMemoryRoomRepository with self-bootstrap and batch sync"
```

---

## Task 6: `PersistenceManager`에서 `loadRooms` 호출 제거

이제 Room 부트스트랩은 어댑터가 책임진다. `PersistenceManager.loadGameState`에서 해당 호출과 import를 제거한다.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java`

- [ ] **Step 1: `PersistenceManager.loadGameState`에서 Room 적재 라인 제거**

`PersistenceManager.java`의 다음 라인 두 줄을 삭제:

```java
        Iterable<Room> rooms = roomRepository.findAll();
        gameWorldService.loadRooms(rooms);
```

또한 다음 두 줄도 함께 정리:
- 사용되지 않게 된 `import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;`
- 사용되지 않게 된 `import com.jefflife.mudmk2.gamedata.application.service.required.RoomRepository;`
- 생성자/필드의 `RoomRepository roomRepository` 의존성 (다른 곳에서 안 쓰면 삭제 — 다른 사용처 없으면 모두 정리)

`PersistenceManager.java`의 변경 후 `loadGameState` 메서드는 다음 형태가 된다:

```java
@EventListener(ApplicationReadyEvent.class)
@Transactional(readOnly = true)
public void loadGameState() {
    Iterable<PlayerCharacter> players = playerCharacterRepository.findAll();
    gameWorldService.loadPlayers(players);

    Iterable<NonPlayerCharacter> nonPlayerCharacters = nonPlayerCharacterRepository.findAll();
    gameWorldService.loadNpcs(nonPlayerCharacters);

    gameWorldService.loadParties(partyRepository.findAll());

    loadMonsters();

    logger.info("loadGameState finished");
}
```

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 애플리케이션 부트 확인 (선택, 가능하면)**

Run: `./gradlew bootRun`로 부트 → 로그에 "Loaded N rooms"(InMemoryRoomRepository) 가 보여야 함. 부트 후 Ctrl+C로 종료.

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java
git commit -m "refactor(gameplay): move Room cache bootstrap into InMemoryRoomRepository"
```

---

## Task 7: 호출처 마이그레이션 — 그룹 1 (`RoomInfoService`, `ItemInstanceService`, `CombatService`)

호출처가 `gameWorldService.getRoom(id)` / `getRoomOptional(id)` 만 쓰는 단순 케이스. `ActiveRoomRepository.findById`로 대체.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/RoomInfoService.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceService.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatService.java`
- Modify (테스트 mocking): `src/test/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceServiceTest.java`

### `RoomInfoService`

- [ ] **Step 1: 의존성 교체 + `getRoom` 호출 치환**

`RoomInfoService.java`에서:

변경 전:
```java
private final GameWorldService gameWorldService;
...
final Room currentRoom = gameWorldService.getRoom(character.getCurrentRoomId());
```

변경 후 (생성자/필드 모두 갱신):
```java
import com.jefflife.mudmk2.gameplay.application.exception.RoomNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
...
private final GameWorldService gameWorldService;
private final ActiveRoomRepository rooms;

public RoomInfoService(
        final GameWorldService gameWorldService,
        final ActiveRoomRepository rooms,
        final SendRoomInfoMessagePort sendRoomInfoMessagePort
) {
    this.gameWorldService = gameWorldService;
    this.rooms = rooms;
    this.sendRoomInfoMessagePort = sendRoomInfoMessagePort;
}
...
final Room currentRoom = rooms.findById(character.getCurrentRoomId())
        .orElseThrow(() -> new RoomNotFoundException(character.getCurrentRoomId()));
```

(NPC/플레이어/몬스터 조회 부분은 본 phase 범위 밖이므로 `gameWorldService` 의존성은 유지.)

### `ItemInstanceService`

- [ ] **Step 2: `ItemInstanceService.java`의 `gameWorldService.getRoomOptional(roomId)` 두 곳을 모두 교체**

변경 전:
```java
gameWorldService.getRoomOptional(roomId).ifPresent(r -> { ... });
```

변경 후 (생성자에 `ActiveRoomRepository rooms` 추가, 호출 치환):
```java
rooms.findById(roomId).ifPresent(r -> { ... });
```

(다른 `gameWorldService` 호출은 phase 범위 밖이므로 유지.)

### `CombatService`

- [ ] **Step 3: `CombatService.java`의 `gameWorldService.getRoomOptional(roomId)` 교체**

변경 전:
```java
Optional<Room> room = gameWorldService.getRoomOptional(roomId);
```

변경 후 (생성자에 `ActiveRoomRepository rooms` 추가, 호출 치환):
```java
Optional<Room> room = rooms.findById(roomId);
```

### 테스트 보정

- [ ] **Step 4: `ItemInstanceServiceTest` 생성자 인자에 `ActiveRoomRepository` mock 추가**

변경 전 (요약):
```java
@Mock private GameWorldService gameWorldService;
...
service = new ItemInstanceService(
    itemTemplateRepository, itemInstanceRepository,
    roomRepository, playerCharacterRepository, gameWorldService
);
```

변경 후:
```java
@Mock private GameWorldService gameWorldService;
@Mock private ActiveRoomRepository rooms;
...
service = new ItemInstanceService(
    itemTemplateRepository, itemInstanceRepository,
    roomRepository, playerCharacterRepository, gameWorldService, rooms
);
// 기존 when(gameWorldService.getRoomOptional(...)) 호출은 when(rooms.findById(...))로 변경
```

기존 `when(gameWorldService.getRoomOptional(any())).thenReturn(...)` 식의 stub을 모두 `when(rooms.findById(...))`로 변경. 또한 `verify(gameWorldService).getRoomOptional(...)` 검증도 `verify(rooms).findById(...)`로 변경.

(`RoomInfoService`, `CombatService`에 단위 테스트가 없다면 Step 4 외 추가 작업 없음. 확인 명령: `find src/test -name "RoomInfoServiceTest.java" -o -name "CombatServiceTest.java"` — 없으면 다음 Step으로.)

- [ ] **Step 5: 전체 테스트 그린 확인 + 커밋**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL

```bash
git add -A
git commit -m "refactor(gameplay): migrate RoomInfo/ItemInstance/Combat services to ActiveRoomRepository"
```

---

## Task 8: 호출처 마이그레이션 — 그룹 2 (Command 서비스 4종)

`MoveCommandService`, `StatusCommandService`, `DropCommandService`, `TakeCommandService`는 모두 동일한 패턴 (`gameWorldService.getRoom(player.getCurrentRoomId())`)을 사용.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/MoveCommandService.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandService.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/DropCommandService.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/TakeCommandService.java`
- Modify: 영향받는 테스트 (`DropCommandServiceTest`, `TakeCommandServiceTest` 등 mock 갱신)

### 공통 변환 패턴

변경 전:
```java
private final GameWorldService gameWorldService;
...
Room room = gameWorldService.getRoom(fromRoomId);  // 또는 player.getCurrentRoomId()
```

변경 후:
```java
private final GameWorldService gameWorldService;
private final ActiveRoomRepository rooms;
...
Room room = rooms.findById(fromRoomId)
        .orElseThrow(() -> new RoomNotFoundException(fromRoomId));
```

각 클래스의 생성자에 `ActiveRoomRepository rooms` 파라미터 추가, 필드 추가, 호출 치환. import 추가:
```java
import com.jefflife.mudmk2.gameplay.application.exception.RoomNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
```

- [ ] **Step 1: `MoveCommandService` 변환**

`MoveCommandService.java:36-38`:
```java
PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
Long fromRoomId = player.getCurrentRoomId();
Room room = gameWorldService.getRoom(fromRoomId);
```
→
```java
PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
Long fromRoomId = player.getCurrentRoomId();
Room room = rooms.findById(fromRoomId)
        .orElseThrow(() -> new RoomNotFoundException(fromRoomId));
```
+ 생성자/필드/import 갱신.

- [ ] **Step 2: `StatusCommandService` 변환**

`StatusCommandService.java:37`의 `gameWorldService.getRoom(player.getCurrentRoomId())` 호출을 같은 패턴으로 변환. 생성자/필드/import 갱신.

- [ ] **Step 3: `DropCommandService` 변환**

`DropCommandService.java:48`의 `gameWorldService.getRoom(player.getCurrentRoomId())` 호출을 같은 패턴으로 변환. 생성자/필드/import 갱신.

- [ ] **Step 4: `TakeCommandService` 변환**

`TakeCommandService.java:34`의 `gameWorldService.getRoom(player.getCurrentRoomId())` 호출을 같은 패턴으로 변환. 생성자/필드/import 갱신.

- [ ] **Step 5: 영향받는 테스트 mock 갱신**

`DropCommandServiceTest`, `TakeCommandServiceTest`가 존재하면 각각의 생성자 인자에 `ActiveRoomRepository` mock 추가, 기존 `when(gameWorldService.getRoom(...))` stub을 `when(rooms.findById(...)).thenReturn(Optional.of(room))`으로 변경. `MoveCommandService`/`StatusCommandService`에 단위 테스트가 없다면 추가 작업 없음.

- [ ] **Step 6: 전체 테스트 그린 확인 + 커밋**

```bash
./gradlew test
git add -A
git commit -m "refactor(command): migrate Move/Status/Drop/Take to ActiveRoomRepository"
```

---

## Task 9: 호출처 마이그레이션 — 그룹 3 (`DirectionSearchStrategy`, `ItemSearchStrategy`)

확인된 남은 Room 의존 호출처는 2개 (`InventoryCommandService`는 Room을 직접 사용하지 않음 — `grep -n "getRoom\|getRoomOptional" InventoryCommandService.java`로 확인 완료).

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/DirectionSearchStrategy.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/look/ItemSearchStrategy.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/DirectionSearchStrategyTest.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/look/ItemSearchStrategyTest.java`

- [ ] **Step 1: `DirectionSearchStrategy` 변환**

`DirectionSearchStrategy.java:29`의 `Room currentRoom = gameWorldService.getRoom(player.getCurrentRoomId());`를 다음으로 변환:

```java
Room currentRoom = rooms.findById(player.getCurrentRoomId())
        .orElseThrow(() -> new RoomNotFoundException(player.getCurrentRoomId()));
```

생성자에 `ActiveRoomRepository rooms` 추가, 필드/import 갱신.

- [ ] **Step 2: `ItemSearchStrategy` 변환**

`ItemSearchStrategy.java:29`의 `Room currentRoom = gameWorldService.getRoom(player.getCurrentRoomId());`를 같은 패턴으로 변환. 생성자/필드/import 갱신.

- [ ] **Step 3: 영향받는 테스트 mock 갱신**

`DirectionSearchStrategyTest`의 `when(gameWorldService.getRoom(roomId)).thenReturn(currentRoom);` 두 곳 (라인 54, 72) → `when(rooms.findById(roomId)).thenReturn(Optional.of(currentRoom));`로 변경. `@Mock ActiveRoomRepository rooms;` 필드 추가, 생성자 인자에 추가.

`ItemSearchStrategyTest`의 `when(gameWorldService.getRoom(roomId)).thenReturn(currentRoom);` 여섯 곳 (라인 63, 79, 98, 113, 125)도 동일하게 변경.

- [ ] **Step 4: 전체 테스트 그린 확인 + 커밋**

```bash
./gradlew test
git add -A
git commit -m "refactor(command): migrate Direction/ItemSearch strategies to ActiveRoomRepository"
```

---

## Task 10: 통합 테스트 마이그레이션 — `LookableTargetFinderTest`

`@SpringBootTest` 통합 테스트가 `gameWorldService.loadRooms`/`removeRoom`을 setUp/tearDown에서 직접 호출. 다음 Task에서 이 메서드들이 삭제되므로 마이그레이션 필요.

**Files:**
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/look/LookableTargetFinderTest.java`

- [ ] **Step 1: 의존성을 `ActiveRoomRepository`로 교체**

변경 전:
```java
@SpringBootTest
record LookableTargetFinderTest(LookableTargetFinder targetFinder, GameWorldService gameWorldService) {
    ...
    @BeforeEach
    void setUp() {
        GameTestFixture.DirectionTestSetup setup = GameTestFixture.createDirectionTestSetup(
                TEST_USER_ID, TEST_ROOM_ID, TEST_NEXT_ROOM_ID
        );
        gameWorldService.loadRooms(List.of(setup.currentRoom(), setup.nextRoom()));
        gameWorldService.addPlayer(setup.player());
    }

    @AfterEach
    void tearDown() {
        gameWorldService.removePlayer(TEST_USER_ID);
        gameWorldService.removeRoom(TEST_ROOM_ID);
        gameWorldService.removeRoom(TEST_NEXT_ROOM_ID);
    }
```

변경 후:
```java
@SpringBootTest
record LookableTargetFinderTest(
        LookableTargetFinder targetFinder,
        GameWorldService gameWorldService,
        ActiveRoomRepository rooms
) {
    ...
    @BeforeEach
    void setUp() {
        GameTestFixture.DirectionTestSetup setup = GameTestFixture.createDirectionTestSetup(
                TEST_USER_ID, TEST_ROOM_ID, TEST_NEXT_ROOM_ID
        );
        rooms.add(setup.currentRoom());
        rooms.add(setup.nextRoom());
        gameWorldService.addPlayer(setup.player());
    }

    @AfterEach
    void tearDown() {
        gameWorldService.removePlayer(TEST_USER_ID);
        rooms.remove(TEST_ROOM_ID);
        rooms.remove(TEST_NEXT_ROOM_ID);
    }
```

import 추가: `import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;`

(player 관련 `addPlayer`/`removePlayer` 호출은 phase 3 (Player) 에서 마이그레이션 — 현재 유지.)

- [ ] **Step 2: 테스트 그린 확인 + 커밋**

Run: `./gradlew test --tests LookableTargetFinderTest`
Expected: BUILD SUCCESSFUL

```bash
git add src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/look/LookableTargetFinderTest.java
git commit -m "test(look): migrate LookableTargetFinderTest setup/teardown to ActiveRoomRepository"
```

---

## Task 11: `GameWorldService`에서 Room 관련 멤버 제거

호출처가 모두 마이그레이션 완료됐으므로 `GameWorldService`에서 Room 관련 필드와 메서드를 안전하게 제거할 수 있다.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java`

- [ ] **Step 1: 제거 대상 식별 확인**

Run: `grep -n "getRoom\|getRoomOptional\|removeRoom\|loadRooms\|rooms\b" src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java`
Expected: 출력된 모든 라인이 다음 중 하나에 속하는지 확인:
- `private final Map<Long, Room> rooms = new ConcurrentHashMap<>();`
- `loadRooms(Iterable<Room> rooms)` 메서드 본체
- `getRoom(Long id)` 메서드
- `getRoomOptional(Long roomId)` 메서드
- `removeRoom(Long roomId)` 메서드
- `moveNpcToRoom`의 `rooms.get(roomId) == null` 검사 (이 부분은 Phase 4(NPC)에서 다루므로 **유지**)

`moveNpcToRoom` 안의 `rooms.get(roomId) == null` 라인은 NPC 도메인 마이그레이션 단계에서 함께 정리되므로 본 phase에서는 임시로 코드를 다음과 같이 변경한다:

```java
// 기존: if (npc == null || rooms.get(roomId) == null) { return false; }
// 변경: if (npc == null) { return false; }  // TODO(phase4): roomId 존재성 검증을 NpcLocationService로 이전
```

이렇게 하면 `rooms` 필드 제거가 가능해진다.

- [ ] **Step 2: `GameWorldService.java`에서 Room 관련 멤버 모두 삭제**

다음을 모두 제거:
- `private final Map<Long, Room> rooms = new ConcurrentHashMap<>();` (라인 32)
- `loadRooms(...)` 메서드 (라인 37-43)
- `getRoom(Long id)` 메서드 (라인 214-220)
- `getRoomOptional(Long roomId)` 메서드 (라인 222-224)
- `removeRoom(Long roomId)` 메서드 (라인 230-235)
- 사용되지 않게 된 `import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;`

`moveNpcToRoom`의 `rooms.get(roomId) == null` 검사를 Step 1에 따라 단순화. (phase 4에서 완전 정리)

- [ ] **Step 3: 전체 빌드/테스트 그린 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL — 어떤 호출자도 더 이상 `getRoom`/`getRoomOptional`/`removeRoom`/`loadRooms`를 호출하지 않으므로 컴파일 그린.

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java
git commit -m "refactor(gameplay): remove Room cache from GameWorldService — fully delegated to InMemoryRoomRepository"
```

---

## 완료 기준

- 신규 파일 15개 (예외 5, 포트 5, Query 3, 어댑터 1, 어댑터 테스트 1) 생성.
- `BatchSyncable` JavaDoc 갱신 (어댑터별 트랜잭션 명시).
- `InMemoryRoomRepository` 단위 테스트 6종 그린 (bootstrap, findById empty, findAll, remove, syncToDb, add).
- `GameWorldService`에 Room 관련 필드/메서드 없음 (`getRoom`, `getRoomOptional`, `removeRoom`, `loadRooms`, `rooms` 필드 모두 제거).
- `PersistenceManager.loadGameState`에서 Room 적재 라인 제거.
- Room 의존 호출처 9개가 `ActiveRoomRepository`를 의존 (`RoomInfoService`, `ItemInstanceService`, `CombatService`, `MoveCommandService`, `StatusCommandService`, `DropCommandService`, `TakeCommandService`, `DirectionSearchStrategy`, `ItemSearchStrategy`).
- 통합 테스트 `LookableTargetFinderTest`의 Room 시드/정리가 `ActiveRoomRepository`를 통과.
- `./gradlew test` 그린.
- `./gradlew bootRun` 시 로그에 "Loaded N rooms" 출력 (InMemoryRoomRepository).

## 다음 phase

본 plan 완료 후 같은 패턴으로 다음 plan을 작성:
- `2026-XX-XX-gameworldservice-refactor-player.md` (Player 도메인 + `PlayerCharacterCreatedEvent` 리스너 이전 + `PlayerCharacterSyncService` 삭제)
- 이후 NPC (리플렉션 제거), Monster (스케줄러 도입), Party (이벤트 책임 이관) 순.
