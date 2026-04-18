# Memory-DB 동기화 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `BatchSyncable` 인터페이스를 도입해 확장 가능한 배치 동기화 구조를 만들고, Party 지속성을 확보한다.

**Architecture:** `PersistenceManager`의 배치 저장 책임을 `BatchSyncable` 구현체들로 분산. 각 엔티티 타입 담당 `SyncService`가 `syncToDb()`를 구현하며 Spring DI로 자동 등록. Party 생성/해산은 `GameWorldService`에서 이벤트를 발행해 `PartySyncService`가 즉시 저장(write-through).

**Tech Stack:** Java 25, Spring Boot 4, Spring Data JPA, JUnit 5, Mockito, Gradle (`./gradlew test`)

---

## 파일 구조

### 신규 생성
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/BatchSyncable.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/PlayerCharacterSyncService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/PartySyncService.java`
- `src/main/java/com/jefflife/mudmk2/gamedata/application/event/PartyCreatedEvent.java`
- `src/main/java/com/jefflife/mudmk2/gamedata/application/event/PartyDisbandedEvent.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/sync/PlayerCharacterSyncServiceTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/sync/PartySyncServiceTest.java`

### 기존 수정
- `src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/PartyRepository.java` — 제네릭 타입 `Long` → `UUID` 수정 (버그)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java` — `addParty()`에 이벤트 발행 추가, `removeParty()` / `loadParties()` / `getActiveParties()` 추가
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java` — `persistGameState()`를 `List<BatchSyncable>` 순회로 교체, 스타트업에 파티 로드 추가

---

## Task 1: PartyRepository 버그 수정

`PartyRepository`가 `JpaRepository<Party, Long>`을 상속하지만 `Party`의 `@Id`는 `UUID`다. 이를 먼저 수정한다.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/PartyRepository.java`

- [ ] **Step 1: PartyRepository 제네릭 타입 수정**

```java
package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PartyRepository extends JpaRepository<Party, UUID> {
}
```

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/PartyRepository.java
git commit -m "fix: PartyRepository 제네릭 타입 Long → UUID 수정"
```

---

## Task 2: BatchSyncable 인터페이스 생성

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/BatchSyncable.java`

- [ ] **Step 1: 인터페이스 작성**

```java
package com.jefflife.mudmk2.gameplay.application.service.sync;

public interface BatchSyncable {
    void syncToDb();
}
```

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/BatchSyncable.java
git commit -m "feat: BatchSyncable 인터페이스 추가"
```

---

## Task 3: PlayerCharacterSyncService 구현

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/PlayerCharacterSyncService.java`
- Create: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/sync/PlayerCharacterSyncServiceTest.java`

- [ ] **Step 1: 테스트 작성**

```java
package com.jefflife.mudmk2.gameplay.application.service.sync;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerCharacterSyncServiceTest {

    @Mock
    private PlayerCharacterRepository playerCharacterRepository;

    @Mock
    private GameWorldService gameWorldService;

    private PlayerCharacterSyncService sut;

    @BeforeEach
    void setUp() {
        sut = new PlayerCharacterSyncService(playerCharacterRepository, gameWorldService);
    }

    @Test
    @DisplayName("syncToDb 호출 시 활성 플레이어 전체를 저장한다")
    void syncToDb_savesAllActivePlayers() {
        PlayerCharacter pc = mock(PlayerCharacter.class);
        when(gameWorldService.getActivePlayers()).thenReturn(List.of(pc));

        sut.syncToDb();

        verify(playerCharacterRepository).saveAll(List.of(pc));
    }
}
```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.sync.PlayerCharacterSyncServiceTest"
```

Expected: FAIL (PlayerCharacterSyncService 없음)

- [ ] **Step 3: 구현 작성**

```java
package com.jefflife.mudmk2.gameplay.application.service.sync;

import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.springframework.stereotype.Component;

@Component
public class PlayerCharacterSyncService implements BatchSyncable {

    private final PlayerCharacterRepository playerCharacterRepository;
    private final GameWorldService gameWorldService;

    public PlayerCharacterSyncService(
            final PlayerCharacterRepository playerCharacterRepository,
            final GameWorldService gameWorldService
    ) {
        this.playerCharacterRepository = playerCharacterRepository;
        this.gameWorldService = gameWorldService;
    }

    @Override
    public void syncToDb() {
        playerCharacterRepository.saveAll(gameWorldService.getActivePlayers());
    }
}
```

