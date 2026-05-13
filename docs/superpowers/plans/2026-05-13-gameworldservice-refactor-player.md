# GameWorldService 리팩토링 구현 계획 — Phase 3 (Player 도메인)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Player 도메인을 `InMemoryPlayerRepository`로 완전 이전한다. `PlayerCharacterCreatedEvent` 리스너와 부트스트랩을 어댑터로 흡수하고, `PlayerCharacterSyncService`를 삭제하며, 13개 호출처를 새 포트(`ActivePlayerRepository`)로 옮긴 뒤 `GameWorldService`에서 Player 관련 멤버를 모두 제거한다. 빌드/테스트 그린 상태로 PR 가능한 자기완결 단위.

**Architecture:** `gameplay/adapter/out/cache/InMemoryPlayerRepository`가 `gamedata`의 JPA `PlayerCharacterRepository`에 위임해 `ApplicationReadyEvent` 시점에 self-bootstrap한다. `byId(UUID)` / `byUserId(Long)` 이중 인덱스는 `synchronized` 블록으로 `add`/`removeByUserId` 갱신을 직렬화한다. `BatchSyncable.syncToDb()`로 60초마다 메모리→DB 일괄 저장. `PlayerCharacterCreatedEvent` 리스너도 어댑터에서 처리. 호출처 13개는 `ActivePlayerRepository`를 새 의존으로 받고 `findByUserId()/findById()` + `orElseThrow(PlayerNotFoundException::new)` 패턴으로 마이그레이션.

**Tech Stack:** Java 17, Spring Boot 3.x, JPA, JUnit 5, Mockito, AssertJ

**Spec:** `docs/superpowers/specs/2026-05-13-gameworldservice-refactor-design.md` (섹션 5.2, 5.4, 6.3, 8.3 step 3)

**Prior phase:** `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-room.md` (Phase 1+2 완료)

---

## 파일 구조

**생성**
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryPlayerRepository.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryPlayerRepositoryTest.java`

**삭제**
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/PlayerCharacterSyncService.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/sync/PlayerCharacterSyncServiceTest.java`

**수정 (main)**
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java` (loadPlayers + handlePlayerCharacterCreatedEvent + 사용처 사라진 PlayerCharacterRepository 의존 제거)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java` (Player 관련 필드/메서드 제거)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/RoomInfoService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/MoveCommandService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/AttackCommandService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/InventoryCommandService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/DropCommandService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/TakeCommandService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/DirectionSearchStrategy.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/look/ItemSearchStrategy.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/RecruitCommandService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/event/PartyMemberMoveListener.java`

**수정 (test)**
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceServiceTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/InventoryCommandServiceTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/DropCommandServiceTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/TakeCommandServiceTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/DirectionSearchStrategyTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/look/ItemSearchStrategyTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/RecruitCommandServiceTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/look/LookableTargetFinderTest.java`

---

## Task 1: `InMemoryPlayerRepository` — TDD로 구현

JPA `PlayerCharacterRepository`에 위임해 부트스트랩하고, `BatchSyncable`로 동기화한다. `PlayerCharacterCreatedEvent` 리스너로 새 캐릭터를 캐시에 추가. 이중 인덱스(byId / byUserId)는 `synchronized` 블록으로 보호.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryPlayerRepository.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryPlayerRepositoryTest.java`

- [ ] **Step 1: 테스트 클래스 작성**

```java
package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.event.PlayerCharacterCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryPlayerRepositoryTest {

    @Mock private PlayerCharacterRepository jpa;
    private InMemoryPlayerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPlayerRepository(jpa);
    }

    @Test
    void bootstrap_loadsAllPlayersFromJpa_andInitializesAssociations() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        PlayerCharacter p1 = mock(PlayerCharacter.class);
        PlayerCharacter p2 = mock(PlayerCharacter.class);
        when(p1.getId()).thenReturn(id1);
        when(p1.getUserId()).thenReturn(10L);
        when(p2.getId()).thenReturn(id2);
        when(p2.getUserId()).thenReturn(20L);
        when(jpa.findAll()).thenReturn(List.of(p1, p2));

        repository.bootstrap();

        verify(p1).initializeAssociatedEntities();
        verify(p2).initializeAssociatedEntities();
        assertThat(repository.findById(id1)).contains(p1);
        assertThat(repository.findById(id2)).contains(p2);
        assertThat(repository.findByUserId(10L)).contains(p1);
        assertThat(repository.findByUserId(20L)).contains(p2);
    }

    @Test
    void findById_returnsEmpty_whenNotCached() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findByUserId_returnsEmpty_whenNotCached() {
        assertThat(repository.findByUserId(999L)).isEmpty();
    }

    @Test
    void findAll_returnsAllCachedPlayers() {
        PlayerCharacter p1 = mock(PlayerCharacter.class);
        PlayerCharacter p2 = mock(PlayerCharacter.class);
        when(p1.getId()).thenReturn(UUID.randomUUID());
        when(p1.getUserId()).thenReturn(1L);
        when(p2.getId()).thenReturn(UUID.randomUUID());
        when(p2.getUserId()).thenReturn(2L);

        repository.add(p1);
        repository.add(p2);

        assertThat(repository.findAll()).containsExactlyInAnyOrder(p1, p2);
    }

    @Test
    void add_putsPlayerIntoBothIndexes() {
        UUID id = UUID.randomUUID();
        PlayerCharacter p = mock(PlayerCharacter.class);
        when(p.getId()).thenReturn(id);
        when(p.getUserId()).thenReturn(42L);

        repository.add(p);

        assertThat(repository.findById(id)).contains(p);
        assertThat(repository.findByUserId(42L)).contains(p);
    }

    @Test
    void removeByUserId_dropsPlayerFromBothIndexes() {
        UUID id = UUID.randomUUID();
        PlayerCharacter p = mock(PlayerCharacter.class);
        when(p.getId()).thenReturn(id);
        when(p.getUserId()).thenReturn(42L);
        repository.add(p);

        repository.removeByUserId(42L);

        assertThat(repository.findById(id)).isEmpty();
        assertThat(repository.findByUserId(42L)).isEmpty();
    }

    @Test
    void removeByUserId_isNoOp_whenUserIdNotPresent() {
        repository.removeByUserId(999L);

        assertThat(repository.findByUserId(999L)).isEmpty();
    }

    @Test
    void onCreated_addsPlayerAndInitializesAssociations() {
        UUID id = UUID.randomUUID();
        PlayerCharacter p = mock(PlayerCharacter.class);
        when(p.getId()).thenReturn(id);
        when(p.getUserId()).thenReturn(7L);
        PlayerCharacterCreatedEvent event = new PlayerCharacterCreatedEvent(new Object(), p);

        repository.onCreated(event);

        verify(p).initializeAssociatedEntities();
        assertThat(repository.findById(id)).contains(p);
        assertThat(repository.findByUserId(7L)).contains(p);
    }

    @Test
    void syncToDb_savesCachedPlayersViaJpa() {
        PlayerCharacter p1 = mock(PlayerCharacter.class);
        PlayerCharacter p2 = mock(PlayerCharacter.class);
        when(p1.getId()).thenReturn(UUID.randomUUID());
        when(p1.getUserId()).thenReturn(1L);
        when(p2.getId()).thenReturn(UUID.randomUUID());
        when(p2.getUserId()).thenReturn(2L);
        repository.add(p1);
        repository.add(p2);

        repository.syncToDb();

        verify(jpa).saveAll(argThat(players -> {
            List<PlayerCharacter> list = new java.util.ArrayList<>();
            players.forEach(list::add);
            return list.size() == 2 && list.contains(p1) && list.contains(p2);
        }));
    }
}
```

- [ ] **Step 2: 테스트 실행으로 컴파일 실패 확인**

Run: `./gradlew test --tests InMemoryPlayerRepositoryTest`
Expected: 컴파일 실패 (`InMemoryPlayerRepository` 미존재)

- [ ] **Step 3: `InMemoryPlayerRepository` 구현체 작성**

```java
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
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests InMemoryPlayerRepositoryTest`
Expected: 9개 테스트 모두 PASS

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryPlayerRepository.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryPlayerRepositoryTest.java
git commit -m "feat(cache): add InMemoryPlayerRepository with dual index and event listener"
```

