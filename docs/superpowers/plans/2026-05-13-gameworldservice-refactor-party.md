# GameWorldService 리팩토링 구현 계획 — Phase 6 (Party 도메인 + 3개 Query 구현체 + GameWorldService/PersistenceManager 삭제)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Party 도메인을 `InMemoryPartyRepository`(self-bootstrap + `BatchSyncable` + `PartyCreatedEvent`/`PartyDisbandedEvent` 핸들러 흡수)로 완전 이전하고, 이벤트 발행 책임을 `DefaultPartyService.create`/`disband`로 분리한다. spec 4.2의 3개 Query 구현체(`DefaultRoomOccupancyQuery`, `DefaultCreatureLookupQuery`, `DefaultPartyMembershipQuery`)를 모두 신설해 위치/이름/멤버십 기반 조회를 Read Model로 분리. Phase 2~5에서 임시 위임 형태로 남겨둔 6개 메서드(`getMonstersInRoom`, `getMonstersByType`, `getNpcsInRoom`, `getNpcByName`, `getPlayersInRoom`, `getPlayerByName`)와 Party 4개 메서드(`isInParty`, `addParty`, `removeParty`, `loadParties`, `getActiveParties`, `getPartyByPlayerId`)의 모든 호출처(`RoomInfoService`, `SpeakCommandService`, `AttackCommandService`, `CombatService`, `PartyMemberMoveListener`, `RecruitCommandService`)를 새 Query/Service로 마이그레이션. 호출처가 모두 정리되면 `PartySyncService` 삭제(InMemoryPartyRepository로 책임 흡수), `PersistenceManager.java` + `GameWorldService.java` 파일 통째 삭제. `FakeGameWorldService`도 함께 제거. Phase 7은 도메인 모델 주석 갱신과 잔여 검증만 남는다.

**Architecture:** `InMemoryPartyRepository`는 Phase 2~5의 표준 패턴(`@EventListener(ApplicationReadyEvent.class)` + `@Transactional(readOnly = true)` self-bootstrap, `ConcurrentHashMap<UUID, Party>` 캐시, `ActivePartyRepository, BatchSyncable` 구현)에 더해 **현재 `PartySyncService`의 두 이벤트 핸들러(`onPartyCreated` → `jpa.save`, `onPartyDisbanded` → `jpa.deleteById`)까지 흡수**한다. 즉시 DB 저장/삭제 보장은 유지되며 `syncToDb()`는 60초마다 `jpa.saveAll(cache.values())`로 위치/멤버 변경사항을 일괄 반영(spec 5.1 표준 패턴). `DefaultPartyService.create(party)`는 `parties.add(party) + eventPublisher.publishEvent(PartyCreatedEvent)`를, `disband(partyId)`는 `parties.findById → remove + PartyDisbandedEvent`만 담당하며 DB 저장은 어댑터 핸들러가 처리. 3개 Query는 모두 `Default*` 구현체 + `@Component` + stateless stream 조합으로, 기존 `ActivePlayerRepository`/`ActiveNpcRepository`/`ActiveMonsterRepository`/`ActivePartyRepository`만 의존(`getPlayerByName` 등은 `StringUtils.equalsIgnoreCase`로 case-insensitive 매칭 유지, `findMonstersByType`은 `monsterTypeId.equals(...)` 필터). 호출처 마이그레이션은 한 task = 한 호출처 = 한 commit으로 분해해 회귀 시 정확히 어느 변경이 원인인지 식별 가능하게 한다. Task 12에서 `PartySyncService.java` + `PartySyncServiceTest.java` 삭제, Task 13에서 `PersistenceManager.java` + `GameWorldService.java` + `SpeakCommandServiceTest.FakeGameWorldService` 삭제로 phase 6 종료. Phase 7은 도메인 모델 주석 갱신 + 잔여 reflection/리플렉션 0건 검증만 남는다.

**Tech Stack:** Java 17, Spring Boot 3.x, JPA, JUnit 5, Mockito, AssertJ

**Spec:** `docs/superpowers/specs/2026-05-13-gameworldservice-refactor-design.md` (섹션 4.2, 4.3, 5.1, 6.3 패턴, 8.3 step 6)

**Prior phases:**
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-room.md` (Phase 1+2 완료)
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-player.md` (Phase 3 완료)
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-npc.md` (Phase 4 완료)
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-monster.md` (Phase 5 완료)

---

## 파일 구조

**생성**
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryPartyRepository.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PartyService.java` (interface)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/DefaultPartyService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultRoomOccupancyQuery.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultCreatureLookupQuery.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultPartyMembershipQuery.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryPartyRepositoryTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/DefaultPartyServiceTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultRoomOccupancyQueryTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultCreatureLookupQueryTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultPartyMembershipQueryTest.java`

**수정 (main)**
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/RoomInfoService.java` (3 호출 → RoomOccupancyQuery)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandService.java` (3 호출 → RoomOccupancyQuery + CreatureLookupQuery)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/AttackCommandService.java` (3 호출 → RoomOccupancyQuery)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatService.java` (1 호출 → PartyMembershipQuery)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/event/PartyMemberMoveListener.java` (1 호출 → PartyMembershipQuery)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/RecruitCommandService.java` (4 호출 → CreatureLookupQuery + PartyMembershipQuery + PartyService)

**삭제 (main)**
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/PartySyncService.java` (책임이 InMemoryPartyRepository로 흡수됨)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java` (InMemoryPartyRepository self-bootstrap이 마지막 책임 흡수)
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java` (모든 멤버가 새 컴포넌트로 이전됨)

**수정 (test)**
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java` (FakeGameWorldService 제거 → 새 Query mock + ActivePlayerRepository fake)
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/RecruitCommandServiceTest.java` (GameWorldService mock → CreatureLookupQuery + PartyMembershipQuery + PartyService mock)

**삭제 (test)**
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/sync/PartySyncServiceTest.java` (PartySyncService와 함께 삭제)

---

## Task 1: `InMemoryPartyRepository` — TDD로 구현 (이벤트 핸들러 흡수 포함)

