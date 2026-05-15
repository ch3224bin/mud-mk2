# 장비 시스템 설계 (Equipment System)

작성일: 2026-05-15
상태: 설계 승인 대기

## 1. 배경 및 목표

아이템 생성·소지품·바닥 배치까지는 구현되어 있다. 이제 캐릭터가 무기·방어구·악세서리를 **착용**하고, 착용한 장비의 stat이 캐릭터에 반영되며, 전투 시 장착한 무기와 그에 맞는 무공으로 공격하는 메커니즘이 필요하다.

### 1.1 사용자 시나리오

```
> 철검 장착
철검을 장착했다.

> 야구모자 입어
야구모자를 머리에 착용했다.

> 금반지 입어
금반지를 왼손에 착용했다.

> 금반지 입어            ← 동일 아이템 한 개 더 있을 때
금반지를 오른손에 착용했다.

> 장비
[ 장비 ]
머리       : 야구모자 (+2 vigor)
...
무기       : 철검 (+5 sword_method, +2 vigor)

[ 적용 스탯 ] (base → effective)
근력(vigor)        : 10 → 14
검법(sword_method) : 3  → 8

> 다람쥐 공격
철수가 다람쥐를 철검으로 힘껏 베었다!
  → 7 데미지! (남은 HP: 13)

> 철검 벗어
철검을 해제했다.
```

### 1.2 범위

- 슬롯 모델 확장(반지 2개형 10슬롯)
- `EquippedItems` 컨테이너 (PlayerCharacter @OneToOne, Inventory와 동일 레벨)
- 장착/해제 명령어 + "장비" 조회 명령어
- Effective stat 계산 레이어 (base + 장비 statModifier 합산)
- 전투 narrative에 무기 이름·무기 타입별 동사 반영
- 전투 데미지 dice roll의 최대값(현재 1d6의 "6")을 effective 무공 stat 값으로 교체

### 1.3 범위 외 (Out of Scope)

- 무기 자체에 별도 baseDamage 필드 추가 (사용자 결정: 무기 데미지는 무공+근력/민첩 등 캐릭터 stat으로 계산)
- 방어구 별도 armorValue 필드 추가 (statModifier로만 표현)
- 명중/회피/방어 등 다른 전투 공식 변경 (사용자 결정: 추후 작업)
- NPC/몬스터의 무기 장착 (현재는 FIST 디폴트)

## 2. 도메인 모델

### 2.1 EquipmentSlot (enum 확장)

```
기존: HELMET, UPPER_ARMOR, LOWER_ARMOR, GLOVES, BOOTS, BELT
추가: WEAPON, NECKLACE, RING_LEFT, RING_RIGHT
총 10슬롯
```

한글 표시명(`displayName()` 또는 별도 매핑):
머리 / 상의 / 하의 / 장갑 / 신발 / 허리띠 / 무기 / 목걸이 / 왼손 반지 / 오른손 반지

### 2.2 EquippedItems (신규 @Entity)

```
package: gamedata.application.domain.model.player
@Entity @Table(name = "equipped_items")

필드:
  id: UUID
  slots: Map<EquipmentSlot, ItemInstance>
    @OneToMany(cascade = {PERSIST, MERGE})
    @JoinColumn(name = "equipped_items_id")
    @MapKeyEnumerated(STRING) @MapKeyColumn(name = "equipped_slot")

API:
  static create(): EquippedItems
  equip(slot, instance) → Optional<ItemInstance>  // 기존 슬롯 아이템 swap 반환
  unequip(slot)         → Optional<ItemInstance>
  getSlot(slot)         → Optional<ItemInstance>
  findByItemName(name)  → Optional<Map.Entry<EquipmentSlot, ItemInstance>>
  sumStatModifiers()    → Map<StatType, Integer>  // 모든 슬롯 합산
  initializeAssociatedEntities()    // 각 ItemInstance lazy 그래프 초기화
```

#### 반지 슬롯 자동 선택 규칙