---

## Task 2: `PersistenceManager`에서 Player 적재 제거

Player 부트스트랩과 `PlayerCharacterCreatedEvent` 리스너는 이제 `InMemoryPlayerRepository`가 책임진다. `PersistenceManager`에서 관련 코드와 사용되지 않는 의존성을 제거.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java`

- [ ] **Step 1: `PersistenceManager.java`에서 Player 관련 코드 제거**

다음 두 줄 (`loadGameState` 메서드 내부) 삭제:
```java
        Iterable<PlayerCharacter> players = playerCharacterRepository.findAll();
        gameWorldService.loadPlayers(players);
```

다음 메서드 (`handlePlayerCharacterCreatedEvent`) 전체 삭제:
```java
    @EventListener
    public void handlePlayerCharacterCreatedEvent(PlayerCharacterCreatedEvent event) {
        final PlayerCharacter playerCharacter = event.getPlayerCharacter();
        playerCharacter.initializeAssociatedEntities(); // 연관 객체 초기화
        gameWorldService.addPlayer(playerCharacter);
        logger.info("New player character added to game world: {}", playerCharacter.getNickname());
    }
```

다음을 모두 제거:
- 필드: `private final PlayerCharacterRepository playerCharacterRepository;`
- 생성자 파라미터: `final PlayerCharacterRepository playerCharacterRepository,`
- 생성자 할당: `this.playerCharacterRepository = playerCharacterRepository;`
- 사용하지 않게 된 import:
  - `import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;`
  - `import com.jefflife.mudmk2.gamedata.application.event.PlayerCharacterCreatedEvent;`
  - `import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;`

변경 후 `loadGameState` 메서드:
```java
@EventListener(ApplicationReadyEvent.class)
@Transactional(readOnly = true)
public void loadGameState() {
    Iterable<NonPlayerCharacter> nonPlayerCharacters = nonPlayerCharacterRepository.findAll();
    gameWorldService.loadNpcs(nonPlayerCharacters);

    gameWorldService.loadParties(partyRepository.findAll());

    loadMonsters();

    logger.info("loadGameState finished");
}
```

변경 후 생성자 (PlayerCharacterRepository 제거):
```java
public PersistenceManager(
        final NonPlayerCharacterRepository nonPlayerCharacterRepository,
        final MonsterTypeRepository monsterTypeRepository,
        final GameWorldService gameWorldService,
        final PartyRepository partyRepository,
        final List<BatchSyncable> batchSyncables
) {
    this.nonPlayerCharacterRepository = nonPlayerCharacterRepository;
    this.monsterTypeRepository = monsterTypeRepository;
    this.gameWorldService = gameWorldService;
    this.partyRepository = partyRepository;
    this.batchSyncables = batchSyncables;
}
```

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java
git commit -m "refactor(gameplay): move Player cache bootstrap into InMemoryPlayerRepository"
```

---

## Task 3: `PlayerCharacterSyncService` 삭제

이제 `InMemoryPlayerRepository`가 `BatchSyncable`을 직접 구현하므로 별도 sync 서비스 불필요. 사용처가 없음을 확인하고 파일과 테스트를 삭제.

**Files:**
- Delete: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/PlayerCharacterSyncService.java`
- Delete: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/sync/PlayerCharacterSyncServiceTest.java`

- [ ] **Step 1: 사용처 부재 재확인**

Run: `grep -rn "PlayerCharacterSyncService" src/main/ --include="*.java"`
Expected: `PlayerCharacterSyncService.java` 자체 외에 출력 없음 (스프링 빈으로만 등록되어 BatchSyncable 리스트에 들어가던 형태)

- [ ] **Step 2: 파일 삭제 + 커밋**

```bash
git rm src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/PlayerCharacterSyncService.java
git rm src/test/java/com/jefflife/mudmk2/gameplay/application/service/sync/PlayerCharacterSyncServiceTest.java
./gradlew test
git commit -m "refactor(sync): delete PlayerCharacterSyncService — replaced by InMemoryPlayerRepository.syncToDb"
```

