# 아이템 & 소지품 시스템 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 아이템 타입 계층(ItemTemplate), 아이템 인스턴스(ItemInstance), 소지품(Inventory)을 구현하고 줍기/버리기/소지품 조회 커맨드를 추가한다.

**Architecture:** JPA JOINED 상속으로 ItemTemplate 계층을 구성하고, Room과 Inventory가 ItemInstance 컬렉션을 소유한다. 기존 커맨드 체인(Command → Parser → Executor → UseCase → Service) 패턴을 그대로 따른다.

**Tech Stack:** Spring Boot, JPA/Hibernate, Lombok, JUnit 5, AssertJ

---

## 파일 맵

### 신규 생성

```
src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/
  ItemType.java
  WeaponType.java
  EquipmentSlot.java
  AccessoryType.java
  MissionItemType.java
  StatType.java
  StatModifier.java
  ItemTemplate.java
  FoodTemplate.java
  WeaponTemplate.java
  EquipmentTemplate.java
  AccessoryTemplate.java
  MartialArtsBookTemplate.java
  MissionItemTemplate.java
  ItemInstance.java

src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/
  Inventory.java

src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/
  ItemTemplateRepository.java
  ItemInstanceRepository.java
  InventoryRepository.java

src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/
  DropCommand.java
  InventoryCommand.java

src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/
  DropUseCase.java
  InventoryUseCase.java

src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/
  TakeCommandService.java
  DropCommandService.java
  InventoryCommandService.java

src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/
  TakeCommandParser.java
  DropCommandParser.java
  InventoryCommandParser.java

src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/
  TakeCommandExecutor.java
  DropCommandExecutor.java
  InventoryCommandExecutor.java
```

### 수정

```
src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/
  TakeCommand.java        ← 필드 재정의 (itemName, index)

src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/
  TakeUseCase.java        ← 서명 변경

src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/
  CommandDictionary.java  ← TAKE, DROP, INVENTORY 추가

src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/map/
  Room.java               ← floorItems 컬렉션 + 관련 메서드 추가

src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/
  PlayerCharacter.java    ← inventory @OneToOne 추가

src/main/java/com/jefflife/mudmk2/gamedata/application/service/
  PlayerCharacterService.java ← Inventory 생성 포함
```

---

## Task 1: Enum 타입 + StatModifier

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemType.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponType.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentSlot.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/AccessoryType.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/MissionItemType.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/StatType.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/StatModifier.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/StatModifierTest.java`

- [ ] **Step 1: 테스트 작성**

```java
// src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/StatModifierTest.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class StatModifierTest {

    @Test
    void statModifier_holdsStatTypeAndValue() {
        StatModifier modifier = new StatModifier(StatType.VIGOR, 10);
        assertThat(modifier.getStatType()).isEqualTo(StatType.VIGOR);
        assertThat(modifier.getValue()).isEqualTo(10);
    }

    @Test
    void statModifier_supportsAllStatTypes() {
        for (StatType type : StatType.values()) {
            StatModifier modifier = new StatModifier(type, 5);
            assertThat(modifier.getStatType()).isEqualTo(type);
        }
    }
}
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifierTest"
```
Expected: FAIL (StatModifier 클래스 없음)

- [ ] **Step 3: Enum 타입 구현**

```java
// ItemType.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;
public enum ItemType { FOOD, WEAPON, EQUIPMENT, ACCESSORY, MARTIAL_ARTS_BOOK, MISSION }
```

```java
// WeaponType.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;
public enum WeaponType { SWORD, BLADE, FIST, ARCHERY, ESOTERIC, LONG_WEAPON }
```

```java
// EquipmentSlot.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;
public enum EquipmentSlot { HELMET, UPPER_ARMOR, LOWER_ARMOR, GLOVES, BOOTS, BELT }
```

```java
// AccessoryType.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;
public enum AccessoryType { NECKLACE, RING }
```

```java
// MissionItemType.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;
public enum MissionItemType { KEY, QUEST_COMPLETION }
```

```java
// StatType.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;
public enum StatType {
    VIGOR, PHYSIQUE, AGILITY, INTELLECT, WILL, MERIDIAN,
    INNER_POWER, SPECIAL_TECHNIQUE, LIGHT_STEP,
    FISTS_AND_PALMS, SWORD_METHOD, BLADE_METHOD,
    LONG_WEAPON, ESOTERIC_WEAPON, ARCHERY
}
```

```java
// StatModifier.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StatModifier {
    @Enumerated(EnumType.STRING)
    private StatType statType;
    private int value;
}
```

- [ ] **Step 4: 테스트 실행 - 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifierTest"
```
Expected: PASS

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/
git add src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/StatModifierTest.java
git commit -m "feat: 아이템 Enum 타입 및 StatModifier 추가"
```

---

## Task 2: ItemTemplate 추상 클래스 + 서브클래스

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemTemplate.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/FoodTemplate.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTemplate.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentTemplate.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/AccessoryTemplate.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/MartialArtsBookTemplate.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/MissionItemTemplate.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemTemplateTest.java`