`AccessoryType.RING` 아이템 장착 시:
1. `RING_LEFT`가 비어 있으면 그쪽에 장착
2. `RING_LEFT`가 차 있고 `RING_RIGHT`가 비어 있으면 `RING_RIGHT`에 장착
3. 둘 다 차 있으면 `RING_LEFT` 자동 교체 (swap)

### 2.3 EquippableItemTemplate (마커 인터페이스)

```
package: gamedata.application.domain.model.item
interface EquippableItemTemplate {
    List<StatModifier> getStatModifiers();
}

구현:
  WeaponTemplate, EquipmentTemplate, AccessoryTemplate
비구현(장착 불가):
  FoodTemplate, MissionItemTemplate, MartialArtsBookTemplate
```

타겟 슬롯 해석은 통일된 메서드 대신 service 레벨에서 분기 (§3.4 참조). 이유: RING은 컨텍스트(빈 슬롯) 의존이라 templates 만으로 결정 불가하므로 template 인터페이스에 `getTargetSlot()`을 두면 RING 케이스만 의미가 어색해진다. 마커 인터페이스로 "장착 가능" 여부와 statModifier 접근만 통일.

### 2.4 ItemInstance (변경)

기존 `inventory_id` FK와 동일한 패턴으로 컬럼 추가:
- `equipped_items_id` (FK, nullable)
- `equipped_slot` (enum, nullable)

invariant: 한 ItemInstance는 inventory_id / equipped_items_id / (room의 floor FK) 중 정확히 하나만 not null.

**강제 방식:** EquipCommandService / UnequipCommandService 가 항상 다음 순서를 지킨다.
- 장착: `Inventory.removeItem(instance)` → `EquippedItems.equip(slot, instance)` (Hibernate가 inventory_id를 null로, equipped_items_id를 채움)
- 해제: `EquippedItems.unequip(slot)` → `Inventory.addItem(instance)` (반대 방향)
- swap: removeItem(new) → equip(slot, new) → addItem(old)

ItemInstance 자체에 별도 상태 enum이나 가드 로직은 두지 않는다(과도한 방어 회피). 이 invariant는 service 로직과 테스트로 보장한다.

### 2.5 WeaponTypeMapping (신규 utility)

```
package: gamedata.application.domain.model.item
final class WeaponTypeMapping {
    static StatType weaponSkillFor(WeaponType):
        SWORD       → SWORD_METHOD
        BLADE       → BLADE_METHOD
        FIST        → FISTS_AND_PALMS
        ARCHERY     → ARCHERY
        ESOTERIC    → ESOTERIC_WEAPON
        LONG_WEAPON → LONG_WEAPON

    static String attackVerb(WeaponType):
        SWORD       → "베었다"
        BLADE       → "내려쳤다"
        FIST        → "내질렀다"
        ARCHERY     → "쐈다"
        LONG_WEAPON → "휘둘렀다"
        ESOTERIC    → "휘둘렀다"
}
```

### 2.6 PlayerCharacter (변경)

```
필드 추가:
  @OneToOne(cascade = ALL, orphanRemoval = true, fetch = LAZY, optional = false)
  @JoinColumn(name = "equipped_items_id")
  private EquippedItems equippedItems;

메서드 변경:
  getStats() → CharacterStats:
    base = baseCharacterInfo.getStats()
    modifiers = equippedItems.sumStatModifiers()
    return new CharacterStats(
      base.hp, base.mp, base.ap,                           // 현재 자원은 base 그대로
      base.vigor + modifiers.getOrDefault(VIGOR, 0),       // 6속성 + 9무예에만 적용
      ...
    )

메서드 추가:
  getBaseStats(): CharacterStats   // "장비" 명령 base vs effective 비교용

initializeAssociatedEntities() 보강:
  this.inventory.initializeAssociatedEntities();
  this.equippedItems.initializeAssociatedEntities();  // 추가
```

캐릭터 생성 시 `EquippedItems.create()`를 함께 부여(`CharacterCreationService` 점검).

