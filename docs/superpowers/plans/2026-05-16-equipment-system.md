# 장비 시스템 (Equipment System) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 캐릭터가 무기·방어구·악세서리를 10개 슬롯에 장착/해제하고, 장비 stat이 effective stat으로 반영되며, 전투 시 장착한 무기의 이름·무공 stat이 데미지·narrative에 적용되도록 구현한다.

**Architecture:** PlayerCharacter @OneToOne으로 `EquippedItems`(Inventory와 동일 레벨)를 두고, 각 슬롯에 `ItemInstance`를 매핑한다. ItemInstance에 `equipped_items_id` + `equipped_slot` FK 컬럼을 추가해 Inventory와 같은 단방향 매핑 패턴을 따른다. effective stat은 `PlayerCharacter.getStats()`가 매 호출 시 base + 장비 statModifier를 합산해 반환. 전투 데미지의 dice max(현재 1d6의 "6")를 effective 무공 stat 값으로 교체한다. 다른 전투 공식은 placeholder 유지.

**Tech Stack:** Java 25, Spring Boot 4.0.3, JPA/Hibernate, Lombok, JUnit 5, AssertJ, Mockito, Gradle.

**Spec:** [docs/superpowers/specs/2026-05-15-equipment-system-design.md](../specs/2026-05-15-equipment-system-design.md)

---

## File Structure

### Create
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquippableItemTemplate.java` — 마커 인터페이스
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTypeMapping.java` — WeaponType ↔ StatType / 동사 utility
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/EquippedItems.java` — @Entity, 슬롯 매핑 컨테이너
- `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/EquipCommand.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/UnequipCommand.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/EquipmentViewCommand.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EquipCommandParser.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/UnequipCommandParser.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EquipmentViewCommandParser.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/EquipCommandExecutor.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/UnequipCommandExecutor.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/EquipmentViewCommandExecutor.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/EquipUseCase.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/UnequipUseCase.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/EquipmentViewUseCase.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/EquipCommandService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/UnequipCommandService.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/EquipmentViewCommandService.java`
- 각 위 클래스의 테스트 파일 (대응 경로 `src/test/...`)

### Modify
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentSlot.java` — WEAPON, NECKLACE, RING_LEFT, RING_RIGHT 추가 + 한글 displayName
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTemplate.java` — `implements EquippableItemTemplate`
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentTemplate.java` — `implements EquippableItemTemplate`
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/AccessoryTemplate.java` — `implements EquippableItemTemplate`
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstance.java` — `equipped_items_id`, `equipped_slot` 컬럼
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java` — equippedItems 필드, getStats() effective, getBaseStats() 추가, initializeAssociatedEntities() 보강, 생성자 시그니처 변경
- `src/main/java/com/jefflife/mudmk2/gamedata/application/service/PlayerCharacterService.java` — `createCharacter()`에서 `EquippedItems.create()` 부여
- `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionary.java` — EQUIP, UNEQUIP, EQUIPMENT_VIEW 엔트리
- `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatLog.java` — weaponName 필드 추가
- `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/Combat.java` — 무기/스킬 결정 로직 + dice max = effective 무공 stat
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatter.java` — 무기 이름·무기 타입별 동사

---

## 공통 사항

**테스트 실행 명령:**
- 단일 테스트: `./gradlew test --tests "com.jefflife.mudmk2.<package>.<ClassName>"`
- 단일 메서드: `./gradlew test --tests "com.jefflife.mudmk2.<package>.<ClassName>.<methodName>"`
- 전체: `./gradlew test`

**명명 규칙:** 테스트 메서드 명은 `methodName_condition_expectedBehavior` 패턴(예: `parse_itemNameOnly_returnsFirstIndex`).

**커밋 메시지 prefix:** `feat(equip):`, `feat(combat):`, `test(equip):`, `refactor(...)` 등.

---

## Task 1: EquipmentSlot enum 확장 + displayName

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentSlot.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentSlotTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

Create `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentSlotTest.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EquipmentSlotTest {

    @Test
    void enum_containsAllTenSlots() {
        assertThat(EquipmentSlot.values()).containsExactlyInAnyOrder(
                EquipmentSlot.HELMET, EquipmentSlot.UPPER_ARMOR, EquipmentSlot.LOWER_ARMOR,
                EquipmentSlot.GLOVES, EquipmentSlot.BOOTS, EquipmentSlot.BELT,
                EquipmentSlot.WEAPON, EquipmentSlot.NECKLACE,
                EquipmentSlot.RING_LEFT, EquipmentSlot.RING_RIGHT
        );
    }

    @Test
    void displayName_returnsKoreanLabel() {
        assertThat(EquipmentSlot.HELMET.displayName()).isEqualTo("머리");
        assertThat(EquipmentSlot.UPPER_ARMOR.displayName()).isEqualTo("상의");
        assertThat(EquipmentSlot.LOWER_ARMOR.displayName()).isEqualTo("하의");
        assertThat(EquipmentSlot.GLOVES.displayName()).isEqualTo("장갑");
        assertThat(EquipmentSlot.BOOTS.displayName()).isEqualTo("신발");
        assertThat(EquipmentSlot.BELT.displayName()).isEqualTo("허리띠");
        assertThat(EquipmentSlot.WEAPON.displayName()).isEqualTo("무기");
        assertThat(EquipmentSlot.NECKLACE.displayName()).isEqualTo("목걸이");
        assertThat(EquipmentSlot.RING_LEFT.displayName()).isEqualTo("왼손 반지");
        assertThat(EquipmentSlot.RING_RIGHT.displayName()).isEqualTo("오른손 반지");
    }
}
```

- [ ] **Step 2: 테스트 실행하여 실패 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.EquipmentSlotTest"`
Expected: COMPILE FAIL — `displayName()` 메서드 없음, WEAPON/NECKLACE/RING_LEFT/RING_RIGHT 심볼 없음.

- [ ] **Step 3: EquipmentSlot 수정**

Replace `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentSlot.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

public enum EquipmentSlot {
    HELMET("머리"),
    UPPER_ARMOR("상의"),
    LOWER_ARMOR("하의"),
    GLOVES("장갑"),
    BOOTS("신발"),
    BELT("허리띠"),
    WEAPON("무기"),
    NECKLACE("목걸이"),
    RING_LEFT("왼손 반지"),
    RING_RIGHT("오른손 반지");

    private final String displayName;

    EquipmentSlot(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
```

- [ ] **Step 4: 테스트 실행하여 통과 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.EquipmentSlotTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentSlot.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentSlotTest.java
git commit -m "feat(equip): EquipmentSlot 4개 추가(WEAPON, NECKLACE, RING_LEFT, RING_RIGHT) + 한글 displayName"
```

---

## Task 2: WeaponTypeMapping utility

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTypeMapping.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTypeMappingTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

Create `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTypeMappingTest.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class WeaponTypeMappingTest {

    @Test
    void weaponSkillFor_mapsAllWeaponTypes() {
        assertThat(WeaponTypeMapping.weaponSkillFor(WeaponType.SWORD)).isEqualTo(StatType.SWORD_METHOD);
        assertThat(WeaponTypeMapping.weaponSkillFor(WeaponType.BLADE)).isEqualTo(StatType.BLADE_METHOD);
        assertThat(WeaponTypeMapping.weaponSkillFor(WeaponType.FIST)).isEqualTo(StatType.FISTS_AND_PALMS);
        assertThat(WeaponTypeMapping.weaponSkillFor(WeaponType.ARCHERY)).isEqualTo(StatType.ARCHERY);
        assertThat(WeaponTypeMapping.weaponSkillFor(WeaponType.ESOTERIC)).isEqualTo(StatType.ESOTERIC_WEAPON);
        assertThat(WeaponTypeMapping.weaponSkillFor(WeaponType.LONG_WEAPON)).isEqualTo(StatType.LONG_WEAPON);
    }

    @Test
    void attackVerb_returnsKoreanVerbPerWeaponType() {
        assertThat(WeaponTypeMapping.attackVerb(WeaponType.SWORD)).isEqualTo("베었다");
        assertThat(WeaponTypeMapping.attackVerb(WeaponType.BLADE)).isEqualTo("내려쳤다");
        assertThat(WeaponTypeMapping.attackVerb(WeaponType.FIST)).isEqualTo("내질렀다");
        assertThat(WeaponTypeMapping.attackVerb(WeaponType.ARCHERY)).isEqualTo("쐈다");
        assertThat(WeaponTypeMapping.attackVerb(WeaponType.LONG_WEAPON)).isEqualTo("휘둘렀다");
        assertThat(WeaponTypeMapping.attackVerb(WeaponType.ESOTERIC)).isEqualTo("휘둘렀다");
    }
}
```

- [ ] **Step 2: 테스트 실행하여 실패 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponTypeMappingTest"`
Expected: COMPILE FAIL.

- [ ] **Step 3: WeaponTypeMapping 작성**

Create `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTypeMapping.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

public final class WeaponTypeMapping {

    private WeaponTypeMapping() {}

    public static StatType weaponSkillFor(WeaponType type) {
        return switch (type) {
            case SWORD -> StatType.SWORD_METHOD;
            case BLADE -> StatType.BLADE_METHOD;
            case FIST -> StatType.FISTS_AND_PALMS;
            case ARCHERY -> StatType.ARCHERY;
            case ESOTERIC -> StatType.ESOTERIC_WEAPON;
            case LONG_WEAPON -> StatType.LONG_WEAPON;
        };
    }

    public static String attackVerb(WeaponType type) {
        return switch (type) {
            case SWORD -> "베었다";
            case BLADE -> "내려쳤다";
            case FIST -> "내질렀다";
            case ARCHERY -> "쐈다";
            case LONG_WEAPON, ESOTERIC -> "휘둘렀다";
        };
    }
}
```

- [ ] **Step 4: 테스트 실행하여 통과 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponTypeMappingTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTypeMapping.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTypeMappingTest.java
git commit -m "feat(equip): WeaponTypeMapping — 무기 타입별 무공 스탯·공격 동사 매핑"
```

---

## Task 3: EquippableItemTemplate 마커 인터페이스 + Weapon/Equipment/Accessory 적용

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquippableItemTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/AccessoryTemplate.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquippableItemTemplateTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

Create `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquippableItemTemplateTest.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class EquippableItemTemplateTest {

    @Test
    void weaponTemplate_isEquippable() {
        WeaponTemplate w = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 2)))
                .build();
        assertThat(w).isInstanceOf(EquippableItemTemplate.class);
        assertThat(((EquippableItemTemplate) w).getStatModifiers())
                .containsExactly(new StatModifier(StatType.VIGOR, 2));
    }

    @Test
    void equipmentTemplate_isEquippable() {
        EquipmentTemplate e = EquipmentTemplate.builder()
                .name("야구모자").description("d").weight(1).stackable(false)
                .equipmentSlot(EquipmentSlot.HELMET)
                .statModifiers(List.of(new StatModifier(StatType.AGILITY, 1)))
                .build();
        assertThat(e).isInstanceOf(EquippableItemTemplate.class);
        assertThat(((EquippableItemTemplate) e).getStatModifiers())
                .containsExactly(new StatModifier(StatType.AGILITY, 1));
    }

    @Test
    void accessoryTemplate_isEquippable() {
        AccessoryTemplate a = AccessoryTemplate.builder()
                .name("금반지").description("d").weight(1).stackable(false)
                .accessoryType(AccessoryType.RING)
                .statModifiers(List.of(new StatModifier(StatType.INNER_POWER, 3)))
                .build();
        assertThat(a).isInstanceOf(EquippableItemTemplate.class);
        assertThat(((EquippableItemTemplate) a).getStatModifiers())
                .containsExactly(new StatModifier(StatType.INNER_POWER, 3));
    }

    @Test
    void foodTemplate_isNotEquippable() {
        FoodTemplate f = FoodTemplate.builder()
                .name("만두").description("d").weight(1).stackable(true)
                .hpRecovery(0).mpRecovery(0).apRecovery(50).build();
        assertThat(f).isNotInstanceOf(EquippableItemTemplate.class);
    }
}
```

- [ ] **Step 2: 테스트 실행하여 실패 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.EquippableItemTemplateTest"`
Expected: COMPILE FAIL — `EquippableItemTemplate` 미존재.

- [ ] **Step 3: 인터페이스 생성**