- [ ] **Step 1: 테스트 작성**

```java
// src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemTemplateTest.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ItemTemplateTest {

    @Test
    void foodTemplate_storesRecoveryValues() {
        FoodTemplate food = FoodTemplate.builder()
                .name("만두").description("맛있는 만두").weight(1).stackable(true)
                .hpRecovery(0).mpRecovery(0).apRecovery(50)
                .build();
        assertThat(food.getName()).isEqualTo("만두");
        assertThat(food.getApRecovery()).isEqualTo(50);
        assertThat(food.isStackable()).isTrue();
        assertThat(food.getItemType()).isEqualTo(ItemType.FOOD);
    }

    @Test
    void weaponTemplate_storesWeaponTypeAndStatModifiers() {
        StatModifier modifier = new StatModifier(StatType.SWORD_METHOD, 15);
        WeaponTemplate weapon = WeaponTemplate.builder()
                .name("철검").description("평범한 철검").weight(3).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(modifier))
                .build();
        assertThat(weapon.getWeaponType()).isEqualTo(WeaponType.SWORD);
        assertThat(weapon.getStatModifiers()).hasSize(1);
        assertThat(weapon.getStatModifiers().get(0).getValue()).isEqualTo(15);
        assertThat(weapon.getItemType()).isEqualTo(ItemType.WEAPON);
    }

    @Test
    void equipmentTemplate_storesSlotAndStatModifiers() {
        EquipmentTemplate equipment = EquipmentTemplate.builder()
                .name("철투구").description("평범한 투구").weight(2).stackable(false)
                .equipmentSlot(EquipmentSlot.HELMET)
                .statModifiers(List.of(new StatModifier(StatType.PHYSIQUE, 5)))
                .build();
        assertThat(equipment.getEquipmentSlot()).isEqualTo(EquipmentSlot.HELMET);
        assertThat(equipment.getItemType()).isEqualTo(ItemType.EQUIPMENT);
    }

    @Test
    void missionItemTemplate_storesTypeAndTargetRef() {
        MissionItemTemplate key = MissionItemTemplate.builder()
                .name("금빛 열쇠").description("금고를 여는 열쇠").weight(0).stackable(false)
                .missionItemType(MissionItemType.KEY)
                .targetRef("door-123")
                .build();
        assertThat(key.getMissionItemType()).isEqualTo(MissionItemType.KEY);
        assertThat(key.getTargetRef()).isEqualTo("door-123");
        assertThat(key.getItemType()).isEqualTo(ItemType.MISSION);
    }
}
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplateTest"
```
Expected: FAIL (ItemTemplate 클래스 없음)

- [ ] **Step 3: ItemTemplate 추상 클래스 구현**

```java
// ItemTemplate.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_template")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "item_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ItemTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    private int weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", insertable = false, updatable = false)
    private ItemType itemType;

    private boolean stackable;

    protected ItemTemplate(String name, String description, int weight, ItemType itemType, boolean stackable) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.itemType = itemType;
        this.stackable = stackable;
    }
}
```

- [ ] **Step 4: FoodTemplate 구현**

```java
// FoodTemplate.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "food_template")
@DiscriminatorValue("FOOD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodTemplate extends ItemTemplate {
    private int hpRecovery;
    private int mpRecovery;
    private int apRecovery;

    @Builder
    public FoodTemplate(String name, String description, int weight, boolean stackable,
                        int hpRecovery, int mpRecovery, int apRecovery) {
        super(name, description, weight, ItemType.FOOD, stackable);
        this.hpRecovery = hpRecovery;
        this.mpRecovery = mpRecovery;
        this.apRecovery = apRecovery;
    }
}
```

- [ ] **Step 5: WeaponTemplate 구현**

```java
// WeaponTemplate.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "weapon_template")
@DiscriminatorValue("WEAPON")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeaponTemplate extends ItemTemplate {
    @Enumerated(EnumType.STRING)
    private WeaponType weaponType;

    @ElementCollection
    @CollectionTable(name = "weapon_stat_modifiers", joinColumns = @JoinColumn(name = "weapon_template_id"))
    private List<StatModifier> statModifiers = new ArrayList<>();

    @Builder
    public WeaponTemplate(String name, String description, int weight, boolean stackable,
                          WeaponType weaponType, List<StatModifier> statModifiers) {
        super(name, description, weight, ItemType.WEAPON, stackable);
        this.weaponType = weaponType;
        if (statModifiers != null) this.statModifiers.addAll(statModifiers);
    }
}
```

- [ ] **Step 6: EquipmentTemplate 구현**

```java
// EquipmentTemplate.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipment_template")
@DiscriminatorValue("EQUIPMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquipmentTemplate extends ItemTemplate {
    @Enumerated(EnumType.STRING)
    private EquipmentSlot equipmentSlot;

    @ElementCollection
    @CollectionTable(name = "equipment_stat_modifiers", joinColumns = @JoinColumn(name = "equipment_template_id"))
    private List<StatModifier> statModifiers = new ArrayList<>();

    @Builder
    public EquipmentTemplate(String name, String description, int weight, boolean stackable,
                             EquipmentSlot equipmentSlot, List<StatModifier> statModifiers) {
        super(name, description, weight, ItemType.EQUIPMENT, stackable);
        this.equipmentSlot = equipmentSlot;
        if (statModifiers != null) this.statModifiers.addAll(statModifiers);
    }
}
```

