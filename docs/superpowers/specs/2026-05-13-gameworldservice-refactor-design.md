# GameWorldService 리팩토링 설계

- 일자: 2026-05-13
- 브랜치: `refactor/gameworld-service`
- 범위: `gameplay` 모듈의 인메모리 캐시 구조

## 1. 배경과 문제

`GameWorldService` (`gameplay/application/service/GameWorldService.java`, 356라인) 는 다음 5개 도메인의 인메모리 캐시를 한 클래스가 모두 관리하는 God Object 형태로 자라났다.

- `PlayerCharacter` (이중 인덱스: `activePlayers` / `activePlayersByUserId`)
- `Room`
- `NonPlayerCharacter`
- `Monster`
- `Party`

22개 파일이 직접 의존하고 있고, 다음과 같은 문제가 누적되어 있다.

- **SRP 위반**: 한 클래스가 5개 도메인의 CRUD + 위치/관계 조회 + 도메인 행위(`respawnMonsters`) + 이벤트 발행(`addParty`)을 모두 담당.
- **추상 누수**: 호출자가 "이건 캐시다"를 이름과 메서드로 알고 있어 헥사고날 경계가 흐려짐. 도메인 모델 코드의 주석에도 `GameWorldService 캐시 적재 시점` 같은 결합 흔적이 남아 있음.
- **OCP 위반**: 새 도메인 객체 추가 시 `GameWorldService`를 매번 수정해야 함.
- **코드 스멜**: `moveNpcToRoom`이 도메인의 private 필드를 리플렉션으로 변경.
- **부트스트랩과 동기화의 결합**: `PersistenceManager`가 5개 도메인의 적재 순서를 명령형으로 호출하고, `*SyncService`가 `getActivePlayers()` 같은 메서드로 캐시를 끄집어내 JPA에 저장.

## 2. 결정 사항

브레인스토밍 단계의 6개 핵심 결정:

| # | 결정 | 채택 |
|---|---|---|
| 1 | 방향 | DDD 인메모리 리포지토리화 (Repository 포트 추상 뒤로 캐시 은닉) |
| 2 | 포트 전략 | `gameplay`에 별도 `Active*Repository` 신설, JPA는 위임 대상 |
| 3 | 위치/관계 조회 | 별도 Query 컴포넌트 (Read Model) |
| 4 | 부트스트랩 | 각 어댑터 self-bootstrap (`@EventListener(ApplicationReadyEvent)`) |
| 5 | DB 동기화 | Repository 어댑터가 `BatchSyncable` 직접 구현 |
| 6 | 이벤트·도메인 행위 | Application Service 계층 신설 |

추가 규약:

- Query 와 Service 도 인터페이스 + `Default*` 구현체 쌍으로 분리.
- 단, `GameWorldScheduler`는 `@Scheduled` 진입점이므로 구체 클래스로 둠.
- Repository vs Query 분리 규칙:
    - Repository: 그 애그리거트의 식별자(자연 키 포함) 기반 단건/전체 조회 + 컬렉션 변경
    - Query: 위치·이름·상태 기반 검색, 여러 애그리거트를 가로지르는 조합

## 3. 패키지 구조

```
gameplay
├── adapter
│   └── out/cache                                    (신규)
│       ├── InMemoryRoomRepository.java              implements ActiveRoomRepository, BatchSyncable
│       ├── InMemoryPlayerRepository.java            implements ActivePlayerRepository, BatchSyncable
│       ├── InMemoryNpcRepository.java               implements ActiveNpcRepository, BatchSyncable
│       ├── InMemoryMonsterRepository.java           implements ActiveMonsterRepository, BatchSyncable
│       └── InMemoryPartyRepository.java             implements ActivePartyRepository, BatchSyncable
│
└── application
    ├── exception                                    (신규)
    │   ├── RoomNotFoundException.java
    │   ├── PlayerNotFoundException.java
    │   ├── NpcNotFoundException.java
    │   ├── MonsterNotFoundException.java
    │   └── PartyNotFoundException.java
    │
    └── service
        ├── required                                 (out ports, 신규)
        │   ├── ActiveRoomRepository.java
        │   ├── ActivePlayerRepository.java
        │   ├── ActiveNpcRepository.java
        │   ├── ActiveMonsterRepository.java
        │   └── ActivePartyRepository.java
        │
        ├── query                                    (신규)
        │   ├── RoomOccupancyQuery.java              (interface)
        │   ├── DefaultRoomOccupancyQuery.java
        │   ├── CreatureLookupQuery.java             (interface)
        │   ├── DefaultCreatureLookupQuery.java
        │   ├── PartyMembershipQuery.java            (interface)
        │   └── DefaultPartyMembershipQuery.java
        │
        ├── PartyService.java                        (interface, 신규)
        ├── DefaultPartyService.java                 (신규)
        ├── MonsterRespawnService.java               (interface, 신규)
        ├── DefaultMonsterRespawnService.java        (신규)
        ├── NpcLocationService.java                  (interface, 신규: 리플렉션 제거)
        ├── DefaultNpcLocationService.java           (신규)
        │
        ├── GameWorldScheduler.java                  (구체 @Component, 신규)
        │
        ├── command/...                              (호출자 의존만 변경)
        ├── sync/BatchSyncable.java                  (유지)
        ├── sync/PlayerCharacterSyncService.java     ❌ 삭제
        └── sync/PartySyncService.java               ❌ 삭제

❌ gameplay/application/service/GameWorldService.java   삭제
❌ gameplay/application/service/PersistenceManager.java 삭제
```