- [ ] **Step 4: `GameWorldService.getActivePlayers()` 반환 타입 확인**

`GameWorldService`에 `getActivePlayers()`가 `Collection<PlayerCharacter>`를 반환하는지 확인한다.

```bash
grep -n "getActivePlayers" src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java
```

반환 타입이 `Collection`이면 그대로 사용. 없으면 아래를 `GameWorldService`에 추가:

```java
public Collection<PlayerCharacter> getActivePlayers() {
    return activePlayers.values();
}
```

- [ ] **Step 5: 테스트 실행 — 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.sync.PlayerCharacterSyncServiceTest"
```

Expected: PASS

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/PlayerCharacterSyncService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/sync/PlayerCharacterSyncServiceTest.java
git commit -m "feat: PlayerCharacterSyncService 구현 (BatchSyncable)"
```

---

## Task 4: Party 이벤트 클래스 생성

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/event/PartyCreatedEvent.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/event/PartyDisbandedEvent.java`

- [ ] **Step 1: PartyCreatedEvent 작성**

```java
package com.jefflife.mudmk2.gamedata.application.event;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import org.springframework.context.ApplicationEvent;

public class PartyCreatedEvent extends ApplicationEvent {
    private final Party party;

    public PartyCreatedEvent(Object source, Party party) {
        super(source);
        this.party = party;
    }

    public Party getParty() {
        return party;
    }
}
```

- [ ] **Step 2: PartyDisbandedEvent 작성**

```java
package com.jefflife.mudmk2.gamedata.application.event;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class PartyDisbandedEvent extends ApplicationEvent {
    private final UUID partyId;

    public PartyDisbandedEvent(Object source, UUID partyId) {
        super(source);
        this.partyId = partyId;
    }

    public UUID getPartyId() {
        return partyId;
    }
}
```

- [ ] **Step 3: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/event/PartyCreatedEvent.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/event/PartyDisbandedEvent.java
git commit -m "feat: PartyCreatedEvent, PartyDisbandedEvent 추가"
```

---

## Task 5: GameWorldService에 Party 관련 메서드 추가

`addParty()`에 이벤트 발행을 추가하고, `removeParty()` / `loadParties()` / `getActiveParties()`를 추가한다.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java`

- [ ] **Step 1: `ApplicationEventPublisher` 의존성 주입 추가**

클래스 상단 필드와 생성자를 수정한다. `GameWorldService`는 현재 `@Component`이며 생성자가 없어 필드 주입 방식이다. `ApplicationEventPublisher`를 추가한다.

```java
// 기존 import에 추가
import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import org.springframework.context.ApplicationEventPublisher;

// 클래스 내 필드 추가 (기존 필드들 아래에)
private final ApplicationEventPublisher eventPublisher;

// 생성자 추가 (GameWorldService는 현재 기본 생성자 사용 중이므로, 생성자 추가)
public GameWorldService(final ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
}
```

- [ ] **Step 2: `addParty()` 수정 — 이벤트 발행 추가**

```java
public void addParty(final Party party) {
    parties.put(party.getId(), party);
    eventPublisher.publishEvent(new PartyCreatedEvent(this, party));
}
```

- [ ] **Step 3: `removeParty()` 추가**

```java
public void removeParty(final UUID partyId) {
    Party removed = parties.remove(partyId);
    if (removed != null) {
        eventPublisher.publishEvent(new PartyDisbandedEvent(this, partyId));
    }
}
```

- [ ] **Step 4: `loadParties()` 추가 (스타트업용, 이벤트 발행 없음)**

```java
public void loadParties(final Iterable<Party> parties) {
    parties.forEach(party -> this.parties.put(party.getId(), party));
    logger.info("Loaded {} parties", this.parties.size());
}
```

- [ ] **Step 5: `getActiveParties()` 추가**

```java
public Collection<Party> getActiveParties() {
    return parties.values();
}
```

- [ ] **Step 6: 기존 테스트가 깨지는지 확인 및 수정**

`RecruitCommandServiceTest`가 `GameWorldService`를 mock하므로 직접 영향 없음. 단, `GameWorldService` 생성자 변경으로 다른 테스트에서 직접 생성한다면 수정 필요.

```bash
./gradlew test
```

컴파일 에러가 있으면 해당 테스트에서 `GameWorldService` 생성 부분에 `mock(ApplicationEventPublisher.class)`를 전달하도록 수정.

- [ ] **Step 7: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java
git commit -m "feat: GameWorldService에 Party 이벤트 발행 및 관리 메서드 추가"
```