## 3. 명령어 처리

### 3.1 CommandDictionary 확장

```
EQUIP           = ["장착", "착용", "입어", "입고", "차다", "차고", "끼다", "껴"]
UNEQUIP         = ["해제", "벗어", "벗고", "빼다", "빼"]
EQUIPMENT_VIEW  = ["장비", "장비창"]
```

### 3.2 Command 객체

```
record EquipCommand(Long userId, String itemName, int index) implements Command {}
record UnequipCommand(Long userId, String itemName) implements Command {}
record EquipmentViewCommand(Long userId) implements Command {}
```

### 3.3 Parser

```
EquipCommandParser
  Pattern: (\S+?)(?:\s+(\d+))?\s+(장착|착용|입어|입고|차다|차고|끼다|껴)
  index 미지정 시 1

UnequipCommandParser
  Pattern: (\S+?)\s+(해제|벗어|벗고|빼다|빼)

EquipmentViewCommandParser
  Pattern: (장비|장비창)
```

### 3.4 Use Case / Service

```
interface EquipUseCase             { void equip(EquipCommand); }
interface UnequipUseCase           { void unequip(UnequipCommand); }
interface EquipmentViewUseCase     { void showEquipment(EquipmentViewCommand); }

EquipCommandService.equip(cmd):
  1. player = ActivePlayerRepository.findByUserId(userId).orElseThrow(...)
  2. instance = player.inventory.findItemsByName(itemName).get(index - 1)
     없으면: "X을(를) 가지고 있지 않습니다."
  3. template = instance.template
     EquippableItemTemplate 아니면: "X은(는) 장착할 수 없습니다."
  4. targetSlot 결정:
     WeaponTemplate    → WEAPON
     EquipmentTemplate → template.equipmentSlot
     AccessoryTemplate(NECKLACE) → NECKLACE
     AccessoryTemplate(RING):
       RING_LEFT 비어있으면 → RING_LEFT
       RING_LEFT 차고 RING_RIGHT 비어있으면 → RING_RIGHT
       둘 다 차 있으면 → RING_LEFT (swap 발생)
  5. swap 가능 여부 사전 검사:
     기존 슬롯에 oldItem 있으면 → inventory.canAdd(oldItem.template, oldItem.quantity) 검사
     실패 시: "X을(를) 장착하기 위해 Y을(를) 해제해야 하지만 소지품 무게가 부족합니다."
  6. oldItem = player.equippedItems.equip(targetSlot, instance)
  7. player.inventory.removeItem(instance)
  8. oldItem.ifPresent(player.inventory::addItem)
  9. 메시지:
     swap 없으면: "X을(를) 장착했다."
     swap 있으면: "기존 Y을(를) 해제하고 X을(를) 장착했다."

UnequipCommandService.unequip(cmd):
  1. player = ...
  2. entry = player.equippedItems.findByItemName(itemName)
     없으면: "X을(를) 장착하고 있지 않습니다."
  3. inventory.canAdd 검사:
     실패 시: "소지품 무게가 부족해 해제할 수 없습니다."
  4. instance = player.equippedItems.unequip(entry.getKey())
  5. player.inventory.addItem(instance)
  6. 메시지: "X을(를) 해제했다."

EquipmentViewCommandService.showEquipment(cmd):
  player.equippedItems 슬롯 10개 순회 → 슬롯 이름 + 아이템 이름 + statModifier 나열
  base vs effective stat 차이가 있는 stat만 추가 섹션에 표시 (hp/mp/ap 제외)
```

### 3.5 무게 초과 처리

**모든 경우 거부(reject).** 사용자 결정: 강제 진행이나 바닥 떨어뜨림 처리 없음.

## 4. 전투 통합

### 4.1 CombatLog 확장

```
필드 추가:
  String weaponName  // 무기 이름 (예: "철검", "맨손")
기존 weaponTypeName은 유지(호환), narrative에서는 weaponName 사용
```