**의존성 흐름**

```
호출자 (CommandService, RoomInfoService, ...)
   │
   ├──▶ ActiveXxxRepository (interface) ──┐
   ├──▶ XxxQuery            (interface)   │
   └──▶ XxxService          (interface)   │
                                          │
                  ┌───────────────────────┘
                  ▼
       InMemoryXxxRepository  /  DefaultXxxQuery  /  DefaultXxxService
                  │
                  └─위임─▶ gamedata 의 JPA Repository (InMemory* 어댑터만)
```

## 4. 포트·Query·Service 시그니처

### 4.1 Active*Repository

```java
public interface ActiveRoomRepository {
    Optional<Room> findById(Long id);
    Iterable<Room> findAll();
    void add(Room room);
    void remove(Long id);
}

public interface ActivePlayerRepository {
    Optional<PlayerCharacter> findById(UUID id);
    Optional<PlayerCharacter> findByUserId(Long userId);
    Iterable<PlayerCharacter> findAll();
    void add(PlayerCharacter player);
    void removeByUserId(Long userId);
}

public interface ActiveNpcRepository {
    Optional<NonPlayerCharacter> findById(UUID id);
    Iterable<NonPlayerCharacter> findAll();
    void add(NonPlayerCharacter npc);
    void remove(UUID id);
}

public interface ActiveMonsterRepository {
    Optional<Monster> findById(UUID id);
    Iterable<Monster> findAll();
    void add(Monster monster);
    void remove(UUID id);
}

public interface ActivePartyRepository {
    Optional<Party> findById(UUID id);
    Iterable<Party> findAll();
    void add(Party party);
    void remove(UUID id);
}
```

### 4.2 Query

```java
public interface RoomOccupancyQuery {
    List<PlayerCharacter>     playersIn(Long roomId);
    List<NonPlayerCharacter>  npcsIn(Long roomId);
    List<Monster>             monstersIn(Long roomId);
}

public interface CreatureLookupQuery {
    Optional<PlayerCharacter>     findPlayerByName(String name);
    Optional<NonPlayerCharacter>  findNpcByName(String name);
    List<Monster>                 findMonstersByType(Long monsterTypeId);
}

public interface PartyMembershipQuery {
    Optional<Party> findByMemberId(UUID memberId);
    boolean         isInParty(UUID memberId);
}
```

### 4.3 Service

```java
public interface PartyService {
    void create(Party party);      // add + PartyCreatedEvent 발행
    void disband(UUID partyId);    // remove + PartyDisbandedEvent 발행
}

public interface MonsterRespawnService {
    int respawnAll();              // 죽었고 respawn 가능한 몬스터 일괄 부활, 부활 수 반환
}

public interface NpcLocationService {
    boolean move(UUID npcId, Long roomId);
}
```

### 4.4 기존 GameWorldService → 새 위치 매핑