---

## Task 6: PartySyncService 구현

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/PartySyncService.java`
- Create: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/sync/PartySyncServiceTest.java`

- [ ] **Step 1: 테스트 작성**

```java
package com.jefflife.mudmk2.gameplay.application.service.sync;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import com.jefflife.mudmk2.gamedata.application.service.required.PartyRepository;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartySyncServiceTest {

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private GameWorldService gameWorldService;

    private PartySyncService sut;

    @BeforeEach
    void setUp() {
        sut = new PartySyncService(partyRepository, gameWorldService);
    }

    @Test
    @DisplayName("syncToDb 호출 시 활성 파티 전체를 저장한다")
    void syncToDb_savesAllActiveParties() {
        Party party = mock(Party.class);
        when(gameWorldService.getActiveParties()).thenReturn(List.of(party));

        sut.syncToDb();

        verify(partyRepository).saveAll(List.of(party));
    }

    @Test
    @DisplayName("PartyCreatedEvent 수신 시 파티를 즉시 저장한다")
    void onPartyCreated_savesPartyImmediately() {
        Party party = mock(Party.class);
        PartyCreatedEvent event = new PartyCreatedEvent(this, party);

        sut.onPartyCreated(event);

        verify(partyRepository).save(party);
    }

    @Test
    @DisplayName("PartyDisbandedEvent 수신 시 파티를 즉시 삭제한다")
    void onPartyDisbanded_deletesPartyImmediately() {
        UUID partyId = UUID.randomUUID();
        PartyDisbandedEvent event = new PartyDisbandedEvent(this, partyId);

        sut.onPartyDisbanded(event);

        verify(partyRepository).deleteById(partyId);
    }
}
```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.sync.PartySyncServiceTest"
```

Expected: FAIL (PartySyncService 없음)

- [ ] **Step 3: 구현 작성**

```java
package com.jefflife.mudmk2.gameplay.application.service.sync;

import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import com.jefflife.mudmk2.gamedata.application.service.required.PartyRepository;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PartySyncService implements BatchSyncable {

    private final PartyRepository partyRepository;
    private final GameWorldService gameWorldService;

    public PartySyncService(
            final PartyRepository partyRepository,
            final GameWorldService gameWorldService
    ) {
        this.partyRepository = partyRepository;
        this.gameWorldService = gameWorldService;
    }

    @Override
    @Transactional
    public void syncToDb() {
        partyRepository.saveAll(gameWorldService.getActiveParties());
    }

    @EventListener
    @Transactional
    public void onPartyCreated(PartyCreatedEvent event) {
        partyRepository.save(event.getParty());
    }

    @EventListener
    @Transactional
    public void onPartyDisbanded(PartyDisbandedEvent event) {
        partyRepository.deleteById(event.getPartyId());
    }
}
```

- [ ] **Step 4: 테스트 실행 — 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.sync.PartySyncServiceTest"
```

Expected: PASS

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/PartySyncService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/sync/PartySyncServiceTest.java
git commit -m "feat: PartySyncService 구현 (BatchSyncable + write-through)"
```

---

## Task 7: PersistenceManager 리팩토링

`persistGameState()`를 `List<BatchSyncable>` 순회로 교체하고, 스타트업에 파티 로드를 추가한다.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java`

- [ ] **Step 1: PersistenceManager 전체 교체**