- [ ] **Step 7: AccessoryTemplate 구현**

```java
// AccessoryTemplate.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accessory_template")
@DiscriminatorValue("ACCESSORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccessoryTemplate extends ItemTemplate {
    @Enumerated(EnumType.STRING)
    private AccessoryType accessoryType;

    @ElementCollection
    @CollectionTable(name = "accessory_stat_modifiers", joinColumns = @JoinColumn(name = "accessory_template_id"))
    private List<StatModifier> statModifiers = new ArrayList<>();

    @Builder
    public AccessoryTemplate(String name, String description, int weight, boolean stackable,
                             AccessoryType accessoryType, List<StatModifier> statModifiers) {
        super(name, description, weight, ItemType.ACCESSORY, stackable);
        this.accessoryType = accessoryType;
        if (statModifiers != null) this.statModifiers.addAll(statModifiers);
    }
}
```

- [ ] **Step 8: MartialArtsBookTemplate 구현**

```java
// MartialArtsBookTemplate.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "martial_arts_book_template")
@DiscriminatorValue("MARTIAL_ARTS_BOOK")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MartialArtsBookTemplate extends ItemTemplate {
    private String skillRef;

    @Builder
    public MartialArtsBookTemplate(String name, String description, int weight, boolean stackable,
                                   String skillRef) {
        super(name, description, weight, ItemType.MARTIAL_ARTS_BOOK, stackable);
        this.skillRef = skillRef;
    }
}
```

- [ ] **Step 9: MissionItemTemplate 구현**

```java
// MissionItemTemplate.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mission_item_template")
@DiscriminatorValue("MISSION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionItemTemplate extends ItemTemplate {
    @Enumerated(EnumType.STRING)
    private MissionItemType missionItemType;

    private String targetRef;

    @Builder
    public MissionItemTemplate(String name, String description, int weight, boolean stackable,
                               MissionItemType missionItemType, String targetRef) {
        super(name, description, weight, ItemType.MISSION, stackable);
        this.missionItemType = missionItemType;
        this.targetRef = targetRef;
    }
}
```

- [ ] **Step 10: 테스트 실행 - 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplateTest"
```
Expected: PASS

- [ ] **Step 11: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/
git add src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemTemplateTest.java
git commit -m "feat: ItemTemplate 계층 구조 추가 (JOINED 상속)"
```

---

## Task 3: ItemInstance

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstance.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstanceTest.java`

- [ ] **Step 1: 테스트 작성**

```java
// src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstanceTest.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ItemInstanceTest {

    private FoodTemplate makeFood() {
        return FoodTemplate.builder()
                .name("만두").description("맛있는 만두").weight(1).stackable(true)
                .hpRecovery(0).mpRecovery(0).apRecovery(50)
                .build();
    }

    @Test
    void itemInstance_createdWithQuantityOne_byDefault() {
        FoodTemplate food = makeFood();
        ItemInstance instance = new ItemInstance(food, 1);
        assertThat(instance.getQuantity()).isEqualTo(1);
        assertThat(instance.getTemplate()).isSameAs(food);
    }

    @Test
    void addQuantity_increasesQuantity() {
        FoodTemplate food = makeFood();
        ItemInstance instance = new ItemInstance(food, 1);
        instance.addQuantity(3);
        assertThat(instance.getQuantity()).isEqualTo(4);
    }
}
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstanceTest"
```
Expected: FAIL

- [ ] **Step 3: ItemInstance 구현**

```java
// ItemInstance.java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public ItemInstance(ItemTemplate template, int quantity) {
        this.template = template;
        this.quantity = quantity;
    }

    public void addQuantity(int amount) {
        this.quantity += amount;
    }
}
```

- [ ] **Step 4: 테스트 실행 - 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstanceTest"
```
Expected: PASS

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstance.java
git add src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstanceTest.java
git commit -m "feat: ItemInstance 추가"
```

---

## Task 4: Inventory

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Inventory.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/InventoryTest.java`

- [ ] **Step 1: 테스트 작성**