Create `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquippableItemTemplate.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import java.util.List;

/**
 * Marker interface for ItemTemplates that can be equipped.
 * Equipped templates expose their stat modifiers; target slot resolution is delegated
 * to the equip service because RING accessories choose between RING_LEFT/RING_RIGHT
 * based on current EquippedItems state.
 */
public interface EquippableItemTemplate {
    List<StatModifier> getStatModifiers();
}
```

- [ ] **Step 4: WeaponTemplate 변경**

Edit `WeaponTemplate.java` — `extends ItemTemplate` 옆에 `implements EquippableItemTemplate` 추가:

```java
public class WeaponTemplate extends ItemTemplate implements EquippableItemTemplate {
```

(`getStatModifiers()`는 Lombok `@Getter`가 이미 생성하므로 추가 변경 불필요.)

- [ ] **Step 5: EquipmentTemplate 변경**

Edit `EquipmentTemplate.java`:

```java
public class EquipmentTemplate extends ItemTemplate implements EquippableItemTemplate {
```

- [ ] **Step 6: AccessoryTemplate 변경**

Edit `AccessoryTemplate.java`:

```java
public class AccessoryTemplate extends ItemTemplate implements EquippableItemTemplate {
```

- [ ] **Step 7: 테스트 실행하여 통과 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.EquippableItemTemplateTest"`
Expected: PASS (4 tests).

또한 회귀 확인:
Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.*"`
Expected: 모든 기존 item 도메인 테스트 통과.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquippableItemTemplate.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTemplate.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentTemplate.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/AccessoryTemplate.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquippableItemTemplateTest.java
git commit -m "feat(equip): EquippableItemTemplate 마커 인터페이스 + Weapon/Equipment/Accessory 적용"
```

---

## Task 4: ItemInstance 슬롯 FK 컬럼 추가

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstance.java`

본 task에서는 단지 두 컬럼만 추가한다. EquippedItems가 이를 활용하는 매핑은 Task 5에서 설정한다. ItemInstance 자체 동작 변화 없음 → 신규 테스트 없음.

- [ ] **Step 1: ItemInstance 수정**

Edit `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstance.java` — class body에 컬럼 추가:

```java
@Entity
@Table(name = "item_instance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ItemTemplate template;

    private int quantity;

    // EquippedItems → ItemInstance 단방향 매핑(@JoinColumn + @MapKeyEnumerated)을 위한 컬럼.
    // 직접 setter 노출하지 않음 — EquippedItems의 @OneToMany 관계가 채워 넣는다.
    @Column(name = "equipped_items_id", insertable = false, updatable = false)
    private java.util.UUID equippedItemsId;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipped_slot", insertable = false, updatable = false)
    private EquipmentSlot equippedSlot;

    public ItemInstance(ItemTemplate template, int quantity) {
        this.template = template;
        this.quantity = quantity;
    }

    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    public void initializeAssociatedEntities() {
        this.template.initializeAssociatedEntities();
    }
}
```

`insertable = false, updatable = false` 로 둬서 EquippedItems의 @OneToMany 측이 컬럼을 관리하도록 한다(이중 매핑 충돌 회피, Inventory의 inventory_id 컬럼을 ItemInstance가 노출하지 않는 패턴과 동일하게 단순함을 유지).

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: 기존 ItemInstance 테스트 회귀 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstanceTest"`
Expected: 기존 테스트 통과.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstance.java
git commit -m "feat(equip): ItemInstance에 equipped_items_id, equipped_slot 컬럼 추가"
```

---

## Task 5: EquippedItems 도메인 모델

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/EquippedItems.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/EquippedItemsTest.java`

EquippedItems는 슬롯→ItemInstance 매핑 컨테이너. 도메인 메서드:
- `equip(slot, instance)` → 기존 슬롯 아이템 swap 반환 `Optional<ItemInstance>`
- `unequip(slot)` → `Optional<ItemInstance>`
- `getSlot(slot)` → `Optional<ItemInstance>`
- `findByItemName(name)` → `Optional<Map.Entry<EquipmentSlot, ItemInstance>>`
- `sumStatModifiers()` → `Map<StatType, Integer>`
- `pickRingSlotFor(...)` → 반지 자동 슬롯 선택 (left first; 둘 다 차면 LEFT swap)
- `initializeAssociatedEntities()`

- [ ] **Step 1: 실패하는 테스트 작성 (1/4 — 기본 equip/unequip)**

Create `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/EquippedItemsTest.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EquippedItemsTest {

    private EquippedItems equipped;
    private WeaponTemplate swordTemplate;
    private EquipmentTemplate helmetTemplate;
    private AccessoryTemplate ringTemplate;

    @BeforeEach
    void setUp() {
        equipped = EquippedItems.create();
        swordTemplate = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 2)))
                .build();
        helmetTemplate = EquipmentTemplate.builder()
                .name("야구모자").description("d").weight(1).stackable(false)
                .equipmentSlot(EquipmentSlot.HELMET)
                .statModifiers(List.of(new StatModifier(StatType.AGILITY, 1)))
                .build();
        ringTemplate = AccessoryTemplate.builder()
                .name("금반지").description("d").weight(1).stackable(false)
                .accessoryType(AccessoryType.RING)
                .statModifiers(List.of(new StatModifier(StatType.INNER_POWER, 3)))
                .build();
    }

    @Test
    void equip_emptySlot_storesInstance_returnsEmpty() {
        ItemInstance sword = new ItemInstance(swordTemplate, 1);
        Optional<ItemInstance> prev = equipped.equip(EquipmentSlot.WEAPON, sword);
        assertThat(prev).isEmpty();
        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).contains(sword);
    }

    @Test
    void equip_occupiedSlot_swapsAndReturnsPrevious() {
        ItemInstance sword1 = new ItemInstance(swordTemplate, 1);
        ItemInstance sword2 = new ItemInstance(swordTemplate, 1);
        equipped.equip(EquipmentSlot.WEAPON, sword1);
        Optional<ItemInstance> prev = equipped.equip(EquipmentSlot.WEAPON, sword2);
        assertThat(prev).contains(sword1);
        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).contains(sword2);
    }

    @Test
    void unequip_occupiedSlot_returnsInstance_andClears() {
        ItemInstance helmet = new ItemInstance(helmetTemplate, 1);
        equipped.equip(EquipmentSlot.HELMET, helmet);
        Optional<ItemInstance> removed = equipped.unequip(EquipmentSlot.HELMET);
        assertThat(removed).contains(helmet);
        assertThat(equipped.getSlot(EquipmentSlot.HELMET)).isEmpty();
    }

    @Test
    void unequip_emptySlot_returnsEmpty() {
        assertThat(equipped.unequip(EquipmentSlot.HELMET)).isEmpty();
    }
}
```

- [ ] **Step 2: 테스트 실행하여 실패 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.EquippedItemsTest"`
Expected: COMPILE FAIL — `EquippedItems` 미존재.

- [ ] **Step 3: EquippedItems 최소 구현**

Create `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/EquippedItems.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.EquipmentSlot;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.EquippableItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "equipped_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquippedItems {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "equipped_items_id")
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "equipped_slot")
    private Map<EquipmentSlot, ItemInstance> slots = new EnumMap<>(EquipmentSlot.class);

    private EquippedItems(Map<EquipmentSlot, ItemInstance> slots) {
        this.slots = slots;
    }

    public static EquippedItems create() {
        return new EquippedItems(new EnumMap<>(EquipmentSlot.class));
    }

    public Optional<ItemInstance> equip(EquipmentSlot slot, ItemInstance instance) {
        ItemInstance previous = slots.put(slot, instance);
        return Optional.ofNullable(previous);
    }

    public Optional<ItemInstance> unequip(EquipmentSlot slot) {
        return Optional.ofNullable(slots.remove(slot));
    }

    public Optional<ItemInstance> getSlot(EquipmentSlot slot) {
        return Optional.ofNullable(slots.get(slot));
    }
}
```

(주의: JPA가 `EnumMap`을 직접 다루지 못할 수 있어 `slots` 필드 자체는 일반 `Map` 으로 보관해도 무방. EnumMap 사용은 in-memory 초기값으로만. Hibernate가 `HashMap` 으로 로드하더라도 본 API는 영향 없음.)

- [ ] **Step 4: 테스트 실행하여 통과 확인 (1/4)**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.EquippedItemsTest"`
Expected: PASS (4 tests).

- [ ] **Step 5: 테스트 추가 (2/4 — findByItemName)**

Append to `EquippedItemsTest.java`:

```java
    @Test
    void findByItemName_returnsSlotAndInstance() {
        ItemInstance helmet = new ItemInstance(helmetTemplate, 1);
        ItemInstance sword = new ItemInstance(swordTemplate, 1);
        equipped.equip(EquipmentSlot.HELMET, helmet);
        equipped.equip(EquipmentSlot.WEAPON, sword);

        Optional<Map.Entry<EquipmentSlot, ItemInstance>> found = equipped.findByItemName("철검");
        assertThat(found).isPresent();
        assertThat(found.get().getKey()).isEqualTo(EquipmentSlot.WEAPON);
        assertThat(found.get().getValue()).isSameAs(sword);
    }

    @Test
    void findByItemName_notEquipped_returnsEmpty() {
        assertThat(equipped.findByItemName("철검")).isEmpty();
    }
```

Run: `./gradlew test --tests "...EquippedItemsTest.findByItemName*"`
Expected: COMPILE FAIL — `findByItemName` 미존재.

- [ ] **Step 6: findByItemName 구현**

Add to `EquippedItems.java`:

```java
    public Optional<Map.Entry<EquipmentSlot, ItemInstance>> findByItemName(String name) {
        return slots.entrySet().stream()
                .filter(e -> e.getValue().getTemplate().getName().equals(name))
                .findFirst();
    }
```

Run again — Expected: PASS.

- [ ] **Step 7: 테스트 추가 (3/4 — sumStatModifiers)**

Append to test:

```java
    @Test
    void sumStatModifiers_aggregatesAcrossSlots() {
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(swordTemplate, 1));   // +2 VIGOR
        equipped.equip(EquipmentSlot.HELMET, new ItemInstance(helmetTemplate, 1));  // +1 AGILITY
        equipped.equip(EquipmentSlot.RING_LEFT, new ItemInstance(ringTemplate, 1)); // +3 INNER_POWER

        Map<StatType, Integer> sums = equipped.sumStatModifiers();
        assertThat(sums.get(StatType.VIGOR)).isEqualTo(2);
        assertThat(sums.get(StatType.AGILITY)).isEqualTo(1);
        assertThat(sums.get(StatType.INNER_POWER)).isEqualTo(3);
    }

    @Test
    void sumStatModifiers_stacksSameStatTypeAcrossSlots() {
        WeaponTemplate doubleVigorSword = WeaponTemplate.builder()
                .name("쌍철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 3)))
                .build();
        EquipmentTemplate vigorHelmet = EquipmentTemplate.builder()
                .name("힘투구").description("d").weight(1).stackable(false)
                .equipmentSlot(EquipmentSlot.HELMET)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 5)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(doubleVigorSword, 1));
        equipped.equip(EquipmentSlot.HELMET, new ItemInstance(vigorHelmet, 1));

        assertThat(equipped.sumStatModifiers().get(StatType.VIGOR)).isEqualTo(8);
    }

    @Test
    void sumStatModifiers_emptyEquipped_returnsEmptyMap() {
        assertThat(equipped.sumStatModifiers()).isEmpty();
    }
```

Run — Expected: COMPILE FAIL.

- [ ] **Step 8: sumStatModifiers 구현**

Add to `EquippedItems.java`:

```java
    public Map<StatType, Integer> sumStatModifiers() {
        Map<StatType, Integer> sums = new HashMap<>();
        for (ItemInstance instance : slots.values()) {
            if (instance.getTemplate() instanceof EquippableItemTemplate equip) {
                for (StatModifier mod : equip.getStatModifiers()) {
                    sums.merge(mod.getStatType(), mod.getValue(), Integer::sum);
                }
            }
        }
        return sums;
    }
```

Run — Expected: PASS.

- [ ] **Step 9: 테스트 추가 (4/4 — initializeAssociatedEntities)**

Append to test:

```java
    @Test
    void initializeAssociatedEntities_callsOnEachInstance() {
        ItemInstance sword = org.mockito.Mockito.spy(new ItemInstance(swordTemplate, 1));
        ItemInstance helmet = org.mockito.Mockito.spy(new ItemInstance(helmetTemplate, 1));
        equipped.equip(EquipmentSlot.WEAPON, sword);
        equipped.equip(EquipmentSlot.HELMET, helmet);

        equipped.initializeAssociatedEntities();

        org.mockito.Mockito.verify(sword).initializeAssociatedEntities();
        org.mockito.Mockito.verify(helmet).initializeAssociatedEntities();
    }
```

Run — Expected: COMPILE FAIL.

- [ ] **Step 10: initializeAssociatedEntities 구현**

Add to `EquippedItems.java`:

```java
    public void initializeAssociatedEntities() {
        this.slots.size();
        for (ItemInstance instance : slots.values()) {
            instance.initializeAssociatedEntities();
        }
    }
```

Run — Expected: PASS.

- [ ] **Step 11: 전체 테스트 회귀 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.EquippedItemsTest"`
Expected: PASS (모든 테스트).

- [ ] **Step 12: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/EquippedItems.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/EquippedItemsTest.java
git commit -m "feat(equip): EquippedItems @Entity — 슬롯 매핑, statModifier 합산, lazy 초기화"
```

---

## Task 6: PlayerCharacter equippedItems 필드 + effective stat

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacterEffectiveStatsTest.java`

기존 `PlayerCharacter` 생성자는 `(id, baseCharacterInfo, playableCharacterInfo, userId, nickname, characterClass, online, lastActiveAt, inventory)` 시그니처를 가지며, `getStats()`는 base를 반환한다. 본 task에서:
1. `equippedItems` 필드 추가
2. 생성자 시그니처에 `equippedItems` 파라미터 추가
3. `getStats()` 가 effective(base + 장비 modifier) 반환하도록 변경
4. `getBaseStats()` 신규 추가
5. `initializeAssociatedEntities()` 에 `equippedItems.initializeAssociatedEntities()` 호출 추가
6. 호출처(`PlayerCharacterService.createCharacter`) Task 7에서 처리

- [ ] **Step 1: 실패하는 테스트 작성 — effective getStats / getBaseStats**

Create `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacterEffectiveStatsTest.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerCharacterEffectiveStatsTest {

    private PlayerCharacter player;
    private EquippedItems equipped;

    @BeforeEach
    void setUp() {
        BaseCharacter base = BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(10).physique(10).agility(10).intellect(10).will(10).meridian(10)
                .innerPower(0).specialTechnique(0).lightStep(0)
                .fistsAndPalms(0).swordMethod(3).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(1L).alive(true).build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(1).experience(0).nextLevelExp(100).conversable(true).build();
        Inventory inventory = Inventory.create(100);
        equipped = EquippedItems.create();
        player = new PlayerCharacter(null, base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), inventory, equipped);
    }

    @Test
    void getStats_noEquipment_returnsBaseValues() {
        CharacterStats stats = player.getStats();
        assertThat(stats.vigor()).isEqualTo(10);
        assertThat(stats.swordMethod()).isEqualTo(3);
    }

    @Test
    void getStats_withEquippedWeapon_addsModifiers() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(
                        new StatModifier(StatType.VIGOR, 2),
                        new StatModifier(StatType.SWORD_METHOD, 5)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        CharacterStats stats = player.getStats();
        assertThat(stats.vigor()).isEqualTo(12);          // 10 + 2
        assertThat(stats.swordMethod()).isEqualTo(8);     // 3 + 5
    }

    @Test
    void getBaseStats_alwaysReturnsBase_ignoresEquipment() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 2)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        assertThat(player.getBaseStats().vigor()).isEqualTo(10);
    }

    @Test
    void getStats_keepsCurrentResources_fromBase() {
        // hp/mp/ap 는 base 그대로 — 장비로 보강하지 않음 (현재 자원 개념)
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.PHYSIQUE, 5)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        CharacterStats stats = player.getStats();
        assertThat(stats.hp()).isEqualTo(100);   // base 그대로
        assertThat(stats.physique()).isEqualTo(15); // 10 + 5 — physique만 effective 반영
    }
}
```

- [ ] **Step 2: 테스트 실행하여 실패 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacterEffectiveStatsTest"`
Expected: COMPILE FAIL — `PlayerCharacter` 생성자 10-인자 시그니처 없음, `getBaseStats()` 없음.

- [ ] **Step 3: PlayerCharacter 수정**

Edit `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Direction;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@EqualsAndHashCode(of = "id")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PlayerCharacter implements Combatable, Statable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    private BaseCharacter baseCharacterInfo;

    @Embedded
    private PlayableCharacter playableCharacterInfo;

    private Long userId;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private CharacterClass characterClass;

    private boolean online = false;
    private LocalDateTime lastActiveAt;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipped_items_id")
    private EquippedItems equippedItems;

    public PlayerCharacter(
            final UUID id,
            final BaseCharacter baseCharacterInfo,
            final PlayableCharacter playableCharacterInfo,
            final Long userId,
            final String nickname,
            final CharacterClass characterClass,
            final boolean online,
            final LocalDateTime lastActiveAt,
            final Inventory inventory,
            final EquippedItems equippedItems
    ) {
        this.id = id;
        this.baseCharacterInfo = baseCharacterInfo;
        this.playableCharacterInfo = playableCharacterInfo;
        this.userId = userId;
        this.nickname = nickname;
        this.characterClass = characterClass;
        this.online = online;
        this.lastActiveAt = lastActiveAt;
        this.inventory = inventory;
        this.equippedItems = equippedItems;
    }

    public void initializeAssociatedEntities() {
        this.inventory.initializeAssociatedEntities();
        this.equippedItems.initializeAssociatedEntities();
    }

    public MoveResult move(final Room currentRoom, final Direction direction) {
        if (!currentRoom.hasWay(direction)) {
            return MoveResult.NO_WAY;
        }
        if (currentRoom.isLocked(direction)) {
            return MoveResult.LOCKED;
        }
        Optional<Room> nextRoomByDirection = currentRoom.getNextRoomByDirection(direction);
        if (nextRoomByDirection.isPresent()) {
            Room nextRoom = nextRoomByDirection.get();
            this.setCurrentRoomId(nextRoom.getId());
            return MoveResult.SUCCESS;
        } else {
            return MoveResult.FAILED;
        }
    }

    public void setCurrentRoomId(final Long roomId) {
        this.baseCharacterInfo.setRoomId(roomId);
    }

    public Long getCurrentRoomId() {
        return this.baseCharacterInfo.getRoomId();
    }

    @Override
    public String getName() {
        return this.nickname;
    }

    public CharacterStats getBaseStats() {
        return this.baseCharacterInfo.getStats();
    }

    @Override
    public CharacterStats getStats() {
        CharacterStats base = baseCharacterInfo.getStats();
        Map<StatType, Integer> mods = equippedItems.sumStatModifiers();
        return new CharacterStats(
                base.hp(), base.mp(), base.ap(),
                base.vigor()           + mods.getOrDefault(StatType.VIGOR, 0),
                base.physique()        + mods.getOrDefault(StatType.PHYSIQUE, 0),
                base.agility()         + mods.getOrDefault(StatType.AGILITY, 0),
                base.intellect()       + mods.getOrDefault(StatType.INTELLECT, 0),
                base.will()            + mods.getOrDefault(StatType.WILL, 0),
                base.meridian()        + mods.getOrDefault(StatType.MERIDIAN, 0),
                base.innerPower()      + mods.getOrDefault(StatType.INNER_POWER, 0),
                base.specialTechnique()+ mods.getOrDefault(StatType.SPECIAL_TECHNIQUE, 0),
                base.lightStep()       + mods.getOrDefault(StatType.LIGHT_STEP, 0),
                base.fistsAndPalms()   + mods.getOrDefault(StatType.FISTS_AND_PALMS, 0),
                base.swordMethod()     + mods.getOrDefault(StatType.SWORD_METHOD, 0),
                base.bladeMethod()     + mods.getOrDefault(StatType.BLADE_METHOD, 0),
                base.longWeapon()      + mods.getOrDefault(StatType.LONG_WEAPON, 0),
                base.esotericWeapon()  + mods.getOrDefault(StatType.ESOTERIC_WEAPON, 0),
                base.archery()         + mods.getOrDefault(StatType.ARCHERY, 0)
        );
    }

    @Override
    public CharacterState getState() {
        return this.baseCharacterInfo.getState();
    }

    @Override
    public void enterCombatState() {
        this.baseCharacterInfo.setState(CharacterState.COMBAT);
    }

    @Override
    public void damaged(int damage) {
        baseCharacterInfo.decreaseHp(damage);
    }

    @Override
    public boolean isAlive() {
        return baseCharacterInfo.isAlive();
    }

    public void fullRestore() {
        baseCharacterInfo.fullRestore();
    }

    public enum MoveResult {
        NO_WAY,
        LOCKED,
        SUCCESS,
        FAILED
    }
}
```

- [ ] **Step 4: 컴파일 — 호출처 영향 파악**

Run: `./gradlew compileJava`
Expected: COMPILE FAIL — `PlayerCharacterService.createCharacter()` 의 9-인자 생성자 호출이 깨짐.

Task 7에서 이 호출처를 수정한다. 본 task에서는 일단 PlayerCharacter 자체의 변경만 끝낸다. 호출처 빌드 깨짐을 임시 해결하기 위해 **PlayerCharacterService를 한 라인 수정**한다:

Edit `src/main/java/com/jefflife/mudmk2/gamedata/application/service/PlayerCharacterService.java` — `createCharacter()` 메서드의 `new PlayerCharacter(...)` 호출 끝에 `, EquippedItems.create()` 추가, 그리고 import `EquippedItems` 추가:

```java
import com.jefflife.mudmk2.gamedata.application.domain.model.player.EquippedItems;
```

```java
        final PlayerCharacter playerCharacter = new PlayerCharacter(
                null,
                baseCharacter,
                playableCharacter,
                userId,
                name,
                characterClass,
                true,
                LocalDateTime.now(),
                inventory,
                EquippedItems.create()
        );
```

(Task 7에서 별도 테스트로 검증.)

- [ ] **Step 5: 컴파일 재확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: PlayerCharacter 테스트 통과 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacterEffectiveStatsTest"`
Expected: PASS (4 tests).

- [ ] **Step 7: 전체 회귀 — gamedata.player 패키지**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.*"`
Expected: 모든 테스트 통과.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/service/PlayerCharacterService.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacterEffectiveStatsTest.java
git commit -m "feat(equip): PlayerCharacter equippedItems + effective getStats + getBaseStats"
```

---

## Task 7: PlayerCharacterService — 캐릭터 생성 시 EquippedItems 부여 검증

Task 6 Step 4에서 이미 라인을 추가했지만, 동작 검증 테스트를 더한다.

**Files:**
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/service/PlayerCharacterServiceEquippedItemsTest.java`

기존 `PlayerCharacterServiceTest.java` 가 있다면 그 안에 통합. 없으면 별도 파일.

- [ ] **Step 1: 기존 PlayerCharacterServiceTest 존재 확인**

Run: `ls src/test/java/com/jefflife/mudmk2/gamedata/application/service/PlayerCharacterServiceTest.java`
존재하면 그 파일에 테스트 추가, 없으면 신규 파일 생성.

- [ ] **Step 2: 테스트 추가**

If existing: append; else create `PlayerCharacterServiceEquippedItemsTest.java`:

```java
package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerCharacterServiceEquippedItemsTest {

    private PlayerCharacterRepository repo;
    private ApplicationEventPublisher publisher;
    private PlayerCharacterService service;

    @BeforeEach
    void setUp() {
        repo = mock(PlayerCharacterRepository.class);
        publisher = mock(ApplicationEventPublisher.class);
        service = new PlayerCharacterService(repo, publisher);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createCharacter_attachesNonNullEquippedItems() {
        PlayerCharacter pc = service.createCharacter(1L, "철수", CharacterClass.WARRIOR, Gender.MALE);
        assertThat(pc.getEquippedItems()).isNotNull();
        assertThat(pc.getEquippedItems().getSlots()).isEmpty();
    }
}
```