Expected: `./gradlew test` BUILD SUCCESSFUL

---

## Task 4: 호출처 마이그레이션 — 그룹 1 (단위 테스트가 없는 4종)

`MoveCommandService`, `StatusCommandService`, `AttackCommandService`, `RoomInfoService`. 모두 `gameWorldService.getPlayerByUserId(...)` 한 번씩만 호출. 단위 테스트가 없어 mock 갱신 불필요.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/MoveCommandService.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandService.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/AttackCommandService.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/RoomInfoService.java`

### 공통 변환 패턴

변경 전:
```java
PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
```

변경 후:
```java
PlayerCharacter player = players.findByUserId(command.userId())
        .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
```

각 클래스에:
- 생성자에 `ActivePlayerRepository players` 파라미터 추가 (기존 의존성 다음 위치)
- 필드 `private final ActivePlayerRepository players;` 추가 (기존 필드 다음)
- import 추가:
  ```java
  import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
  import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
  ```

- [ ] **Step 1: `MoveCommandService` 변환**

`MoveCommandService.java`:

- 필드 추가: 기존 `private final ActiveRoomRepository rooms;` 다음 줄에
  ```java
  private final ActivePlayerRepository players;
  ```
- 생성자 파라미터 추가: `final ActiveRoomRepository rooms,` 다음 줄에 `final ActivePlayerRepository players,` (필드 순서와 일치)
- 생성자 본문에 `this.players = players;` 추가
- `move` 메서드의 line 41 변경:
  ```java
  PlayerCharacter player = players.findByUserId(command.userId())
          .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
  ```
- import 추가 (위 공통 패턴)

- [ ] **Step 2: `StatusCommandService` 변환**

`StatusCommandService.java`:
- 필드/생성자에 `ActivePlayerRepository players` 추가 (기존 `rooms` 다음 위치)
- `showStatus` 메서드 line 41 변경:
  ```java
  PlayerCharacter player = players.findByUserId(command.userId())
          .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
  ```
- import 추가 (위 공통 패턴)

- [ ] **Step 3: `AttackCommandService` 변환**

`AttackCommandService.java`:
- 필드/생성자에 `ActivePlayerRepository players` 추가 (기존 `gameWorldService` 다음 위치)
- `attack` 메서드 line 34 변경:
  ```java
  PlayerCharacter player = players.findByUserId(command.userId())
          .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
  ```
- `getPlayersInRoom`(line 87) 호출은 phase 6에서 다루므로 **그대로 유지** (`gameWorldService.getPlayersInRoom(...)`)
- import 추가 (위 공통 패턴)

- [ ] **Step 4: `RoomInfoService` 변환**

`RoomInfoService.java`:
- 필드/생성자에 `ActivePlayerRepository players` 추가 (기존 `gameWorldService` 다음 위치, `rooms` 앞)
- `describe` 메서드 line 38 변경:
  ```java
  final PlayerCharacter character = players.findByUserId(userId)
          .orElseThrow(() -> new PlayerNotFoundException(userId));
  ```
- 변경 후 character null 체크(line 40-43) 삭제 (orElseThrow가 대신 처리):
  ```java
  // 삭제:
  // if (character == null) {
  //     logger.info("Player character not found for user {}. Room info will not be displayed.", userId);
  //     return;
  // }
  ```
- 사용되지 않게 된 logger 메시지에 따른 import 정리 불필요 (logger는 여전히 다른 곳에서 사용됨)
- `getNpcsInRoom`(line 49), `getPlayersInRoom`(line 55), `getMonstersInRoom`(line 62) 호출은 phase 6에서 다루므로 그대로 유지
- import 추가 (위 공통 패턴)

- [ ] **Step 5: 전체 테스트 그린 확인 + 커밋**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/MoveCommandService.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandService.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/AttackCommandService.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/RoomInfoService.java
git commit -m "refactor(gameplay): migrate Move/Status/Attack/RoomInfo to ActivePlayerRepository"
```

---

## Task 5: 호출처 마이그레이션 — 그룹 2 (`InventoryCommandService` + 테스트)

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/InventoryCommandService.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/InventoryCommandServiceTest.java`

- [ ] **Step 1: `InventoryCommandService` 변환**

`InventoryCommandService.java`:
- 필드 추가: 기존 `private final GameWorldService gameWorldService;` 다음 줄에 `private final ActivePlayerRepository players;`
- 생성자 파라미터/할당 추가
- `showInventory` 메서드 line 24 변경:
  ```java
  PlayerCharacter player = players.findByUserId(command.userId())
          .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
  ```
- import 추가:
  ```java
  import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
  import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
  ```

> 주: `InventoryCommandService`는 `GameWorldService`를 더 이상 사용하지 않으므로 의존성 제거 가능. 단, 변경 폭을 최소화하기 위해 `gameWorldService` 필드/import는 유지하고 phase 7에서 일괄 정리한다. (다른 phase 4-6에서도 이 패턴 동일)
>
> ❗ 정정: 본 phase에서 `gameWorldService`를 더는 사용하지 않게 되는 클래스 (`InventoryCommandService`, `MoveCommandService`, `StatusCommandService`, `DropCommandService`, `TakeCommandService`, `DirectionSearchStrategy`, `ItemSearchStrategy`)가 다수 발생한다. 이런 클래스에서는 미사용 `gameWorldService` 필드/import도 함께 제거한다 (compileJava warning 회피 + 의존성 명확화). Phase 2의 패턴과 동일.

`InventoryCommandService` 최종 형태:
```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.InventoryCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.provided.InventoryUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;

@Service
public class InventoryCommandService implements InventoryUseCase {
    private final ActivePlayerRepository players;
    private final SendMessageToUserPort sendMessageToUserPort;