```java
// src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/InventoryTest.java
package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class InventoryTest {

    private Inventory inventory;
    private FoodTemplate foodTemplate;
    private WeaponTemplate swordTemplate;

    @BeforeEach
    void setUp() {
        inventory = Inventory.create(100);
        foodTemplate = FoodTemplate.builder()
                .name("만두").description("맛있는 만두").weight(1).stackable(true)
                .hpRecovery(0).mpRecovery(0).apRecovery(50).build();
        swordTemplate = WeaponTemplate.builder()
                .name("철검").description("평범한 철검").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD).statModifiers(List.of()).build();
    }

    @Test
    void canAdd_returnsTrueWhenWeightFits() {
        assertThat(inventory.canAdd(swordTemplate, 1)).isTrue();
    }

    @Test
    void canAdd_returnsFalseWhenWeightExceeds() {
        // 무게 5짜리 아이템 20개 = 100 (한도)이면 딱 맞음
        for (int i = 0; i < 20; i++) {
            inventory.addItem(new ItemInstance(swordTemplate, 1));
        }
        assertThat(inventory.canAdd(swordTemplate, 1)).isFalse();
    }

    @Test
    void addItem_stackableItem_mergesQuantity() {
        inventory.addItem(new ItemInstance(foodTemplate, 3));
        inventory.addItem(new ItemInstance(foodTemplate, 2));
        assertThat(inventory.getItems()).hasSize(1);
        assertThat(inventory.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void addItem_nonStackableItem_addsSeparateInstances() {
        inventory.addItem(new ItemInstance(swordTemplate, 1));
        inventory.addItem(new ItemInstance(swordTemplate, 1));
        assertThat(inventory.getItems()).hasSize(2);
    }

    @Test
    void removeItem_removesFromList() {
        ItemInstance instance = new ItemInstance(swordTemplate, 1);
        inventory.addItem(instance);
        inventory.removeItem(instance);
        assertThat(inventory.getItems()).isEmpty();
    }

    @Test
    void findItemsByName_returnsMatchingItems() {
        inventory.addItem(new ItemInstance(swordTemplate, 1));
        inventory.addItem(new ItemInstance(swordTemplate, 1));
        List<ItemInstance> found = inventory.findItemsByName("철검");
        assertThat(found).hasSize(2);
    }

    @Test
    void currentWeight_calculatesCorrectly() {
        inventory.addItem(new ItemInstance(swordTemplate, 1)); // 5
        inventory.addItem(new ItemInstance(foodTemplate, 3));  // 1 * 3 = 3
        assertThat(inventory.currentWeight()).isEqualTo(8);
    }
}
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.InventoryTest"
```
Expected: FAIL

- [ ] **Step 3: Inventory 구현**

```java
// Inventory.java
package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private int maxWeightCapacity;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "inventory_id")
    private List<ItemInstance> items = new ArrayList<>();

    private Inventory(int maxWeightCapacity) {
        this.maxWeightCapacity = maxWeightCapacity;
    }

    public static Inventory create(int maxWeightCapacity) {
        return new Inventory(maxWeightCapacity);
    }

    public boolean canAdd(ItemTemplate template, int qty) {
        return currentWeight() + template.getWeight() * qty <= maxWeightCapacity;
    }

    public int currentWeight() {
        return items.stream()
                .mapToInt(i -> i.getTemplate().getWeight() * i.getQuantity())
                .sum();
    }

    public void addItem(ItemInstance instance) {
        if (instance.getTemplate().isStackable()) {
            Optional<ItemInstance> existing = items.stream()
                    .filter(i -> i.getTemplate().equals(instance.getTemplate()))
                    .findFirst();
            if (existing.isPresent()) {
                existing.get().addQuantity(instance.getQuantity());
                return;
            }
        }
        items.add(instance);
    }

    public void removeItem(ItemInstance instance) {
        items.remove(instance);
    }

    public List<ItemInstance> findItemsByName(String name) {
        return items.stream()
                .filter(i -> i.getTemplate().getName().equals(name))
                .toList();
    }
}
```

- [ ] **Step 4: 테스트 실행 - 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.InventoryTest"
```
Expected: PASS

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Inventory.java
git add src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/InventoryTest.java
git commit -m "feat: Inventory 엔티티 추가"
```

---

## Task 5: Room에 floorItems 추가

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/map/Room.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/map/RoomFloorItemsTest.java`

- [ ] **Step 1: 테스트 작성**

```java
// src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/map/RoomFloorItemsTest.java
package com.jefflife.mudmk2.gamedata.application.domain.model.map;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class RoomFloorItemsTest {

    private Room room;
    private WeaponTemplate swordTemplate;

    @BeforeEach
    void setUp() {
        room = Room.builder()
                .areaId(1L).name("테스트 방").summary("테스트").description("테스트용 방").build();
        swordTemplate = WeaponTemplate.builder()
                .name("철검").description("평범한 철검").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD).statModifiers(List.of()).build();
    }

    @Test
    void addFloorItem_addsItemToFloor() {
        ItemInstance item = new ItemInstance(swordTemplate, 1);
        room.addFloorItem(item);
        assertThat(room.getFloorItems()).hasSize(1);
    }

    @Test
    void removeFloorItem_removesItemFromFloor() {
        ItemInstance item = new ItemInstance(swordTemplate, 1);
        room.addFloorItem(item);
        room.removeFloorItem(item);
        assertThat(room.getFloorItems()).isEmpty();
    }

    @Test
    void findFloorItemsByName_returnsMatchingItemsInOrder() {
        ItemInstance sword1 = new ItemInstance(swordTemplate, 1);
        ItemInstance sword2 = new ItemInstance(swordTemplate, 1);
        room.addFloorItem(sword1);
        room.addFloorItem(sword2);
        List<ItemInstance> found = room.findFloorItemsByName("철검");
        assertThat(found).hasSize(2);
        assertThat(found.get(0)).isSameAs(sword1);
        assertThat(found.get(1)).isSameAs(sword2);
    }

    @Test
    void findFloorItemsByName_returnsEmptyWhenNoMatch() {
        assertThat(room.findFloorItemsByName("없는아이템")).isEmpty();
    }
}
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomFloorItemsTest"
```
Expected: FAIL

- [ ] **Step 3: Room.java 수정**

`Room.java` 클래스 body 안에 다음을 추가한다 (기존 `@Embedded private WayOuts wayOuts` 필드 아래):

```java
@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinColumn(name = "room_id")
private List<ItemInstance> floorItems = new ArrayList<>();
```

Room 클래스 메서드 끝에 다음 메서드를 추가한다:

```java
public List<ItemInstance> getFloorItems() {
    return Collections.unmodifiableList(floorItems);
}