spec 5.1 표준 패턴 + `PartySyncService.onPartyCreated`/`onPartyDisbanded`의 즉시 DB save/delete 책임 흡수. `PartyRepository`는 JPA `JpaRepository<Party, UUID>`로 `findAll`/`saveAll`/`save`/`deleteById` 모두 제공.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryPartyRepository.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryPartyRepositoryTest.java`

- [ ] **Step 1: 테스트 클래스 작성**

`InMemoryPartyRepositoryTest.java`:
```java
package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import com.jefflife.mudmk2.gamedata.application.service.required.PartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryPartyRepositoryTest {

    @Mock private PartyRepository jpa;
    private InMemoryPartyRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPartyRepository(jpa);
    }

    @Test
    void bootstrap_loadsAllPartiesFromJpa() {
        Party p1 = Party.createParty(UUID.randomUUID());
        Party p2 = Party.createParty(UUID.randomUUID());
        when(jpa.findAll()).thenReturn(List.of(p1, p2));

        repository.bootstrap();

        assertThat(repository.findById(p1.getId())).contains(p1);
        assertThat(repository.findById(p2.getId())).contains(p2);
    }

    @Test
    void findById_returnsEmpty_whenNotCached() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findAll_returnsAllCachedParties() {
        Party p1 = Party.createParty(UUID.randomUUID());
        Party p2 = Party.createParty(UUID.randomUUID());

        repository.add(p1);
        repository.add(p2);

        assertThat(repository.findAll()).containsExactlyInAnyOrder(p1, p2);
    }

    @Test
    void add_putsPartyIntoCache() {
        Party party = Party.createParty(UUID.randomUUID());

        repository.add(party);

        assertThat(repository.findById(party.getId())).contains(party);
    }

    @Test
    void remove_dropsPartyFromCache() {
        Party party = Party.createParty(UUID.randomUUID());
        repository.add(party);

        repository.remove(party.getId());

        assertThat(repository.findById(party.getId())).isEmpty();
    }

    @Test
    void remove_isNoOp_whenIdNotPresent() {
        repository.remove(UUID.randomUUID());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void onPartyCreated_savesPartyImmediately_toJpa() {
        Party party = Party.createParty(UUID.randomUUID());
        PartyCreatedEvent event = new PartyCreatedEvent(this, party);

        repository.onPartyCreated(event);

        verify(jpa).save(party);
    }

    @Test
    void onPartyDisbanded_deletesPartyImmediately_fromJpa() {
        UUID partyId = UUID.randomUUID();
        PartyDisbandedEvent event = new PartyDisbandedEvent(this, partyId);

        repository.onPartyDisbanded(event);

        verify(jpa).deleteById(partyId);
    }

    @Test
    void syncToDb_savesCachedPartiesViaJpa() {
        Party p1 = Party.createParty(UUID.randomUUID());
        Party p2 = Party.createParty(UUID.randomUUID());
        repository.add(p1);
        repository.add(p2);

        repository.syncToDb();

        verify(jpa).saveAll(argThat(parties -> {
            java.util.List<Party> list = new java.util.ArrayList<>();
            parties.forEach(list::add);
            return list.size() == 2 && list.contains(p1) && list.contains(p2);
        }));
    }
}
```

- [ ] **Step 2: 컴파일 실패 확인**

Run: `./gradlew test --tests InMemoryPartyRepositoryTest`
Expected: 컴파일 실패 (`InMemoryPartyRepository` 미존재)

- [ ] **Step 3: `InMemoryPartyRepository` 구현체 작성**

`InMemoryPartyRepository.java`:
```java
package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import com.jefflife.mudmk2.gamedata.application.service.required.PartyRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePartyRepository;
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
public class InMemoryPartyRepository implements ActivePartyRepository, BatchSyncable {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryPartyRepository.class);

    private final PartyRepository jpa;
    private final Map<UUID, Party> cache = new ConcurrentHashMap<>();

    public InMemoryPartyRepository(PartyRepository jpa) {
        this.jpa = jpa;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void bootstrap() {
        jpa.findAll().forEach(party -> cache.put(party.getId(), party));
        logger.info("Loaded {} parties", cache.size());
    }

    @Override
    public Optional<Party> findById(UUID id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public Iterable<Party> findAll() {
        return cache.values();
    }

    @Override
    public void add(Party party) {
        cache.put(party.getId(), party);
    }

    @Override
    public void remove(UUID id) {
        cache.remove(id);
    }

    @EventListener
    @Transactional
    public void onPartyCreated(PartyCreatedEvent event) {
        jpa.save(event.getParty());
    }

    @EventListener
    @Transactional
    public void onPartyDisbanded(PartyDisbandedEvent event) {
        jpa.deleteById(event.getPartyId());
    }

    @Override
    @Transactional
    public void syncToDb() {
        jpa.saveAll(cache.values());
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests InMemoryPartyRepositoryTest`
Expected: 9개 테스트 모두 PASS

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryPartyRepository.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/InMemoryPartyRepositoryTest.java
git commit -m "feat(cache): add InMemoryPartyRepository with self-bootstrap, BatchSyncable, and event handlers"
```

---

## Task 2: `PartyService` + `DefaultPartyService` — TDD

spec 4.3 명세: `create(party)` = `add` + `PartyCreatedEvent` 발행, `disband(partyId)` = `findById → remove` + `PartyDisbandedEvent` 발행. 즉시 DB 저장은 Task 1의 InMemoryPartyRepository 이벤트 핸들러가 처리하므로 본 서비스는 메모리/이벤트만 책임.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PartyService.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/DefaultPartyService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/DefaultPartyServiceTest.java`

- [ ] **Step 1: 인터페이스 작성**

`PartyService.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;

import java.util.UUID;

public interface PartyService {
    /**
     * 새 파티를 활성 캐시에 추가하고 PartyCreatedEvent 를 발행한다.
     */
    void create(Party party);

    /**
     * 파티를 활성 캐시에서 제거하고 PartyDisbandedEvent 를 발행한다. 없으면 무시.
     */
    void disband(UUID partyId);
}
```

- [ ] **Step 2: 실패 테스트 작성**

`DefaultPartyServiceTest.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultPartyServiceTest {

    @Mock private ActivePartyRepository parties;
    @Mock private ApplicationEventPublisher eventPublisher;
    private DefaultPartyService service;

    @BeforeEach
    void setUp() {
        service = new DefaultPartyService(parties, eventPublisher);
    }

    @Test
    void create_addsPartyToRepository_andPublishesPartyCreatedEvent() {
        Party party = Party.createParty(UUID.randomUUID());

        service.create(party);

        verify(parties).add(party);
        ArgumentCaptor<PartyCreatedEvent> captor = ArgumentCaptor.forClass(PartyCreatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getParty()).isEqualTo(party);
    }

    @Test
    void disband_removesPartyFromRepository_andPublishesPartyDisbandedEvent_whenPartyExists() {
        UUID partyId = UUID.randomUUID();
        Party party = Party.createParty(UUID.randomUUID());
        when(parties.findById(partyId)).thenReturn(Optional.of(party));

        service.disband(partyId);

        verify(parties).remove(partyId);
        ArgumentCaptor<PartyDisbandedEvent> captor = ArgumentCaptor.forClass(PartyDisbandedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getPartyId()).isEqualTo(partyId);
    }

    @Test
    void disband_isNoOp_whenPartyNotFound() {
        UUID partyId = UUID.randomUUID();
        when(parties.findById(partyId)).thenReturn(Optional.empty());

        service.disband(partyId);

        verify(parties, never()).remove(any(UUID.class));
        verifyNoInteractions(eventPublisher);
    }
}
```

- [ ] **Step 3: 컴파일 실패 확인**

Run: `./gradlew test --tests DefaultPartyServiceTest`
Expected: 컴파일 실패 (`DefaultPartyService` 미존재)

- [ ] **Step 4: `DefaultPartyService` 구현**

`DefaultPartyService.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePartyRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultPartyService implements PartyService {

    private final ActivePartyRepository parties;
    private final ApplicationEventPublisher eventPublisher;

    public DefaultPartyService(ActivePartyRepository parties, ApplicationEventPublisher eventPublisher) {
        this.parties = parties;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void create(Party party) {
        parties.add(party);
        eventPublisher.publishEvent(new PartyCreatedEvent(this, party));
    }

    @Override
    public void disband(UUID partyId) {
        parties.findById(partyId).ifPresent(party -> {
            parties.remove(partyId);
            eventPublisher.publishEvent(new PartyDisbandedEvent(this, partyId));
        });
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests DefaultPartyServiceTest`
Expected: 3개 테스트 모두 PASS

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/PartyService.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/DefaultPartyService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/DefaultPartyServiceTest.java
git commit -m "feat(gameplay): add PartyService + DefaultPartyService (replaces GameWorldService.addParty/removeParty)"
```

---

## Task 3: `DefaultRoomOccupancyQuery` — TDD

spec 4.2: `playersIn(roomId)`, `npcsIn(roomId)`, `monstersIn(roomId)` — 위치 기반 조회. `GameWorldService.getPlayersInRoom`/`getNpcsInRoom`/`getMonstersInRoom`의 기존 필터 조건(`getCurrentRoomId().equals(roomId)`; monsters는 `isAlive() && getCurrentRoomId().equals(roomId)`)을 유지.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultRoomOccupancyQuery.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultRoomOccupancyQueryTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`DefaultRoomOccupancyQueryTest.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultRoomOccupancyQueryTest {

    @Mock private ActivePlayerRepository players;
    @Mock private ActiveNpcRepository npcs;
    @Mock private ActiveMonsterRepository monsters;
    private DefaultRoomOccupancyQuery query;

    @BeforeEach
    void setUp() {
        query = new DefaultRoomOccupancyQuery(players, npcs, monsters);
    }

    @Test
    void playersIn_returnsOnlyPlayersInThatRoom() {
        PlayerCharacter p1 = mock(PlayerCharacter.class);
        PlayerCharacter p2 = mock(PlayerCharacter.class);
        when(p1.getCurrentRoomId()).thenReturn(100L);
        when(p2.getCurrentRoomId()).thenReturn(200L);
        when(players.findAll()).thenReturn(List.of(p1, p2));

        assertThat(query.playersIn(100L)).containsExactly(p1);
    }

    @Test
    void npcsIn_returnsOnlyNpcsInThatRoom() {
        NonPlayerCharacter n1 = mock(NonPlayerCharacter.class);
        NonPlayerCharacter n2 = mock(NonPlayerCharacter.class);
        when(n1.getCurrentRoomId()).thenReturn(100L);
        when(n2.getCurrentRoomId()).thenReturn(200L);
        when(npcs.findAll()).thenReturn(List.of(n1, n2));

        assertThat(query.npcsIn(100L)).containsExactly(n1);
    }

    @Test
    void monstersIn_returnsOnlyAliveMonstersInThatRoom() {
        Monster aliveInRoom = mock(Monster.class);
        Monster deadInRoom = mock(Monster.class);
        Monster aliveOtherRoom = mock(Monster.class);
        when(aliveInRoom.isAlive()).thenReturn(true);
        when(aliveInRoom.getCurrentRoomId()).thenReturn(100L);
        when(deadInRoom.isAlive()).thenReturn(false);
        when(aliveOtherRoom.isAlive()).thenReturn(true);
        when(aliveOtherRoom.getCurrentRoomId()).thenReturn(200L);
        when(monsters.findAll()).thenReturn(List.of(aliveInRoom, deadInRoom, aliveOtherRoom));

        assertThat(query.monstersIn(100L)).containsExactly(aliveInRoom);
    }

    @Test
    void playersIn_returnsEmpty_whenNoMatch() {
        when(players.findAll()).thenReturn(List.of());

        assertThat(query.playersIn(100L)).isEmpty();
    }
}
```

- [ ] **Step 2: 컴파일 실패 확인**

Run: `./gradlew test --tests DefaultRoomOccupancyQueryTest`
Expected: 컴파일 실패 (`DefaultRoomOccupancyQuery` 미존재)

- [ ] **Step 3: `DefaultRoomOccupancyQuery` 구현**

`DefaultRoomOccupancyQuery.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class DefaultRoomOccupancyQuery implements RoomOccupancyQuery {

    private final ActivePlayerRepository players;
    private final ActiveNpcRepository npcs;
    private final ActiveMonsterRepository monsters;

    public DefaultRoomOccupancyQuery(
            ActivePlayerRepository players,
            ActiveNpcRepository npcs,
            ActiveMonsterRepository monsters
    ) {
        this.players = players;
        this.npcs = npcs;
        this.monsters = monsters;
    }

    @Override
    public List<PlayerCharacter> playersIn(Long roomId) {
        return StreamSupport.stream(players.findAll().spliterator(), false)
                .filter(pc -> pc.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    @Override
    public List<NonPlayerCharacter> npcsIn(Long roomId) {
        return StreamSupport.stream(npcs.findAll().spliterator(), false)
                .filter(npc -> npc.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Monster> monstersIn(Long roomId) {
        return StreamSupport.stream(monsters.findAll().spliterator(), false)
                .filter(monster -> monster.isAlive() && monster.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests DefaultRoomOccupancyQueryTest`
Expected: 4개 테스트 모두 PASS

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultRoomOccupancyQuery.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultRoomOccupancyQueryTest.java
git commit -m "feat(query): add DefaultRoomOccupancyQuery (Read Model for room-based lookups)"
```

---

## Task 4: `DefaultCreatureLookupQuery` — TDD

spec 4.2: `findPlayerByName(name): Optional<PlayerCharacter>`, `findNpcByName(name): Optional<NonPlayerCharacter>`, `findMonstersByType(monsterTypeId): List<Monster>`. `GameWorldService`의 기존 동작(`StringUtils.equalsIgnoreCase` 매칭, NPC는 `equalsIgnoreCase`, monsters는 `equals` typeId)을 유지하되 nullable 반환을 `Optional`로 격상.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultCreatureLookupQuery.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultCreatureLookupQueryTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`DefaultCreatureLookupQueryTest.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCreatureLookupQueryTest {

    @Mock private ActivePlayerRepository players;
    @Mock private ActiveNpcRepository npcs;
    @Mock private ActiveMonsterRepository monsters;
    private DefaultCreatureLookupQuery query;

    @BeforeEach
    void setUp() {
        query = new DefaultCreatureLookupQuery(players, npcs, monsters);
    }

    @Test
    void findPlayerByName_returnsPlayer_caseInsensitive() {
        PlayerCharacter p = mock(PlayerCharacter.class);
        when(p.getName()).thenReturn("Alice");
        when(players.findAll()).thenReturn(List.of(p));

        assertThat(query.findPlayerByName("ALICE")).contains(p);
        assertThat(query.findPlayerByName("alice")).contains(p);
    }

    @Test
    void findPlayerByName_returnsEmpty_whenNoMatch() {
        when(players.findAll()).thenReturn(List.of());

        assertThat(query.findPlayerByName("missing")).isEmpty();
    }

    @Test
    void findNpcByName_returnsNpc_caseInsensitive() {
        NonPlayerCharacter npc = mock(NonPlayerCharacter.class);
        when(npc.getName()).thenReturn("Goblin");
        when(npcs.findAll()).thenReturn(List.of(npc));

        assertThat(query.findNpcByName("GOBLIN")).contains(npc);
    }

    @Test
    void findNpcByName_returnsEmpty_whenNoMatch() {
        when(npcs.findAll()).thenReturn(List.of());

        assertThat(query.findNpcByName("nobody")).isEmpty();
    }

    @Test
    void findMonstersByType_returnsAllMonstersWithMatchingTypeId() {
        Monster m1 = mock(Monster.class);
        Monster m2 = mock(Monster.class);
        Monster otherType = mock(Monster.class);
        when(m1.getMonsterTypeId()).thenReturn(10L);
        when(m2.getMonsterTypeId()).thenReturn(10L);
        when(otherType.getMonsterTypeId()).thenReturn(20L);
        when(monsters.findAll()).thenReturn(List.of(m1, m2, otherType));

        assertThat(query.findMonstersByType(10L)).containsExactlyInAnyOrder(m1, m2);
    }

    @Test
    void findMonstersByType_returnsEmpty_whenNoMatch() {
        when(monsters.findAll()).thenReturn(List.of());

        assertThat(query.findMonstersByType(99L)).isEmpty();
    }
}
```

- [ ] **Step 2: 컴파일 실패 확인**

Run: `./gradlew test --tests DefaultCreatureLookupQueryTest`
Expected: 컴파일 실패 (`DefaultCreatureLookupQuery` 미존재)

- [ ] **Step 3: `DefaultCreatureLookupQuery` 구현**

`DefaultCreatureLookupQuery.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class DefaultCreatureLookupQuery implements CreatureLookupQuery {

    private final ActivePlayerRepository players;
    private final ActiveNpcRepository npcs;
    private final ActiveMonsterRepository monsters;

    public DefaultCreatureLookupQuery(
            ActivePlayerRepository players,
            ActiveNpcRepository npcs,
            ActiveMonsterRepository monsters
    ) {
        this.players = players;
        this.npcs = npcs;
        this.monsters = monsters;
    }

    @Override
    public Optional<PlayerCharacter> findPlayerByName(String name) {
        return StreamSupport.stream(players.findAll().spliterator(), false)
                .filter(pc -> StringUtils.equalsIgnoreCase(pc.getName(), name))
                .findFirst();
    }

    @Override
    public Optional<NonPlayerCharacter> findNpcByName(String name) {
        return StreamSupport.stream(npcs.findAll().spliterator(), false)
                .filter(npc -> StringUtils.equalsIgnoreCase(npc.getName(), name))
                .findFirst();
    }

    @Override
    public List<Monster> findMonstersByType(Long monsterTypeId) {
        return StreamSupport.stream(monsters.findAll().spliterator(), false)
                .filter(monster -> monster.getMonsterTypeId().equals(monsterTypeId))
                .collect(Collectors.toList());
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests DefaultCreatureLookupQueryTest`
Expected: 6개 테스트 모두 PASS

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultCreatureLookupQuery.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultCreatureLookupQueryTest.java
git commit -m "feat(query): add DefaultCreatureLookupQuery (Read Model for name/type-based creature lookups)"
```

---

## Task 5: `DefaultPartyMembershipQuery` — TDD

spec 4.2: `findByMemberId(memberId): Optional<Party>`, `isInParty(memberId): boolean`. `GameWorldService.getPartyByPlayerId`/`isInParty` 의 stream 구조를 그대로 흡수.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultPartyMembershipQuery.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultPartyMembershipQueryTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`DefaultPartyMembershipQueryTest.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePartyRepository;
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
class DefaultPartyMembershipQueryTest {

    @Mock private ActivePartyRepository parties;
    private DefaultPartyMembershipQuery query;

    @BeforeEach
    void setUp() {
        query = new DefaultPartyMembershipQuery(parties);
    }

    @Test
    void findByMemberId_returnsParty_whenMemberBelongs() {
        UUID memberId = UUID.randomUUID();
        Party party = mock(Party.class);
        when(party.contains(memberId)).thenReturn(true);
        when(parties.findAll()).thenReturn(List.of(party));

        assertThat(query.findByMemberId(memberId)).contains(party);
    }

    @Test
    void findByMemberId_returnsEmpty_whenMemberInNoParty() {
        UUID memberId = UUID.randomUUID();
        Party party = mock(Party.class);
        when(party.contains(memberId)).thenReturn(false);
        when(parties.findAll()).thenReturn(List.of(party));

        assertThat(query.findByMemberId(memberId)).isEmpty();
    }

    @Test
    void isInParty_returnsTrue_whenMemberBelongs() {
        UUID memberId = UUID.randomUUID();
        Party party = mock(Party.class);
        when(party.contains(memberId)).thenReturn(true);
        when(parties.findAll()).thenReturn(List.of(party));

        assertThat(query.isInParty(memberId)).isTrue();
    }

    @Test
    void isInParty_returnsFalse_whenMemberInNoParty() {
        UUID memberId = UUID.randomUUID();
        when(parties.findAll()).thenReturn(List.of());

        assertThat(query.isInParty(memberId)).isFalse();
    }
}
```

- [ ] **Step 2: 컴파일 실패 확인**

Run: `./gradlew test --tests DefaultPartyMembershipQueryTest`
Expected: 컴파일 실패 (`DefaultPartyMembershipQuery` 미존재)

- [ ] **Step 3: `DefaultPartyMembershipQuery` 구현**

`DefaultPartyMembershipQuery.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePartyRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Component
public class DefaultPartyMembershipQuery implements PartyMembershipQuery {

    private final ActivePartyRepository parties;

    public DefaultPartyMembershipQuery(ActivePartyRepository parties) {
        this.parties = parties;
    }

    @Override
    public Optional<Party> findByMemberId(UUID memberId) {
        return StreamSupport.stream(parties.findAll().spliterator(), false)
                .filter(party -> party.contains(memberId))
                .findFirst();
    }

    @Override
    public boolean isInParty(UUID memberId) {
        return StreamSupport.stream(parties.findAll().spliterator(), false)
                .anyMatch(party -> party.contains(memberId));
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests DefaultPartyMembershipQueryTest`
Expected: 4개 테스트 모두 PASS

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultPartyMembershipQuery.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/query/DefaultPartyMembershipQueryTest.java
git commit -m "feat(query): add DefaultPartyMembershipQuery (Read Model for party membership lookups)"
```

---

## Task 6: `RoomInfoService` 마이그레이션

`gameWorldService.getNpcsInRoom`/`getPlayersInRoom`/`getMonstersInRoom` 3 호출을 `RoomOccupancyQuery.npcsIn`/`playersIn`/`monstersIn`으로 교체. `GameWorldService` 의존 완전 제거.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/RoomInfoService.java`

- [ ] **Step 1: `RoomInfoService.java` 변환**

import 변경:
- 제거: `import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;` (자동, 사용 안 됨)
- 추가: `import com.jefflife.mudmk2.gameplay.application.service.query.RoomOccupancyQuery;`

변경 후 전체:
```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.exception.RoomNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.provided.RoomDescriber;
import com.jefflife.mudmk2.gameplay.application.service.query.RoomOccupancyQuery;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendRoomInfoMessagePort;
import com.jefflife.mudmk2.gameplay.application.service.model.template.CreatureInfo;
import com.jefflife.mudmk2.gameplay.application.service.model.template.FloorItemInfo;
import com.jefflife.mudmk2.gameplay.application.service.model.template.RoomInfoVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomInfoService implements RoomDescriber {
    private static final Logger logger = LoggerFactory.getLogger(RoomInfoService.class);

    private final RoomOccupancyQuery roomOccupancy;
    private final ActiveRoomRepository rooms;
    private final ActivePlayerRepository players;
    private final SendRoomInfoMessagePort sendRoomInfoMessagePort;

    public RoomInfoService(
            final RoomOccupancyQuery roomOccupancy,
            final ActiveRoomRepository rooms,
            final ActivePlayerRepository players,
            final SendRoomInfoMessagePort sendRoomInfoMessagePort
    ) {
        this.roomOccupancy = roomOccupancy;
        this.rooms = rooms;
        this.players = players;
        this.sendRoomInfoMessagePort = sendRoomInfoMessagePort;
    }

    @Override
    public void describe(Long userId) {
        final PlayerCharacter character = players.findByUserId(userId)
                .orElseThrow(() -> new PlayerNotFoundException(userId));

        final Room currentRoom = rooms.findById(character.getCurrentRoomId())
                .orElseThrow(() -> new RoomNotFoundException(character.getCurrentRoomId()));

        // 현재 방에 있는 NPC 목록 가져오기
        List<CreatureInfo> npcsInRoom = roomOccupancy.npcsIn(currentRoom.getId())
                .stream()
                .map(npc -> new CreatureInfo(npc.getName(), npc.getState()))
                .toList();

        // 현재 방에 있는 다른 플레이어 캐릭터 목록 가져오기
        List<CreatureInfo> otherPlayersInRoom = roomOccupancy.playersIn(currentRoom.getId())
                .stream()
                .filter(pc -> !pc.getId().equals(character.getId())) // 자신 제외
                .map(pc -> new CreatureInfo(pc.getNickname(), pc.getState()))
                .toList();

        // 현재 방에 있는 몬스터 목록 가져오기
        List<CreatureInfo> monstersInRoom = roomOccupancy.monstersIn(currentRoom.getId())
                .stream()
                .map(monster -> new CreatureInfo(
                        monster.getName() + " (레벨 " + monster.getLevel() + ")",
                        monster.getState()))
                .toList();

        List<FloorItemInfo> floorItems = currentRoom.getFloorItems().stream()
                .map(item -> new FloorItemInfo(
                        item.getTemplate().getName(),
                        item.getQuantity(),
                        item.getTemplate().isStackable()))
                .toList();

        sendRoomInfoMessagePort.sendMessage(new RoomInfoVariables(
                userId,
                currentRoom.getName(),
                currentRoom.getDescription(),
                currentRoom.getExitString(),
                npcsInRoom,
                otherPlayersInRoom,
                monstersInRoom,
                floorItems
        ));
        logger.info("Sent room info to user {}", userId);
    }
}
```

- [ ] **Step 2: 빌드/회귀 그린 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/RoomInfoService.java
git commit -m "refactor(gameplay): migrate RoomInfoService creature lookups to RoomOccupancyQuery"
```

---

## Task 7: `SpeakCommandService` + 테스트 마이그레이션

`getPlayersInRoom` → `RoomOccupancyQuery.playersIn`, `getPlayerByName`/`getNpcByName` → `CreatureLookupQuery.findPlayerByName`/`findNpcByName`(Optional). `SpeakCommandServiceTest`의 `FakeGameWorldService`는 새 Query를 stub하는 fake로 교체.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandService.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java`

- [ ] **Step 1: `SpeakCommandService.java` 변환**

`GameWorldService` 의존을 `RoomOccupancyQuery` + `CreatureLookupQuery`로 교체. `findTarget`은 Optional 반환을 풀어 기존 동작 유지.

변경 후 전체:
```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Statable;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.SpeakCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.provided.SpeakUseCase;
import com.jefflife.mudmk2.gameplay.application.service.query.CreatureLookupQuery;
import com.jefflife.mudmk2.gameplay.application.service.query.RoomOccupancyQuery;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for the SpeakUseCase.
 * Handles speak commands in the game.
 */
@Service
public class SpeakCommandService implements SpeakUseCase {
    private static final Logger logger = LoggerFactory.getLogger(SpeakCommandService.class);

    private final RoomOccupancyQuery roomOccupancy;
    private final CreatureLookupQuery creatureLookup;
    private final ActivePlayerRepository players;
    private final SendMessageToUserPort sendMessageToUserPort;

    public SpeakCommandService(
            final RoomOccupancyQuery roomOccupancy,
            final CreatureLookupQuery creatureLookup,
            final ActivePlayerRepository players,
            final SendMessageToUserPort sendMessageToUserPort
    ) {
        this.roomOccupancy = roomOccupancy;
        this.creatureLookup = creatureLookup;
        this.players = players;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Async("taskExecutor")
    @Override
    public void speak(final SpeakCommand command) {
        PlayerCharacter speaker = getSpeaker(command.userId());
        Long roomId = speaker.getCurrentRoomId();

        Statable speakTarget = null;
        if (command.hasTarget()) {
            speakTarget = findTarget(command.target(), roomId);
            if (speakTarget == null) {
                sendMessageToUser(command.userId(), String.format("%s은(는) 이 방안에 없습니다.", command.target()));
                return;
            }
        }

        List<PlayerCharacter> playersInRoom = roomOccupancy.playersIn(roomId);
        for (PlayerCharacter playerInRoom : playersInRoom) {
            String message = createMessageForPlayer(command, playerInRoom, speaker, speakTarget);
            sendMessageToUser(playerInRoom.getUserId(), message);
        }
    }

    private static String createMessageForPlayer(SpeakCommand command, PlayerCharacter playerInRoom, PlayerCharacter speaker, Statable speakTarget) {
        if (playerInRoom.equals(speaker)) {
            String message = String.format("당신은 \"%s\"라고 말합니다.", command.message());
            if (speakTarget != null) {
                message = String.format("당신은 %s에게 \"%s\"라고 말합니다.", speakTarget.getName(), command.message());
            }
            return message;
        } else if (playerInRoom.equals(speakTarget)) {
            return String.format("%s이(가) 당신에게 \"%s\"라고 말합니다.", speaker.getName(), command.message());
        } else {
            String message = String.format("%s이(가) \"%s\"라고 말합니다", speaker.getName(), command.message());
            if (speakTarget != null) {
                message = String.format("%s이(가) %s에게 \"%s\"라고 말합니다", speaker.getName(), speakTarget.getName(), command.message());
            }
            return message;
        }
    }

    private Statable findTarget(String target, Long roomId) {
        Optional<PlayerCharacter> targetPlayer = creatureLookup.findPlayerByName(target);
        if (targetPlayer.isPresent() && isInSameRoom(targetPlayer.get().getCurrentRoomId(), roomId)) {
            return targetPlayer.get();
        }

        Optional<NonPlayerCharacter> targetNpc = creatureLookup.findNpcByName(target);
        if (targetNpc.isPresent() && isInSameRoom(targetNpc.get().getCurrentRoomId(), roomId)) {
            return targetNpc.get();
        }

        return null;
    }

    private PlayerCharacter getSpeaker(Long userId) {
        return players.findByUserId(userId)
                .orElseThrow(() -> new PlayerNotFoundException(userId));
    }

    private boolean isInSameRoom(Long targetRoomId, Long speakerRoomId) {
        return targetRoomId.equals(speakerRoomId);
    }

    private void sendMessageToUser(Long userId, String message) {
        sendMessageToUserPort.messageToUser(userId, message);
    }
}
```

- [ ] **Step 2: `SpeakCommandServiceTest.java` 변환 — FakeGameWorldService 제거**

`SpeakCommandServiceTest.java`의 다음을 변경:
- 필드 `private FakeGameWorldService gameWorldService;` 제거
- 새 필드 추가:
  ```java
  private FakeRoomOccupancyQuery roomOccupancy;
  private FakeCreatureLookupQuery creatureLookup;
  ```
- `setUp()` (현재 `gameWorldService = new FakeGameWorldService();`) → 새 fake 두 개 인스턴스화 + `speakCommandService = new SpeakCommandService(roomOccupancy, creatureLookup, ..., messageSender)`
- 테스트 본문에서:
  - `gameWorldService.indexPlayer(player)` → `roomOccupancy.indexPlayer(player); creatureLookup.indexPlayer(player);`
  - `gameWorldService.addNpc(npc)` → `creatureLookup.addNpc(npc)`
- `FakeGameWorldService` 내부 클래스 통째로 제거
- 새 내부 클래스 두 개 추가 (다음 `static class`로):

```java
static class FakeRoomOccupancyQuery implements com.jefflife.mudmk2.gameplay.application.service.query.RoomOccupancyQuery {
    private final Map<Long, List<PlayerCharacter>> playersByRoomId = new HashMap<>();

    public void indexPlayer(PlayerCharacter player) {
        playersByRoomId.computeIfAbsent(player.getCurrentRoomId(), k -> new ArrayList<>()).add(player);
    }

    @Override
    public List<PlayerCharacter> playersIn(Long roomId) {
        return playersByRoomId.getOrDefault(roomId, Collections.emptyList());
    }

    @Override
    public List<NonPlayerCharacter> npcsIn(Long roomId) {
        return Collections.emptyList();
    }

    @Override
    public List<com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster> monstersIn(Long roomId) {
        return Collections.emptyList();
    }
}

static class FakeCreatureLookupQuery implements com.jefflife.mudmk2.gameplay.application.service.query.CreatureLookupQuery {
    private final Map<String, PlayerCharacter> playersByName = new HashMap<>();
    private final Map<String, NonPlayerCharacter> npcsByName = new HashMap<>();

    public void indexPlayer(PlayerCharacter player) {
        playersByName.put(player.getName(), player);
    }

    public void addNpc(NonPlayerCharacter npc) {
        npcsByName.put(npc.getName(), npc);
    }

    @Override
    public Optional<PlayerCharacter> findPlayerByName(String name) {
        return Optional.ofNullable(playersByName.get(name));
    }

    @Override
    public Optional<NonPlayerCharacter> findNpcByName(String name) {
        return Optional.ofNullable(npcsByName.get(name));
    }

    @Override
    public List<com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster> findMonstersByType(Long monsterTypeId) {
        return Collections.emptyList();
    }
}
```

(`FakeActivePlayerRepository`와 `FakeMessageSender` 내부 클래스는 그대로 유지)

이 패치를 작성할 때 현재 `SpeakCommandServiceTest.java`의 전체 구조를 한 번 읽어 정확한 위치/들여쓰기를 맞추도록 한다. `import` 누락이 없는지(특히 `Optional` 사용 추가)에도 주의.

- [ ] **Step 3: 빌드/회귀 그린 확인**

Run: `./gradlew test --tests SpeakCommandServiceTest`
Expected: 기존 SpeakCommandServiceTest의 모든 케이스 PASS

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java
git commit -m "refactor(gameplay): migrate SpeakCommandService to RoomOccupancyQuery + CreatureLookupQuery"
```

---

## Task 8: `AttackCommandService` 마이그레이션

`getMonstersInRoom`/`getNpcsInRoom`/`getPlayersInRoom` 3 호출을 `RoomOccupancyQuery.monstersIn`/`npcsIn`/`playersIn`으로 교체. `GameWorldService` 의존 완전 제거.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/AttackCommandService.java`

- [ ] **Step 1: `AttackCommandService.java` 변환**

import 변경:
- 제거: `import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;`
- 추가: `import com.jefflife.mudmk2.gameplay.application.service.query.RoomOccupancyQuery;`

`GameWorldService gameWorldService` 필드/생성자/본문을 `RoomOccupancyQuery roomOccupancy`로 교체.

변경된 메서드 본문:
```java
    private Statable getMonsterInRoom(final AttackCommand command, final Long playerRoomId) {
        return roomOccupancy.monstersIn(playerRoomId)
                .stream()
                .filter(monster -> monster.getName().startsWith(command.target()))
                .min(sortNormalStateFirst())
                .orElse(null);
    }

    private Statable getNpcInRoom(final AttackCommand command, final Long playerRoomId) {
        return roomOccupancy.npcsIn(playerRoomId)
                .stream()
                .filter(npc -> npc.getName().startsWith(command.target()))
                .filter(Statable::isAttackableTarget)
                .min(sortNormalStateFirst())
                .orElse(null);
    }
```

`sendAttackNoticeOtherPlayersInRoom`:
```java
    private void sendAttackNoticeOtherPlayersInRoom(Long playerRoomId, PlayerCharacter player, Statable target) {
        List<PlayerCharacter> playersInRoom = roomOccupancy.playersIn(playerRoomId);
        String message = String.format("%s이(가) %s을(를) 공격합니다!", player.getName(), target.getName());
        for (PlayerCharacter playerInRoom : playersInRoom) {
            sendMessageToUserPort.messageToUser(playerInRoom.getUserId(), message);
        }
    }
```

생성자:
```java
    public AttackCommandService(
            final RoomOccupancyQuery roomOccupancy,
            final ActivePlayerRepository players,
            final CombatService combatService,
            final SendMessageToUserPort sendMessageToUserPort
    ) {
        this.roomOccupancy = roomOccupancy;
        this.players = players;
        this.combatService = combatService;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }
```

필드 선언부:
```java
    private final RoomOccupancyQuery roomOccupancy;
    private final ActivePlayerRepository players;
    private final CombatService combatService;
    private final SendMessageToUserPort sendMessageToUserPort;
```

- [ ] **Step 2: 빌드/회귀 그린 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL (AttackCommandService 단위 테스트 없음 — 컴파일 + 전체 회귀 확인)

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/AttackCommandService.java
git commit -m "refactor(gameplay): migrate AttackCommandService creature lookups to RoomOccupancyQuery"
```

---

## Task 9: `CombatService` 마이그레이션

`gameWorldService.getPartyByPlayerId(...)` 1 호출을 `PartyMembershipQuery.findByMemberId(...)`로 교체. `GameWorldService` 의존 완전 제거(`rooms`, `sendMessageToUserPort`, `narrativeFormatter`는 유지).

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatService.java`

- [ ] **Step 1: `CombatService.java` 변환**

import 변경:
- 제거: `import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;` (자동)
- 추가: `import com.jefflife.mudmk2.gameplay.application.service.query.PartyMembershipQuery;`

`GameWorldService gameWorldService` 필드/생성자/본문을 `PartyMembershipQuery partyMembership`으로 교체.

`startCombat` 메서드 line 58 변경:
```java
        Party party = partyMembership.findByMemberId(attacker.getId()).orElse(null);
```

생성자:
```java
    public CombatService(PartyMembershipQuery partyMembership,
                         ActiveRoomRepository rooms,
                         SendMessageToUserPort sendMessageToUserPort,
                         CombatNarrativeFormatter narrativeFormatter) {
        this.partyMembership = partyMembership;
        this.rooms = rooms;
        this.sendMessageToUserPort = sendMessageToUserPort;
        this.narrativeFormatter = narrativeFormatter;
    }
```

필드 선언부:
```java
    private final PartyMembershipQuery partyMembership;
    private final ActiveRoomRepository rooms;
    private final SendMessageToUserPort sendMessageToUserPort;
    private final CombatNarrativeFormatter narrativeFormatter;
```

- [ ] **Step 2: 빌드/회귀 그린 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatService.java
git commit -m "refactor(gameplay): migrate CombatService party lookup to PartyMembershipQuery"
```

---

## Task 10: `PartyMemberMoveListener` 마이그레이션

`gameWorldService.getPartyByPlayerId(...)` 1 호출을 `PartyMembershipQuery.findByMemberId(...)`로 교체. NPC 호출은 Phase 4에서 이미 마이그레이션 완료. `GameWorldService` 의존 완전 제거.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/event/PartyMemberMoveListener.java`

- [ ] **Step 1: `PartyMemberMoveListener.java` 변환**

import 변경:
- 제거: `import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;`
- 추가: `import com.jefflife.mudmk2.gameplay.application.service.query.PartyMembershipQuery;`

`GameWorldService gameWorldService` 필드/생성자/본문을 `PartyMembershipQuery partyMembership`으로 교체.

`onApplicationEvent` line 54 변경:
```java
        Optional<Party> partyOpt = partyMembership.findByMemberId(characterId);
```

생성자:
```java
    public PartyMemberMoveListener(
            PartyMembershipQuery partyMembership,
            ActivePlayerRepository players,
            ActiveNpcRepository npcs,
            NpcLocationService npcLocations,
            SendMessageToUserPort sendMessageToUserPort
    ) {
        this.partyMembership = partyMembership;
        this.players = players;
        this.npcs = npcs;
        this.npcLocations = npcLocations;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }
```

필드 선언부:
```java
    private final PartyMembershipQuery partyMembership;
    private final ActivePlayerRepository players;
    private final ActiveNpcRepository npcs;
    private final NpcLocationService npcLocations;
    private final SendMessageToUserPort sendMessageToUserPort;
```

- [ ] **Step 2: 빌드/회귀 그린 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/event/PartyMemberMoveListener.java
git commit -m "refactor(event): migrate PartyMemberMoveListener party lookup to PartyMembershipQuery"
```

---

## Task 11: `RecruitCommandService` + 테스트 마이그레이션

4 호출 모두 마이그레이션:
- `getNpcByName(name)` → `CreatureLookupQuery.findNpcByName(name)` (Optional)
- `isInParty(npcId)` → `PartyMembershipQuery.isInParty(npcId)`
- `getPartyByPlayerId(playerId)` → `PartyMembershipQuery.findByMemberId(playerId)`
- `addParty(newParty)` → `PartyService.create(newParty)`

`RecruitCommandServiceTest`의 `@Mock GameWorldService` 4종 stub을 새 3개 mock으로 분산.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/RecruitCommandService.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/RecruitCommandServiceTest.java`

- [ ] **Step 1: `RecruitCommandService.java` 변환**

import 변경:
- 제거: `import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;`
- 추가:
  ```java
  import com.jefflife.mudmk2.gameplay.application.service.PartyService;
  import com.jefflife.mudmk2.gameplay.application.service.query.CreatureLookupQuery;
  import com.jefflife.mudmk2.gameplay.application.service.query.PartyMembershipQuery;
  ```

생성자/필드 교체:
```java
    private final CreatureLookupQuery creatureLookup;
    private final PartyMembershipQuery partyMembership;
    private final PartyService partyService;
    private final ActivePlayerRepository players;
    private final SendMessageToUserPort sendMessageToUserPort;

    public RecruitCommandService(
            final CreatureLookupQuery creatureLookup,
            final PartyMembershipQuery partyMembership,
            final PartyService partyService,
            final ActivePlayerRepository players,
            final SendMessageToUserPort sendMessageToUserPort
    ) {
        this.creatureLookup = creatureLookup;
        this.partyMembership = partyMembership;
        this.partyService = partyService;
        this.players = players;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }
```

`recruit` 메서드 본문 변경 — `getNpcByName(name)` 결과를 Optional로 받고 기존 null 체크 대신 `isEmpty()` 사용:
```java
    @Override
    public void recruit(final RecruitCommand recruitCommand) {
        // 1. 플레이어 정보 가져오기
        PlayerCharacter player = players.findByUserId(recruitCommand.userId())
                .orElseThrow(() -> new PlayerNotFoundException(recruitCommand.userId()));
        UUID playerId = player.getId();
        Long playerRoomId = player.getCurrentRoomId();

        // 2. 초대할 NPC 찾기
        Optional<NonPlayerCharacter> targetNpcOpt = creatureLookup.findNpcByName(recruitCommand.npcName());
        if (targetNpcOpt.isEmpty()) {
            sendMessageToUserPort.messageToUser(
                    recruitCommand.userId(),
                    "대상을 찾을 수 없습니다: " + recruitCommand.npcName()
            );
            return;
        }
        NonPlayerCharacter targetNpc = targetNpcOpt.get();

        // 3. NPC가 같은 방에 있는지 확인
        if (!targetNpc.getCurrentRoomId().equals(playerRoomId)) {
            sendMessageToUserPort.messageToUser(
                    recruitCommand.userId(),
                    targetNpc.getName() + "는 당신과 같은 방에 있지 않습니다."
            );
            return;
        }

        // 4. NPC가 이미 다른 파티에 속해 있는지 확인 (NPC ID를 사용)
        UUID npcId = targetNpc.getId();
        if (partyMembership.isInParty(npcId)) {
            sendMessageToUserPort.messageToUser(
                    recruitCommand.userId(),
                    targetNpc.getName() + "는 이미 다른 파티에 속해 있습니다."
            );
            return;
        }

        // 5. 플레이어의 파티가 있는지 확인, 없으면 생성
        Party party = partyMembership.findByMemberId(playerId)
                .orElseGet(() -> createParty(playerId));

        if (!party.isLeader(playerId)) {
            sendMessageToUserPort.messageToUser(
                    recruitCommand.userId(),
                    "파티 리더만 NPC를 초대할 수 있습니다."
            );
            return;
        }

        // 6. NPC를 파티에 추가
        Party.AddPartyMemberResult result = party.addMember(npcId);
        switch (result) {
            case PARTY_FULL -> {
                sendMessageToUserPort.messageToUser(
                        recruitCommand.userId(),
                        "파티가 가득 찼습니다."
                );
            }
            case ALREADY_IN_SAME_PARTY -> {
                sendMessageToUserPort.messageToUser(
                        recruitCommand.userId(),
                        targetNpc.getName() + "는 이미 당신의 파티에 있습니다."
                );
            }
            case ALREADY_IN_OTHER_PARTY -> {
                sendMessageToUserPort.messageToUser(
                        recruitCommand.userId(),
                        targetNpc.getName() + "는 이미 다른 파티에 속해 있습니다."
                );
            }
            case SUCCESS -> {
                sendMessageToUserPort.messageToUser(
                        recruitCommand.userId(),
                        targetNpc.getName() + "가 당신의 파티에 합류했습니다."
                );
                logger.info("NPC {} joined player {}'s party", npcId, playerId);
            }
        }
    }

    private Party createParty(UUID playerId) {
        Party newParty = Party.createParty(playerId);
        partyService.create(newParty);
        logger.info("Created new party for player {}", playerId);
        return newParty;
    }
```

import `java.util.Optional` 추가 (현재 없음).

- [ ] **Step 2: `RecruitCommandServiceTest.java` 변환**

기존 mock:
```java
@Mock private GameWorldService gameWorldService;
```

다음 3개로 교체:
```java
@Mock private CreatureLookupQuery creatureLookup;
@Mock private PartyMembershipQuery partyMembership;
@Mock private PartyService partyService;
```

import 추가:
```java
import com.jefflife.mudmk2.gameplay.application.service.PartyService;
import com.jefflife.mudmk2.gameplay.application.service.query.CreatureLookupQuery;
import com.jefflife.mudmk2.gameplay.application.service.query.PartyMembershipQuery;
```
import 제거:
```java
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
```

`@InjectMocks` 또는 setUp의 RecruitCommandService 생성자 인자 갱신:
- 만약 `@InjectMocks RecruitCommandService recruitCommandService;` 형태라면 생성자 파라미터가 (creatureLookup, partyMembership, partyService, players, sendMessageToUserPort)로 변경됐으므로 mock 필드 순서는 무관(인자 타입 매칭).
- 만약 명시적 생성자 호출이 있다면 새 인자로 변경.

모든 `gameWorldService.getNpcByName(NPC_NAME).thenReturn(...)` stub을 변경:
- `lenient().when(gameWorldService.getNpcByName(NPC_NAME)).thenReturn(null)` → `lenient().when(creatureLookup.findNpcByName(NPC_NAME)).thenReturn(Optional.empty())`
- `lenient().when(gameWorldService.getNpcByName(NPC_NAME)).thenReturn(npc)` → `lenient().when(creatureLookup.findNpcByName(NPC_NAME)).thenReturn(Optional.of(npc))`

`isInParty` stub은 mock 이름만 변경:
- `lenient().when(gameWorldService.isInParty(NPC_ID)).thenReturn(true/false)` → `lenient().when(partyMembership.isInParty(NPC_ID)).thenReturn(true/false)`

`getPartyByPlayerId` stub 동일:
- `lenient().when(gameWorldService.getPartyByPlayerId(PLAYER_ID)).thenReturn(Optional.of(party))` → `lenient().when(partyMembership.findByMemberId(PLAYER_ID)).thenReturn(Optional.of(party))`
- `lenient().when(gameWorldService.getPartyByPlayerId(PLAYER_ID)).thenReturn(Optional.empty())` → `lenient().when(partyMembership.findByMemberId(PLAYER_ID)).thenReturn(Optional.empty())`

이 변경은 약 18개 라인을 건드린다 (라인 78, 104, 130, 131, 159, 160, 161, 190, 191, 192, 221, 222, 223, 252, 253, 254, 283, 284, 285).

`addParty` 검증이 있다면 `partyService.create(...)`로 변경.

- [ ] **Step 3: 빌드/회귀 그린 확인**

Run: `./gradlew test --tests RecruitCommandServiceTest`
Expected: 모든 케이스 PASS

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/RecruitCommandService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/RecruitCommandServiceTest.java
git commit -m "refactor(gameplay): migrate RecruitCommandService to CreatureLookupQuery/PartyMembershipQuery/PartyService"
```

---

## Task 12: `PartySyncService` + 테스트 삭제

`PartySyncService`의 모든 책임이 `InMemoryPartyRepository`(Task 1)로 흡수됨. 파일 통째 삭제. 테스트도 함께 삭제.

**Files:**
- Delete: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/PartySyncService.java`
- Delete: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/sync/PartySyncServiceTest.java`

- [ ] **Step 1: 잔여 호출처 부재 재확인**

Run:
```bash
grep -rn "PartySyncService" src/ --include="*.java"
```
Expected: 출력 없음 (만약 있다면 어디서 import 또는 참조 중인지 확인 필요)

- [ ] **Step 2: 두 파일 삭제**

```bash
git rm src/main/java/com/jefflife/mudmk2/gameplay/application/service/sync/PartySyncService.java
git rm src/test/java/com/jefflife/mudmk2/gameplay/application/service/sync/PartySyncServiceTest.java
```

- [ ] **Step 3: 빌드/회귀 그린 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL (PartySyncServiceTest 사라져도 InMemoryPartyRepository 테스트가 동일 책임 커버)

- [ ] **Step 4: 커밋**

```bash
git commit -m "refactor(gameplay): delete PartySyncService — responsibility absorbed by InMemoryPartyRepository"
```

---

## Task 13: `PersistenceManager.java` + `GameWorldService.java` 삭제 + 잔여 테스트 정리

호출처가 모두 마이그레이션 완료. `PersistenceManager`는 `gameWorldService.loadParties(partyRepository.findAll())` 한 줄만 남았는데 이건 `InMemoryPartyRepository.bootstrap()`(Task 1)이 흡수했으므로 통째 삭제. `GameWorldService`는 임시 위임 6개 + Party 4개 메서드 모두 사용처가 없어 통째 삭제. `SpeakCommandServiceTest`에서 이미 Task 7에서 `FakeGameWorldService`가 제거됐으므로 잔여 정리 없음.

**Files:**
- Delete: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java`
- Delete: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java`

- [ ] **Step 1: 잔여 호출처 부재 재확인**

Run:
```bash
grep -rn "GameWorldService\|gameWorldService" src/ --include="*.java"
```
Expected: 출력 없음

Run:
```bash
grep -rn "PersistenceManager" src/ --include="*.java"
```
Expected: 출력 없음

- [ ] **Step 2: 두 파일 삭제**

```bash
git rm src/main/java/com/jefflife/mudmk2/gameplay/application/service/PersistenceManager.java
git rm src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java
```

- [ ] **Step 3: 빌드/회귀 그린 확인**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL — Phase 5 + 6 신규 + 기존 모든 테스트 그린

- [ ] **Step 4: 부트스트랩 검증 (수동)**

다음을 확인:
- 모든 InMemory*Repository 5개(`InMemoryRoomRepository`, `InMemoryPlayerRepository`, `InMemoryNpcRepository`, `InMemoryMonsterRepository`, `InMemoryPartyRepository`)가 `@EventListener(ApplicationReadyEvent.class)`로 self-bootstrap.
- `GameWorldScheduler`(Phase 5)가 5개 `BatchSyncable` 빈 자동 주입(`InMemoryRoomRepository`/`InMemoryPlayerRepository`/`InMemoryNpcRepository`/`InMemoryMonsterRepository`/`InMemoryPartyRepository`)에 60초마다 `syncToDb()` 호출.

이는 코드 inspection으로 확인.

- [ ] **Step 5: 커밋**

```bash
git commit -m "refactor(gameplay): delete PersistenceManager + GameWorldService — all responsibilities migrated to InMemory*Repository + Query + Service"
```

---

## 완료 기준

- 신규 파일 11개:
  - `InMemoryPartyRepository` + 단위 테스트 9종 (bootstrap, findById empty, findAll, add, remove, remove noop, onPartyCreated, onPartyDisbanded, syncToDb)
  - `PartyService` 인터페이스 + `DefaultPartyService` + 3종 테스트 (create, disband 성공, disband 없음)
  - `DefaultRoomOccupancyQuery` + 4종 테스트 (playersIn, npcsIn, monstersIn alive 필터, playersIn empty)
  - `DefaultCreatureLookupQuery` + 6종 테스트 (findPlayerByName case-insensitive, empty / findNpcByName case-insensitive, empty / findMonstersByType match, empty)
  - `DefaultPartyMembershipQuery` + 4종 테스트 (findByMemberId match, no match / isInParty true, false)
- 호출처 마이그레이션 (각 1 commit):
  - `RoomInfoService` → `RoomOccupancyQuery`
  - `SpeakCommandService` → `RoomOccupancyQuery` + `CreatureLookupQuery` (+ 테스트 FakeGameWorldService → Fake Query 2개)
  - `AttackCommandService` → `RoomOccupancyQuery`
  - `CombatService` → `PartyMembershipQuery`
  - `PartyMemberMoveListener` → `PartyMembershipQuery`
  - `RecruitCommandService` → `CreatureLookupQuery` + `PartyMembershipQuery` + `PartyService` (+ 테스트 mock 3개로 분산)
- 삭제 파일 4개:
  - `PartySyncService.java`
  - `PartySyncServiceTest.java`
  - `PersistenceManager.java`
  - `GameWorldService.java`
- `gameplay` 모듈에서 `GameWorldService` 잔여 사용 0건, `PersistenceManager` 잔여 사용 0건
- `gameplay` 모듈 리플렉션 0건 유지 (Phase 4부터 보장)
- `./gradlew test` 그린 — Phase 5 끝 254개 + 본 phase 신규 약 26개 = 약 280개 그린
- 애플리케이션 부트 시 5개 InMemory*Repository 모두 `Loaded N <도메인>` 로그 출력. `PartyCreatedEvent`/`PartyDisbandedEvent` 발행 시 `InMemoryPartyRepository.onPartyCreated`/`onPartyDisbanded`가 즉시 DB save/delete 호출.

## 다음 phase

본 plan 완료 후:
- `2026-XX-XX-gameworldservice-refactor-cleanup.md` (Phase 7 — 도메인 모델 주석 갱신 `ItemInstance`/`ItemTemplate`/`Inventory`/`Room`/`PlayerCharacter`/`NonPlayerCharacter` 의 "GameWorldService 캐시 적재" 표현 → "InMemory*Repository 캐시 적재"로 일괄 교체 + 최종 리플렉션/잔여 의존 검증)