    public InventoryCommandService(ActivePlayerRepository players, SendMessageToUserPort sendMessageToUserPort) {
        this.players = players;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void showInventory(InventoryCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
        Inventory inventory = player.getInventory();

        StringBuilder sb = new StringBuilder("[ 소지품 ]\n");
        if (inventory.getItems().isEmpty()) {
            sb.append("소지품이 없습니다.\n");
        } else {
            for (ItemInstance item : inventory.getItems()) {
                sb.append("- ").append(item.getTemplate().getName());
                if (item.getTemplate().isStackable()) {
                    sb.append(" x").append(item.getQuantity());
                }
                sb.append(" (").append(item.getTemplate().getWeight() * item.getQuantity()).append("kg)\n");
            }
        }
        sb.append("무게: ").append(inventory.currentWeight())
          .append("/").append(inventory.getMaxWeightCapacity()).append("kg");

        sendMessageToUserPort.messageToUser(command.userId(), sb.toString());
    }
}
```

- [ ] **Step 2: `InventoryCommandServiceTest` mock 교체**

`InventoryCommandServiceTest.java` 변경:
- `@Mock private GameWorldService gameWorldService;` → `@Mock private ActivePlayerRepository players;`
- `stubPlayerWithInventory` 메서드 변경:
  ```java
  private PlayerCharacter stubPlayerWithInventory(Inventory inventory) {
      PlayerCharacter player = mock(PlayerCharacter.class);
      when(player.getInventory()).thenReturn(inventory);
      when(players.findByUserId(1L)).thenReturn(java.util.Optional.of(player));
      return player;
  }
  ```
- import 갱신:
  - 추가: `import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;`
  - 추가: `import java.util.Optional;` (또는 위 코드처럼 inline FQN 사용)
  - 삭제: `import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;`

- [ ] **Step 3: 테스트 그린 확인 + 커밋**

Run: `./gradlew test --tests InventoryCommandServiceTest`
Expected: 5개 테스트 모두 PASS

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/InventoryCommandService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/InventoryCommandServiceTest.java
git commit -m "refactor(command): migrate InventoryCommandService to ActivePlayerRepository"
```

---

## Task 6: 호출처 마이그레이션 — 그룹 3 (`DropCommandService` + `TakeCommandService` + 테스트)

두 서비스 모두 `gameWorldService`를 `getPlayerByUserId` 한 번만 사용하므로 의존성 완전 제거.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/DropCommandService.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/TakeCommandService.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/DropCommandServiceTest.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/TakeCommandServiceTest.java`

- [ ] **Step 1: `DropCommandService` 변환**

`DropCommandService.java`:
- `GameWorldService` 필드/생성자 파라미터/import 제거
- `ActivePlayerRepository players` 필드/생성자 파라미터/import 추가 (`rooms` 앞에)
- `PlayerNotFoundException` import 추가
- line 36 변경:
  ```java
  PlayerCharacter player = players.findByUserId(command.userId())
          .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
  ```

변경 후 생성자:
```java
public DropCommandService(ActivePlayerRepository players, ActiveRoomRepository rooms, SendMessageToUserPort sendMessageToUserPort) {
    this.players = players;
    this.rooms = rooms;
    this.sendMessageToUserPort = sendMessageToUserPort;
}
```

- [ ] **Step 2: `TakeCommandService` 변환**

`TakeCommandService.java`:
- 동일하게 `GameWorldService` 제거, `ActivePlayerRepository players` 추가
- line 37 변경:
  ```java
  PlayerCharacter player = players.findByUserId(command.userId())
          .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
  ```

- [ ] **Step 3: `DropCommandServiceTest` mock 교체**

`DropCommandServiceTest.java`:
- `@Mock private GameWorldService gameWorldService;` → `@Mock private ActivePlayerRepository players;`
- `stubPlayer` 메서드 변경:
  ```java
  private PlayerCharacter stubPlayer(Inventory inventory) {
      PlayerCharacter player = mock(PlayerCharacter.class);
      lenient().when(player.getCurrentRoomId()).thenReturn(100L);
      when(player.getInventory()).thenReturn(inventory);
      when(players.findByUserId(1L)).thenReturn(Optional.of(player));
      return player;
  }
  ```
- `drop_invalidIndex_sendsIndexErrorMessage` 테스트의 `verifyNoInteractions(gameWorldService);` 호출은 `verifyNoInteractions(players);`로 변경
- import 갱신: `GameWorldService` 삭제, `ActivePlayerRepository` 추가

- [ ] **Step 4: `TakeCommandServiceTest` mock 교체**

`TakeCommandServiceTest.java`:
- `@Mock private GameWorldService gameWorldService;` → `@Mock private ActivePlayerRepository players;`
- `stubPlayer` 메서드 변경:
  ```java
  private PlayerCharacter stubPlayer(Room room) {
      PlayerCharacter player = mock(PlayerCharacter.class);
      when(player.getCurrentRoomId()).thenReturn(100L);
      when(players.findByUserId(1L)).thenReturn(Optional.of(player));
      when(rooms.findById(100L)).thenReturn(Optional.of(room));
      return player;
  }
  ```
- `take_invalidIndex_sendsIndexErrorMessage` 테스트의 `verifyNoInteractions(gameWorldService);`는 `verifyNoInteractions(players);`로 변경
- import 갱신

- [ ] **Step 5: 테스트 그린 확인 + 커밋**

Run: `./gradlew test --tests DropCommandServiceTest --tests TakeCommandServiceTest`
Expected: BUILD SUCCESSFUL

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/DropCommandService.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/TakeCommandService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/DropCommandServiceTest.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/TakeCommandServiceTest.java
git commit -m "refactor(command): migrate Drop/Take to ActivePlayerRepository"
```

---

## Task 7: 호출처 마이그레이션 — 그룹 4 (`DirectionSearchStrategy` + `ItemSearchStrategy` + 테스트)

두 strategy 모두 `gameWorldService`를 `getPlayerByUserId`에만 사용 — 의존성 완전 제거.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/DirectionSearchStrategy.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/look/ItemSearchStrategy.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/DirectionSearchStrategyTest.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/look/ItemSearchStrategyTest.java`

- [ ] **Step 1: `DirectionSearchStrategy` 변환**

`DirectionSearchStrategy.java`:
- `GameWorldService` 필드/생성자/import 제거
- `ActivePlayerRepository players` 필드/생성자/import 추가 (`rooms` 앞)
- `PlayerNotFoundException` import 추가
- line 32 변경:
  ```java
  PlayerCharacter player = players.findByUserId(userId)
          .orElseThrow(() -> new PlayerNotFoundException(userId));
  ```

- [ ] **Step 2: `ItemSearchStrategy` 변환**

`ItemSearchStrategy.java`:
- 동일하게 `GameWorldService` 제거, `ActivePlayerRepository players` 추가
- line 32 변경:
  ```java
  PlayerCharacter player = players.findByUserId(userId)
          .orElseThrow(() -> new PlayerNotFoundException(userId));
  ```

- [ ] **Step 3: `DirectionSearchStrategyTest` mock 교체**

`DirectionSearchStrategyTest.java`:
- `@Mock private GameWorldService gameWorldService;` → `@Mock private ActivePlayerRepository players;`
- `when(gameWorldService.getPlayerByUserId(userId)).thenReturn(player);` (라인 57, 75) →
  ```java
  when(players.findByUserId(userId)).thenReturn(Optional.of(player));
  ```
- import 갱신

- [ ] **Step 4: `ItemSearchStrategyTest` mock 교체**

`ItemSearchStrategyTest.java`:
- `@Mock private GameWorldService gameWorldService;` → `@Mock private ActivePlayerRepository players;`
- 생성자 호출 `new ItemSearchStrategy(gameWorldService, rooms)` (line 44) → `new ItemSearchStrategy(players, rooms)`
- 5곳의 `when(gameWorldService.getPlayerByUserId(userId)).thenReturn(player);` (라인 66, 82, 101, 116, 128) →
  ```java
  when(players.findByUserId(userId)).thenReturn(Optional.of(player));
  ```
- import 갱신 (`Optional`은 이미 import 있는지 확인 — 있으면 그대로, 없으면 추가)

- [ ] **Step 5: 테스트 그린 확인 + 커밋**

Run: `./gradlew test --tests DirectionSearchStrategyTest --tests ItemSearchStrategyTest`
Expected: BUILD SUCCESSFUL

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/DirectionSearchStrategy.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/look/ItemSearchStrategy.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/DirectionSearchStrategyTest.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/look/ItemSearchStrategyTest.java
git commit -m "refactor(command): migrate Direction/ItemSearch strategies to ActivePlayerRepository"
```

---

## Task 8: 호출처 마이그레이션 — `RecruitCommandService` + 테스트

`RecruitCommandService`는 `getPlayerByUserId` 외에도 `getNpcByName`, `isInParty`, `getPartyByPlayerId`, `addParty`를 사용 (phase 6 영역). Player 호출만 마이그레이션.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/RecruitCommandService.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/RecruitCommandServiceTest.java`

- [ ] **Step 1: `RecruitCommandService` 변환**

`RecruitCommandService.java`:
- `ActivePlayerRepository players` 필드/생성자/import 추가 (`gameWorldService` 다음)
- `PlayerNotFoundException` import 추가
- line 34 변경:
  ```java
  PlayerCharacter player = players.findByUserId(recruitCommand.userId())
          .orElseThrow(() -> new PlayerNotFoundException(recruitCommand.userId()));
  ```
- `gameWorldService`는 다른 호출(`getNpcByName`, `isInParty`, `getPartyByPlayerId`, `addParty`)에 여전히 사용되므로 유지

변경 후 생성자:
```java
public RecruitCommandService(
        final GameWorldService gameWorldService,
        final ActivePlayerRepository players,
        final SendMessageToUserPort sendMessageToUserPort
) {
    this.gameWorldService = gameWorldService;
    this.players = players;
    this.sendMessageToUserPort = sendMessageToUserPort;
}
```

- [ ] **Step 2: `RecruitCommandServiceTest` mock 교체**

`RecruitCommandServiceTest.java`:
- `@Mock private ActivePlayerRepository players;` 필드 추가
- `recruitCommandService = new RecruitCommandService(gameWorldService, sendMessageToUserPort);` (line 50) →
  ```java
  recruitCommandService = new RecruitCommandService(gameWorldService, players, sendMessageToUserPort);
  ```
- 8곳의 `lenient().when(gameWorldService.getPlayerByUserId(USER_ID)).thenReturn(player);` (lines 73, 99, 125, 154, 185, 216, 247, 278) →
  ```java
  lenient().when(players.findByUserId(USER_ID)).thenReturn(Optional.of(player));
  ```
- import 추가:
  ```java
  import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
  ```
- `import java.util.Optional;` 가 이미 있는지 확인 (line 19에 있음)

- [ ] **Step 3: 테스트 그린 확인 + 커밋**

Run: `./gradlew test --tests RecruitCommandServiceTest`
Expected: BUILD SUCCESSFUL

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/RecruitCommandService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/RecruitCommandServiceTest.java
git commit -m "refactor(command): migrate RecruitCommandService player lookup to ActivePlayerRepository"
```

---

## Task 9: 호출처 마이그레이션 — `SpeakCommandService` + `Fake` 분리

`SpeakCommandService`는 `getPlayerByUserId` 외에 `getPlayerByName`, `getPlayersInRoom`, `getNpcByName`도 사용 (phase 6). Player 호출만 분리. 테스트는 내부 `FakeGameWorldService`가 `getPlayerByUserId`를 오버라이드하던 구조였으므로 새 `FakeActivePlayerRepository`를 추가하고 SpeakCommandService 생성자도 갱신.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandService.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java`

- [ ] **Step 1: `SpeakCommandService` 변환**

`SpeakCommandService.java`:
- `ActivePlayerRepository players` 필드/생성자/import 추가 (`gameWorldService` 다음)
- `PlayerNotFoundException` import 추가
- `getSpeaker` 메서드 (line 90-92) 변경:
  ```java
  private PlayerCharacter getSpeaker(Long userId) {
      return players.findByUserId(userId)
              .orElseThrow(() -> new PlayerNotFoundException(userId));
  }
  ```
- `gameWorldService`는 다른 호출(`getPlayerByName`, `getNpcByName`, `getPlayersInRoom`)에 여전히 사용되므로 유지

변경 후 생성자:
```java
public SpeakCommandService(
        final GameWorldService gameWorldService,
        final ActivePlayerRepository players,
        final SendMessageToUserPort sendMessageToUserPort
) {
    this.gameWorldService = gameWorldService;
    this.players = players;
    this.sendMessageToUserPort = sendMessageToUserPort;
}
```

- [ ] **Step 2: `SpeakCommandServiceTest` Fake 분리**

`SpeakCommandServiceTest.java`:

(a) 새 inner class `FakeActivePlayerRepository` 추가 (기존 `FakeGameWorldService` 위 또는 아래에):
```java
static class FakeActivePlayerRepository implements ActivePlayerRepository {
    private final Map<Long, PlayerCharacter> byUserId = new HashMap<>();
    private final Map<UUID, PlayerCharacter> byId = new HashMap<>();