| 기존 메서드 | 새 위치 |
|---|---|
| `getRoom`, `getRoomOptional` | `ActiveRoomRepository.findById` (+ 호출자 `orElseThrow`) |
| `getPlayerByUserId`, `getPlayerById`, `getActivePlayers`, `addPlayer`, `removePlayer` | `ActivePlayerRepository.*` |
| `getNpcById`, `addNpc` | `ActiveNpcRepository.*` |
| `getMonsterById`, `getAllMonsters`, `addMonster`, `removeMonster` | `ActiveMonsterRepository.*` |
| `getActiveParties` | `ActivePartyRepository.findAll` |
| `getPlayersInRoom`, `getNpcsInRoom`, `getMonstersInRoom` | `RoomOccupancyQuery.*` |
| `getPlayerByName`, `getNpcByName`, `getMonstersByType` | `CreatureLookupQuery.*` |
| `getPartyByPlayerId`, `isInParty` | `PartyMembershipQuery.*` |
| `getUserIdByCharacterId` | 호출자가 `ActivePlayerRepository.findById().map(PlayerCharacter::getUserId)` |
| `addParty`, `removeParty` | `PartyService.create`, `disband` |
| `respawnMonsters` | `MonsterRespawnService.respawnAll` |
| `moveNpcToRoom` | `NpcLocationService.move` |
| `loadRooms`, `loadPlayers`, `loadNpcs`, `loadMonsters`, `loadParties` | 각 `InMemory*Repository` self-bootstrap |

## 5. 어댑터 구현

### 5.1 표준 패턴 (`InMemoryRoomRepository` 예시)

```java
@Component
public class InMemoryRoomRepository implements ActiveRoomRepository, BatchSyncable {
    private static final Logger log = LoggerFactory.getLogger(InMemoryRoomRepository.class);

    private final RoomRepository jpa;
    private final Map<Long, Room> cache = new ConcurrentHashMap<>();

    public InMemoryRoomRepository(RoomRepository jpa) { this.jpa = jpa; }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void bootstrap() {
        jpa.findAll().forEach(r -> {
            r.initializeAssociatedEntities();
            cache.put(r.getId(), r);
        });
        log.info("Loaded {} rooms", cache.size());
    }

    @Override public Optional<Room> findById(Long id) { return Optional.ofNullable(cache.get(id)); }
    @Override public Iterable<Room> findAll()         { return cache.values(); }
    @Override public void remove(Long id)             { cache.remove(id); }

    @Override @Transactional
    public void syncToDb() { jpa.saveAll(cache.values()); }
}
```

### 5.2 도메인별 차이점

| 어댑터 | 부트스트랩 특이점 | 키 | 비고 |
|---|---|---|---|
| `InMemoryRoomRepository` | `RoomRepository.findAll()` | `Long id` | — |
| `InMemoryPlayerRepository` | `PlayerCharacterRepository.findAll()` | `UUID id` + `Long userId` 이중 인덱스 | `removeByUserId`는 두 맵을 함께 정리. `PlayerCharacterCreatedEvent`도 이 어댑터에서 처리 |
| `InMemoryNpcRepository` | `NonPlayerCharacterRepository.findAll()` | `UUID id` | — |
| `InMemoryMonsterRepository` | `MonsterTypeRepository.findAll()` 후 `Monster.createFromType(...)`로 생성해 적재 (현재 `PersistenceManager.loadMonsters` 로직 흡수) | `UUID id` | `Random` 의존성 유지 |
| `InMemoryPartyRepository` | `PartyRepository.findAll()` | `UUID id` | 어댑터는 순수 CRUD, 이벤트 발행은 `DefaultPartyService` |

### 5.3 부트스트랩 순서

`@Order` 불필요. 도메인 간 메모리 의존이 없으며, 각 어댑터가 JPA에서 직접 적재한다.

### 5.4 Player 이중 인덱스 동시성

`add` / `removeByUserId` 메서드는 두 맵 (`byId`, `byUserId`) 을 동시에 갱신하므로 `synchronized` 블록 또는 공유 락 객체로 보호한다. 그 외 캐시는 `ConcurrentHashMap` 자체 보호로 충분.

## 6. 도메인 모델 변경

### 6.1 리플렉션 제거

`BaseCharacter` (또는 `NonPlayerCharacter`) 에 도메인 메서드 추가:

```java
public void moveTo(Long roomId) {
    if (roomId == null) throw new IllegalArgumentException("roomId must not be null");
    this.roomId = roomId;
}
```

`DefaultNpcLocationService.move(UUID, Long)`이 이 메서드를 호출한다. 기존 `GameWorldService.moveNpcToRoom`의 리플렉션은 제거.

### 6.2 `initializeAssociatedEntities()`

`Room`, `PlayerCharacter`, `NonPlayerCharacter`, `ItemTemplate`, `ItemInstance`, `Inventory`에 분산된 이 메서드들은 그대로 유지. 어댑터 부트스트랩 시점에 호출된다. 도메인 모델의 주석 문구만 갱신:

```
// 변경 전: GameWorldService 캐시 적재 시점에 LAZY ...
// 변경 후: 인메모리 캐시 적재 시점에 LAZY ...
```

### 6.3 `PlayerCharacterCreatedEvent`