- [ ] **Step 3: 테스트 실행하여 통과 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.service.PlayerCharacterServiceEquippedItemsTest"`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/jefflife/mudmk2/gamedata/application/service/PlayerCharacterServiceEquippedItemsTest.java
git commit -m "test(equip): PlayerCharacterService.createCharacter — EquippedItems 부여 검증"
```

---

## Task 8: CommandDictionary 확장

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionary.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionaryTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

Create `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionaryTest.java`:

```java
package com.jefflife.mudmk2.gameplay.application.domain.model.command;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CommandDictionaryTest {

    @Test
    void EQUIP_containsExpectedAliases() {
        String regex = CommandDictionary.EQUIP.toRegex();
        assertThat(regex).contains("장착").contains("입어").contains("끼다").contains("차다");
    }

    @Test
    void UNEQUIP_containsExpectedAliases() {
        String regex = CommandDictionary.UNEQUIP.toRegex();
        assertThat(regex).contains("해제").contains("벗어").contains("빼다");
    }

    @Test
    void EQUIPMENT_VIEW_containsOnlyTwoAliases() {
        String regex = CommandDictionary.EQUIPMENT_VIEW.toRegex();
        assertThat(regex).contains("장비").contains("장비창");
        assertThat(regex).doesNotContain("착용");
    }
}
```

- [ ] **Step 2: 테스트 실행하여 실패 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionaryTest"`
Expected: COMPILE FAIL — `EQUIP`, `UNEQUIP`, `EQUIPMENT_VIEW` 미존재.

- [ ] **Step 3: CommandDictionary 수정**

Replace `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionary.java`:

```java
package com.jefflife.mudmk2.gameplay.application.domain.model.command;

import java.util.Set;

public enum CommandDictionary {
    ATTACK("공격", "때려", "공"),
    TAKE("줍다", "주워", "집어", "집"),
    DROP("버리다", "버려", "놓다", "버"),
    INVENTORY("소지품", "가방", "인벤"),
    EQUIP("장착", "착용", "입어", "입고", "차다", "차고", "끼다", "껴"),
    UNEQUIP("해제", "벗어", "벗고", "빼다", "빼"),
    EQUIPMENT_VIEW("장비", "장비창");

    private final Set<String> aliases;

    CommandDictionary(String... aliases) {
        this.aliases = Set.of(aliases);
    }

    public String toRegex() {
        return String.join("|", aliases);
    }
}
```

- [ ] **Step 4: 테스트 실행하여 통과 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionaryTest"`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionary.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionaryTest.java
git commit -m "feat(equip): CommandDictionary — EQUIP/UNEQUIP/EQUIPMENT_VIEW 추가"
```

---

## Task 9: EquipCommand — record + parser + executor + service

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/EquipCommand.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EquipCommandParser.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/EquipCommandExecutor.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/EquipUseCase.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/EquipCommandService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EquipCommandParserTest.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/EquipCommandServiceTest.java`

### 9.1 EquipCommand record + Parser

- [ ] **Step 1: Parser 테스트 작성**

Create `src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EquipCommandParserTest.java`:

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EquipCommandParserTest {

    private EquipCommandParser parser;

    @BeforeEach
    void setUp() { parser = new EquipCommandParser(); }

    @Test
    void parse_swordWith_장착_returnsEquipCommand() {
        Command c = parser.parse(1L, "철검 장착");
        assertThat(c).isInstanceOf(EquipCommand.class);
        EquipCommand e = (EquipCommand) c;
        assertThat(e.itemName()).isEqualTo("철검");
        assertThat(e.index()).isEqualTo(1);
        assertThat(e.userId()).isEqualTo(1L);
    }

    @Test
    void parse_helmetWith_입어_returnsEquipCommand() {
        EquipCommand e = (EquipCommand) parser.parse(1L, "야구모자 입어");
        assertThat(e.itemName()).isEqualTo("야구모자");
    }

    @Test
    void parse_ringWith_끼다_returnsEquipCommand() {
        EquipCommand e = (EquipCommand) parser.parse(1L, "금반지 끼다");
        assertThat(e.itemName()).isEqualTo("금반지");
    }

    @Test
    void parse_withIndex_returnsCorrectIndex() {
        EquipCommand e = (EquipCommand) parser.parse(1L, "철검 2 장착");
        assertThat(e.index()).isEqualTo(2);
    }

    @Test
    void parse_nonEquipText_returnsNull() {
        assertThat(parser.parse(1L, "철검 주워")).isNull();
    }
}
```

- [ ] **Step 2: 실패 확인**

Run: `./gradlew test --tests "...EquipCommandParserTest"`
Expected: COMPILE FAIL.

- [ ] **Step 3: EquipCommand 작성**

Create `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/EquipCommand.java`:

```java
package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record EquipCommand(
        Long userId,
        String itemName,
        int index
) implements Command {}
```

- [ ] **Step 4: EquipCommandParser 작성**

Create `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EquipCommandParser.java`:

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionary;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EquipCommandParser extends AbstractCommandParser {
    private static final Pattern EQUIP_PATTERN =
            Pattern.compile("(\\S+?)(?:\\s+(\\d+))?\\s+(" + CommandDictionary.EQUIP.toRegex() + ")");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = EQUIP_PATTERN.matcher(content);
        if (matcher.matches()) {
            String itemName = matcher.group(1);
            int index = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 1;
            return new EquipCommand(userId, itemName, index);
        }
        return null;
    }
}
```

- [ ] **Step 5: Parser 테스트 통과 확인**

Run: `./gradlew test --tests "...EquipCommandParserTest"`
Expected: PASS (5 tests).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/EquipCommand.java \
        src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EquipCommandParser.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EquipCommandParserTest.java
git commit -m "feat(equip): EquipCommand + EquipCommandParser — 한글 alias 다수 지원"
```

### 9.2 EquipUseCase + EquipCommandService

- [ ] **Step 7: EquipUseCase 인터페이스 작성**

Create `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/EquipUseCase.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipCommand;

public interface EquipUseCase {
    void equip(EquipCommand command);
}
```

- [ ] **Step 8: Service 테스트 작성**

Create `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/EquipCommandServiceTest.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipCommand;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EquipCommandServiceTest {

    private ActivePlayerRepository players;
    private SendMessageToUserPort sender;
    private EquipCommandService service;
    private PlayerCharacter player;
    private Inventory inventory;
    private EquippedItems equipped;

    private WeaponTemplate swordTemplate;
    private EquipmentTemplate helmetTemplate;
    private AccessoryTemplate ringTemplate;
    private AccessoryTemplate necklaceTemplate;
    private FoodTemplate foodTemplate;

    @BeforeEach
    void setUp() {
        players = mock(ActivePlayerRepository.class);
        sender = mock(SendMessageToUserPort.class);
        service = new EquipCommandService(players, sender);

        inventory = Inventory.create(100);
        equipped = EquippedItems.create();
        BaseCharacter base = BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(10).physique(10).agility(10).intellect(10).will(10).meridian(10)
                .innerPower(0).specialTechnique(0).lightStep(0)
                .fistsAndPalms(0).swordMethod(0).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(1L).alive(true).build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(1).experience(0).nextLevelExp(100).conversable(true).build();
        player = new PlayerCharacter(null, base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), inventory, equipped);

        when(players.findByUserId(1L)).thenReturn(Optional.of(player));

        swordTemplate = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 2)))
                .build();
        helmetTemplate = EquipmentTemplate.builder()
                .name("야구모자").description("d").weight(1).stackable(false)
                .equipmentSlot(EquipmentSlot.HELMET)
                .statModifiers(List.of()).build();
        ringTemplate = AccessoryTemplate.builder()
                .name("금반지").description("d").weight(1).stackable(false)
                .accessoryType(AccessoryType.RING)
                .statModifiers(List.of()).build();
        necklaceTemplate = AccessoryTemplate.builder()
                .name("목걸이").description("d").weight(1).stackable(false)
                .accessoryType(AccessoryType.NECKLACE)
                .statModifiers(List.of()).build();
        foodTemplate = FoodTemplate.builder()
                .name("만두").description("d").weight(1).stackable(true)
                .hpRecovery(0).mpRecovery(0).apRecovery(0).build();
    }

    @Test
    void equip_weaponFromInventory_movesToWeaponSlot() {
        ItemInstance sword = new ItemInstance(swordTemplate, 1);
        inventory.addItem(sword);

        service.equip(new EquipCommand(1L, "철검", 1));

        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).contains(sword);
        assertThat(inventory.getItems()).isEmpty();
        verify(sender).messageToUser(eq(1L), contains("철검"));
    }

    @Test
    void equip_helmet_movesToHelmetSlot() {
        ItemInstance helmet = new ItemInstance(helmetTemplate, 1);
        inventory.addItem(helmet);

        service.equip(new EquipCommand(1L, "야구모자", 1));

        assertThat(equipped.getSlot(EquipmentSlot.HELMET)).contains(helmet);
    }

    @Test
    void equip_necklace_movesToNecklaceSlot() {
        ItemInstance necklace = new ItemInstance(necklaceTemplate, 1);
        inventory.addItem(necklace);

        service.equip(new EquipCommand(1L, "목걸이", 1));

        assertThat(equipped.getSlot(EquipmentSlot.NECKLACE)).contains(necklace);
    }

    @Test
    void equip_firstRing_goesToRingLeft() {
        ItemInstance ring = new ItemInstance(ringTemplate, 1);
        inventory.addItem(ring);

        service.equip(new EquipCommand(1L, "금반지", 1));

        assertThat(equipped.getSlot(EquipmentSlot.RING_LEFT)).contains(ring);
        assertThat(equipped.getSlot(EquipmentSlot.RING_RIGHT)).isEmpty();
    }

    @Test
    void equip_secondRing_goesToRingRight_whenLeftOccupied() {
        ItemInstance ring1 = new ItemInstance(ringTemplate, 1);
        ItemInstance ring2 = new ItemInstance(ringTemplate, 1);
        inventory.addItem(ring1);
        inventory.addItem(ring2);

        service.equip(new EquipCommand(1L, "금반지", 1));
        service.equip(new EquipCommand(1L, "금반지", 1));

        assertThat(equipped.getSlot(EquipmentSlot.RING_LEFT)).isPresent();
        assertThat(equipped.getSlot(EquipmentSlot.RING_RIGHT)).isPresent();
    }

    @Test
    void equip_thirdRing_swapsLeft_andReturnsOldToInventory() {
        ItemInstance ring1 = new ItemInstance(ringTemplate, 1);
        ItemInstance ring2 = new ItemInstance(ringTemplate, 1);
        ItemInstance ring3 = new ItemInstance(ringTemplate, 1);
        inventory.addItem(ring1);
        inventory.addItem(ring2);
        inventory.addItem(ring3);

        service.equip(new EquipCommand(1L, "금반지", 1)); // left
        service.equip(new EquipCommand(1L, "금반지", 1)); // right
        service.equip(new EquipCommand(1L, "금반지", 1)); // swap left

        assertThat(equipped.getSlot(EquipmentSlot.RING_LEFT)).isPresent();
        assertThat(equipped.getSlot(EquipmentSlot.RING_RIGHT)).isPresent();
        assertThat(inventory.getItems()).hasSize(1); // 빈 인벤토리 + 1 swap된 옛 반지
    }

    @Test
    void equip_occupiedSlot_swapsAndReturnsOldToInventory() {
        ItemInstance helmet1 = new ItemInstance(helmetTemplate, 1);
        ItemInstance helmet2 = new ItemInstance(helmetTemplate, 1);
        inventory.addItem(helmet1);
        inventory.addItem(helmet2);

        service.equip(new EquipCommand(1L, "야구모자", 1));
        service.equip(new EquipCommand(1L, "야구모자", 1));

        assertThat(equipped.getSlot(EquipmentSlot.HELMET)).isPresent();
        assertThat(inventory.getItems()).hasSize(1);
        verify(sender, atLeastOnce()).messageToUser(eq(1L), contains("해제"));
    }

    @Test
    void equip_itemNotInInventory_sendsErrorMessage() {
        service.equip(new EquipCommand(1L, "없는검", 1));

        verify(sender).messageToUser(eq(1L), contains("가지고 있지 않습니다"));
        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).isEmpty();
    }

    @Test
    void equip_nonEquippableItem_sendsErrorMessage() {
        ItemInstance food = new ItemInstance(foodTemplate, 1);
        inventory.addItem(food);

        service.equip(new EquipCommand(1L, "만두", 1));

        verify(sender).messageToUser(eq(1L), contains("장착할 수 없습니다"));
        assertThat(inventory.getItems()).hasSize(1);
    }

    @Test
    void equip_swapWouldExceedInventoryWeight_rejected() {
        Inventory small = Inventory.create(6); // 단 6kg
        // 작은 인벤토리로 재구성
        BaseCharacter base = player.getBaseCharacterInfo();
        equipped = EquippedItems.create();
        player = new PlayerCharacter(null, base, player.getPlayableCharacterInfo(), 1L,
                "철수", CharacterClass.WARRIOR, true, LocalDateTime.now(), small, equipped);
        when(players.findByUserId(1L)).thenReturn(Optional.of(player));

        ItemInstance sword1 = new ItemInstance(swordTemplate, 1); // 5kg
        small.addItem(sword1);
        service.equip(new EquipCommand(1L, "철검", 1)); // 무기 슬롯 점유, 인벤 비움

        ItemInstance sword2 = new ItemInstance(swordTemplate, 1); // 5kg
        small.addItem(sword2); // 인벤토리 5/6
        // 이제 sword2 장착하면 sword1이 인벤토리에 돌아와야 하는데 5+5>6 → 거부
        service.equip(new EquipCommand(1L, "철검", 1));

        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).contains(sword1);
        verify(sender, atLeastOnce()).messageToUser(eq(1L), contains("무게가 부족"));
    }
}
```

- [ ] **Step 9: 실패 확인**

Run: `./gradlew test --tests "...EquipCommandServiceTest"`
Expected: COMPILE FAIL.

- [ ] **Step 10: EquipCommandService 작성**

Create `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/EquipCommandService.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.EquippedItems;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.provided.EquipUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipCommandService implements EquipUseCase {

    private final ActivePlayerRepository players;
    private final SendMessageToUserPort sender;

    public EquipCommandService(ActivePlayerRepository players, SendMessageToUserPort sender) {
        this.players = players;
        this.sender = sender;
    }

    @Override
    public void equip(EquipCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));

        Inventory inventory = player.getInventory();
        EquippedItems equipped = player.getEquippedItems();

        List<ItemInstance> matches = inventory.findItemsByName(command.itemName());
        int idx = command.index();
        if (matches.size() < idx) {
            sender.messageToUser(command.userId(), command.itemName() + "을(를) 가지고 있지 않습니다.");
            return;
        }
        ItemInstance instance = matches.get(idx - 1);

        ItemTemplate template = instance.getTemplate();
        if (!(template instanceof EquippableItemTemplate)) {
            sender.messageToUser(command.userId(), template.getName() + "은(는) 장착할 수 없습니다.");
            return;
        }

        EquipmentSlot targetSlot = resolveSlot(template, equipped);

        // swap 시 인벤토리 무게 사전 검사
        Optional<ItemInstance> currentOpt = equipped.getSlot(targetSlot);
        if (currentOpt.isPresent()) {
            ItemInstance current = currentOpt.get();
            int weightAfterRemove = inventory.currentWeight() - template.getWeight() * instance.getQuantity();
            int weightAfterSwap = weightAfterRemove + current.getTemplate().getWeight() * current.getQuantity();
            if (weightAfterSwap > inventory.getMaxWeightCapacity()) {
                sender.messageToUser(command.userId(),
                        template.getName() + "을(를) 장착하려면 " + current.getTemplate().getName()
                                + "을(를) 해제해야 하지만 소지품 무게가 부족합니다.");
                return;
            }
        }

        Optional<ItemInstance> swapped = equipped.equip(targetSlot, instance);
        inventory.removeItem(instance);
        swapped.ifPresent(inventory::addItem);

        if (swapped.isPresent()) {
            sender.messageToUser(command.userId(),
                    "기존 " + swapped.get().getTemplate().getName()
                            + "을(를) 해제하고 " + template.getName() + "을(를) 장착했다.");
        } else {
            sender.messageToUser(command.userId(), template.getName() + "을(를) 장착했다.");
        }
    }

    private EquipmentSlot resolveSlot(ItemTemplate template, EquippedItems equipped) {
        if (template instanceof WeaponTemplate) {
            return EquipmentSlot.WEAPON;
        }
        if (template instanceof EquipmentTemplate eq) {
            return eq.getEquipmentSlot();
        }
        if (template instanceof AccessoryTemplate acc) {
            return switch (acc.getAccessoryType()) {
                case NECKLACE -> EquipmentSlot.NECKLACE;
                case RING -> pickRingSlot(equipped);
            };
        }
        throw new IllegalStateException("Unknown equippable template: " + template.getClass());
    }

    private EquipmentSlot pickRingSlot(EquippedItems equipped) {
        if (equipped.getSlot(EquipmentSlot.RING_LEFT).isEmpty()) return EquipmentSlot.RING_LEFT;
        if (equipped.getSlot(EquipmentSlot.RING_RIGHT).isEmpty()) return EquipmentSlot.RING_RIGHT;
        return EquipmentSlot.RING_LEFT; // 둘 다 차면 LEFT swap
    }
}
```

- [ ] **Step 11: 서비스 테스트 통과 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.command.EquipCommandServiceTest"`
Expected: PASS (10 tests).