    public void addPlayer(PlayerCharacter player) {
        byUserId.put(player.getUserId(), player);
        byId.put(player.getId(), player);
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
        return byUserId.values();
    }

    @Override
    public void add(PlayerCharacter player) {
        addPlayer(player);
    }

    @Override
    public void removeByUserId(Long userId) {
        PlayerCharacter p = byUserId.remove(userId);
        if (p != null) byId.remove(p.getId());
    }
}
```

(b) `FakeGameWorldService`에서 Player 관련 멤버 제거:
```java
// 제거: addPlayer 메서드 (현재 line 212-216)
// 제거: playersByUserId 맵 필드 (line 207)
// 제거: playersByName 맵 필드 (line 210)
// 제거: @Override getPlayerByUserId (line 222-225)
// (참고) getPlayerByName, getNpcByName, getPlayersInRoom, addNpc, playersByRoomId, npcsByName은 유지 — phase 6에서 처리
```

(c) `playersByName` 갱신이 사라졌으므로 `getPlayerByName`이 빈 맵을 반환하지 않게 처리 — Speak 테스트의 "PC를 타겟으로 말하면" 케이스(line 119-149)가 영향받음. 해결 방법: `getPlayerByName`도 `FakeActivePlayerRepository`에서 처리되어야 하지만, 본 phase는 Player 도메인만 다루므로 `playersByName` 필드는 유지하고 `addPlayer`는 `FakeActivePlayerRepository`로 옮겨야 한다.

대안: `FakeGameWorldService`에 `seedPlayerForLookup(PlayerCharacter)` 메서드를 별도로 추가하고 `playersByName`/`playersByRoomId`를 거기에서 채우게 한다.

다음과 같이 `FakeGameWorldService` 최종 형태로 변경 (Player 캐시는 제거, 이름/방 인덱스만 유지):
```java
static class FakeGameWorldService extends GameWorldService {
    public FakeGameWorldService() {
        super(event -> {});
    }