public void addFloorItem(ItemInstance item) {
    this.floorItems.add(item);
}

public void removeFloorItem(ItemInstance item) {
    this.floorItems.remove(item);
}

public List<ItemInstance> findFloorItemsByName(String name) {
    return floorItems.stream()
            .filter(i -> i.getTemplate().getName().equals(name))
            .toList();
}
```

`Room.java` 상단 imports에 추가:
```java
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import java.util.ArrayList;
import java.util.Collections;
```

- [ ] **Step 4: 테스트 실행 - 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomFloorItemsTest"
```
Expected: PASS

- [ ] **Step 5: 전체 테스트 확인**

```bash
./gradlew test
```
Expected: 기존 테스트 모두 통과

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/map/Room.java
git add src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/map/RoomFloorItemsTest.java
git commit -m "feat: Room에 floorItems 컬렉션 추가"
```

---

## Task 6: PlayerCharacter에 Inventory 추가 + PlayerCharacterService 수정

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/PlayerCharacterService.java`

- [ ] **Step 1: PlayerCharacter.java 수정**

`PlayerCharacter` 클래스에 필드 추가 (기존 `private LocalDateTime lastActiveAt;` 아래):

```java
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "inventory_id")
private Inventory inventory;
```

import 추가:
```java
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToOne;
```

기존 생성자에 `Inventory inventory` 파라미터 추가:

```java
public PlayerCharacter(
        final UUID id,
        final BaseCharacter baseCharacterInfo,
        final PlayableCharacter playableCharacterInfo,
        final Long userId,
        final String nickname,
        final CharacterClass characterClass,
        final boolean online,
        final LocalDateTime lastActiveAt,
        final Inventory inventory
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
}
```

- [ ] **Step 2: PlayerCharacterService.java 수정**

`createCharacter` 메서드에서 `PlayerCharacter` 생성 코드를 수정한다. 기존:

```java
final PlayerCharacter playerCharacter = new PlayerCharacter(
        null,
        baseCharacter,
        playableCharacter,
        userId,
        name,
        characterClass,
        true,
        LocalDateTime.now()
);
```

수정 후:

```java
final Inventory inventory = Inventory.create(100);

final PlayerCharacter playerCharacter = new PlayerCharacter(
        null,
        baseCharacter,
        playableCharacter,
        userId,
        name,
        characterClass,
        true,
        LocalDateTime.now(),
        inventory
);
```

import 추가:
```java
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
```

- [ ] **Step 3: 전체 테스트 확인**

```bash
./gradlew test
```
Expected: 기존 테스트 모두 통과 (컴파일 오류 없음 확인)

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/PlayerCharacterService.java
git commit -m "feat: PlayerCharacter에 Inventory 추가, 캐릭터 생성 시 기본 인벤토리 생성"
```

---

## Task 7: Repository 인터페이스 추가

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/ItemTemplateRepository.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/ItemInstanceRepository.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/InventoryRepository.java`

- [ ] **Step 1: Repository 인터페이스 구현**

```java
// ItemTemplateRepository.java
package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ItemTemplateRepository extends CrudRepository<ItemTemplate, Long> {
    List<ItemTemplate> findByName(String name);
}
```

```java
// ItemInstanceRepository.java
package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import org.springframework.data.repository.CrudRepository;

public interface ItemInstanceRepository extends CrudRepository<ItemInstance, Long> {
}
```

```java
// InventoryRepository.java
package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface InventoryRepository extends CrudRepository<Inventory, UUID> {
}
```

- [ ] **Step 2: 전체 빌드 확인**

```bash
./gradlew build -x test
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/ItemTemplateRepository.java
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/ItemInstanceRepository.java
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/InventoryRepository.java
git commit -m "feat: Item, Inventory Repository 인터페이스 추가"
```

---

