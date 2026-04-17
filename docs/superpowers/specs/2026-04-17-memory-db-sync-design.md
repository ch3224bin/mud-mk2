# Memory-DB 동기화 설계

**날짜**: 2026-04-17  
**범위**: PlayerCharacter + Party (향후 아이템 등 확장 고려)

---

## 배경 및 목표

MUD 게임 특성상 퍼포먼스를 위해 게임 상태를 인메모리(`ConcurrentHashMap`)에 유지하고, 주기적으로 DB에 동기화한다. 현재는 `PersistenceManager`가 `PlayerCharacter`만 60초마다 `saveAll`하는 구조이며, `Party`는 `@Entity`임에도 저장 코드가 없어 재시작 시 소멸된다.

**목표:**
- 확장 가능한 배치 동기화 구조 도입
- Party 지속성 확보
- 새 엔티티(아이템 등) 추가 시 `PersistenceManager` 수정 없이 등록 가능

---

## 요구사항

- 데이터 손실 허용: 60초 이상 허용 (배치 중심 설계)
- Dirty flag 없음: 활성 엔티티는 항상 변화가 있으므로 주기마다 전체 저장
- Party 생성/해산: write-through (즉시 저장/삭제)
- Party 멤버 변경 등 상태 변화: 배치 저장
- 확장 방식: `BatchSyncable` 인터페이스 구현 + Spring DI 자동 등록

---

## 아키텍처

```
┌─────────────────────────────────────────────────────┐
│                  PersistenceManager                  │
│                                                      │
│  @Scheduled(60s)  ──→  List<BatchSyncable>.forEach   │
│                           └─ syncToDb()              │
│                                                      │
│  ApplicationReadyEvent ──→  loadGameState()          │
│  @Scheduled(5s)    ──→  checkMonsterRespawn()        │
└─────────────────────────────────────────────────────┘
         ↑ @Autowired List<BatchSyncable> (Spring DI 자동 수집)
         │
┌────────┴──────────────────────────────┐
│  BatchSyncable (interface)            │
│  + syncToDb(): void                   │
└───────────────────────────────────────┘
         ↑ 구현
         ├─ PlayerCharacterSyncService   (60s 배치)
         ├─ PartySyncService             (60s 배치 + write-through)
         └─ (미래: ItemSyncService 등)
```

---

## 컴포넌트 상세

### `BatchSyncable` 인터페이스
```
gameplay/application/service/sync/BatchSyncable.java
```
- 메서드: `void syncToDb()`
- Spring이 `List<BatchSyncable>`로 자동 수집하므로 별도 등록 코드 불필요

### `PlayerCharacterSyncService`
```
gameplay/application/service/sync/PlayerCharacterSyncService.java
```
- `BatchSyncable` 구현
- `syncToDb()`: `playerCharacterRepository.saveAll(gameWorldService.getActivePlayers())`
- 기존 `PersistenceManager.persistGameState()`의 로직을 이곳으로 이동

### `PartySyncService`
```
gameplay/application/service/sync/PartySyncService.java
```
- `BatchSyncable` 구현
- `syncToDb()`: `partyRepository.saveAll(gameWorldService.getActiveParties())`
- `@EventListener(PartyCreatedEvent)`: 생성 즉시 `partyRepository.save(party)`
- `@EventListener(PartyDisbandedEvent)`: 해산 즉시 `partyRepository.deleteById(partyId)`

### `PersistenceManager` 변경
- `persistGameState()` 내부를 `batchSyncables.forEach(BatchSyncable::syncToDb)`로 교체
- `List<BatchSyncable> batchSyncables` 생성자 주입 추가
- `playerCharacterRepository`, `nonPlayerCharacterRepository` 직접 의존성 제거 가능 (SyncService로 이동)
- `loadGameState()`, `checkMonsterRespawn()`은 변경 없음

### `GameWorldService` 변경
- `addParty(Party)`: 기존 로직 + `PartyCreatedEvent` 발행 추가
- `removeParty(UUID partyId)`: 신규 추가, `PartyDisbandedEvent` 발행
- `getActiveParties()`: `PartySyncService`가 사용할 컬렉션 반환 메서드 추가

### 이벤트 클래스 (신규)
```
gamedata/application/event/PartyCreatedEvent.java
gamedata/application/event/PartyDisbandedEvent.java
```

---

## 데이터 흐름

### 배치 동기화 (60초마다)
```
@Scheduled → PersistenceManager.persistGameState()
  → batchSyncables.forEach(syncToDb)
      ├─ PlayerCharacterSyncService.syncToDb()
      │     → playerCharacterRepository.saveAll(activePlayers)
      └─ PartySyncService.syncToDb()
            → partyRepository.saveAll(activeParties)
```

### Party 생성 (write-through)
```
RecruitCommandService.createParty()
  → Party.createParty(leaderId)
  → GameWorldService.addParty(party)
      └─ eventPublisher.publishEvent(PartyCreatedEvent)
           └─ PartySyncService.onPartyCreated()
                 → partyRepository.save(party)
```

### Party 해산 (write-through, 향후 구현)
```
[해산 커맨드 서비스]
  → GameWorldService.removeParty(partyId)
      └─ eventPublisher.publishEvent(PartyDisbandedEvent)
           └─ PartySyncService.onPartyDisbanded()
                 → partyRepository.deleteById(partyId)
```

### 서버 시작 (DB → Memory)
```
ApplicationReadyEvent → PersistenceManager.loadGameState()
  → roomRepository.findAll() → gameWorldService.loadRooms()
  → playerCharacterRepository.findAll() → gameWorldService.loadPlayers()
  → nonPlayerCharacterRepository.findAll() → gameWorldService.loadNpcs()
  → monsterTypeRepository.findAll() → loadMonsters() (DB 없음, 템플릿에서 생성)
  + partyRepository.findAll() → gameWorldService.loadParties()  ← 신규 추가
```

---

## 향후 확장 예시 (아이템)

```java
@Component
public class ItemSyncService implements BatchSyncable {
    @Override
    public void syncToDb() {
        itemRepository.saveAll(gameWorldService.getActiveItems());
    }
}
```

`PersistenceManager` 수정 없이 자동으로 60초 배치에 포함됨.

---

## 현재 미구현 / 향후 과제

| 항목 | 상태 |
|---|---|
| Party 해산 커맨드 | 미구현 — 이번 설계로 기반 마련 |
| NPC 상태 동기화 | 미포함 — 요구사항 밖 |
| 게임 시간/날씨 지속성 | 미포함 — 요구사항 밖 |
| 서버 종료 시 즉시 저장 (shutdown hook) | 미포함 — 손실 허용 범위 내 |