### 4.2 Combat.executeGroupAction() 변경

```
공격자 무기·무공 결정 로직 (PlayerCharacter):
  weaponInstance = attacker.equippedItems.getSlot(WEAPON).orElse(null)
  if (weaponInstance != null):
    weaponTemplate = (WeaponTemplate) weaponInstance.template
    weaponType     = weaponTemplate.weaponType
    weaponName     = weaponTemplate.name
  else:
    weaponType = FIST
    weaponName = "맨손"

  skillType   = WeaponTypeMapping.weaponSkillFor(weaponType)
  skillValue  = attacker.getStats().<skillType>()    // effective stat에서 조회

Monster / NPC (무기 시스템 미지원):
  weaponType = FIST, weaponName = "맨손"
  skillValue = stats.fistsAndPalms()

데미지 계산 변경:
  baseDamage = diceRoller.roll(1, max(1, skillValue))   // 1d6의 6을 effective 무공 stat 값으로 교체
  damageModifier, damageTotal, defenseValue(=10), finalDamage 공식은 placeholder 유지

CombatLog 생성 시 weaponName, weaponTypeName 모두 채움
```

### 4.3 CombatNarrativeFormatter 변경

```
포맷:
  evaded:    "%s이(가) %s을(를) %s(으)로 휘둘렀지만 피했다!" — 간단한 회피 메시지 유지, 동사는 attackVerb 사용
  crit:      "%s이(가) %s을(를) %s(으)로 치명적으로 %s!"
  normal:    "%s이(가) %s을(를) %s(으)로 힘껏 %s!"

%s 순서: 공격자 / 대상 / 무기이름 / 무기타입별 동사

예시:
  "철수이(가) 다람쥐을(를) 철검(으)로 힘껏 베었다!"
  "철수이(가) 다람쥐을(를) 맨손(으)로 힘껏 내질렀다!"
  "철수이(가) 곰을(를) 활(으)로 힘껏 쐈다!"
```

조사 처리(이/가, 을/를, 으로/로)는 기존 코드 패턴(`이(가)`, `을(를)`) 유지.

## 5. "장비" 조회 명령 출력

```
─────────────────────────────────
[ 장비 ]
머리       : 야구모자 (+2 vigor)
상의       : (없음)
하의       : 반바지 (+1 agility)
장갑       : (없음)
신발       : (없음)
허리띠     : (없음)
목걸이     : (없음)
왼손 반지  : 금반지 (+3 inner_power)
오른손 반지: (없음)
무기       : 철검 (+5 sword_method, +2 vigor)

[ 적용 스탯 ] (base → effective)
근력(vigor)        : 10 → 14
민첩(agility)      : 8  → 9
검법(sword_method) : 3  → 8
내공(inner_power)  : 5  → 8
─────────────────────────────────
```

- 슬롯이 비면 `(없음)`
- 아이템별 statModifier 나열
- "적용 스탯"은 base ≠ effective 인 stat만 노출 (hp/mp/ap 제외)

## 6. DB 스키마

### 6.1 신규 / 변경 (운영 서비스 아님 — 수동 적용)

```
equipped_items
  id            UUID PK
  -- player와 1:1, FK는 player_character.equipped_items_id 측에 보유

item_instance (변경)
  + equipped_items_id  UUID NULL FK → equipped_items(id)
  + equipped_slot      VARCHAR NULL  -- EquipmentSlot enum

player_character (변경)
  + equipped_items_id  UUID FK → equipped_items(id)
```

### 6.2 매핑 결정 (질문 응답)

ItemInstance에 FK 직접 추가(A) 채택. Inventory의 기존 `@JoinColumn("inventory_id")` 패턴과 동일.

### 6.3 invariant

한 ItemInstance는 inventory_id / equipped_items_id / 향후 추가될 컨테이너 FK 중 정확히 하나만 not null. **도메인 service 레벨에서 강제** (DB 체크 제약은 적용하지 않음).

## 7. 테스트 전략