## Task 8: TakeCommand 재정의 + CommandDictionary + Parser + Executor + Service

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/TakeCommand.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionary.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/TakeUseCase.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/TakeCommandParser.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/TakeCommandExecutor.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/TakeCommandService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/TakeCommandParserTest.java`

- [ ] **Step 1: 파서 테스트 작성**

```java
// src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/TakeCommandParserTest.java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.TakeCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TakeCommandParserTest {

    private TakeCommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new TakeCommandParser();
    }

    @Test
    void parse_itemNameOnly_returnsFirstIndex() {
        Command command = parser.parse(1L, "철검 주워");
        assertThat(command).isInstanceOf(TakeCommand.class);
        TakeCommand take = (TakeCommand) command;
        assertThat(take.itemName()).isEqualTo("철검");
        assertThat(take.index()).isEqualTo(1);
    }

    @Test
    void parse_itemNameWithIndex_returnsCorrectIndex() {
        Command command = parser.parse(1L, "철검 2 주워");
        assertThat(command).isInstanceOf(TakeCommand.class);
        TakeCommand take = (TakeCommand) command;
        assertThat(take.itemName()).isEqualTo("철검");
        assertThat(take.index()).isEqualTo(2);
    }

    @Test
    void parse_alternativeVerb_줍다_returns() {
        Command command = parser.parse(1L, "만두 줍다");
        assertThat(command).isInstanceOf(TakeCommand.class);
    }

    @Test
    void parse_nonTakeCommand_returnsNull() {
        Command command = parser.parse(1L, "동");
        assertThat(command).isNull();
    }
}
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser.TakeCommandParserTest"
```
Expected: FAIL

- [ ] **Step 3: TakeCommand 재정의**

```java
// TakeCommand.java
package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record TakeCommand(
    Long userId,
    String itemName,
    int index
) implements Command {}
```

- [ ] **Step 4: CommandDictionary에 TAKE 추가**

기존 `CommandDictionary.java`에 TAKE 항목 추가:

```java
public enum CommandDictionary {
    ATTACK("공격", "때려", "공"),
    TAKE("줍다", "주워", "집어"),
    DROP("버리다", "버려", "놓다"),
    INVENTORY("소지품", "가방", "인벤");

    private final Set<String> aliases;

    CommandDictionary(String... aliases) {
        this.aliases = Set.of(aliases);
    }

    public String toRegex() {
        return String.join("|", aliases);
    }
}
```

- [ ] **Step 5: TakeUseCase 수정**

```java
// TakeUseCase.java
package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.TakeCommand;

public interface TakeUseCase {
    void take(TakeCommand command);
}
```

(서명이 이미 같으면 변경 불필요)

- [ ] **Step 6: TakeCommandParser 구현**

```java
// TakeCommandParser.java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionary;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.TakeCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TakeCommandParser extends AbstractCommandParser {
    private static final Pattern TAKE_PATTERN =
            Pattern.compile("(\\S+?)(?:\\s+(\\d+))?\\s+(" + CommandDictionary.TAKE.toRegex() + ")");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = TAKE_PATTERN.matcher(content);
        if (matcher.matches()) {
            String itemName = matcher.group(1);
            int index = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 1;
            return new TakeCommand(userId, itemName, index);
        }
        return null;
    }
}
```

- [ ] **Step 7: 파서 테스트 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser.TakeCommandParserTest"
```
Expected: PASS

- [ ] **Step 8: TakeCommandExecutor 구현**

```java
// TakeCommandExecutor.java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.TakeCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.TakeUseCase;
import org.springframework.stereotype.Component;

@Component
public class TakeCommandExecutor implements CommandExecutor {
    private final TakeUseCase takeUseCase;

    public TakeCommandExecutor(TakeUseCase takeUseCase) {
        this.takeUseCase = takeUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof TakeCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof TakeCommand takeCommand)) {
            throw new IllegalArgumentException("Command must be a TakeCommand");
        }
        takeUseCase.take(takeCommand);
    }
}
```

- [ ] **Step 9: TakeCommandService 구현**

```java
// TakeCommandService.java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.TakeCommand;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import com.jefflife.mudmk2.gameplay.application.service.provided.TakeUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TakeCommandService implements TakeUseCase {
    private final GameWorldService gameWorldService;
    private final SendMessageToUserPort sendMessageToUserPort;

    public TakeCommandService(GameWorldService gameWorldService, SendMessageToUserPort sendMessageToUserPort) {
        this.gameWorldService = gameWorldService;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void take(TakeCommand command) {
        PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
        Room room = gameWorldService.getRoom(player.getCurrentRoomId());

        List<ItemInstance> matching = room.findFloorItemsByName(command.itemName());
        if (matching.isEmpty()) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    command.itemName() + "(을)를 찾을 수 없습니다.");
            return;
        }
        if (command.index() > matching.size()) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    command.itemName() + " " + command.index() + "번째 아이템을 찾을 수 없습니다.");
            return;
        }

        ItemInstance item = matching.get(command.index() - 1);
        Inventory inventory = player.getInventory();

        if (!inventory.canAdd(item.getTemplate(), item.getQuantity())) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    "무게 초과로 " + item.getTemplate().getName() + "(을)를 들 수 없습니다.");
            return;
        }

        room.removeFloorItem(item);
        inventory.addItem(item);

        sendMessageToUserPort.messageToUser(command.userId(),
                item.getTemplate().getName() + "(을)를 주웠습니다.");
    }
}
```

