# GameWorldService 리팩토링 구현 계획 — Phase 4 (NPC 도메인 + 리플렉션 제거)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** NPC 도메인을 `InMemoryNpcRepository`로 완전 이전하고, `GameWorldService.moveNpcToRoom`의 리플렉션 해킹을 도메인 메서드 `BaseCharacter.moveTo(Long)`(NPC API는 `NonPlayerCharacter.moveTo(Long)`)로 대체한다. 이동 오케스트레이션을 `DefaultNpcLocationService`로 분리하고, `PersistenceManager.loadNpcs` 호출과 `PartyMemberMoveListener`의 NPC 호출 2건을 새 포트로 옮긴 뒤 `GameWorldService`에서 NPC 캐시 멤버(`activeNpcs`, `loadNpcs`, `addNpc`, `getNpcById`, `moveNpcToRoom`)를 모두 제거한다. `getNpcsInRoom`/`getNpcByName`은 phase 6에서 처리하므로 본 phase에서는 `ActiveNpcRepository`로 임시 위임. 빌드/테스트 그린 상태로 PR 가능한 자기완결 단위.

**Architecture:** `BaseCharacter`는 위치를 소유하므로 `moveTo(Long)`(null 검증 + 필드 갱신)을 BaseCharacter에 두고, NPC 공개 API는 `NonPlayerCharacter.moveTo(Long)`이 `baseCharacterInfo.moveTo(roomId)`에 위임한다(기존 `damaged`/`enterCombatState` 패턴과 동일). `gameplay/adapter/out/cache/InMemoryNpcRepository`가 `gamedata`의 JPA `NonPlayerCharacterRepository`에 위임해 `ApplicationReadyEvent` 시점에 self-bootstrap. `BatchSyncable.syncToDb()`로 60초마다 메모리→DB 일괄 저장. `DefaultNpcLocationService`(application service 계층)는 `ActiveNpcRepository.findById(npcId)`로 NPC를 찾고 `npc.moveTo(roomId)`를 호출 — 리플렉션 제거. NPC 호출처는 `PartyMemberMoveListener` 1곳만 마이그레이션(기존 `getNpcById`+`moveNpcToRoom` → `ActiveNpcRepository`+`NpcLocationService`). `getNpcsInRoom`/`getNpcByName`은 `GameWorldService`에 임시 위임 형태로 남고 phase 6에서 Query로 이전.

**Tech Stack:** Java 17, Spring Boot 3.x, JPA, JUnit 5, Mockito, AssertJ

**Spec:** `docs/superpowers/specs/2026-05-13-gameworldservice-refactor-design.md` (섹션 4.3, 5.2, 6.1, 8.3 step 4)

**Prior phases:**
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-room.md` (Phase 1+2 완료)
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-player.md` (Phase 3 완료)

---

## 파일 구조

**생성**
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryNpcRepository.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/NpcLocationService.java` (interface)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/DefaultNpcLocationService.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryNpcRepositoryTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/DefaultNpcLocationServiceTest.java`
- `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacterMoveToTest.java`

**수정 (main)**
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacter.java` (`moveTo(Long)` 도메인 메서드 추가)
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/NonPlayerCharacter.java` (`moveTo(Long)` 위임 메서드 추가)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java` (NPC 필드/메서드 제거, `getNpcsInRoom`/`getNpcByName` 임시 위임)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java` (`loadNpcs` 호출 제거 + 미사용된 `NonPlayerCharacterRepository` 의존성 제거)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/event/PartyMemberMoveListener.java` (NPC 호출 2건 마이그레이션)

**수정 (test)**
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java` (FakeGameWorldService 부모 생성자 호출 갱신)

---

## Task 1: `BaseCharacter.moveTo(Long)` + `NonPlayerCharacter.moveTo(Long)` 도메인 메서드 — TDD