```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterSpawnRoom;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.MonsterTypeRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.NonPlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PartyRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.RoomRepository;
import com.jefflife.mudmk2.gamedata.application.event.PlayerCharacterCreatedEvent;
import com.jefflife.mudmk2.gameplay.application.service.sync.BatchSyncable;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class PersistenceManager {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PersistenceManager.class);
    private static final Random random = new Random();

    private final PlayerCharacterRepository playerCharacterRepository;
    private final NonPlayerCharacterRepository nonPlayerCharacterRepository;
    private final RoomRepository roomRepository;
    private final MonsterTypeRepository monsterTypeRepository;
    private final PartyRepository partyRepository;
    private final GameWorldService gameWorldService;
    private final List<BatchSyncable> batchSyncables;

    public PersistenceManager(
            final PlayerCharacterRepository playerCharacterRepository,
            final NonPlayerCharacterRepository nonPlayerCharacterRepository,
            final RoomRepository roomRepository,
            final MonsterTypeRepository monsterTypeRepository,
            final PartyRepository partyRepository,
            final GameWorldService gameWorldService,
            final List<BatchSyncable> batchSyncables
    ) {
        this.playerCharacterRepository = playerCharacterRepository;
        this.nonPlayerCharacterRepository = nonPlayerCharacterRepository;
        this.roomRepository = roomRepository;
        this.monsterTypeRepository = monsterTypeRepository;
        this.partyRepository = partyRepository;
        this.gameWorldService = gameWorldService;
        this.batchSyncables = batchSyncables;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void loadGameState() {
        Iterable<Room> rooms = roomRepository.findAll();
        gameWorldService.loadRooms(rooms);

        Iterable<PlayerCharacter> players = playerCharacterRepository.findAll();
        gameWorldService.loadPlayers(players);

        Iterable<NonPlayerCharacter> nonPlayerCharacters = nonPlayerCharacterRepository.findAll();
        gameWorldService.loadNpcs(nonPlayerCharacters);

        gameWorldService.loadParties(partyRepository.findAll());

        loadMonsters();

        logger.info("loadGameState finished");
    }

    private void loadMonsters() {
        List<Monster> monsters = new ArrayList<>();
        Iterable<MonsterType> monsterTypes = monsterTypeRepository.findAll();

        for (MonsterType monsterType : monsterTypes) {
            if (monsterType.getMonsterSpawnRooms() != null && monsterType.getMonsterSpawnRooms().getSpawnRooms() != null) {
                for (MonsterSpawnRoom spawnRoom : monsterType.getMonsterSpawnRooms().getSpawnRooms()) {
                    for (int i = 0; i < spawnRoom.getSpawnCount(); i++) {
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

    @EventListener
    public void handlePlayerCharacterCreatedEvent(PlayerCharacterCreatedEvent event) {
        final PlayerCharacter playerCharacter = event.getPlayerCharacter();
        playerCharacter.initializeAssociatedEntities();
        gameWorldService.addPlayer(playerCharacter);
        logger.info("New player character added to game world: {}", playerCharacter.getNickname());
    }

    @Transactional
    @Scheduled(fixedDelay = 60_000)
    public void persistGameState() {
        batchSyncables.forEach(BatchSyncable::syncToDb);
    }

    @Scheduled(fixedDelay = 5_000)
    public void checkMonsterRespawn() {
        int respawnCount = gameWorldService.respawnMonsters();
        if (respawnCount > 0) {
            logger.debug("Respawned {} monsters", respawnCount);
        }
    }
}
```

- [ ] **Step 2: 전체 테스트 실행**

```bash
./gradlew test
```

Expected: BUILD SUCCESSFUL, 모든 기존 테스트 통과

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java
git commit -m "refactor: PersistenceManager를 BatchSyncable 기반으로 리팩토링, Party 로드 추가"
```

---

## Task 8: 최종 통합 확인

- [ ] **Step 1: 전체 테스트 재실행**

```bash
./gradlew test
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: 애플리케이션 실행 후 로그 확인**

```bash
./gradlew bootRun
```

스타트업 로그에서 아래 항목이 모두 출력되어야 한다:
- `Loaded X rooms`
- `Loaded X players`
- `Loaded X NPCs`
- `Loaded X parties`
- `Created and loaded X monsters`
- `loadGameState finished`

- [ ] **Step 3: 최종 커밋 (변경사항 없으면 생략)**

```bash
git log --oneline -8
```