- [ ] **Step 10: 전체 테스트 통과 확인**

```bash
./gradlew test
```
Expected: 모든 테스트 통과

- [ ] **Step 11: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/TakeCommand.java
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionary.java
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/TakeUseCase.java
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/TakeCommandParser.java
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/TakeCommandExecutor.java
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/TakeCommandService.java
git add src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/TakeCommandParserTest.java
git commit -m "feat: 아이템 줍기 커맨드 추가 (TakeCommand, Parser, Executor, Service)"
```

---

## Task 9: DropCommand + Parser + Executor + Service

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/DropCommand.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/DropUseCase.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/DropCommandParser.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/DropCommandExecutor.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/DropCommandService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/DropCommandParserTest.java`

- [ ] **Step 1: 파서 테스트 작성**

```java
// src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/DropCommandParserTest.java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.DropCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DropCommandParserTest {

    private DropCommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new DropCommandParser();
    }

    @Test
    void parse_itemNameOnly_returnsFirstIndex() {
        Command command = parser.parse(1L, "철검 버려");
        assertThat(command).isInstanceOf(DropCommand.class);
        DropCommand drop = (DropCommand) command;
        assertThat(drop.itemName()).isEqualTo("철검");
        assertThat(drop.index()).isEqualTo(1);
    }

    @Test
    void parse_itemNameWithIndex_returnsCorrectIndex() {
        Command command = parser.parse(1L, "철검 2 버려");
        assertThat(command).isInstanceOf(DropCommand.class);
        DropCommand drop = (DropCommand) command;
        assertThat(drop.itemName()).isEqualTo("철검");
        assertThat(drop.index()).isEqualTo(2);
    }

    @Test
    void parse_nonDropCommand_returnsNull() {
        Command command = parser.parse(1L, "동");
        assertThat(command).isNull();
    }
}
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser.DropCommandParserTest"
```
Expected: FAIL

- [ ] **Step 3: DropCommand 구현**

```java
// DropCommand.java
package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record DropCommand(
    Long userId,
    String itemName,
    int index
) implements Command {}
```

- [ ] **Step 4: DropUseCase 구현**

```java
// DropUseCase.java
package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.DropCommand;

public interface DropUseCase {
    void drop(DropCommand command);
}
```

- [ ] **Step 5: DropCommandParser 구현**

```java
// DropCommandParser.java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionary;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.DropCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DropCommandParser extends AbstractCommandParser {
    private static final Pattern DROP_PATTERN =
            Pattern.compile("(\\S+?)(?:\\s+(\\d+))?\\s+(" + CommandDictionary.DROP.toRegex() + ")");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = DROP_PATTERN.matcher(content);
        if (matcher.matches()) {
            String itemName = matcher.group(1);
            int index = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 1;
            return new DropCommand(userId, itemName, index);
        }
        return null;
    }
}
```

- [ ] **Step 6: 파서 테스트 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser.DropCommandParserTest"
```
Expected: PASS

- [ ] **Step 7: DropCommandExecutor 구현**

```java
// DropCommandExecutor.java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.DropCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.DropUseCase;
import org.springframework.stereotype.Component;

@Component
public class DropCommandExecutor implements CommandExecutor {
    private final DropUseCase dropUseCase;

    public DropCommandExecutor(DropUseCase dropUseCase) {
        this.dropUseCase = dropUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof DropCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof DropCommand dropCommand)) {
            throw new IllegalArgumentException("Command must be a DropCommand");
        }
        dropUseCase.drop(dropCommand);
    }
}
```

- [ ] **Step 8: DropCommandService 구현**

```java
// DropCommandService.java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.DropCommand;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import com.jefflife.mudmk2.gameplay.application.service.provided.DropUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DropCommandService implements DropUseCase {
    private final GameWorldService gameWorldService;
    private final SendMessageToUserPort sendMessageToUserPort;

    public DropCommandService(GameWorldService gameWorldService, SendMessageToUserPort sendMessageToUserPort) {
        this.gameWorldService = gameWorldService;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void drop(DropCommand command) {
        PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
        Inventory inventory = player.getInventory();

        List<ItemInstance> matching = inventory.findItemsByName(command.itemName());
        if (matching.isEmpty()) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    command.itemName() + "(을)를 소지하고 있지 않습니다.");
            return;
        }
        if (command.index() > matching.size()) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    command.itemName() + " " + command.index() + "번째 아이템을 찾을 수 없습니다.");
            return;
        }

        ItemInstance item = matching.get(command.index() - 1);
        Room room = gameWorldService.getRoom(player.getCurrentRoomId());

        inventory.removeItem(item);
        room.addFloorItem(item);

        sendMessageToUserPort.messageToUser(command.userId(),
                item.getTemplate().getName() + "(을)를 바닥에 버렸습니다.");
    }
}
```

- [ ] **Step 9: 전체 테스트 통과 확인**

```bash
./gradlew test
```
Expected: 모든 테스트 통과

- [ ] **Step 10: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/DropCommand.java
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/DropUseCase.java
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/DropCommandParser.java
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/DropCommandExecutor.java
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/DropCommandService.java
git add src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/DropCommandParserTest.java
git commit -m "feat: 아이템 버리기 커맨드 추가 (DropCommand, Parser, Executor, Service)"
```