리플렉션을 제거할 도메인 메서드를 먼저 도입한다. `BaseCharacter`에 null 검증 + 필드 갱신을 두고, `NonPlayerCharacter`는 기존 위임 패턴(`damaged`, `enterCombatState`)을 따라 `baseCharacterInfo.moveTo`에 위임한다. 기존 `setRoomId`는 `PlayerCharacter.setCurrentRoomId`에서 사용 중이므로 유지(추가만, 변경 없음).

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacter.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/NonPlayerCharacter.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacterMoveToTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`BaseCharacterMoveToTest.java`:
```java
package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaseCharacterMoveToTest {

    @Test
    void moveTo_updatesRoomId() {
        BaseCharacter character = BaseCharacter.builder().roomId(100L).build();

        character.moveTo(200L);

        assertThat(character.getRoomId()).isEqualTo(200L);
    }

    @Test
    void moveTo_throwsWhenRoomIdIsNull() {
        BaseCharacter character = BaseCharacter.builder().roomId(100L).build();

        assertThatThrownBy(() -> character.moveTo(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("roomId");
    }

    @Test
    void nonPlayerCharacter_moveTo_delegatesToBaseCharacter() {
        BaseCharacter base = BaseCharacter.builder().roomId(100L).build();
        NonPlayerCharacter npc = new NonPlayerCharacter(
                java.util.UUID.randomUUID(),
                base,
                PlayableCharacter.builder().build(),
                "persona",
                NPCType.MERCHANT,
                new java.util.HashMap<>(),
                100L,
                false
        );

        npc.moveTo(200L);

        assertThat(npc.getCurrentRoomId()).isEqualTo(200L);
    }
}
```

- [ ] **Step 2: 컴파일 실패 확인**

Run: `./gradlew test --tests BaseCharacterMoveToTest`
Expected: 컴파일 실패 (`BaseCharacter.moveTo` 미존재, `NonPlayerCharacter.moveTo` 미존재)

- [ ] **Step 3: `BaseCharacter.moveTo` 구현**

`BaseCharacter.java`의 기존 `setRoomId` 메서드 바로 아래에 추가:
```java
    public void moveTo(final Long roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId must not be null");
        }
        this.roomId = roomId;
    }
```

- [ ] **Step 4: `NonPlayerCharacter.moveTo` 위임 메서드 추가**

`NonPlayerCharacter.java`의 기존 `getCurrentRoomId()` 아래(또는 `damaged` 근처)에 추가:
```java
    public void moveTo(final Long roomId) {
        baseCharacterInfo.moveTo(roomId);
    }
```

`PlayableCharacter` 가 builder를 사용하는지 확인할 수 없다면 (NPC 생성자에 `PlayableCharacter.builder()` 호출 가능 여부 점검): 테스트에서 `PlayableCharacter.builder().build()`가 동작하지 않으면 위 테스트의 NPC 생성 코드를 다음으로 대체:
```java
NonPlayerCharacter npc = new NonPlayerCharacter(
        java.util.UUID.randomUUID(),
        base,
        null,                       // PlayableCharacter는 본 테스트에서 사용 안 됨
        "persona",
        NPCType.MERCHANT,
        new java.util.HashMap<>(),
        100L,
        false
);
```

(`moveTo`는 `baseCharacterInfo` 한 곳만 건드리므로 `playableCharacterInfo`가 null이어도 안전. 단, 빌더가 가능하면 builder 우선.)

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests BaseCharacterMoveToTest`
Expected: 3개 테스트 모두 PASS

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacter.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/NonPlayerCharacter.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacterMoveToTest.java
git commit -m "feat(domain): add BaseCharacter.moveTo and NonPlayerCharacter.moveTo to replace reflection-based NPC move"
```

---

## Task 2: `InMemoryNpcRepository` — TDD로 구현

JPA `NonPlayerCharacterRepository`에 위임해 부트스트랩하고, `BatchSyncable`로 동기화한다. NPC는 이중 인덱스가 필요 없으므로 단일 `ConcurrentHashMap<UUID, NonPlayerCharacter>`만으로 충분. `ApplicationReadyEvent` 시점에 `initializeAssociatedEntities()` 호출 후 캐시 적재.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryNpcRepository.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryNpcRepositoryTest.java`

- [ ] **Step 1: 테스트 클래스 작성**

`InMemoryNpcRepositoryTest.java`:
```java
package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.NonPlayerCharacterRepository;
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
class InMemoryNpcRepositoryTest {