기존 `PersistenceManager.handlePlayerCharacterCreatedEvent`를 `InMemoryPlayerRepository`로 이전:

```java
@EventListener
@Transactional(readOnly = true)
public void onCreated(PlayerCharacterCreatedEvent event) {
    var pc = event.getPlayerCharacter();
    pc.initializeAssociatedEntities();
    add(pc);
}
```

## 7. GameWorldScheduler

```java
@Component
public class GameWorldScheduler {
    private static final Logger log = LoggerFactory.getLogger(GameWorldScheduler.class);

    private final List<BatchSyncable>   syncables;
    private final MonsterRespawnService respawn;

    public GameWorldScheduler(List<BatchSyncable> syncables, MonsterRespawnService respawn) {
        this.syncables = syncables;
        this.respawn = respawn;
    }

    @Scheduled(fixedDelay = 60_000)
    public void persist() {
        syncables.forEach(BatchSyncable::syncToDb);
    }

    @Scheduled(fixedDelay = 5_000)
    public void respawnMonsters() {
        int count = respawn.respawnAll();
        if (count > 0) log.debug("Respawned {} monsters", count);
    }
}
```

스프링이 `BatchSyncable` 빈 5개를 자동으로 `List`에 주입한다. 트랜잭션 경계는 각 어댑터의 `syncToDb()`에 걸리므로 스케줄러 자체는 트랜잭션 무관.

## 8. 호출처 마이그레이션

### 8.1 변환 패턴

```java
// before
PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
Room room              = gameWorldService.getRoom(player.getCurrentRoomId());

// after
PlayerCharacter player = players.findByUserId(command.userId())
    .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
Room room = rooms.findById(player.getCurrentRoomId())
    .orElseThrow(() -> new RoomNotFoundException(player.getCurrentRoomId()));
```

### 8.2 호출처별 새 의존

| 호출처 | 새 의존 |
|---|---|
| `MoveCommandService`, `StatusCommandService`, `DropCommandService`, `TakeCommandService`, `DirectionSearchStrategy`, `InventoryCommandService`, `RoomInfoService` | `ActivePlayerRepository`, `ActiveRoomRepository` |
| `AttackCommandService` | `ActivePlayerRepository`, `RoomOccupancyQuery` |
| `SpeakCommandService` | `ActivePlayerRepository`, `RoomOccupancyQuery`, `CreatureLookupQuery` |
| `RecruitCommandService` | `ActivePlayerRepository`, `CreatureLookupQuery`, `PartyMembershipQuery`, `PartyService` |
| `CombatService` | `ActiveRoomRepository`, `PartyMembershipQuery` |
| `ItemInstanceService` | `ActiveRoomRepository`, `ActivePlayerRepository` |
| `SimulationService` | `ActiveMonsterRepository` |
| `PartyMemberMoveListener` | `PartyMembershipQuery`, `ActiveNpcRepository`, `ActivePlayerRepository`, `NpcLocationService` |
| `PlayerCharacterSyncService`, `PartySyncService` | ❌ 삭제 |
| `ItemInstance.java`, `ItemTemplate.java`, `Inventory.java` | 주석 문구만 갱신 |

### 8.3 점진적 단계 (각 단계 후 빌드/테스트 그린 보장)

도메인별로 "어댑터 추가 → 호출처 마이그레이션 → `GameWorldService` 해당 메서드 제거"를 한 호흡에 끊는다. 두 캐시가 분리된 중간 상태를 만들지 않기 위함이다.

1. **공통 기반**: 5개 `Active*Repository` 인터페이스, 5개 도메인 예외, 3개 Query 인터페이스. (구현체와 어댑터는 도메인 단계에서 추가)
2. **Room 도메인 마이그레이션**:
    - `InMemoryRoomRepository` 추가 (self-bootstrap + `BatchSyncable`).
    - `PersistenceManager.loadGameState`에서 `loadRooms` 호출 제거.
    - 호출처 (`RoomInfoService`, `MoveCommandService`, `StatusCommandService`, `DropCommandService`, `TakeCommandService`, `DirectionSearchStrategy`, `InventoryCommandService`, `ItemInstanceService`, `CombatService`, `ItemSearchStrategy`) 의존 변경.
    - `GameWorldService`에서 `rooms` 필드와 `getRoom/getRoomOptional/removeRoom/loadRooms` 제거.
3. **Player 도메인 마이그레이션**:
    - `InMemoryPlayerRepository` 추가 + `PlayerCharacterCreatedEvent` 리스너 이전.
    - `PersistenceManager`에서 `loadPlayers`/`handlePlayerCharacterCreatedEvent` 제거.
    - 호출처 의존 변경.
    - `PlayerCharacterSyncService` 삭제.
    - `GameWorldService`에서 player 관련 메서드 제거.
