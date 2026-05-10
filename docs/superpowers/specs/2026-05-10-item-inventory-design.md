# 아이템 & 소지품 시스템 설계

**날짜**: 2026-05-10  
**브랜치**: feature/item-inventory

---

## 개요

아이템과 소지품(인벤토리) 시스템을 추가한다. 아이템은 타입별로 관리되는 속성이 다르며, 방 바닥이나 캐릭터 소지품 안에 존재할 수 있다.

---

## 1. ItemTemplate 계층 구조

JPA `JOINED` 상속 전략을 사용한다. 서브클래스마다 별도 테이블을 가진다.

### 공통 필드 (item_template 테이블)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | PK |
| name | String | 아이템 이름 |
| description | String | 설명 |
| weight | int | 무게 (캐릭터 무게 한도 계산에 사용) |
| itemType | ItemType | 아이템 타입 구분자 |
| stackable | boolean | 스택 가능 여부 |

### 서브클래스 및 타입별 필드

**FoodTemplate** (`food_template`)
- `hpRecovery`: int — HP 회복량
- `mpRecovery`: int — MP 회복량
- `apRecovery`: int — AP 회복량
- 버프 기능은 미래 확장을 위해 예약 (현재 미구현)

**WeaponTemplate** (`weapon_template`)
- `weaponType`: WeaponType enum — SWORD(검) | BLADE(도) | FIST(권) | ARCHERY(사술) | ESOTERIC(기문) | LONG_WEAPON(장병기)
- `statModifiers`: List\<StatModifier\> — 하나 이상의 스탯 보너스

**EquipmentTemplate** (`equipment_template`)
- `equipmentSlot`: EquipmentSlot enum — HELMET(투구) | UPPER_ARMOR(갑옷 상의) | LOWER_ARMOR(갑옷 하의) | GLOVES(장갑) | BOOTS(신발) | BELT(허리띠)
- `statModifiers`: List\<StatModifier\>

**AccessoryTemplate** (`accessory_template`)
- `accessoryType`: AccessoryType enum — NECKLACE(목걸이) | RING(반지)
- `statModifiers`: List\<StatModifier\>

**MartialArtsBookTemplate** (`martial_arts_book_template`)
- `skillRef`: String — 무공 시스템 연결용 식별자 (무공 시스템 미구현으로 확장 예약)

**MissionItemTemplate** (`mission_item_template`)
- `missionItemType`: MissionItemType enum — KEY | QUEST_COMPLETION
- `targetRef`: String — 열 수 있는 문 ID, 퀘스트 ID 등

### StatModifier (@Embeddable)

```
StatModifier {
  statType: StatType  // VIGOR, PHYSIQUE, AGILITY, INTELLECT, WILL, MERIDIAN,
                      // INNER_POWER, SPECIAL_TECHNIQUE, LIGHT_STEP,
                      // FISTS_AND_PALMS, SWORD_METHOD, BLADE_METHOD,
                      // LONG_WEAPON, ESOTERIC_WEAPON, ARCHERY
  value: int
}
```

`CharacterStats`의 모든 스탯을 `StatType` enum으로 커버한다.

---

## 2. ItemInstance & 위치 관리

### ItemInstance

```
ItemInstance {
  id: Long
  template: ItemTemplate (FK)
  quantity: int  // stackable=false인 아이템은 항상 1
}
```

아이템 위치는 ItemInstance 자체에 없다. 어느 컬렉션에 속하는지로 결정된다.

### 위치 컬렉션

**Room** (기존 엔티티에 추가)
```
Room {
  ...기존 필드...
  List<ItemInstance> floorItems  // 바닥 아이템
}
```

**Inventory** (신규 엔티티)
```
Inventory {
  id: UUID
  maxWeightCapacity: int
  List<ItemInstance> items
}
```

**PlayerCharacter** (기존 엔티티에 추가)
```
PlayerCharacter {
  ...기존 필드...
  Inventory inventory  // @OneToOne
}
```

### Inventory 도메인 로직

```java
// 무게 검증
public boolean canAdd(ItemTemplate template, int qty) {
    return currentWeight() + template.getWeight() * qty <= maxWeightCapacity;
}

public int currentWeight() {
    return items.stream()
        .mapToInt(i -> i.getTemplate().getWeight() * i.getQuantity())
        .sum();
}

// 스택 처리: stackable 아이템은 기존 ItemInstance의 quantity 증가
// 비stackable 아이템은 새 ItemInstance 추가
public void addItem(ItemInstance instance) { ... }
```

### 아이템 이동 흐름 (방 → 소지품)

1. `room.floorItems`에서 이름으로 ItemInstance 목록 필터링
2. N번째 항목 선택 (기본 1번째)
3. `inventory.canAdd()` 무게 검증
4. `room.removeFloorItem(instance)` → `inventory.addItem(instance)`

---

## 3. 커맨드

기존 커맨드 체인(Chain of Responsibility) 패턴을 따른다.

### 입력 패턴

| 입력 예시 | 커맨드 클래스 | 파싱 패턴 |
|---|---|---|
| `철검 주워` | `TakeCommand` | `(\S+)( \d+)? (주워\|줍다\|...)` |
| `철검 2 주워` | `TakeCommand` | 동일, index=2 |
| `철검 버려` | `DropCommand` | `(\S+)( \d+)? (버려\|버리다\|...)` |
| `철검 2 버려` | `DropCommand` | 동일, index=2 |
| `소지품` / `가방` | `InventoryCommand` | `소지품\|가방` |

`TakeCommand`, `DropCommand`는 `itemName: String`과 `index: int`(기본값 1)를 필드로 갖는다.

### 파일 추가 목록

**커맨드 모델** (`gameplay.application.domain.model.command`)
- `TakeCommand` (stub 존재, 내용 구현)
- `DropCommand`
- `InventoryCommand`

**파서** (`gameplay.adapter.in.eventlistener.parser`)
- `TakeCommandParser`
- `DropCommandParser`
- `InventoryCommandParser`

**익스큐터** (`gameplay.adapter.in.eventlistener.executor`)
- `TakeCommandExecutor`
- `DropCommandExecutor`
- `InventoryCommandExecutor`

**CommandDictionary** enum에 TAKE, DROP, INVENTORY 항목 추가

---

## 4. 모듈 배치

```
gamedata.application.domain.model.item
├── ItemTemplate          (abstract, @Entity, @Inheritance JOINED)
├── FoodTemplate
├── WeaponTemplate
├── EquipmentTemplate
├── AccessoryTemplate
├── MartialArtsBookTemplate
├── MissionItemTemplate
├── ItemInstance
├── StatModifier          (@Embeddable)
├── ItemType              (enum: FOOD, WEAPON, EQUIPMENT, ACCESSORY, MARTIAL_ARTS_BOOK, MISSION)
├── WeaponType            (enum)
├── EquipmentSlot         (enum)
├── AccessoryType         (enum)
├── MissionItemType       (enum)
└── StatType              (enum)

gamedata.application.domain.model.player
└── Inventory             (신규 @Entity)

gamedata.application.domain.model.map
└── Room                  (floorItems 컬렉션 추가)
```

---

## 5. 이번 구현 범위 외

- 아이템 장착/해제 (착용, 해제 커맨드)
- 무공비급 사용 (무공 시스템 미구현)
- 음식 버프 효과
- 아이템 관리 웹 UI (Admin)
- 아이템 제작 시스템