- [ ] **Step 12: Executor 작성 (테스트 없이도 됨 — 단순 dispatch)**

Create `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/EquipCommandExecutor.java`:

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.EquipUseCase;
import org.springframework.stereotype.Component;

@Component
public class EquipCommandExecutor implements CommandExecutor {
    private final EquipUseCase equipUseCase;

    public EquipCommandExecutor(EquipUseCase equipUseCase) {
        this.equipUseCase = equipUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof EquipCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof EquipCommand equipCommand)) {
            throw new IllegalArgumentException("Command must be an EquipCommand");
        }
        equipUseCase.equip(equipCommand);
    }
}
```

- [ ] **Step 13: 빌드 확인**

Run: `./gradlew compileJava compileTestJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 14: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/EquipUseCase.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/EquipCommandService.java \
        src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/EquipCommandExecutor.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/EquipCommandServiceTest.java
git commit -m "feat(equip): EquipCommandService — 인벤토리→슬롯 이동, 반지 자동 슬롯, 무게 가드"
```

---

## Task 10: UnequipCommand — record + parser + executor + service

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/UnequipCommand.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/UnequipCommandParser.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/UnequipCommandExecutor.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/UnequipUseCase.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/UnequipCommandService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/UnequipCommandParserTest.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/UnequipCommandServiceTest.java`

- [ ] **Step 1: Parser 테스트**

Create `EquipCommandParserTest.java`와 같은 패턴으로:

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.UnequipCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UnequipCommandParserTest {

    private UnequipCommandParser parser;

    @BeforeEach
    void setUp() { parser = new UnequipCommandParser(); }

    @Test
    void parse_swordWith_벗어_returnsUnequipCommand() {
        Command c = parser.parse(1L, "철검 벗어");
        assertThat(c).isInstanceOf(UnequipCommand.class);
        assertThat(((UnequipCommand) c).itemName()).isEqualTo("철검");
    }

    @Test
    void parse_pantsWith_벗어_returnsUnequipCommand() {
        UnequipCommand u = (UnequipCommand) parser.parse(1L, "반바지 벗어");
        assertThat(u.itemName()).isEqualTo("반바지");
    }

    @Test
    void parse_with_해제_returnsUnequipCommand() {
        UnequipCommand u = (UnequipCommand) parser.parse(1L, "철검 해제");
        assertThat(u.itemName()).isEqualTo("철검");
    }

    @Test
    void parse_with_빼다_returnsUnequipCommand() {
        UnequipCommand u = (UnequipCommand) parser.parse(1L, "금반지 빼다");
        assertThat(u.itemName()).isEqualTo("금반지");
    }

    @Test
    void parse_irrelevant_returnsNull() {
        assertThat(parser.parse(1L, "철검 장착")).isNull();
    }
}
```

- [ ] **Step 2: 실패 확인**

Run: `./gradlew test --tests "...UnequipCommandParserTest"`
Expected: COMPILE FAIL.

- [ ] **Step 3: UnequipCommand 작성**

```java
package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record UnequipCommand(Long userId, String itemName) implements Command {}
```

- [ ] **Step 4: UnequipCommandParser 작성**

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionary;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.UnequipCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UnequipCommandParser extends AbstractCommandParser {
    private static final Pattern UNEQUIP_PATTERN =
            Pattern.compile("(\\S+?)\\s+(" + CommandDictionary.UNEQUIP.toRegex() + ")");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = UNEQUIP_PATTERN.matcher(content);
        if (matcher.matches()) {
            return new UnequipCommand(userId, matcher.group(1));
        }
        return null;
    }
}
```

- [ ] **Step 5: Parser 테스트 통과 확인**

Run: `./gradlew test --tests "...UnequipCommandParserTest"`
Expected: PASS (5 tests).

- [ ] **Step 6: UnequipUseCase + Service 테스트**

Create `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/UnequipUseCase.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.UnequipCommand;

public interface UnequipUseCase {
    void unequip(UnequipCommand command);
}
```

Create `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/UnequipCommandServiceTest.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.UnequipCommand;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UnequipCommandServiceTest {

    private ActivePlayerRepository players;
    private SendMessageToUserPort sender;
    private UnequipCommandService service;
    private PlayerCharacter player;
    private Inventory inventory;
    private EquippedItems equipped;

    private WeaponTemplate swordTemplate;

    @BeforeEach
    void setUp() {
        players = mock(ActivePlayerRepository.class);
        sender = mock(SendMessageToUserPort.class);
        service = new UnequipCommandService(players, sender);

        inventory = Inventory.create(100);
        equipped = EquippedItems.create();
        BaseCharacter base = BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(10).physique(10).agility(10).intellect(10).will(10).meridian(10)
                .innerPower(0).specialTechnique(0).lightStep(0)
                .fistsAndPalms(0).swordMethod(0).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(1L).alive(true).build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(1).experience(0).nextLevelExp(100).conversable(true).build();
        player = new PlayerCharacter(null, base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), inventory, equipped);
        when(players.findByUserId(1L)).thenReturn(Optional.of(player));

        swordTemplate = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of()).build();
    }

    @Test
    void unequip_equippedWeapon_returnsToInventory() {
        ItemInstance sword = new ItemInstance(swordTemplate, 1);
        equipped.equip(EquipmentSlot.WEAPON, sword);

        service.unequip(new UnequipCommand(1L, "철검"));

        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).isEmpty();
        assertThat(inventory.getItems()).containsExactly(sword);
        verify(sender).messageToUser(eq(1L), contains("해제"));
    }

    @Test
    void unequip_notEquipped_sendsErrorMessage() {
        service.unequip(new UnequipCommand(1L, "없는검"));

        verify(sender).messageToUser(eq(1L), contains("장착하고 있지 않습니다"));
    }

    @Test
    void unequip_inventoryWeightExceeded_rejected() {
        Inventory small = Inventory.create(5);
        equipped = EquippedItems.create();
        player = new PlayerCharacter(null, player.getBaseCharacterInfo(), player.getPlayableCharacterInfo(),
                1L, "철수", CharacterClass.WARRIOR, true, LocalDateTime.now(), small, equipped);
        when(players.findByUserId(1L)).thenReturn(Optional.of(player));

        ItemInstance sword = new ItemInstance(swordTemplate, 1); // 5kg
        equipped.equip(EquipmentSlot.WEAPON, sword);
        // 인벤토리에 1kg 아이템 5개로 채워 5/5 가득
        FoodTemplate food = FoodTemplate.builder()
                .name("만두").description("d").weight(1).stackable(false)
                .hpRecovery(0).mpRecovery(0).apRecovery(0).build();
        for (int i = 0; i < 5; i++) small.addItem(new ItemInstance(food, 1));

        service.unequip(new UnequipCommand(1L, "철검"));

        assertThat(equipped.getSlot(EquipmentSlot.WEAPON)).contains(sword);
        verify(sender).messageToUser(eq(1L), contains("무게가 부족"));
    }
}
```

- [ ] **Step 7: 실패 확인**

Run: `./gradlew test --tests "...UnequipCommandServiceTest"`
Expected: COMPILE FAIL.

- [ ] **Step 8: UnequipCommandService 작성**