4. **NPC 도메인 마이그레이션 + 리플렉션 제거**:
    - `InMemoryNpcRepository` 추가. 도메인 모델 `BaseCharacter.moveTo(Long)` 추가.
    - `DefaultNpcLocationService` 도입, `PartyMemberMoveListener` 등 호출처 의존 변경.
    - `GameWorldService`에서 npc 관련 메서드 + `moveNpcToRoom` 제거.
5. **Monster 도메인 마이그레이션 + 스케줄러 도입**:
    - `InMemoryMonsterRepository` 추가 (MonsterType 기반 생성 로직 흡수).
    - `DefaultMonsterRespawnService` + `GameWorldScheduler` 추가.
    - `PersistenceManager`의 `loadMonsters` / `checkMonsterRespawn` / `persistGameState` 제거 → `PersistenceManager`는 빈 껍데기.
    - 호출처 (`SimulationService`, `AttackCommandService`) 의존 변경.
    - `GameWorldService`에서 monster 관련 메서드 제거.
6. **Party 도메인 마이그레이션 + 이벤트 책임 이관**:
    - `InMemoryPartyRepository` + `DefaultPartyService` + 3개 Query 구현체 추가.
    - 호출처 (`RecruitCommandService`, `CombatService`, `PartyMemberMoveListener`, `SpeakCommandService`) 의존 변경.
    - `PartySyncService` 삭제.
    - `GameWorldService`에서 party 관련 메서드 제거.
7. **잔여 정리**:
    - 도메인 모델 (`ItemInstance`, `ItemTemplate`, `Inventory`) 의 주석 문구 갱신.
    - `GameWorldService.java`, `PersistenceManager.java` 삭제.

## 9. 에러 처리·트랜잭션

### 9.1 예외 설계

- 5종 도메인 예외 (`RoomNotFoundException` 등) 를 `gameplay/application/exception/`에 추가. 모두 `RuntimeException` 상속 (CODING_RULES 3.4: 런타임 예외 선호).
- Repository는 `Optional`만 반환하고 던지지 않는다. "없을 때 예외냐 빈값이냐"의 결정은 호출자에 위임.
- 기존 `getRoom`이 던지던 `IllegalArgumentException`은 호출자가 도메인 예외로 격상.

### 9.2 트랜잭션 경계

| 위치 | 어노테이션 | 이유 |
|---|---|---|
| `InMemory*Repository.bootstrap()` | `@Transactional(readOnly = true)` | JPA LAZY 초기화 |
| `InMemory*Repository.syncToDb()` | `@Transactional` (어댑터 독립) | 한 도메인 동기화 실패가 다른 도메인에 전파되지 않음 |
| `InMemoryPlayerRepository.onCreated` | `@Transactional(readOnly = true)` | `initializeAssociatedEntities` 호출 시 LAZY |
| 일반 read/write (`findById`, `add`, `remove`) | 없음 | 메모리만 |
| `GameWorldScheduler` | 없음 | 트랜잭션은 어댑터 내부에서 끊김 |

## 10. 테스트 전략

### 10.1 어댑터 단위 테스트

- JPA Repository를 Mockito mock으로 주입.
- 검증: `bootstrap()` 후 `findById` 정확성, `syncToDb()`에서 `saveAll` 호출 인자, `add`/`remove` 후 캐시 상태.

### 10.2 Query 단위 테스트

- `Active*Repository`는 fake in-memory 구현이나 mock으로 stub.
- Query는 stream 조합이므로 fake가 더 깔끔.

### 10.3 Service 단위 테스트

- Repository + `ApplicationEventPublisher` mock.
- `PartyService.create()` 후 `add` 호출과 `PartyCreatedEvent` 발행 모두 검증.
- `NpcLocationService.move()`에서 `npc.moveTo(roomId)` 호출 검증 (리플렉션 제거 회귀 방지).

### 10.4 통합 테스트

- `@SpringBootTest`로 골든 패스 1개: JPA 시드 → `ApplicationReadyEvent` → 캐시 조회 → `add` 후 `syncToDb()`로 JPA 반영.

### 10.5 기존 테스트 이관

- `GameWorldService` mock을 쓰던 테스트는 신규 포트 mock으로 교체 (각 마이그레이션 PR과 동반).
- `PersistenceManagerTest` (존재 시) 는 어댑터·스케줄러 테스트로 분해.