### 유닛 테스트

- `EquippedItemsTest`: equip / unequip, swap, findByItemName, sumStatModifiers, 반지 자동 슬롯 선택
- `PlayerCharacterTest`: getStats() effective 반환, getBaseStats() 분리, initializeAssociatedEntities() 보강
- `WeaponTypeMappingTest`: 6 WeaponType × StatType + attackVerb 분기
- `EquipCommandServiceTest`: 정상 / 슬롯 교체 / 비장착 아이템 거부 / 무게 초과 거부 / 인벤토리 없음
- `UnequipCommandServiceTest`: 정상 / 미장착 거부 / 무게 초과 거부
- `EquipmentViewCommandServiceTest`: 빈 슬롯 / 모든 슬롯 채움 / base ≠ effective stat 행
- `EquipCommandParserTest`, `UnequipCommandParserTest`, `EquipmentViewCommandParserTest`: alias·index·동사 매칭
- `CombatNarrativeFormatterTest`: 6 동사 분기 × (evaded / crit / normal)
- `CombatTest` (기존 보강): 장착 무기로 narrative weaponName, dice max = effective 무공 stat, 맨손 시 FIST·FISTS_AND_PALMS

### 통합 시나리오

캐릭터 생성 → 아이템 생성·인벤토리 추가 → 장착 → 장비 조회 → 공격(narrative 확인) → 해제 → 인벤토리 복귀.

## 8. 영향 영역

- `InMemoryPlayerRepository`: 캐시 적재 시 `equippedItems.initializeAssociatedEntities()` 호출 추가 (PlayerCharacter.initializeAssociatedEntities 내부에서 호출되므로 자동 반영)
- `CharacterCreationService`: 신규 캐릭터에 `EquippedItems.create()` 부여
- `ItemTemplate` 계열: `EquippableItemTemplate` 인터페이스 구현 추가 (Weapon / Equipment / Accessory). 비장착 템플릿(Food / MissionItem / MartialArtsBook)은 미구현
- `Combat.java`: 공격자/대상 무기 결정 로직 + dice roll max = skillValue로 변경
- `CombatLog`: weaponName 필드 추가
- `CombatNarrativeFormatter`: 무기 타입별 동사 분기

영향 없음(확인 필요):
- `ItemSearchStrategy` / `ItemDescriber` / `ItemDisplayLabels` (인벤토리·바닥만 조회)
- `InventoryCommandService` (소지품 명령은 변경 없음)

## 9. 핵심 결정 사항 요약

| 항목 | 결정 |
|------|------|
| 슬롯 구성 | 반지 2개형 10슬롯 (RING_LEFT, RING_RIGHT) |
| 장착 라이프사이클 | 인벤토리에서 제거, 슬롯에만 존재 |
| Stat 적용 방식 | Effective stat 계산 레이어 (getStats() 마다 합산) |
| 무기 baseDamage 필드 | 추가하지 않음 — dice roll의 6을 effective 무공 stat으로 교체 |
| 맨손 공격 | WeaponType.FIST + FISTS_AND_PALMS 적용 |
| WeaponType → 무공 매핑 | 기본 매핑 (SWORD→SWORD_METHOD 등) |
| 무기 타입별 동사 | 베다 / 내려치다 / 내지르다 / 쏘다 / 휘두르다(장병·기문) |
| 슬롯 충돌 | 자동 해제 후 교체 |
| 명령 alias | EQUIP / UNEQUIP / EQUIPMENT_VIEW 단일 명령 + 다중 alias |
| 무게 초과 | 모든 경우 거부 |
| "장비" alias | "장비", "장비창" 2개만 |
| Stat·전투 공식 변경 범위 | dice max만 effective 무공 stat 으로 교체, 다른 공식 placeholder 유지 |
| EquippedItems 위치 | PlayerCharacter @OneToOne (Inventory 와 동일 레벨) |
| 스키마 매핑 | ItemInstance에 FK 직접 추가 (A안) |
