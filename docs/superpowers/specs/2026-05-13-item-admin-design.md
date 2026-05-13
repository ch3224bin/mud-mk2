# 아이템 Admin 관리 UI 설계

**날짜**: 2026-05-13  
**브랜치**: feature/item-inventory (현재 브랜치에서 계속)

---

## 개요

아이템 템플릿(ItemTemplate) CRUD와 아이템 인스턴스(ItemInstance) 배치를 위한 Admin 웹 UI를 추가한다.  
두 개의 분리된 페이지로 구성하며, 기존 Admin 패턴(Thymeleaf + Bootstrap + 바닐라 JS + REST API)을 따른다.

---

## 1. 페이지 구조

### 1-1. 아이템 템플릿 관리 (`/item-template-management`)

6종 ItemTemplate을 생성/편집/삭제한다.

**UI 구성:**

- 상단: 전체 템플릿 목록 테이블
  - 컬럼: ID | 이름 | 타입(배지) | 무게 | 스택여부 | 편집 / 삭제 버튼
  - 타입 필터 드롭다운 (모든 타입 / FOOD / WEAPON / EQUIPMENT / ACCESSORY / MARTIAL_ARTS_BOOK / MISSION)
- 하단: 생성/편집 폼 (인라인, 토글 가능)
  - **공통 필드**
    - 아이템 타입 선택 (드롭다운 6종)
    - 이름 (text input)
    - 설명 (textarea, 세로 크기 조절 가능)
    - 무게 (number input)
    - stackable (checkbox)
  - **타입별 전용 필드 섹션** (타입 선택 시 JS로 해당 섹션만 표시/숨김)
    - FOOD: hpRecovery(int), mpRecovery(int), apRecovery(int)
    - WEAPON: weaponType(드롭다운: SWORD/BLADE/FIST/ARCHERY/ESOTERIC/LONG_WEAPON) + StatModifiers 동적 리스트
    - EQUIPMENT: equipmentSlot(드롭다운: HELMET/UPPER_ARMOR/LOWER_ARMOR/GLOVES/BOOTS/BELT) + StatModifiers 동적 리스트
    - ACCESSORY: accessoryType(드롭다운: NECKLACE/RING) + StatModifiers 동적 리스트
    - MARTIAL_ARTS_BOOK: skillRef(text input)
    - MISSION: missionItemType(드롭다운: KEY/QUEST_COMPLETION) + targetRef(text input)
  - StatModifiers 동적 리스트: 각 행 = statType 드롭다운(15종) + value(number input) + 삭제 버튼, "스탯 추가" 버튼

### 1-2. 인스턴스 배치 관리 (`/item-instance-management`)

새 ItemInstance를 생성하고 방 바닥 또는 캐릭터 소지품에 배치한다.

**UI 구성 — 3단계 폼:**

**STEP 1 — 아이템 템플릿 검색 및 선택**
- 이름 입력 필드 + live 검색: 타이핑 시 `GET /api/v1/item-templates?name={query}` 호출
- 결과 드롭다운 표시 (타입 배지, 이름, 무게, stackable 여부)
- 선택 시 하단에 선택된 템플릿 정보 미리보기 표시

**STEP 2 — 수량**
- number input (min=1)
- stackable=false인 아이템은 1로 고정 (disabled)

**STEP 3 — 배치 위치**
- 라디오 버튼: 방(Room) / 캐릭터 소지품(Character)
- **방 선택 시**: 방 ID 직접 입력 + "확인" 버튼 → `GET /api/v1/rooms/{id}` 호출로 방 이름 조회하여 표시
- **캐릭터 선택 시**: 닉네임 입력 필드 + live 검색 → `GET /api/v1/player-characters/search?nickname={query}` 호출 → 결과에서 선택

"배치 실행" 버튼 → 성공/실패 메시지 인라인 표시

---

## 2. API 설계

### ItemTemplate API (`gamedata/adapter/webapi/ItemTemplateController`)

| Method | Path | 설명 |
|---|---|---|
| GET | `/api/v1/item-templates` | 전체 목록 (타입 필터: `?type=WEAPON`) |
| GET | `/api/v1/item-templates?name={query}` | 이름 포함 검색 (live 검색용) |
| GET | `/api/v1/item-templates/{id}` | 단건 조회 |
| POST | `/api/v1/item-templates` | 생성 |
| PUT | `/api/v1/item-templates/{id}` | 수정 |
| DELETE | `/api/v1/item-templates/{id}` | 삭제 |

**POST/PUT 요청 바디** — 타입에 따라 다른 필드 포함:

```json
{
  "itemType": "WEAPON",
  "name": "철검",
  "description": "날카로운 철로 만든 검",
  "weight": 5,
  "stackable": false,
  "weaponType": "SWORD",
  "statModifiers": [
    { "statType": "SWORD_METHOD", "value": 5 },
    { "statType": "AGILITY", "value": 2 }
  ]
}
```

FOOD 예시:
```json
{
  "itemType": "FOOD",
  "name": "만두",
  "description": "찐만두",
  "weight": 1,
  "stackable": true,
  "hpRecovery": 10,
  "mpRecovery": 0,
  "apRecovery": 5
}
```

**응답 바디** — 공통 필드 + 타입별 필드:

```json
{
  "id": 1,
  "itemType": "WEAPON",
  "name": "철검",
  "description": "...",
  "weight": 5,
  "stackable": false,
  "weaponType": "SWORD",
  "statModifiers": [...]
}
```

### ItemInstance 배치 API (`gamedata/adapter/webapi/ItemInstanceController`)

| Method | Path | 설명 |
|---|---|---|
| POST | `/api/v1/item-instances/place` | 인스턴스 생성 + 배치 |

요청 바디:
```json
{
  "templateId": 1,
  "quantity": 10,
  "locationType": "ROOM",
  "locationId": "42"
}
```
- `locationType`: `"ROOM"` 또는 `"CHARACTER"`
- `locationId`: ROOM이면 방 ID(Long), CHARACTER이면 캐릭터 UUID(String)

### 캐릭터 검색 API

| Method | Path | 설명 |
|---|---|---|
| GET | `/api/v1/player-characters/search?nickname={query}` | 닉네임 포함 검색 (배치 UI용) |

응답:
```json
[
  { "id": "uuid-...", "nickname": "홍길동" },
  { "id": "uuid-...", "nickname": "홍두깨" }
]
```

---

## 3. 백엔드 아키텍처

### UseCase 포트 (`gamedata/application/service/provided/`)

```
ItemTemplateCreator      ItemTemplate create(ItemTemplateCreateRequest)
ItemTemplateFinder       List<ItemTemplate> findAll()
                         List<ItemTemplate> findByType(ItemType)
                         List<ItemTemplate> findByNameContaining(String name)
                         ItemTemplate findById(Long)
ItemTemplateModifier     ItemTemplate update(Long id, ItemTemplateUpdateRequest)
ItemTemplateRemover      void delete(Long id)
ItemInstancePlacer       ItemInstance place(ItemInstancePlaceRequest)
```

### 서비스 (`gamedata/application/service/`)

**ItemTemplateService** — ItemTemplateCreator/Finder/Modifier/Remover 구현  
- ItemTemplateRepository 사용  
- 생성/수정 시 `itemType`에 따라 적절한 서브클래스 빌더 호출

**ItemInstanceService** — ItemInstancePlacer 구현  
- 의존성: ItemTemplateRepository, ItemInstanceRepository, RoomRepository, PlayerCharacterRepository, GameWorldService

```
place(request):
  1. ItemTemplate 조회 (by templateId)
  2. ItemInstance 생성 (new ItemInstance(template, quantity))
  3a. ROOM:
      - RoomRepository.findById(roomId) → 없으면 예외
      - room.addFloorItem(instance)
      - RoomRepository.save(room)  [DB 반영, cascade]
      - GameWorldService.getRoom(roomId).addFloorItem(instance)  [인메모리 반영]
  3b. CHARACTER:
      - PlayerCharacterRepository.findById(characterUUID) → 없으면 예외
      - character.getInventory().addItem(instance)
      - PlayerCharacterRepository.save(character)  [DB 반영]
      - GameWorldService의 activePlayers에서 동일 UUID로 조회 → inventory.addItem(instance)  [인메모리 반영]
```

### 컨트롤러

**ItemTemplateController** (`gamedata/adapter/webapi/`)
- 요청 바디: `ItemTemplateRequest` (공통 + 모든 타입별 필드를 포함하는 단일 DTO, nullable 필드)
- 응답: `ItemTemplateResponse` (동일 구조)

**ItemInstanceController** (`gamedata/adapter/webapi/`)
- 요청 바디: `ItemInstancePlaceRequest` (templateId, quantity, locationType, locationId)
- 응답: 성공 메시지 + 생성된 인스턴스 ID

**PlayerCharacterController** (`gamedata/adapter/webapi/`) — 신규 or 기존 확장
- `GET /api/v1/player-characters/search?nickname={query}` 추가
- PlayerCharacterRepository에 `List<PlayerCharacter> findByNicknameContaining(String)` 추가

### 웹 라우팅

`IndexController`에 두 매핑 추가:
```java
@GetMapping("/item-template-management")
@GetMapping("/item-instance-management")
```

---

## 4. 파일 목록

### 신규 생성

```
gamedata/application/service/provided/
  ItemTemplateCreator.java
  ItemTemplateFinder.java
  ItemTemplateModifier.java
  ItemTemplateRemover.java
  ItemInstancePlacer.java

gamedata/application/service/
  ItemTemplateService.java
  ItemInstanceService.java

gamedata/adapter/webapi/
  ItemTemplateController.java
  ItemInstanceController.java
  PlayerCharacterController.java     ← 신규 (또는 기존 파일 확장)
  request/ItemTemplateRequest.java
  request/ItemInstancePlaceRequest.java
  response/ItemTemplateResponse.java
  response/PlayerCharacterSearchResponse.java

resources/templates/web/
  item-template-management.html
  item-instance-management.html
```

### 수정

```
web/IndexController.java             ← 두 매핑 추가
resources/templates/web/admin.html  ← 카드 2개 추가
gamedata/application/service/required/PlayerCharacterRepository.java
  ← findByNicknameContaining(String) 추가
```

---

## 5. 이번 구현 범위 외

- 인스턴스 목록 조회 / 삭제 / 이동
- 페이지네이션 (템플릿이 많아지면 추가)
- 아이템 장착 관리
- 권한 제어 (현재 Admin 페이지는 로그인만 확인)