Create `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/UnequipCommandService.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.EquipmentSlot;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.EquippedItems;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.UnequipCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.provided.UnequipUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UnequipCommandService implements UnequipUseCase {

    private final ActivePlayerRepository players;
    private final SendMessageToUserPort sender;

    public UnequipCommandService(ActivePlayerRepository players, SendMessageToUserPort sender) {
        this.players = players;
        this.sender = sender;
    }

    @Override
    public void unequip(UnequipCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));

        Inventory inventory = player.getInventory();
        EquippedItems equipped = player.getEquippedItems();

        Optional<Map.Entry<EquipmentSlot, ItemInstance>> found = equipped.findByItemName(command.itemName());
        if (found.isEmpty()) {
            sender.messageToUser(command.userId(), command.itemName() + "을(를) 장착하고 있지 않습니다.");
            return;
        }

        ItemInstance instance = found.get().getValue();
        if (!inventory.canAdd(instance.getTemplate(), instance.getQuantity())) {
            sender.messageToUser(command.userId(), "소지품 무게가 부족해 해제할 수 없습니다.");
            return;
        }

        equipped.unequip(found.get().getKey());
        inventory.addItem(instance);
        sender.messageToUser(command.userId(), instance.getTemplate().getName() + "을(를) 해제했다.");
    }
}
```

- [ ] **Step 9: 서비스 테스트 통과 확인**

Run: `./gradlew test --tests "...UnequipCommandServiceTest"`
Expected: PASS (3 tests).

- [ ] **Step 10: Executor 작성**

Create `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/UnequipCommandExecutor.java`:

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.UnequipCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.UnequipUseCase;
import org.springframework.stereotype.Component;

@Component
public class UnequipCommandExecutor implements CommandExecutor {
    private final UnequipUseCase unequipUseCase;

    public UnequipCommandExecutor(UnequipUseCase unequipUseCase) {
        this.unequipUseCase = unequipUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof UnequipCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof UnequipCommand unequipCommand)) {
            throw new IllegalArgumentException("Command must be an UnequipCommand");
        }
        unequipUseCase.unequip(unequipCommand);
    }
}
```

- [ ] **Step 11: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/UnequipCommand.java \
        src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/UnequipCommandParser.java \
        src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/UnequipCommandExecutor.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/UnequipUseCase.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/UnequipCommandService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/UnequipCommandParserTest.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/UnequipCommandServiceTest.java
git commit -m "feat(equip): UnequipCommand + Parser + Service — 슬롯→인벤토리 복귀, 무게 가드"
```

---

## Task 11: EquipmentViewCommand — 장비 조회

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/EquipmentViewCommand.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EquipmentViewCommandParser.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/EquipmentViewCommandExecutor.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/EquipmentViewUseCase.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/EquipmentViewCommandService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EquipmentViewCommandParserTest.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/EquipmentViewCommandServiceTest.java`

- [ ] **Step 1: Parser 테스트**

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EquipmentViewCommandParserTest {

    private EquipmentViewCommandParser parser;

    @BeforeEach
    void setUp() { parser = new EquipmentViewCommandParser(); }

    @Test
    void parse_장비_returnsCommand() {
        Command c = parser.parse(1L, "장비");
        assertThat(c).isInstanceOf(EquipmentViewCommand.class);
    }

    @Test
    void parse_장비창_returnsCommand() {
        assertThat(parser.parse(1L, "장비창")).isInstanceOf(EquipmentViewCommand.class);
    }

    @Test
    void parse_other_returnsNull() {
        assertThat(parser.parse(1L, "소지품")).isNull();
        assertThat(parser.parse(1L, "철검 장착")).isNull();
    }
}
```

- [ ] **Step 2: 실패 확인 → 작성**

Create `EquipmentViewCommand.java`:

```java
package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record EquipmentViewCommand(Long userId) implements Command {}
```

Create `EquipmentViewCommandParser.java`:

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionary;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EquipmentViewCommandParser extends AbstractCommandParser {
    private static final Pattern VIEW_PATTERN =
            Pattern.compile("(" + CommandDictionary.EQUIPMENT_VIEW.toRegex() + ")");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = VIEW_PATTERN.matcher(content);
        if (matcher.matches()) {
            return new EquipmentViewCommand(userId);
        }
        return null;
    }
}
```

Run: `./gradlew test --tests "...EquipmentViewCommandParserTest"` → PASS.

- [ ] **Step 3: Service 테스트 작성**

Create `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/EquipmentViewCommandServiceTest.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EquipmentViewCommandServiceTest {

    private ActivePlayerRepository players;
    private SendMessageToUserPort sender;
    private EquipmentViewCommandService service;
    private PlayerCharacter player;
    private EquippedItems equipped;

    @BeforeEach
    void setUp() {
        players = mock(ActivePlayerRepository.class);
        sender = mock(SendMessageToUserPort.class);
        service = new EquipmentViewCommandService(players, sender);

        equipped = EquippedItems.create();
        BaseCharacter base = BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(10).physique(10).agility(10).intellect(10).will(10).meridian(10)
                .innerPower(0).specialTechnique(0).lightStep(0)
                .fistsAndPalms(0).swordMethod(3).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(1L).alive(true).build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(1).experience(0).nextLevelExp(100).conversable(true).build();
        player = new PlayerCharacter(null, base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), Inventory.create(100), equipped);
        when(players.findByUserId(1L)).thenReturn(Optional.of(player));
    }

    @Test
    void showEquipment_emptySlots_listsAllAsEmpty() {
        service.showEquipment(new EquipmentViewCommand(1L));

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(sender).messageToUser(eq(1L), cap.capture());
        String msg = cap.getValue();
        assertThat(msg).contains("[ 장비 ]");
        assertThat(msg).contains("머리").contains("상의").contains("하의").contains("장갑")
                       .contains("신발").contains("허리띠").contains("목걸이")
                       .contains("왼손 반지").contains("오른손 반지").contains("무기");
        assertThat(msg).contains("(없음)");
    }

    @Test
    void showEquipment_withWeapon_listsItemNameAndModifiers() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(
                        new StatModifier(StatType.VIGOR, 2),
                        new StatModifier(StatType.SWORD_METHOD, 5)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        service.showEquipment(new EquipmentViewCommand(1L));

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(sender).messageToUser(eq(1L), cap.capture());
        String msg = cap.getValue();
        assertThat(msg).contains("철검");
        assertThat(msg).contains("VIGOR").contains("SWORD_METHOD");
    }

    @Test
    void showEquipment_displaysBaseToEffectiveDiffOnly() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(new StatModifier(StatType.VIGOR, 2)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        service.showEquipment(new EquipmentViewCommand(1L));

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(sender).messageToUser(eq(1L), cap.capture());
        String msg = cap.getValue();
        assertThat(msg).contains("[ 적용 스탯 ]");
        assertThat(msg).contains("10").contains("12"); // base → effective
        assertThat(msg).contains("VIGOR");
        assertThat(msg).doesNotContain("PHYSIQUE"); // 변동 없는 stat 미표시
    }
}
```

- [ ] **Step 4: 실패 확인 → 구현**

Create `EquipmentViewUseCase.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;