---

## Task 10: InventoryCommand + Parser + Executor + Service

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/InventoryCommand.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/InventoryUseCase.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/InventoryCommandParser.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/InventoryCommandExecutor.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/InventoryCommandService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/InventoryCommandParserTest.java`

- [ ] **Step 1: 파서 테스트 작성**

```java
// src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/InventoryCommandParserTest.java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.InventoryCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InventoryCommandParserTest {

    private InventoryCommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new InventoryCommandParser();
    }

    @Test
    void parse_소지품_returnsInventoryCommand() {
        Command command = parser.parse(1L, "소지품");
        assertThat(command).isInstanceOf(InventoryCommand.class);
        assertThat(command.userId()).isEqualTo(1L);
    }

    @Test
    void parse_가방_returnsInventoryCommand() {
        Command command = parser.parse(1L, "가방");
        assertThat(command).isInstanceOf(InventoryCommand.class);
    }

    @Test
    void parse_인벤_returnsInventoryCommand() {
        Command command = parser.parse(1L, "인벤");
        assertThat(command).isInstanceOf(InventoryCommand.class);
    }

    @Test
    void parse_nonInventoryCommand_returnsNull() {
        Command command = parser.parse(1L, "동");
        assertThat(command).isNull();
    }
}
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser.InventoryCommandParserTest"
```
Expected: FAIL

- [ ] **Step 3: InventoryCommand 구현**

```java
// InventoryCommand.java
package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record InventoryCommand(Long userId) implements Command {}
```

- [ ] **Step 4: InventoryUseCase 구현**

```java
// InventoryUseCase.java
package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.InventoryCommand;

public interface InventoryUseCase {
    void showInventory(InventoryCommand command);
}
```

- [ ] **Step 5: InventoryCommandParser 구현**

```java
// InventoryCommandParser.java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionary;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.InventoryCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InventoryCommandParser extends AbstractCommandParser {
    private static final Pattern INVENTORY_PATTERN =
            Pattern.compile("(" + CommandDictionary.INVENTORY.toRegex() + ")");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = INVENTORY_PATTERN.matcher(content);
        if (matcher.matches()) {
            return new InventoryCommand(userId);
        }
        return null;
    }
}
```

- [ ] **Step 6: 파서 테스트 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser.InventoryCommandParserTest"
```
Expected: PASS

- [ ] **Step 7: InventoryCommandExecutor 구현**

```java
// InventoryCommandExecutor.java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.InventoryCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.InventoryUseCase;
import org.springframework.stereotype.Component;

@Component
public class InventoryCommandExecutor implements CommandExecutor {
    private final InventoryUseCase inventoryUseCase;

    public InventoryCommandExecutor(InventoryUseCase inventoryUseCase) {
        this.inventoryUseCase = inventoryUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof InventoryCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof InventoryCommand inventoryCommand)) {
            throw new IllegalArgumentException("Command must be an InventoryCommand");
        }
        inventoryUseCase.showInventory(inventoryCommand);
    }
}
```

- [ ] **Step 8: InventoryCommandService 구현**

```java
// InventoryCommandService.java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.InventoryCommand;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import com.jefflife.mudmk2.gameplay.application.service.provided.InventoryUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;

@Service
public class InventoryCommandService implements InventoryUseCase {
    private final GameWorldService gameWorldService;
    private final SendMessageToUserPort sendMessageToUserPort;

    public InventoryCommandService(GameWorldService gameWorldService, SendMessageToUserPort sendMessageToUserPort) {
        this.gameWorldService = gameWorldService;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void showInventory(InventoryCommand command) {
        PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
        Inventory inventory = player.getInventory();

        StringBuilder sb = new StringBuilder("[ 소지품 ]\n");
        if (inventory.getItems().isEmpty()) {
            sb.append("소지품이 없습니다.\n");
        } else {
            for (ItemInstance item : inventory.getItems()) {
                sb.append("- ").append(item.getTemplate().getName());
                if (item.getTemplate().isStackable()) {
                    sb.append(" x").append(item.getQuantity());
                }
                sb.append(" (").append(item.getTemplate().getWeight() * item.getQuantity()).append("kg)\n");
            }
        }
        sb.append("무게: ").append(inventory.currentWeight())
          .append("/").append(inventory.getMaxWeightCapacity()).append("kg");

        sendMessageToUserPort.messageToUser(command.userId(), sb.toString());
    }
}
```

- [ ] **Step 9: 전체 테스트 통과 확인**

```bash
./gradlew test
```
Expected: 모든 테스트 통과

- [ ] **Step 10: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/InventoryCommand.java
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/InventoryUseCase.java
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/InventoryCommandParser.java
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/InventoryCommandExecutor.java
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/InventoryCommandService.java
git add src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/InventoryCommandParserTest.java
git commit -m "feat: 소지품 조회 커맨드 추가 (InventoryCommand, Parser, Executor, Service)"
```