    @Mock private NonPlayerCharacterRepository jpa;
    private InMemoryNpcRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNpcRepository(jpa);
    }

    @Test
    void bootstrap_loadsAllNpcsFromJpa_andInitializesAssociations() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        NonPlayerCharacter n1 = mock(NonPlayerCharacter.class);
        NonPlayerCharacter n2 = mock(NonPlayerCharacter.class);
        when(n1.getId()).thenReturn(id1);
        when(n2.getId()).thenReturn(id2);
        when(jpa.findAll()).thenReturn(List.of(n1, n2));

        repository.bootstrap();

        verify(n1).initializeAssociatedEntities();
        verify(n2).initializeAssociatedEntities();
        assertThat(repository.findById(id1)).contains(n1);
        assertThat(repository.findById(id2)).contains(n2);
    }

    @Test
    void findById_returnsEmpty_whenNotCached() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findAll_returnsAllCachedNpcs() {
        NonPlayerCharacter n1 = mock(NonPlayerCharacter.class);
        NonPlayerCharacter n2 = mock(NonPlayerCharacter.class);
        when(n1.getId()).thenReturn(UUID.randomUUID());
        when(n2.getId()).thenReturn(UUID.randomUUID());

        repository.add(n1);
        repository.add(n2);

        assertThat(repository.findAll()).containsExactlyInAnyOrder(n1, n2);
    }

    @Test
    void add_putsNpcIntoCache() {
        UUID id = UUID.randomUUID();
        NonPlayerCharacter n = mock(NonPlayerCharacter.class);
        when(n.getId()).thenReturn(id);

        repository.add(n);

        assertThat(repository.findById(id)).contains(n);
    }

    @Test
    void remove_dropsNpcFromCache() {
        UUID id = UUID.randomUUID();
        NonPlayerCharacter n = mock(NonPlayerCharacter.class);
        when(n.getId()).thenReturn(id);
        repository.add(n);

        repository.remove(id);

        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void remove_isNoOp_whenIdNotPresent() {
        repository.remove(UUID.randomUUID());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void syncToDb_savesCachedNpcsViaJpa() {
        NonPlayerCharacter n1 = mock(NonPlayerCharacter.class);
        NonPlayerCharacter n2 = mock(NonPlayerCharacter.class);
        when(n1.getId()).thenReturn(UUID.randomUUID());
        when(n2.getId()).thenReturn(UUID.randomUUID());
        repository.add(n1);
        repository.add(n2);

        repository.syncToDb();

        verify(jpa).saveAll(argThat(npcs -> {
            java.util.List<NonPlayerCharacter> list = new java.util.ArrayList<>();
            npcs.forEach(list::add);
            return list.size() == 2 && list.contains(n1) && list.contains(n2);
        }));
    }
}
```

- [ ] **Step 2: 컴파일 실패 확인**

Run: `./gradlew test --tests InMemoryNpcRepositoryTest`
Expected: 컴파일 실패 (`InMemoryNpcRepository` 미존재)

- [ ] **Step 3: `InMemoryNpcRepository` 구현체 작성**

`InMemoryNpcRepository.java`:
```java
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
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests InMemoryNpcRepositoryTest`
Expected: 7개 테스트 모두 PASS

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryNpcRepository.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryNpcRepositoryTest.java
git commit -m "feat(cache): add InMemoryNpcRepository with self-bootstrap and BatchSyncable"
```

---

## Task 3: `NpcLocationService` + `DefaultNpcLocationService` — TDD

NPC 이동 오케스트레이션을 application service로 분리. 인터페이스 + `Default*` 구현체 쌍 (프로젝트 규약). `ActiveNpcRepository`에서 NPC를 찾아 `npc.moveTo(roomId)` 호출 — 리플렉션 제거의 핵심.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/NpcLocationService.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/DefaultNpcLocationService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/DefaultNpcLocationServiceTest.java`

- [ ] **Step 1: 인터페이스 작성**

`NpcLocationService.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service;

import java.util.UUID;

public interface NpcLocationService {
    /**
     * NPC를 지정한 방으로 이동시킨다.
     * @param npcId NPC ID
     * @param roomId 목적지 방 ID
     * @return NPC가 존재하면 true, 없으면 false
     */
    boolean move(UUID npcId, Long roomId);
}
```

- [ ] **Step 2: 실패 테스트 작성**

`DefaultNpcLocationServiceTest.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultNpcLocationServiceTest {

    @Mock private ActiveNpcRepository npcs;
    private DefaultNpcLocationService service;

    @BeforeEach
    void setUp() {
        service = new DefaultNpcLocationService(npcs);
    }

    @Test
    void move_callsMoveToOnNpc_andReturnsTrue_whenNpcExists() {
        UUID npcId = UUID.randomUUID();
        Long roomId = 200L;
        NonPlayerCharacter npc = mock(NonPlayerCharacter.class);
        when(npcs.findById(npcId)).thenReturn(Optional.of(npc));

        boolean result = service.move(npcId, roomId);

        verify(npc).moveTo(roomId);
        assertThat(result).isTrue();
    }

    @Test
    void move_returnsFalse_whenNpcNotFound() {
        UUID npcId = UUID.randomUUID();
        when(npcs.findById(npcId)).thenReturn(Optional.empty());

        boolean result = service.move(npcId, 200L);

        assertThat(result).isFalse();
    }
}
```

- [ ] **Step 3: 컴파일 실패 확인**

Run: `./gradlew test --tests DefaultNpcLocationServiceTest`
Expected: 컴파일 실패 (`DefaultNpcLocationService` 미존재)

- [ ] **Step 4: `DefaultNpcLocationService` 구현**

`DefaultNpcLocationService.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultNpcLocationService implements NpcLocationService {

    private final ActiveNpcRepository npcs;

    public DefaultNpcLocationService(ActiveNpcRepository npcs) {
        this.npcs = npcs;
    }

    @Override
    public boolean move(UUID npcId, Long roomId) {
        return npcs.findById(npcId)
                .map(npc -> { npc.moveTo(roomId); return true; })
                .orElse(false);
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests DefaultNpcLocationServiceTest`
Expected: 2개 테스트 모두 PASS

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/NpcLocationService.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/DefaultNpcLocationService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/DefaultNpcLocationServiceTest.java
git commit -m "feat(gameplay): add NpcLocationService + DefaultNpcLocationService (replaces reflection-based moveNpcToRoom)"
```

---

## Task 4: `PersistenceManager`에서 NPC 적재 제거

NPC 부트스트랩은 이제 `InMemoryNpcRepository`가 책임진다. `PersistenceManager.loadGameState`에서 NPC 적재 호출과 더는 쓰이지 않게 된 `NonPlayerCharacterRepository` 의존성을 제거.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java`

- [ ] **Step 1: `PersistenceManager.java`에서 NPC 관련 코드 제거**

`loadGameState` 메서드 내부의 다음 두 줄 삭제 (현재 line 50-51):
```java
        Iterable<NonPlayerCharacter> nonPlayerCharacters = nonPlayerCharacterRepository.findAll();
        gameWorldService.loadNpcs(nonPlayerCharacters);
```

다음을 모두 제거:
- 필드: `private final NonPlayerCharacterRepository nonPlayerCharacterRepository;` (현재 line 27)
- 생성자 파라미터: `final NonPlayerCharacterRepository nonPlayerCharacterRepository,`
- 생성자 할당: `this.nonPlayerCharacterRepository = nonPlayerCharacterRepository;`
- 사용하지 않게 된 import:
  - `import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;`
  - `import com.jefflife.mudmk2.gamedata.application.service.required.NonPlayerCharacterRepository;`

변경 후 `loadGameState` 메서드:
```java
@EventListener(ApplicationReadyEvent.class)
@Transactional(readOnly = true)
public void loadGameState() {
    gameWorldService.loadParties(partyRepository.findAll());

    // MonsterType을 기반으로 Monster 생성 및 로드
    loadMonsters();

    logger.info("loadGameState finished");
}
```

변경 후 생성자 (NonPlayerCharacterRepository 제거):
```java
public PersistenceManager(
        final MonsterTypeRepository monsterTypeRepository,
        final GameWorldService gameWorldService,
        final PartyRepository partyRepository,
        final List<BatchSyncable> batchSyncables
) {
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
git commit -m "refactor(gameplay): move NPC cache bootstrap into InMemoryNpcRepository"
```

---

## Task 5: `PartyMemberMoveListener` 호출처 마이그레이션

`gameWorldService.getNpcById(...)`와 `gameWorldService.moveNpcToRoom(...)` 두 호출을 각각 `ActiveNpcRepository.findById(...)`와 `NpcLocationService.move(...)`로 교체. `gameWorldService.getPartyByPlayerId(...)`는 phase 6 영역이므로 `gameWorldService` 의존성은 유지.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/event/PartyMemberMoveListener.java`

- [ ] **Step 1: `PartyMemberMoveListener.java` 변환**

필드/생성자에 `ActiveNpcRepository npcs`, `NpcLocationService npcLocations` 추가 (`ActivePlayerRepository players` 다음 위치):

```java
import com.jefflife.mudmk2.gameplay.application.service.NpcLocationService;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
```

변경 후 필드 선언부:
```java
    private final GameWorldService gameWorldService;
    private final ActivePlayerRepository players;
    private final ActiveNpcRepository npcs;
    private final NpcLocationService npcLocations;
    private final SendMessageToUserPort sendMessageToUserPort;
```

변경 후 생성자:
```java
public PartyMemberMoveListener(
        GameWorldService gameWorldService,
        ActivePlayerRepository players,
        ActiveNpcRepository npcs,
        NpcLocationService npcLocations,
        SendMessageToUserPort sendMessageToUserPort
) {
    this.gameWorldService = gameWorldService;
    this.players = players;
    this.npcs = npcs;
    this.npcLocations = npcLocations;
    this.sendMessageToUserPort = sendMessageToUserPort;
}
```

`onApplicationEvent` 메서드 내부의 NPC 처리 블록(현재 line 63-84) 변경.

기존:
```java
            // NPC인지 확인 (캐릭터 ID가 NPC의 ID와 일치하는지 확인)
            NonPlayerCharacter npc = gameWorldService.getNpcById(memberId);
            if (npc != null) {
                // NPC를 이동시키는 로직
                Long npcRoomId = npc.getCurrentRoomId();
                if (!npcRoomId.equals(toRoomId)) {
                    // NPC 이동 로직 (여기서는 단순히 방 ID 변경만 수행)
                    gameWorldService.moveNpcToRoom(npc.getId(), toRoomId);

                    // 로그 추가
                    logger.info("NPC {} followed leader {} to room {}", npc.getName(), characterId, toRoomId);

                    // 파티 리더에게 메시지 전송 (실제 플레이어 ID 조회 필요)
                    String message = npc.getName() + "이(가) 당신을 따라옵니다.";
                    players.findById(characterId)
                            .map(PlayerCharacter::getUserId)
                            .ifPresentOrElse(
                                    userId -> sendMessageToUserPort.messageToUser(userId, message),
                                    () -> logger.warn("Failed to send follow message to leader: player not found for characterId={}", characterId)
                            );
                }
            }
```

변경:
```java
            // NPC인지 확인하고 리더를 따라가게 함
            npcs.findById(memberId).ifPresent(npc -> {
                if (!npc.getCurrentRoomId().equals(toRoomId)) {
                    npcLocations.move(npc.getId(), toRoomId);
                    logger.info("NPC {} followed leader {} to room {}", npc.getName(), characterId, toRoomId);

                    String message = npc.getName() + "이(가) 당신을 따라옵니다.";
                    players.findById(characterId)
                            .map(PlayerCharacter::getUserId)
                            .ifPresentOrElse(
                                    userId -> sendMessageToUserPort.messageToUser(userId, message),
                                    () -> logger.warn("Failed to send follow message to leader: player not found for characterId={}", characterId)
                            );
                }
            });
```

> 참고: `NonPlayerCharacter` import는 `ifPresent(npc -> ...)` 람다 안에서 추론되므로 명시 import는 불필요. 그러나 기존에 이미 import되어 있다면(`import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;`) 다른 사용처가 없으면 제거 가능. 람다 안에서만 사용되므로 안전하게 제거해도 됨.

- [ ] **Step 2: 빌드/테스트 그린 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL (단위 테스트 없음 — 컴파일 + 전체 회귀 확인)

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/event/PartyMemberMoveListener.java
git commit -m "refactor(event): migrate PartyMemberMoveListener NPC lookup/move to ActiveNpcRepository + NpcLocationService"
```

---

## Task 6: `GameWorldService`에서 NPC 관련 멤버 제거 + 임시 위임

NPC 호출처가 모두 마이그레이션 완료. `GameWorldService`에서 NPC 캐시 멤버를 제거하고, phase 6에서 처리할 `getNpcsInRoom`/`getNpcByName`은 `ActiveNpcRepository`로 위임. 리플렉션 코드 완전 제거. `SpeakCommandServiceTest.FakeGameWorldService`의 부모 생성자 호출도 갱신.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java`

- [ ] **Step 1: 잔여 호출처 부재 재확인**

Run:
```bash
grep -rn "gameWorldService\.\(getNpcById\|addNpc\|moveNpcToRoom\|loadNpcs\)" src/main/ --include="*.java"
```
Expected: 출력 없음

Run:
```bash
grep -n "activeNpcs\|loadNpcs\|addNpc\|getNpcById\|moveNpcToRoom" src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java
```
Expected: `GameWorldService` 내부 정의만 남아 있음 (활성 호출처 없음 확인)

- [ ] **Step 2: `GameWorldService.java`에서 NPC 관련 멤버 모두 삭제**

다음을 모두 제거:
- `private final Map<UUID, NonPlayerCharacter> activeNpcs = new ConcurrentHashMap<>();` (line 36)
- `loadNpcs(Iterable<NonPlayerCharacter>)` 메서드 (line 44-50)
- `addNpc(NonPlayerCharacter)` 메서드 (line 150-153)
- `getNpcById(UUID)` 메서드 (line 183-185)
- `moveNpcToRoom(UUID, Long)` 메서드 + 리플렉션 코드 (line 205-223)

남아야 할 NPC 관련 메서드 (phase 6에서 처리):
- `getNpcsInRoom(Long roomId)` — `RoomOccupancyQuery.npcsIn` 으로 이전 예정
- `getNpcByName(String name)` — `CreatureLookupQuery.findNpcByName` 으로 이전 예정

위 두 메서드는 내부적으로 `activeNpcs.values()`에 접근하므로, 캐시 필드를 지우면 컴파일 실패한다. Phase 3에서 Player에 적용한 패턴과 동일하게 `ActiveNpcRepository`를 임시 주입하고 두 메서드를 위임 형태로 변경.

`GameWorldService.java` 변경 형태:

import 추가:
```java
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
```

`StreamSupport` 는 이미 import 됨 (Player 위임에 사용 중).

필드/생성자 갱신:
```java
    private final ApplicationEventPublisher eventPublisher;
    private final ActivePlayerRepository players;
    private final ActiveNpcRepository npcs;

    public GameWorldService(
            final ApplicationEventPublisher eventPublisher,
            final ActivePlayerRepository players,
            final ActiveNpcRepository npcs
    ) {
        this.eventPublisher = eventPublisher;
        this.players = players;
        this.npcs = npcs;
    }
```

`getNpcsInRoom` 변경:
```java
    // TODO(phase6): RoomOccupancyQuery.npcsIn(roomId)으로 이전 — 위치 기반 조회는 Read Model로 분리
    public List<NonPlayerCharacter> getNpcsInRoom(Long roomId) {
        return StreamSupport.stream(npcs.findAll().spliterator(), false)
                .filter(npc -> npc.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
    }
```

`getNpcByName` 변경:
```java
    // TODO(phase6): CreatureLookupQuery.findNpcByName(name)으로 이전 — 이름 기반 조회는 Read Model로 분리
    public NonPlayerCharacter getNpcByName(String name) {
        return StreamSupport.stream(npcs.findAll().spliterator(), false)
                .filter(npc -> npc.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
```

`ConcurrentHashMap` import는 다른 NPC 캐시 필드 제거 후에도 `activeMonsters`, `parties` 두 필드가 여전히 사용하므로 유지.
`UUID` import는 `isInParty`, `addParty`, `removeParty`, `getPartyByPlayerId` 등이 여전히 사용하므로 유지.

- [ ] **Step 3: `SpeakCommandServiceTest.FakeGameWorldService` 부모 생성자 호출 갱신**

현재 `SpeakCommandServiceTest.java` (line 215-217):
```java
public FakeGameWorldService() {
    super(event -> {}, new FakeActivePlayerRepository());
}
```

변경:
```java
public FakeGameWorldService() {
    super(event -> {}, new FakeActivePlayerRepository(), null);
}
```

`null`을 넘기는 이유: `FakeGameWorldService`가 `getNpcByName`을 오버라이드(line 238-240)하고 있어 부모의 `npcs` 필드를 접근하지 않음. `getNpcsInRoom`도 오버라이드(line 243-245)되어 안전. 부모 캐시는 도달되지 않으므로 `null` 사용 OK.

- [ ] **Step 4: 전체 빌드/테스트 그린 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 잔여 리플렉션 검증**

Run:
```bash
grep -rn "java\.lang\.reflect\|getDeclaredField\|setAccessible" src/main/java/com/jefflife/mudmk2/gameplay --include="*.java"
```
Expected: 출력 없음 (gameplay 모듈에서 리플렉션 완전 제거 확인)

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java
git commit -m "refactor(gameplay): remove NPC cache from GameWorldService — delegated to InMemoryNpcRepository, reflection eliminated"
```

---

## 완료 기준

- 신규 파일 6개:
  - `InMemoryNpcRepository` + 단위 테스트 (7종 그린: bootstrap, findById empty, findAll, add, remove, remove noop, syncToDb)
  - `NpcLocationService` 인터페이스
  - `DefaultNpcLocationService` + 단위 테스트 (2종 그린: NPC 존재 시 `moveTo` 호출 + true, NPC 부재 시 false)
  - `BaseCharacterMoveToTest` (3종 그린: 정상 갱신, null 검증, NonPlayerCharacter 위임)
- 도메인 모델:
  - `BaseCharacter.moveTo(Long)` 추가 (null 검증 + 필드 갱신)
  - `NonPlayerCharacter.moveTo(Long)` 추가 (`baseCharacterInfo.moveTo`에 위임)
- `GameWorldService`에서 NPC 관련 캐시 필드/메서드 없음:
  - 제거: `activeNpcs`, `loadNpcs`, `addNpc`, `getNpcById`, `moveNpcToRoom` (+ 리플렉션 코드)
  - 임시 위임: `getNpcsInRoom`, `getNpcByName` (phase 6에서 Query로 이관 후 완전 제거)
- `gameplay` 모듈 내 리플렉션 사용 0건 (`grep -rn "java\.lang\.reflect" src/main/java/com/jefflife/mudmk2/gameplay` 출력 없음).
- `PersistenceManager.loadGameState`에서 NPC 관련 라인 제거. `NonPlayerCharacterRepository` 의존성 제거.
- `PartyMemberMoveListener`가 `ActiveNpcRepository` + `NpcLocationService`로 NPC 호출 마이그레이션 완료. `gameWorldService`는 `getPartyByPlayerId` 호출 때문에 유지(phase 6 영역).
- `SpeakCommandServiceTest.FakeGameWorldService` 부모 생성자 호출에 `ActiveNpcRepository` 인자 추가 (`null` 사용 — 오버라이드된 메서드만 호출되므로 안전).
- `./gradlew test` 그린 (전체 회귀 확인 — Phase 3 226개 + 본 phase 12개 신규).
- 애플리케이션 부트 시 로그에 "Loaded N NPCs" 출력 (`InMemoryNpcRepository`) 가능.

## 다음 phase

본 plan 완료 후 같은 패턴으로:
- `2026-XX-XX-gameworldservice-refactor-monster.md` (Monster 도메인 + `MonsterRespawnService` + `GameWorldScheduler` 도입 — `PersistenceManager`가 빈 껍데기에 가까워짐)
- 이후 Party (이벤트 책임 이관 + 3개 Query 구현체), 잔여 정리 순.
