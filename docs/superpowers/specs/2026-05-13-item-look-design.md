# 아이템 보기(Look) 기능 설계

**날짜**: 2026-05-13
**브랜치**: feature/item-inventory (현재 브랜치에서 계속)

---

## 개요

아이템의 정보를 게임 내에서 확인할 수 있도록 두 가지 기능을 추가한다.

1. **룸 뷰 확장**: `봐` 명령으로 방을 둘러볼 때 바닥에 있는 아이템 목록을 표시한다.
2. **아이템 상세 보기**: `철검 봐` / `철검 2 봐` 형태로 특정 아이템의 상세 정보를 표시한다.

기존 `LookableType.DIRECTION` 확장 패턴을 그대로 따라 `LookableType.ITEM`을 추가하는 방식으로 구현한다. 룸 뷰 출력은 기존 `room-info` 템플릿에 섹션을 추가하며, 아이템 상세는 신규 `item-info` 템플릿으로 분리한다.

### 범위 외
- 아이템 줍기/버리기 메시지(#1), 소지품 명령(#3)은 이미 구현되어 있다. 본 작업과 별개의 LAZY 초기화 이슈로 동작 안 했을 가능성이 있으며, 이번 범위에서는 다루지 않는다.
- 인벤토리 출력 포맷 변경, 아이템 장착, 아이템 사용은 범위 외.

---

## 1. 명령어 의미론

### LookCommand 시그니처
```java
public record LookCommand(Long userId, String target, int index) implements Command {}
```
- `target == null` → 룸 뷰
- `target != null` → 인덱스(1-based)로 단일 대상 조회. 기본 1.

### Parser 패턴
정규식: `(\\S+?)(?:\\s+(\\d+))?\\s+봐`

| 입력 | target | index |
|---|---|---|
| `봐` | null | 0 (미사용) |
| `철검 봐` | 철검 | 1 |
| `철검 2 봐` | 철검 | 2 |
| `동 봐` | 동 | 1 (Direction이 처리, index 무시) |

---

## 2. 검색 및 인덱스 규칙

### 통합 인덱스
같은 이름의 아이템이 바닥과 소지품에 동시에 존재할 때, **바닥 → 소지품** 순서로 합친 리스트에 1-based 인덱스를 부여한다. `TakeCommand`/`DropCommand`의 인덱스 컨벤션과 일관성을 유지한다.

예 — 바닥에 철검 2개, 소지품에 철검 1개:
| 명령 | 대상 |
|---|---|
| `철검 봐` | 바닥의 1번째 철검 |
| `철검 2 봐` | 바닥의 2번째 철검 |
| `철검 3 봐` | 소지품의 철검 |
| `철검 4 봐` | 매치 없음 → "찾을 수 없습니다" |

### 우선순위
`TargetSearchStrategy.getPriority()` 로 표현:
| Strategy | priority |
|---|---|
| DirectionSearchStrategy | 1 |
| ItemSearchStrategy (신규) | 2 |

`CompositeRoomTargetFinder`는 priority 오름차순으로 순회하며 첫 매치를 반환한다. 따라서 `"동 봐"`처럼 Direction이 매치되는 입력은 ItemSearchStrategy까지 내려오지 않는다.

---

## 3. 룸 뷰 (#2) — 바닥 아이템 표시

### 출력 포맷
```
=== 바닥의 아이템 ===
📦 철검
📦 만두 x5
```

규칙:
- `floorItems`가 비어있으면 섹션 자체 미표시 (NPC/몬스터 섹션과 동일).
- 스택 가능 + quantity > 1 일 때만 `x수량` 표시.
- 같은 이름 비-스택 아이템은 각자 별도 줄.

### 데이터 흐름
```
RoomInfoService.describe()
  └─ currentRoom.getFloorItems()
       └─ map → List<FloorItemInfo>
            └─ RoomInfoVariables (필드 추가)
                 └─ RoomInfoMessageSender
                      └─ context.setVariable("floorItems", ...)
                           └─ room-info.html (섹션 추가)
```

`FloorItemInfo`:
```java
public record FloorItemInfo(String name, int quantity, boolean stackable) {}
```

---

## 4. 아이템 상세 보기 (#4)

### 출력 포맷 (예: WEAPON)
```
[ 철검 ]
날카로운 철로 만든 검.
위치: 바닥        타입: 무기(검)
무게: 5kg

스탯
  - 검술 +5
  - 민첩 +2
```

공통 영역:
- 헤더: 이름
- 설명
- 위치 (바닥 / 소지품)
- 타입 (한글 라벨)
- 무게
- 스택 아이템 — 수량

타입별 영역:
| 타입 | 표시 |
|---|---|
| FOOD | HP/MP/AP 회복 (값 > 0인 것만) |
| WEAPON | 무기 타입(SWORD 등) + 스탯 목록 |
| EQUIPMENT | 장착 슬롯 + 스탯 목록 |
| ACCESSORY | 악세서리 타입 + 스탯 목록 |
| MARTIAL_ARTS_BOOK | 스킬 참조 |
| MISSION | 미션 타입 + 타깃 참조 |

### 한글 라벨
enum → 한글 표시명 매핑은 `ItemDisplayLabels` 헬퍼에서 일괄 관리.
- `ItemType.WEAPON` → "무기"
- `WeaponType.SWORD` → "검"
- `EquipmentSlot.HELMET` → "투구"
- `StatType.SWORD_METHOD` → "검술"
- 등

### 데이터 흐름
```
LookTargetProcessor
  └─ ItemSearchStrategy.search(userId, "철검", 2)
       └─ Optional<Lookable> (ItemLookable)
            └─ DescriberManager → ItemDescriber
                 └─ ItemInfoVariables 생성
                      └─ SendItemInfoMessagePort.sendMessage()
                           └─ ItemInfoMessageSender → item-info.html
```

`ItemLookable`:
```java
public record ItemLookable(ItemInstance instance, ItemLocation location) implements Lookable {
    public enum ItemLocation { ROOM, INVENTORY }
    // getName() = instance.getTemplate().getName()
    // getType() = LookableType.ITEM
    // getProperties() = Map.of("instance", instance, "location", location)
}
```

---

## 5. 백엔드 아키텍처

### 변경되는 인터페이스 시그니처
```java
// TargetSearchStrategy
Optional<Lookable> search(Long userId, String targetName, int index);

// LookableTargetFinder
Optional<Lookable> findTargetInRoom(Long userId, String targetName, int index);

// LookTargetProcessor
void processLookTarget(Long userId, String targetName, int index);
```

`DirectionSearchStrategy`는 새 시그니처를 구현하되 `index`는 무시한다.

### LookableType 확장
```java
public enum LookableType { DIRECTION, ITEM }
```

### 신규 컴포넌트
- `ItemLookable` (record + ItemLocation enum)
- `ItemSearchStrategy` (priority=2)
- `ItemDescriber` (LookTargetDescriber for LookableType.ITEM)
- `ItemDisplayLabels` (enum → 한글 매핑 유틸 클래스 — static method 기반)
- `SendItemInfoMessagePort` (포트)
- `ItemInfoMessageSender` (어댑터, Thymeleaf 렌더링)
- `ItemInfoVariables` (템플릿 변수 record)
- `FloorItemInfo` (룸 뷰 아이템 DTO)
- `item-info.html` (Thymeleaf 템플릿)

### 수정되는 컴포넌트
- `LookCommand` — index 필드 추가
- `LookCommandParser` — 정규식에 (\\d+)? 추가
- `LookCommandService` — index 전달
- `LookableType` — ITEM 값 추가
- `TargetSearchStrategy` / `LookableTargetFinder` / `LookTargetProcessor` — index 시그니처
- `CompositeRoomTargetFinder` / `DefaultLookTargetProcessor` / `DirectionSearchStrategy` — 새 시그니처 구현
- `RoomInfoVariables` — floorItems 필드
- `RoomInfoService` — floorItems 매핑
- `RoomInfoMessageSender` — context 변수 추가
- `room-info.html` — 바닥 아이템 섹션 추가

---

## 6. 에러 처리

| 상황 | 동작 |
|---|---|
| 이름 매치 0개 | `DefaultLookTargetProcessor` 기존 흐름 — "'X'을(를) 찾을 수 없습니다." |
| index가 매치 수 초과 | 동일 — empty 반환 |
| index 누락 | parser에서 default 1 |
| index가 0 이하 | ItemSearchStrategy에서 `index < 1`이면 empty (방어적) |
| 플레이어/룸 없음 | 기존 예외 전파 |
| 빈 floor / 빈 inventory | 자연스럽게 매치 0개 → 위 흐름 |
| FOOD 회복값 모두 0 | 템플릿에서 `th:if`로 회복 섹션 미표시 |
| 빈 statModifiers | `th:if`로 스탯 섹션 미표시 (NPC/몬스터 섹션과 동일 컨벤션) |

---

## 7. 테스트 전략

### 신규 테스트
- `ItemSearchStrategyTest` — 바닥/소지품 통합 인덱스, 범위 밖, 빈 매치, 우선순위 동작
- `ItemDescriberTest` — 6개 타입별 골든 출력, 한글 라벨, 위치 표시, 스택 수량, 빈 스탯/0 회복값 처리

### 수정되는 테스트
- `LookCommandParserTest` — `"철검 봐"`, `"철검 2 봐"` 케이스 추가
- `LookCommandServiceTest` — index 인자 전달 검증
- `DirectionSearchStrategyTest` (있다면) — 시그니처 변경 반영
- `RoomInfoServiceTest` (있다면) — floorItems 매핑 검증

### 통합 검증 (수동)
`./gradlew bootRun` 후 게임 클라이언트에서:
1. Admin UI로 아이템 배치 → `봐` → "=== 바닥의 아이템 ===" 표시 확인
2. `철검 봐` → 상세 정보 표시 확인
3. 바닥+소지품 동시 존재 시 `철검 N 봐` 인덱스 동작 확인
4. 6종 ItemType 모두 골든 출력 확인 (FOOD, WEAPON, EQUIPMENT, ACCESSORY, MARTIAL_ARTS_BOOK, MISSION)
5. 범위 밖 인덱스 / 존재하지 않는 이름 → 안내 메시지 확인

---

## 8. 파일 목록

### 신규 생성
```
gameplay/application/service/command/look/
  ItemLookable.java
  ItemSearchStrategy.java
  ItemDescriber.java
  ItemDisplayLabels.java

gameplay/application/service/required/
  SendItemInfoMessagePort.java

gameplay/application/service/model/template/
  ItemInfoVariables.java
  FloorItemInfo.java

gameplay/adapter/out/eventpublisher/chat/
  ItemInfoMessageSender.java

resources/templates/gameplay/
  item-info.html
```

### 수정
```
gameplay/application/domain/model/command/LookCommand.java
gameplay/adapter/in/eventlistener/parser/LookCommandParser.java
gameplay/application/service/command/look/LookableType.java
gameplay/application/service/command/look/TargetSearchStrategy.java
gameplay/application/service/command/look/LookableTargetFinder.java
gameplay/application/service/command/look/LookTargetProcessor.java
gameplay/application/service/command/look/CompositeRoomTargetFinder.java
gameplay/application/service/command/look/DefaultLookTargetProcessor.java
gameplay/application/service/command/DirectionSearchStrategy.java
gameplay/application/service/command/LookCommandService.java
gameplay/application/service/model/template/RoomInfoVariables.java
gameplay/application/service/RoomInfoService.java
gameplay/adapter/out/eventpublisher/chat/RoomInfoMessageSender.java
resources/templates/gameplay/room-info.html
```