public interface EquipmentViewUseCase {
    void showEquipment(EquipmentViewCommand command);
}
```

Create `EquipmentViewCommandService.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.provided.EquipmentViewUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EquipmentViewCommandService implements EquipmentViewUseCase {

    private final ActivePlayerRepository players;
    private final SendMessageToUserPort sender;

    public EquipmentViewCommandService(ActivePlayerRepository players, SendMessageToUserPort sender) {
        this.players = players;
        this.sender = sender;
    }

    @Override
    public void showEquipment(EquipmentViewCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
        EquippedItems equipped = player.getEquippedItems();

        StringBuilder sb = new StringBuilder("[ 장비 ]\n");
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            sb.append(String.format("%-10s: ", slot.displayName()));
            Optional<ItemInstance> opt = equipped.getSlot(slot);
            if (opt.isEmpty()) {
                sb.append("(없음)\n");
            } else {
                ItemInstance inst = opt.get();
                sb.append(inst.getTemplate().getName());
                if (inst.getTemplate() instanceof EquippableItemTemplate eq && !eq.getStatModifiers().isEmpty()) {
                    sb.append(" (");
                    for (int i = 0; i < eq.getStatModifiers().size(); i++) {
                        StatModifier mod = eq.getStatModifiers().get(i);
                        if (i > 0) sb.append(", ");
                        sb.append(mod.getValue() >= 0 ? "+" : "").append(mod.getValue())
                          .append(" ").append(mod.getStatType());
                    }
                    sb.append(")");
                }
                sb.append("\n");
            }
        }

        CharacterStats base = player.getBaseStats();
        CharacterStats eff = player.getStats();
        StringBuilder diff = new StringBuilder("\n[ 적용 스탯 ] (base → effective)\n");
        boolean any = false;
        any |= appendDiff(diff, "VIGOR", base.vigor(), eff.vigor());
        any |= appendDiff(diff, "PHYSIQUE", base.physique(), eff.physique());
        any |= appendDiff(diff, "AGILITY", base.agility(), eff.agility());
        any |= appendDiff(diff, "INTELLECT", base.intellect(), eff.intellect());
        any |= appendDiff(diff, "WILL", base.will(), eff.will());
        any |= appendDiff(diff, "MERIDIAN", base.meridian(), eff.meridian());
        any |= appendDiff(diff, "INNER_POWER", base.innerPower(), eff.innerPower());
        any |= appendDiff(diff, "SPECIAL_TECHNIQUE", base.specialTechnique(), eff.specialTechnique());
        any |= appendDiff(diff, "LIGHT_STEP", base.lightStep(), eff.lightStep());
        any |= appendDiff(diff, "FISTS_AND_PALMS", base.fistsAndPalms(), eff.fistsAndPalms());
        any |= appendDiff(diff, "SWORD_METHOD", base.swordMethod(), eff.swordMethod());
        any |= appendDiff(diff, "BLADE_METHOD", base.bladeMethod(), eff.bladeMethod());
        any |= appendDiff(diff, "LONG_WEAPON", base.longWeapon(), eff.longWeapon());
        any |= appendDiff(diff, "ESOTERIC_WEAPON", base.esotericWeapon(), eff.esotericWeapon());
        any |= appendDiff(diff, "ARCHERY", base.archery(), eff.archery());
        if (any) sb.append(diff);

        sender.messageToUser(command.userId(), sb.toString());
    }

    private boolean appendDiff(StringBuilder sb, String label, int base, int eff) {
        if (base == eff) return false;
        sb.append(String.format("%-18s: %d → %d%n", label, base, eff));
        return true;
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests "...EquipmentViewCommandServiceTest"`
Expected: PASS (3 tests).

- [ ] **Step 6: Executor 작성**

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.EquipmentViewUseCase;
import org.springframework.stereotype.Component;

@Component
public class EquipmentViewCommandExecutor implements CommandExecutor {
    private final EquipmentViewUseCase useCase;

    public EquipmentViewCommandExecutor(EquipmentViewUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof EquipmentViewCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof EquipmentViewCommand viewCommand)) {
            throw new IllegalArgumentException("Command must be an EquipmentViewCommand");
        }
        useCase.showEquipment(viewCommand);
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/EquipmentViewCommand.java \
        src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EquipmentViewCommandParser.java \
        src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/EquipmentViewCommandExecutor.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/EquipmentViewUseCase.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/EquipmentViewCommandService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EquipmentViewCommandParserTest.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/EquipmentViewCommandServiceTest.java
git commit -m "feat(equip): EquipmentViewCommand — 슬롯별 명칭·아이템·modifier + base→effective stat diff"
```

---

## Task 12: CombatLog에 weaponName 필드 추가

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatLog.java`

CombatLog 는 record + Lombok @Builder. 필드 추가가 binary-incompatible 하지 않으므로 회귀 우려 적음.

- [ ] **Step 1: CombatLog 수정**

Edit `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatLog.java` — 기존 필드 끝에 `String weaponName` 추가:

```java
public record CombatLog(
    UUID attackerId,
    String attackerName,
    UUID targetId,
    String targetName,
    int attackRoll,
    int attackModifier,
    int attackTotal,
    int defenseRoll,
    int defenseModifier,
    int defenseTotal,
    boolean hitSuccess,
    int baseDamage,
    int damageModifier,
    int damageTotal,
    int defenseValue,
    int finalDamage,
    int targetRemainingHp,
    boolean targetDefeated,
    boolean evaded,
    boolean isCrit,
    int attackerApAfter,
    int targetApAfter,
    String weaponTypeName,
    String weaponName
) {
    @Builder
    public CombatLog {
    }
}
```

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew compileJava compileTestJava`
Expected: BUILD SUCCESSFUL. (Lombok @Builder가 setter 자동 생성)

- [ ] **Step 3: 회귀 — 기존 Combat / Narrative 테스트 통과 확인**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.domain.model.combat.*"`
Expected: 기존 테스트 통과 (weaponName은 null 또는 미설정으로 들어가지만 사용처가 아직 변경 전이므로 영향 없음).

Run: `./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.CombatNarrativeFormatterTest"`
Expected: 통과(기존 테스트는 weaponTypeName만 사용).

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatLog.java
git commit -m "feat(combat): CombatLog에 weaponName 필드 추가"
```

---

## Task 13: Combat 무기·무공 선택 + dice max 변경

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/Combat.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatWeaponTest.java`

기존 `Combat.executeGroupAction()` 는 `diceRoller.roll(1, 6)` 으로 baseDamage 산출. 이를:
- 공격자가 PlayerCharacter 이고 WEAPON 슬롯에 무기가 있으면 → weaponType, weaponName 결정
- 무기 없으면 → FIST, "맨손"
- Monster/NPC → FIST, "맨손"
- skillValue = `attacker.getStats().<weaponSkillFor(weaponType)>()` (effective stat에서 조회)
- baseDamage = `diceRoller.roll(1, max(1, skillValue))` (skillValue 0 → 1로 가드)
- CombatLog에 `weaponName`, `weaponTypeName` 모두 채움 (weaponTypeName은 한글이 아닌 enum.name() 유지 — 기존 동작 변경 안 함, narrative formatter가 weaponName 사용하도록 Task 14에서 분리)

- [ ] **Step 1: 테스트 작성**

Create `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatWeaponTest.java`:

```java
package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CombatWeaponTest {

    private PlayerCharacter playerWithSword(int swordSkill) {
        BaseCharacter base = BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(10).physique(10).agility(10).intellect(10).will(10).meridian(10)
                .innerPower(0).specialTechnique(0).lightStep(0)
                .fistsAndPalms(0).swordMethod(swordSkill).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(1L).alive(true).build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(1).experience(0).nextLevelExp(100).conversable(true).build();
        EquippedItems equipped = EquippedItems.create();
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of()).build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));
        return new PlayerCharacter(UUID.randomUUID(), base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), Inventory.create(100), equipped);
    }

    private PlayerCharacter playerBareHanded(int fistSkill) {
        BaseCharacter base = BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(10).physique(10).agility(10).intellect(10).will(10).meridian(10)
                .innerPower(0).specialTechnique(0).lightStep(0)
                .fistsAndPalms(fistSkill).swordMethod(0).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(1L).alive(true).build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(1).experience(0).nextLevelExp(100).conversable(true).build();
        return new PlayerCharacter(UUID.randomUUID(), base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), Inventory.create(100), EquippedItems.create());
    }

    private NonPlayerCharacter dummyMonster() {
        // 가장 가까운 NPC 생성자 사용 — 프로젝트에서 mock 가능한 최소 형태로 변환.
        // 실제 NPC 생성 헬퍼가 있다면 사용; 없으면 Mockito.mock(NonPlayerCharacter.class) 활용
        NonPlayerCharacter npc = org.mockito.Mockito.mock(NonPlayerCharacter.class);
        org.mockito.Mockito.when(npc.getId()).thenReturn(UUID.randomUUID());
        org.mockito.Mockito.when(npc.getName()).thenReturn("다람쥐");
        CharacterStats stats = new CharacterStats(
                20, 10, 10,
                10, 10, 10, 5, 5, 5,
                0, 0, 0, 0, 0, 0, 0, 0, 0);
        org.mockito.Mockito.when(npc.getStats()).thenReturn(stats);
        org.mockito.Mockito.when(npc.isAlive()).thenReturn(true);
        return npc;
    }

    private Combat makeCombat(PlayerCharacter ally, Combatable enemy, DiceRoller roller) {
        CombatParticipant a = new ATBCombatParticipant(ally);
        CombatParticipant e = new ATBCombatParticipant(enemy);
        CombatGroup allyGroup = new CombatGroup(CombatGroupType.ALLY, List.of(a));
        CombatGroup enemyGroup = new CombatGroup(CombatGroupType.ENEMY, List.of(e));
        InitiativeProvider provider = grp -> new InitiativeRoll(10, 0, 10);
        return new Combat(UUID.randomUUID(), allyGroup, enemyGroup, provider, roller);
    }

    @Test
    void executeGroupAction_playerWithSword_logsWeaponNameAndUsesSwordMethodForDiceMax() {
        PlayerCharacter player = playerWithSword(7);
        Combatable enemy = dummyMonster();
        DiceRoller roller = new FixedDiceRoller(20, 7); // attackRoll=20, baseDamage roll=7 (max)
        Combat combat = makeCombat(player, enemy, roller);
        combat.start();
        // 20 tick advance until a turn fires
        CombatActionResult result = null;
        for (int i = 0; i < 21; i++) result = combat.action();
        assertThat(result).isNotNull();
        assertThat(result.getLogs()).isNotEmpty();
        CombatLog log = result.getLogs().get(0);
        assertThat(log.weaponName()).isEqualTo("철검");
        // dice max == swordMethod(7) — 검증은 FixedDiceRoller 가 max=7 호출됐는지 확인
        assertThat(((FixedDiceRoller) roller).getLastBaseDamageMax()).isEqualTo(7);
    }

    @Test
    void executeGroupAction_playerBareHanded_usesFistsAndPalmsAndWeaponName맨손() {
        PlayerCharacter player = playerBareHanded(4);
        Combatable enemy = dummyMonster();
        FixedDiceRoller roller = new FixedDiceRoller(20, 4);
        Combat combat = makeCombat(player, enemy, roller);
        combat.start();
        CombatActionResult result = null;
        for (int i = 0; i < 21; i++) result = combat.action();
        CombatLog log = result.getLogs().get(0);
        assertThat(log.weaponName()).isEqualTo("맨손");
        assertThat(roller.getLastBaseDamageMax()).isEqualTo(4);
    }

    @Test
    void executeGroupAction_skillValueZero_diceMaxGuardedToOne() {
        PlayerCharacter player = playerBareHanded(0);
        Combatable enemy = dummyMonster();
        FixedDiceRoller roller = new FixedDiceRoller(20, 1);
        Combat combat = makeCombat(player, enemy, roller);
        combat.start();
        for (int i = 0; i < 21; i++) combat.action();
        assertThat(roller.getLastBaseDamageMax()).isEqualTo(1);
    }

    // Test helper: dice roller가 마지막으로 호출된 baseDamage roll max를 기록
    static class FixedDiceRoller implements DiceRoller {
        private final int attackOrDefenseRoll;
        private final int baseDamageRoll;
        private int lastBaseDamageMax = -1;
        private int calls = 0;

        FixedDiceRoller(int attackOrDefenseRoll, int baseDamageRoll) {
            this.attackOrDefenseRoll = attackOrDefenseRoll;
            this.baseDamageRoll = baseDamageRoll;
        }

        @Override
        public int roll(int min, int max) {
            calls++;
            // 호출 순서: attackRoll(1..20), defenseRoll(1..20), baseDamage(1..N)
            if (calls == 3) {
                lastBaseDamageMax = max;
                return baseDamageRoll;
            }
            return attackOrDefenseRoll;
        }

        int getLastBaseDamageMax() { return lastBaseDamageMax; }
    }
}
```

- [ ] **Step 2: 실패 확인**

Run: `./gradlew test --tests "...CombatWeaponTest"`
Expected: FAIL — `weaponName()` null 반환(필드는 있지만 Combat이 채우지 않음), 또는 dice max=6.

- [ ] **Step 3: Combat.java 수정**

Edit `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/Combat.java` — `executeGroupAction(...)` 메서드의 attacker 결정 로직 + baseDamage 라인 + recordCombatLog 호출 인자 보강:

핵심 변경부:

```java
// 기존:
// CombatParticipant target = opposingGroup.getTarget();
// ...
// int attackRoll = diceRoller.roll(1, 20);
// ...
// if (hitSuccess) {
//     baseDamage = diceRoller.roll(1, 6);

// 변경:
//   1. attacker 의 무기/무공 결정
//   2. baseDamage dice max = effective 무공 stat 값
//   3. CombatLog 에 weaponName 채움
```

전체 메서드를 다음으로 교체 (변경 라인만 다시 보여줌, 메서드 전체 구조는 유지):

기존 `executeGroupAction(currentActiveGroup)` 메서드 시작 부분 (`for (CombatParticipant attacker : ...)`) 안의:

```java
            Combatable attackerCombatable = attacker.getParticipant();
            Combatable targetCombatable = target.getParticipant();
```

바로 다음에 추가:

```java
            // 장착 무기·무공 결정
            com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType weaponType =
                    com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType.FIST;
            String weaponName = "맨손";
            if (attackerCombatable instanceof com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter pc) {
                java.util.Optional<com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance> wOpt =
                        pc.getEquippedItems().getSlot(com.jefflife.mudmk2.gamedata.application.domain.model.item.EquipmentSlot.WEAPON);
                if (wOpt.isPresent() && wOpt.get().getTemplate() instanceof com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponTemplate wt) {
                    weaponType = wt.getWeaponType();
                    weaponName = wt.getName();
                }
            }
            com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType skillType =
                    com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponTypeMapping.weaponSkillFor(weaponType);
            int skillValue = readSkillStat(attackerCombatable.getStats(), skillType);
            int diceMax = Math.max(1, skillValue);
```

그리고 `if (hitSuccess)` 블록 안의:

```java
                baseDamage = diceRoller.roll(1, 6);
```

을 다음으로 교체:

```java
                baseDamage = diceRoller.roll(1, diceMax);
```

마지막으로 `recordCombatLog(...)` 호출의 끝에 `, weaponName` 인자를 추가하기 위해 `recordCombatLog` 시그니처를 확장. `recordCombatLog` 메서드 시그니처와 builder 호출에 다음을 추가:

```java
            CombatLog combatLog = recordCombatLog(
                attackerCombatable, targetCombatable,
                attackRoll, attackModifier, attackTotal,
                defenseRoll, defenseModifier, defenseTotal,
                hitSuccess,
                baseDamage, damageModifier, damageTotal,
                defenseValue, finalDamage,
                targetRemainingHp, targetDefeated,
                weaponType.name(), weaponName
            );
```

그리고 `recordCombatLog` 메서드 시그니처 마지막 두 인자 추가:

```java
    private CombatLog recordCombatLog(
            Combatable attacker, Combatable target,
            int attackRoll, int attackModifier, int attackTotal,
            int defenseRoll, int defenseModifier, int defenseTotal,
            boolean hitSuccess,
            int baseDamage, int damageModifier, int damageTotal,
            int defenseValue, int finalDamage,
            int targetRemainingHp, boolean targetDefeated,
            String weaponTypeName, String weaponName) {

        CombatLog.CombatLogBuilder logBuilder = CombatLog.builder()
                .attackerId(attacker.getId())
                .attackerName(attacker.getName())
                .targetId(target.getId())
                .targetName(target.getName())
                .attackRoll(attackRoll)
                .attackModifier(attackModifier)
                .attackTotal(attackTotal)
                .defenseRoll(defenseRoll)
                .defenseModifier(defenseModifier)
                .defenseTotal(defenseTotal)
                .hitSuccess(hitSuccess)
                .baseDamage(baseDamage)
                .damageModifier(damageModifier)
                .damageTotal(damageTotal)
                .defenseValue(defenseValue)
                .finalDamage(finalDamage)
                .targetRemainingHp(targetRemainingHp)
                .targetDefeated(targetDefeated)
                .weaponTypeName(weaponTypeName)
                .weaponName(weaponName);

        return logBuilder.build();
    }
```

`readSkillStat` 헬퍼를 Combat 클래스 끝에 추가:

```java
    private static int readSkillStat(com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats stats,
                                     com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType type) {
        return switch (type) {
            case VIGOR -> stats.vigor();
            case PHYSIQUE -> stats.physique();
            case AGILITY -> stats.agility();
            case INTELLECT -> stats.intellect();
            case WILL -> stats.will();
            case MERIDIAN -> stats.meridian();
            case INNER_POWER -> stats.innerPower();
            case SPECIAL_TECHNIQUE -> stats.specialTechnique();
            case LIGHT_STEP -> stats.lightStep();
            case FISTS_AND_PALMS -> stats.fistsAndPalms();
            case SWORD_METHOD -> stats.swordMethod();
            case BLADE_METHOD -> stats.bladeMethod();
            case LONG_WEAPON -> stats.longWeapon();
            case ESOTERIC_WEAPON -> stats.esotericWeapon();
            case ARCHERY -> stats.archery();
        };
    }
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests "...CombatWeaponTest"`
Expected: PASS (3 tests).

회귀:
Run: `./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.domain.model.combat.*"`
Expected: 기존 ATBCombatTest 등이 통과 (weaponName이 null이거나 채워져도 기존 단위 테스트는 가정하지 않음).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/Combat.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatWeaponTest.java
git commit -m "feat(combat): 무기·무공 기반 데미지 — dice max를 effective 무공 stat으로, weaponName 기록"
```

---

## Task 14: CombatNarrativeFormatter — 무기 이름·동사 분기

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatter.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatterTest.java` (있으면 보강, 없으면 신규)

CombatLog 의 `weaponName` 과 `weaponTypeName(enum.name())` 을 사용해서 narrative 출력 시:
- 무기 이름: `log.weaponName()` 사용
- 동사: `WeaponTypeMapping.attackVerb(WeaponType.valueOf(log.weaponTypeName()))` 사용
- 회피/치명타/일반 분기 모두 적용

- [ ] **Step 1: 기존 테스트 존재 확인**

Run: `ls src/test/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatterTest.java`
존재하면 그 파일 확인 → 기존 케이스 보강. 없으면 신규.

- [ ] **Step 2: 테스트 작성 (또는 추가)**

Create/Update `src/test/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatterTest.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.domain.model.combat.CombatLog;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CombatNarrativeFormatterTest {

    private final CombatNarrativeFormatter formatter = new CombatNarrativeFormatter();

    private CombatLog.CombatLogBuilder baseBuilder() {
        return CombatLog.builder()
                .attackerId(UUID.randomUUID()).attackerName("철수")
                .targetId(UUID.randomUUID()).targetName("다람쥐")
                .attackRoll(15).attackModifier(0).attackTotal(15)
                .defenseRoll(10).defenseModifier(0).defenseTotal(10)
                .hitSuccess(true)
                .baseDamage(5).damageModifier(0).damageTotal(5)
                .defenseValue(0).finalDamage(5)
                .targetRemainingHp(15).targetDefeated(false)
                .evaded(false).isCrit(false)
                .attackerApAfter(0).targetApAfter(0)
                .weaponTypeName("SWORD").weaponName("철검");
    }

    @Test
    void format_normalHit_swordWeapon_usesItemNameAndVerb베었다() {
        CombatLog log = baseBuilder().build();
        String result = formatter.format(log);
        assertThat(result).contains("철검").contains("베었다");
        assertThat(result).contains("다람쥐").contains("철수");
    }

    @Test
    void format_normalHit_bladeWeapon_usesVerb내려쳤다() {
        CombatLog log = baseBuilder().weaponTypeName("BLADE").weaponName("청룡도").build();
        assertThat(formatter.format(log)).contains("청룡도").contains("내려쳤다");
    }

    @Test
    void format_normalHit_fist_usesVerb내질렀다() {
        CombatLog log = baseBuilder().weaponTypeName("FIST").weaponName("맨손").build();
        assertThat(formatter.format(log)).contains("맨손").contains("내질렀다");
    }

    @Test
    void format_normalHit_archery_usesVerb쐈다() {
        CombatLog log = baseBuilder().weaponTypeName("ARCHERY").weaponName("나무활").build();
        assertThat(formatter.format(log)).contains("나무활").contains("쐈다");
    }

    @Test
    void format_normalHit_longWeapon_usesVerb휘둘렀다() {
        CombatLog log = baseBuilder().weaponTypeName("LONG_WEAPON").weaponName("장창").build();
        assertThat(formatter.format(log)).contains("장창").contains("휘둘렀다");
    }

    @Test
    void format_normalHit_esoteric_usesVerb휘둘렀다() {
        CombatLog log = baseBuilder().weaponTypeName("ESOTERIC").weaponName("암기").build();
        assertThat(formatter.format(log)).contains("암기").contains("휘둘렀다");
    }

    @Test
    void format_crit_usesWeaponNameAndVerb() {
        CombatLog log = baseBuilder().isCrit(true).build();
        String result = formatter.format(log);
        assertThat(result).contains("철검").contains("베었다").contains("치명");
    }

    @Test
    void format_evaded_usesWeaponNameAndVerb() {
        CombatLog log = baseBuilder().hitSuccess(false).evaded(true).build();
        String result = formatter.format(log);
        assertThat(result).contains("철검").contains("베었다").contains("피했다");
    }

    @Test
    void format_targetDefeated_appendsDefeatedLine() {
        CombatLog log = baseBuilder().targetDefeated(true).build();
        assertThat(formatter.format(log)).contains("쓰러졌다");
    }
}
```

- [ ] **Step 3: 실패 확인**

Run: `./gradlew test --tests "...CombatNarrativeFormatterTest"`
Expected: FAIL — 동사 분기 없음, weaponName 미사용.

- [ ] **Step 4: CombatNarrativeFormatter 수정**

Replace `src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatter.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponTypeMapping;
import com.jefflife.mudmk2.gameplay.application.domain.model.combat.CombatLog;
import org.springframework.stereotype.Component;

@Component
public class CombatNarrativeFormatter {

    public String format(CombatLog log) {
        String verb = verbFor(log.weaponTypeName());
        String weapon = log.weaponName() != null ? log.weaponName() : "맨손";

        StringBuilder sb = new StringBuilder();

        if (log.evaded()) {
            sb.append(String.format("%s이(가) %s을(를) %s(으)로 %s지만 피했다!",
                    log.attackerName(), log.targetName(), weapon, verb));
        } else if (log.isCrit()) {
            sb.append(String.format("%s이(가) %s을(를) %s(으)로 치명적으로 %s!",
                    log.attackerName(), log.targetName(), weapon, verb));
            sb.append(String.format("\n  → %d 데미지! (남은 HP: %d)",
                    log.finalDamage(), log.targetRemainingHp()));
        } else {
            sb.append(String.format("%s이(가) %s을(를) %s(으)로 힘껏 %s!",
                    log.attackerName(), log.targetName(), weapon, verb));
            sb.append(String.format("\n  → %d 데미지! (남은 HP: %d)",
                    log.finalDamage(), log.targetRemainingHp()));
        }

        if (log.targetDefeated()) {
            sb.append(String.format("\n%s이(가) 쓰러졌다!", log.targetName()));
        }

        return sb.toString();
    }

    private String verbFor(String weaponTypeName) {
        if (weaponTypeName == null) {
            return WeaponTypeMapping.attackVerb(WeaponType.FIST);
        }
        try {
            return WeaponTypeMapping.attackVerb(WeaponType.valueOf(weaponTypeName));
        } catch (IllegalArgumentException e) {
            return WeaponTypeMapping.attackVerb(WeaponType.FIST);
        }
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests "...CombatNarrativeFormatterTest"`
Expected: PASS (9 tests).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatter.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatterTest.java
git commit -m "feat(combat): narrative — 무기 이름 + 무기 타입별 동사 분기"
```

---

## Task 15: 전체 회귀 + 통합 확인

- [ ] **Step 1: 전체 테스트 실행**

Run: `./gradlew test`
Expected: 모든 테스트 통과. 실패가 있으면 어느 task에서 회귀가 발생했는지 식별해 그 task로 돌아가 수정.

- [ ] **Step 2: 빌드 확인**

Run: `./gradlew build -x test`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: DB 테이블 수동 생성**

운영 서비스가 아니므로 사용자가 DB 테이블을 직접 생성한다. 생성할 테이블:

```sql
CREATE TABLE equipped_items (
    id BINARY(16) PRIMARY KEY
);

ALTER TABLE item_instance
    ADD COLUMN equipped_items_id BINARY(16) NULL,
    ADD COLUMN equipped_slot VARCHAR(20) NULL,
    ADD CONSTRAINT fk_item_instance_equipped_items
        FOREIGN KEY (equipped_items_id) REFERENCES equipped_items(id);

ALTER TABLE player_character
    ADD COLUMN equipped_items_id BINARY(16) NULL,
    ADD CONSTRAINT fk_player_character_equipped_items
        FOREIGN KEY (equipped_items_id) REFERENCES equipped_items(id);
```

`spring.jpa.hibernate.ddl-auto=update` 이 활성화되어 있다면 Hibernate 가 자동 생성할 수도 있다 — 사용자에게 확인 요청.

- [ ] **Step 4: 통합 시나리오 수동 점검**

게임을 띄워 다음 시나리오 실행:

1. 캐릭터 생성 → 시작 위치
2. ItemTemplate으로 철검(WEAPON/SWORD, +2 vigor +5 sword_method) 생성, 인벤토리에 추가
3. EquipmentTemplate으로 야구모자(HELMET, +1 agility) 생성, 인벤토리 추가
4. `장비` → 모든 슬롯 "(없음)" + 적용 스탯 섹션 비어 있음
5. `철검 장착` → "철검을 장착했다"
6. `야구모자 입어` → "야구모자를 장착했다"
7. `장비` → 무기/머리 슬롯에 아이템 표시, base→effective stat 차이 표시
8. 다람쥐 spawn → `다람쥐 공격` → narrative "철수이(가) 다람쥐을(를) 철검(으)로 힘껏 베었다!" 확인
9. `철검 벗어` → "철검을 해제했다", 인벤토리에 복귀
10. `장비` → 무기 슬롯 "(없음)"

- [ ] **Step 5: Plan 완료 commit (선택)**

수동 시나리오 통과 후 별도 변경이 없으면 commit 불필요. 변경이 있으면:

```bash
git add <files>
git commit -m "chore(equip): 통합 시나리오 점검 및 보정"
```

---

## 자기 검토 (작성자용 메모)

### Spec 커버리지

| Spec 요구사항 | 구현 Task |
|---|---|
| 10슬롯 (반지 2개형) | Task 1 |
| EquippedItems @OneToOne (Inventory 동일 레벨) | Task 5, 6 |
| ItemInstance에 equipped_items_id FK | Task 4 |
| Effective stat 계산 레이어 | Task 6 |
| getBaseStats() 분리 | Task 6 |
| EquippableItemTemplate 마커 인터페이스 | Task 3 |
| WeaponType → StatType 매핑 | Task 2 |
| 무기 타입별 동사 매핑 | Task 2 |
| EquipCommand + alias 다수 | Task 8, 9 |
| UnequipCommand + alias 다수 | Task 8, 10 |
| EquipmentViewCommand "장비"/"장비창" | Task 8, 11 |
| 반지 자동 슬롯 선택 (left→right→left swap) | Task 9 |
| 슬롯 충돌 시 자동 해제 후 교체 | Task 9 |
| 무게 초과 시 모든 경우 거부 | Task 9, 10 |
| 맨손 = FIST + FISTS_AND_PALMS | Task 13 |
| Dice max = effective 무공 stat | Task 13 |
| Combat narrative 무기 이름 + 동사 | Task 14 |
| "장비" 조회 출력 (슬롯·아이템·modifier + base→effective) | Task 11 |
| 캐릭터 생성 시 EquippedItems 부여 | Task 6 Step 4, Task 7 |

### 타입 일관성 확인

- `EquippedItems.create()` 시그니처 — Task 5, 6, 7, 9, 10, 11 모두 동일하게 사용
- `equip(slot, instance) → Optional<ItemInstance>` — Task 5에서 정의, Task 9에서 사용
- `findByItemName(name) → Optional<Map.Entry<...>>` — Task 5 정의, Task 10 사용
- `WeaponTypeMapping.weaponSkillFor / attackVerb` — Task 2 정의, Task 13/14 사용
- `PlayerCharacter` 생성자 10-인자 — Task 6에서 정의, Task 6 Step 4와 Task 9/10/11 테스트에서 동일하게 사용
- `EquipmentSlot.displayName()` — Task 1 정의, Task 11 사용
- `CombatLog.weaponName` — Task 12 추가, Task 13/14 사용

### Placeholder 스캔

- "TBD", "TODO", "implement later" 부재 확인 완료
- 모든 step에 코드 또는 명확한 명령 포함
- 각 task 종료 시 commit 명령 명시