    private final Map<String, NonPlayerCharacter> npcsByName = new HashMap<>();
    private final Map<Long, List<PlayerCharacter>> playersByRoomId = new HashMap<>();
    private final Map<String, PlayerCharacter> playersByName = new HashMap<>();

    public void indexPlayer(PlayerCharacter player) {
        playersByRoomId.computeIfAbsent(player.getCurrentRoomId(), k -> new ArrayList<>()).add(player);
        playersByName.put(player.getName(), player);
    }

    public void addNpc(NonPlayerCharacter npc) {
        npcsByName.put(npc.getName(), npc);
    }

    @Override
    public PlayerCharacter getPlayerByName(String name) {
        return playersByName.get(name);
    }

    @Override
    public NonPlayerCharacter getNpcByName(String name) {
        return npcsByName.get(name);
    }

    @Override
    public List<PlayerCharacter> getPlayersInRoom(Long roomId) {
        return playersByRoomId.getOrDefault(roomId, Collections.emptyList());
    }
}
```

(d) 테스트 클래스 필드 추가:
```java
private FakeActivePlayerRepository players;
```

(e) `@BeforeEach setUp()` 변경:
```java
@BeforeEach
void setUp() {
    gameWorldService = new FakeGameWorldService();
    players = new FakeActivePlayerRepository();
    messageSender = new FakeMessageSender();
    speakCommandService = new SpeakCommandService(gameWorldService, players, messageSender);
}
```

(f) 각 테스트의 `gameWorldService.addPlayer(player);` 호출(8곳: lines 57-58, 91-92, 125-126, 158, 182)을 다음 두 줄로 변경:
```java
players.addPlayer(player);
gameWorldService.indexPlayer(player);
```

(g) import 추가:
```java
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import java.util.Optional;
```

- [ ] **Step 3: 테스트 그린 확인 + 커밋**

Run: `./gradlew test --tests SpeakCommandServiceTest`
Expected: 5개 테스트 모두 PASS

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java
git commit -m "refactor(command): migrate SpeakCommandService speaker lookup to ActivePlayerRepository"
```

---

## Task 10: 호출처 마이그레이션 — `ItemInstanceService` + 테스트

`ItemInstanceService.placeInCharacter`는 `gameWorldService.getPlayerById(characterId)` (line 93) 한 곳에서 사용. `ActivePlayerRepository.findById`로 대체.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceService.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceServiceTest.java`

- [ ] **Step 1: `ItemInstanceService` 변환**

`ItemInstanceService.java`:
- `gameWorldService` 필드/생성자/import 제거 — 이 클래스는 더는 GameWorldService를 사용하지 않음
- `ActivePlayerRepository players` 필드/생성자/import 추가 (`rooms` 다음)
- `placeInCharacter` 메서드의 line 93 변경:
  ```java
  players.findById(characterId).ifPresent(p -> {
      if (p != character) {
          p.getInventory().addItem(instance);
      }
  });
  ```

변경 후 생성자:
```java
public ItemInstanceService(
    ItemTemplateRepository itemTemplateRepository,
    ItemInstanceRepository itemInstanceRepository,
    RoomRepository roomRepository,
    PlayerCharacterRepository playerCharacterRepository,
    ActiveRoomRepository rooms,
    ActivePlayerRepository players
) {
    this.itemTemplateRepository = itemTemplateRepository;
    this.itemInstanceRepository = itemInstanceRepository;
    this.roomRepository = roomRepository;
    this.playerCharacterRepository = playerCharacterRepository;
    this.rooms = rooms;
    this.players = players;
}
```

- [ ] **Step 2: `ItemInstanceServiceTest` mock 교체**

`ItemInstanceServiceTest.java`:
- `@Mock private GameWorldService gameWorldService;` (line 36) → `@Mock private ActivePlayerRepository players;`
- 생성자 호출 갱신:
  ```java
  service = new ItemInstanceService(
      itemTemplateRepository, itemInstanceRepository,
      roomRepository, playerCharacterRepository, rooms, players
  );
  ```
- `when(gameWorldService.getPlayerById(characterId)).thenReturn(Optional.of(character));` (line 136) →
  ```java
  when(players.findById(characterId)).thenReturn(Optional.of(character));
  ```
- import 갱신: `GameWorldService` 삭제, `ActivePlayerRepository` 추가

- [ ] **Step 3: 테스트 그린 확인 + 커밋**

Run: `./gradlew test --tests ItemInstanceServiceTest`
Expected: 6개 테스트 모두 PASS

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceServiceTest.java
git commit -m "refactor(gameplay): migrate ItemInstanceService player lookup to ActivePlayerRepository"
```

---

## Task 11: 호출처 마이그레이션 — `PartyMemberMoveListener`

`getUserIdByCharacterId`는 spec 매핑상 `players.findById().map(PlayerCharacter::getUserId)`로 변환. NPC/Party 호출은 phase 4/6에서 다루므로 `gameWorldService` 의존성 유지.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/event/PartyMemberMoveListener.java`

- [ ] **Step 1: `PartyMemberMoveListener` 변환**

`PartyMemberMoveListener.java`:
- `ActivePlayerRepository players` 필드/생성자/import 추가 (`gameWorldService` 다음)
- line 73 `Long userId = gameWorldService.getUserIdByCharacterId(characterId);` 변경:

기존 try-catch 블록:
```java
try {
    Long userId = gameWorldService.getUserIdByCharacterId(characterId);
    sendMessageToUserPort.messageToUser(userId, message);
} catch (Exception e) {
    logger.warn("Failed to send follow message to leader: {}", e.getMessage());
}
```

변경:
```java
players.findById(characterId)
        .map(PlayerCharacter::getUserId)
        .ifPresentOrElse(
                userId -> sendMessageToUserPort.messageToUser(userId, message),
                () -> logger.warn("Failed to send follow message to leader: player not found for characterId={}", characterId)
        );
```

- import 추가:
  ```java
  import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
  import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
  ```

변경 후 생성자:
```java
public PartyMemberMoveListener(
        GameWorldService gameWorldService,
        ActivePlayerRepository players,
        SendMessageToUserPort sendMessageToUserPort
) {
    this.gameWorldService = gameWorldService;
    this.players = players;
    this.sendMessageToUserPort = sendMessageToUserPort;
}
```

- [ ] **Step 2: 빌드/테스트 그린 확인 + 커밋**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL (단위 테스트 없음 — 컴파일 확인이 충분)

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/event/PartyMemberMoveListener.java
git commit -m "refactor(event): migrate PartyMemberMoveListener userId lookup to ActivePlayerRepository"
```

---

## Task 12: 통합 테스트 마이그레이션 — `LookableTargetFinderTest`

phase 2에서 Room 시드는 이미 `ActiveRoomRepository`로 이전 완료. Player 시드(`addPlayer/removePlayer`)를 `ActivePlayerRepository`로 이전.

**Files:**
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/look/LookableTargetFinderTest.java`

- [ ] **Step 1: Player 시드를 `ActivePlayerRepository`로 이전**

변경 전 (현재):
```java
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

변경 후:
```java
record LookableTargetFinderTest(
        LookableTargetFinder targetFinder,
        ActiveRoomRepository rooms,
        ActivePlayerRepository players
) {
    ...
    @BeforeEach
    void setUp() {
        GameTestFixture.DirectionTestSetup setup = GameTestFixture.createDirectionTestSetup(
                TEST_USER_ID, TEST_ROOM_ID, TEST_NEXT_ROOM_ID
        );
        rooms.add(setup.currentRoom());
        rooms.add(setup.nextRoom());
        players.add(setup.player());
    }

    @AfterEach
    void tearDown() {
        players.removeByUserId(TEST_USER_ID);
        rooms.remove(TEST_ROOM_ID);
        rooms.remove(TEST_NEXT_ROOM_ID);
    }
```

- import 갱신:
  - 추가: `import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;`
  - 삭제: `import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;`

- [ ] **Step 2: 테스트 그린 확인 + 커밋**

Run: `./gradlew test --tests LookableTargetFinderTest`
Expected: BUILD SUCCESSFUL

```bash
git add src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/look/LookableTargetFinderTest.java
git commit -m "test(look): migrate LookableTargetFinderTest player seed to ActivePlayerRepository"
```

---

## Task 13: `GameWorldService`에서 Player 관련 멤버 제거

호출처가 모두 마이그레이션 완료. `GameWorldService`에서 Player 관련 필드와 메서드를 안전하게 제거.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java`

- [ ] **Step 1: 잔여 호출처 부재 재확인**

Run:
```
grep -rn "gameWorldService\.\(getPlayerByUserId\|getPlayerById\|getActivePlayers\|addPlayer\|removePlayer\|getUserIdByCharacterId\|loadPlayers\)" src/main/ --include="*.java"
```
Expected: 출력 없음

Run:
```
grep -n "activePlayers\|activePlayersByUserId\|getPlayerByUserId\|getPlayerById\|getActivePlayers\|addPlayer\|removePlayer\|getUserIdByCharacterId\|loadPlayers" src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java
```
Expected: GameWorldService 내부 정의만 남아 있음

- [ ] **Step 2: `GameWorldService.java`에서 Player 관련 멤버 모두 삭제**

다음을 모두 제거:
- `private final Map<UUID, PlayerCharacter> activePlayers = new ConcurrentHashMap<>();` (line 29)
- `private final Map<Long, PlayerCharacter> activePlayersByUserId = new ConcurrentHashMap<>();` (line 30)
- `loadPlayers(...)` 메서드 (line 35-42)
- `addPlayer(...)` 메서드 (line 44-53)
- `removePlayer(...)` 메서드 (line 55-65)
- `getPlayersInRoom(...)` 메서드는 **유지** (phase 6에서 처리)
- `getPlayerByUserId(...)` 메서드 (line 204-210)
- `getActivePlayers()` 메서드 (line 212-214)
- `getUserIdByCharacterId(...)` 메서드 (line 268-274)
- `getPlayerByName(...)` 메서드는 **유지** (phase 6에서 처리)
- `getPlayerById(...)` 메서드 (line 322-324)

남아야 할 Player 관련 멤버 (phase 6에서 처리):
- `getPlayersInRoom(Long roomId)` — RoomOccupancyQuery로 이전 예정
- `getPlayerByName(String name)` — CreatureLookupQuery로 이전 예정

> ⚠️ 주의: 위 두 메서드는 내부적으로 `activePlayers.values()`에 접근한다. 캐시 필드 자체를 지우면 이 메서드들도 컴파일 실패한다. 본 phase에서는 두 메서드의 호출 패턴을 **임시로** `InMemoryPlayerRepository`로 우회한다.

해결: `GameWorldService`에 `ActivePlayerRepository players` 의존성을 임시 주입하고, 두 메서드를 다음과 같이 변경 (phase 6에서 메서드 자체가 사라질 때 의존성도 제거):

```java
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import java.util.stream.StreamSupport;

@Component
public class GameWorldService {
    ...
    private final ActivePlayerRepository players;

    public GameWorldService(
            final ApplicationEventPublisher eventPublisher,
            final ActivePlayerRepository players
    ) {
        this.eventPublisher = eventPublisher;
        this.players = players;
    }
    ...

    public List<PlayerCharacter> getPlayersInRoom(Long roomId) {
        return StreamSupport.stream(players.findAll().spliterator(), false)
                .filter(pc -> pc.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    public PlayerCharacter getPlayerByName(String name) {
        return StreamSupport.stream(players.findAll().spliterator(), false)
                .filter(playerCharacter -> StringUtils.equalsIgnoreCase(playerCharacter.getName(), name))
                .findFirst()
                .orElse(null);
    }
}
```

(둘 다 `players.findAll()`은 `Iterable`을 반환하므로 `StreamSupport.stream`로 wrap)

미사용된 `import java.util.UUID;`는 다른 메서드(`isInParty`, NPC 메서드 등) 가 여전히 사용하므로 유지.

- [ ] **Step 3: `GameWorldService` 생성자 변경 영향 확인**

`GameWorldService` 생성자에 `ActivePlayerRepository` 인자가 추가되었으므로, 다음을 확인:
- 모든 `new GameWorldService(...)` 호출이 스프링 DI인지 (FakeGameWorldService는 제외):
  - 프로덕션 코드: 스프링 DI 자동 처리 — 영향 없음
  - 테스트: `SpeakCommandServiceTest.FakeGameWorldService`가 `super(event -> {});`로 부모 생성자 호출. 인자 추가 필요.

`SpeakCommandServiceTest.FakeGameWorldService` 생성자 갱신:
```java
public FakeGameWorldService() {
    super(event -> {}, new FakeActivePlayerRepository());
}
```

또는 `null`을 넘기되 `getPlayersInRoom`/`getPlayerByName`을 어차피 오버라이드하므로 안전:
```java
public FakeGameWorldService() {
    super(event -> {}, null);
}
```

후자(`null`) 선택 — 어차피 오버라이드된 메서드만 호출되므로 부모의 `players`는 접근되지 않는다.

- [ ] **Step 4: 전체 빌드/테스트 그린 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java
git commit -m "refactor(gameplay): remove Player cache from GameWorldService — delegated to InMemoryPlayerRepository"
```

---

## 완료 기준

- 신규 파일 2개 (`InMemoryPlayerRepository` + 단위 테스트) 생성.
- 삭제 2개 (`PlayerCharacterSyncService` + 테스트).
- `InMemoryPlayerRepository` 단위 테스트 9종 그린 (bootstrap, findById empty, findByUserId empty, findAll, add, removeByUserId, removeByUserId noop, onCreated, syncToDb).
- `GameWorldService`에 Player 관련 캐시 필드/메서드 없음 (`activePlayers`, `activePlayersByUserId`, `loadPlayers`, `addPlayer`, `removePlayer`, `getPlayerByUserId`, `getActivePlayers`, `getUserIdByCharacterId`, `getPlayerById` 제거).
- `GameWorldService.getPlayersInRoom`, `getPlayerByName`은 임시 위임 형태로 남음 (phase 6에서 Query로 이관 후 완전 제거).
- `PersistenceManager.loadGameState`에서 Player 관련 라인 제거. `PlayerCharacterRepository` 의존성 제거. `handlePlayerCharacterCreatedEvent` 메서드 제거.
- Player 의존 호출처 13개가 `ActivePlayerRepository`를 의존:
  - `RoomInfoService`, `ItemInstanceService`, `InventoryCommandService`, `DropCommandService`, `TakeCommandService`, `StatusCommandService`, `MoveCommandService`, `AttackCommandService`, `DirectionSearchStrategy`, `ItemSearchStrategy`, `RecruitCommandService`, `SpeakCommandService`, `PartyMemberMoveListener`.
- 통합 테스트 `LookableTargetFinderTest`의 Player 시드/정리가 `ActivePlayerRepository`를 통과.
- 단위 테스트 mock 갱신 (`ItemInstanceServiceTest`, `InventoryCommandServiceTest`, `DropCommandServiceTest`, `TakeCommandServiceTest`, `DirectionSearchStrategyTest`, `ItemSearchStrategyTest`, `RecruitCommandServiceTest`, `SpeakCommandServiceTest`).
- `./gradlew test` 그린.
- 애플리케이션 부트 시 로그에 "Loaded N players" 출력 (InMemoryPlayerRepository) 가능.

## 다음 phase

본 plan 완료 후 같은 패턴으로:
- `2026-XX-XX-gameworldservice-refactor-npc.md` (NPC 도메인 + `BaseCharacter.moveTo` 도메인 메서드 + `DefaultNpcLocationService` — 리플렉션 제거)
- 이후 Monster (스케줄러 도입), Party (이벤트 책임 이관) 순.
