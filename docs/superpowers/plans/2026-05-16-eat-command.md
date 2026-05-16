# `먹어` 명령 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 플레이어가 `먹어` 명령으로 음식 아이템(`FoodTemplate`)을 소비해 hp/mp/ap를 회복하도록 한다. 향후 버프 효과를 끼워 넣을 `EatEffect` 확장 포인트를 마련하고, 아이템 종류에 따라 DB 동기화 정책을 분기하는 `requiresImmediateDeletion()` 메서드를 도입한다.

**Architecture:** 기존 명령 파이프라인(`Parser → Command → Executor → UseCase Service`)을 그대로 따른다. `EatEffect` 인터페이스로 회복 효과를 다형성화하고, `FoodTemplate.getEatEffects()`에서 호출 시점에 빌드. JPA 매핑은 건드리지 않고 `EatCommandService`에서 `ItemInstanceRepository.delete(instance)`를 명시적으로 호출해 가비지 행 누적을 방지.

**Tech Stack:** Java 25, Spring Boot 4, Spring Data JPA, JUnit 5, Mockito, AssertJ, Lombok, Gradle (`./gradlew test`, `./gradlew compileJava`)

**Spec:** `docs/superpowers/specs/2026-05-16-eat-command-design.md`

---

## 파일 구조

### 신규 생성

도메인:
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/EatEffect.java`
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/HpRestoreEffect.java`
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/MpRestoreEffect.java`
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/ApRestoreEffect.java`

명령 컴포넌트:
- `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/EatCommand.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EatCommandParser.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/EatCommandExecutor.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/EatUseCase.java`
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/EatCommandService.java`

테스트:
- `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/HpRestoreEffectTest.java`
- `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/MpRestoreEffectTest.java`
- `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/ApRestoreEffectTest.java`
- `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/FoodTemplateTest.java`
- `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacterTest.java`
- `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacterHealTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/EatCommandTest.java` (선택 — record라 단순)
- `src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EatCommandParserTest.java`
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/EatCommandServiceTest.java`

### 기존 수정
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstance.java` — `decreaseQuantity(int)` 추가
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemTemplate.java` — `requiresImmediateDeletion()` abstract 추가
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/FoodTemplate.java` — `getEatEffects()`, `requiresImmediateDeletion()` 추가
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTemplate.java` — `requiresImmediateDeletion()` 추가
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentTemplate.java` — `requiresImmediateDeletion()` 추가
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/AccessoryTemplate.java` — `requiresImmediateDeletion()` 추가
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/MartialArtsBookTemplate.java` — `requiresImmediateDeletion()` 추가
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/MissionItemTemplate.java` — `requiresImmediateDeletion()` 추가
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacter.java` — `healHp/healMp/healAp` 추가
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java` — `heal(hp, mp, ap)` 추가
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Inventory.java` — `consumeOne(instance)` 추가
- `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstanceTest.java` — `decreaseQuantity` 테스트 추가
- `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/InventoryTest.java` — `consumeOne` 테스트 추가
- `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionary.java` — `EAT` enum 값 추가

---

## Task 1: `ItemInstance.decreaseQuantity(int)` 추가

`addQuantity`와 대칭. 음수가 되지 않도록 가드.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstance.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstanceTest.java`

- [ ] **Step 1: 실패 테스트 추가**

`src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstanceTest.java` 의 기존 클래스에 두 테스트를 추가한다 (기존 메서드 옆에).

```java
    @Test
    void decreaseQuantity_decreasesQuantity() {
        FoodTemplate food = makeFood();
        ItemInstance instance = new ItemInstance(food, 5);
        instance.decreaseQuantity(2);
        assertThat(instance.getQuantity()).isEqualTo(3);
    }

    @Test
    void decreaseQuantity_clampsAtZero() {
        FoodTemplate food = makeFood();
        ItemInstance instance = new ItemInstance(food, 1);
        instance.decreaseQuantity(5);
        assertThat(instance.getQuantity()).isEqualTo(0);
    }
```

- [ ] **Step 2: 테스트 실행 → FAIL 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstanceTest"
```

Expected: `decreaseQuantity` 메서드 없음으로 컴파일 실패.

- [ ] **Step 3: `ItemInstance.decreaseQuantity` 구현**

`src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstance.java` 의 `addQuantity` 메서드 바로 아래에 추가한다.

```java
    public void decreaseQuantity(int amount) {
        this.quantity = Math.max(0, this.quantity - amount);
    }
```

- [ ] **Step 4: 테스트 실행 → PASS 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstanceTest"
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstance.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstanceTest.java
git commit -m "feat(item): ItemInstance.decreaseQuantity — 음수 가드 포함"
```

---

## Task 2: `BaseCharacter.healHp/healMp/healAp` 추가

부분 회복 메서드. max 클램프. max는 호출자가 넘긴다.

**Files:**
- Create: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacterTest.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacter.java`

- [ ] **Step 1: 실패 테스트 작성 (신규 파일)**

`src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacterTest.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseCharacterTest {

    private BaseCharacter makeChar(int hp, int mp, int ap) {
        return BaseCharacter.builder()
                .name("홍길동")
                .hp(hp).mp(mp).ap(ap)
                .build();
    }

    @Test
    void healHp_increasesHpWithinMax() {
        BaseCharacter c = makeChar(50, 0, 0);
        c.healHp(30, 100);
        assertThat(c.getHp()).isEqualTo(80);
    }

    @Test
    void healHp_clampsAtMax() {
        BaseCharacter c = makeChar(50, 0, 0);
        c.healHp(80, 100);
        assertThat(c.getHp()).isEqualTo(100);
    }

    @Test
    void healMp_increasesMpWithinMax() {
        BaseCharacter c = makeChar(0, 20, 0);
        c.healMp(10, 50);
        assertThat(c.getMp()).isEqualTo(30);
    }

    @Test
    void healMp_clampsAtMax() {
        BaseCharacter c = makeChar(0, 20, 0);
        c.healMp(100, 50);
        assertThat(c.getMp()).isEqualTo(50);
    }

    @Test
    void healAp_increasesApWithinMax() {
        BaseCharacter c = makeChar(0, 0, 5);
        c.healAp(10, 40);
        assertThat(c.getAp()).isEqualTo(15);
    }

    @Test
    void healAp_clampsAtMax() {
        BaseCharacter c = makeChar(0, 0, 5);
        c.healAp(100, 40);
        assertThat(c.getAp()).isEqualTo(40);
    }
}
```

- [ ] **Step 2: 테스트 실행 → FAIL 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.BaseCharacterTest"
```

Expected: 컴파일 실패 — healHp/healMp/healAp 메서드 없음.

- [ ] **Step 3: `BaseCharacter`에 메서드 3종 추가**

`src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacter.java` 의 `decreaseHp` 메서드 아래에 추가:

```java
    public void healHp(int amount, int maxHp) {
        this.hp = Math.min(this.hp + amount, maxHp);
    }

    public void healMp(int amount, int maxMp) {
        this.mp = Math.min(this.mp + amount, maxMp);
    }

    public void healAp(int amount, int maxAp) {
        this.ap = Math.min(this.ap + amount, maxAp);
    }
```

- [ ] **Step 4: 테스트 실행 → PASS 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.BaseCharacterTest"
```

Expected: BUILD SUCCESSFUL, 6개 테스트 통과.

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacter.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacterTest.java
git commit -m "feat(player): BaseCharacter.healHp/healMp/healAp — max 클램프"
```

---

## Task 3: `PlayerCharacter.heal(hp, mp, ap)` 진입점 추가

장비 보너스 포함된 max를 `CharacterStats`에서 계산해 `BaseCharacter`에 전달.

**Files:**
- Create: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacterHealTest.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java`

- [ ] **Step 1: 실패 테스트 작성 (신규 파일)**

`src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacterHealTest.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerCharacterHealTest {

    private PlayerCharacter makePlayer(int hp, int mp, int ap, int physique, int meridian, int agility) {
        BaseCharacter base = BaseCharacter.builder()
                .name("홍길동")
                .hp(hp).mp(mp).ap(ap)
                .physique(physique)
                .meridian(meridian)
                .agility(agility)
                .build();
        EquippedItems equipped = mock(EquippedItems.class);
        when(equipped.sumStatModifiers()).thenReturn(Map.<StatType, Integer>of());
        Inventory inventory = mock(Inventory.class);
        return new PlayerCharacter(
                java.util.UUID.randomUUID(),
                base,
                null,
                1L,
                "홍길동",
                null,
                false,
                null,
                inventory,
                equipped
        );
    }

    @Test
    void heal_increasesHpMpApWithinMax() {
        // maxHp = physique*10 + specialTechnique*3 = 10*10 + 0 = 100
        // maxMp = meridian*5 + innerPower*3 = 10*5 + 0 = 50
        // maxAp = agility*8 = 5*8 = 40
        PlayerCharacter p = makePlayer(50, 20, 5, 10, 10, 5);

        p.heal(30, 10, 10);

        assertThat(p.getBaseStats().hp()).isEqualTo(80);
        assertThat(p.getBaseStats().mp()).isEqualTo(30);
        assertThat(p.getBaseStats().ap()).isEqualTo(15);
    }

    @Test
    void heal_clampsAtMax() {
        PlayerCharacter p = makePlayer(50, 20, 5, 10, 10, 5);

        p.heal(999, 999, 999);

        assertThat(p.getBaseStats().hp()).isEqualTo(100);
        assertThat(p.getBaseStats().mp()).isEqualTo(50);
        assertThat(p.getBaseStats().ap()).isEqualTo(40);
    }

    @Test
    void heal_zeroAmounts_doNotChangeValues() {
        PlayerCharacter p = makePlayer(50, 20, 5, 10, 10, 5);

        p.heal(0, 0, 0);

        assertThat(p.getBaseStats().hp()).isEqualTo(50);
        assertThat(p.getBaseStats().mp()).isEqualTo(20);
        assertThat(p.getBaseStats().ap()).isEqualTo(5);
    }
}
```

- [ ] **Step 2: 테스트 실행 → FAIL 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacterHealTest"
```

Expected: `heal` 메서드 없음으로 컴파일 실패.

- [ ] **Step 3: `PlayerCharacter.heal` 구현**

`src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java` 의 `fullRestore` 메서드 위(또는 아래)에 추가:

```java
    public void heal(int hp, int mp, int ap) {
        CharacterStats stats = getStats();
        baseCharacterInfo.healHp(hp, stats.maxHp());
        baseCharacterInfo.healMp(mp, stats.maxMp());
        baseCharacterInfo.healAp(ap, stats.maxAp());
    }
```

- [ ] **Step 4: 테스트 실행 → PASS 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacterHealTest"
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacterHealTest.java
git commit -m "feat(player): PlayerCharacter.heal(hp,mp,ap) — 장비 max 포함 클램프"
```

---

## Task 4: `Inventory.consumeOne(instance)` 추가

수량 1 감소. 0이 되면 List에서 제거. 반환값 = 제거 여부.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Inventory.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/InventoryTest.java`

- [ ] **Step 1: 실패 테스트 추가**

`src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/InventoryTest.java` 클래스 끝에 추가:

```java
    @Test
    void consumeOne_quantityGreaterThanOne_decrementsAndReturnsFalse() {
        ItemInstance instance = new ItemInstance(foodTemplate, 2);
        inventory.addItem(instance);

        boolean removed = inventory.consumeOne(instance);

        assertThat(removed).isFalse();
        assertThat(instance.getQuantity()).isEqualTo(1);
        assertThat(inventory.getItems()).hasSize(1);
    }

    @Test
    void consumeOne_quantityOne_removesAndReturnsTrue() {
        ItemInstance instance = new ItemInstance(foodTemplate, 1);
        inventory.addItem(instance);

        boolean removed = inventory.consumeOne(instance);

        assertThat(removed).isTrue();
        assertThat(instance.getQuantity()).isEqualTo(0);
        assertThat(inventory.getItems()).isEmpty();
    }
```

- [ ] **Step 2: 테스트 실행 → FAIL 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.InventoryTest"
```

Expected: `consumeOne` 메서드 없음으로 컴파일 실패.

- [ ] **Step 3: `Inventory.consumeOne` 구현**

`src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Inventory.java` 의 `removeItem` 메서드 아래에 추가:

```java
    public boolean consumeOne(ItemInstance instance) {
        instance.decreaseQuantity(1);
        if (instance.getQuantity() <= 0) {
            items.remove(instance);
            return true;
        }
        return false;
    }
```

- [ ] **Step 4: 테스트 실행 → PASS 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.InventoryTest"
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Inventory.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/InventoryTest.java
git commit -m "feat(inventory): Inventory.consumeOne — 수량 1 감소 + 0 시 제거"
```

---

## Task 5: `EatEffect` 인터페이스 + 3종 record + 단위 테스트

새 패키지 `gamedata.application.domain.model.item.effect` 신설.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/EatEffect.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/HpRestoreEffect.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/MpRestoreEffect.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/ApRestoreEffect.java`
- Create: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/HpRestoreEffectTest.java`
- Create: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/MpRestoreEffectTest.java`
- Create: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/ApRestoreEffectTest.java`

- [ ] **Step 1: 실패 테스트 작성 (3개 신규 파일)**

`src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/HpRestoreEffectTest.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item.effect;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HpRestoreEffectTest {
    @Test
    void applyTo_callsPlayerHealWithHpOnly() {
        PlayerCharacter player = mock(PlayerCharacter.class);
        new HpRestoreEffect(30).applyTo(player);
        verify(player).heal(30, 0, 0);
    }
}
```

`src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/MpRestoreEffectTest.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item.effect;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MpRestoreEffectTest {
    @Test
    void applyTo_callsPlayerHealWithMpOnly() {
        PlayerCharacter player = mock(PlayerCharacter.class);
        new MpRestoreEffect(15).applyTo(player);
        verify(player).heal(0, 15, 0);
    }
}
```

`src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/ApRestoreEffectTest.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item.effect;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ApRestoreEffectTest {
    @Test
    void applyTo_callsPlayerHealWithApOnly() {
        PlayerCharacter player = mock(PlayerCharacter.class);
        new ApRestoreEffect(5).applyTo(player);
        verify(player).heal(0, 0, 5);
    }
}
```

- [ ] **Step 2: 테스트 실행 → FAIL 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.*"
```

Expected: 컴파일 실패 — `EatEffect` / `HpRestoreEffect` / `MpRestoreEffect` / `ApRestoreEffect` 모두 없음.

- [ ] **Step 3: 인터페이스와 record 4개 작성**

`src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/EatEffect.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item.effect;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

public interface EatEffect {
    void applyTo(PlayerCharacter player);
}
```

`src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/HpRestoreEffect.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item.effect;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

public record HpRestoreEffect(int amount) implements EatEffect {
    @Override
    public void applyTo(PlayerCharacter player) {
        player.heal(amount, 0, 0);
    }
}
```

`src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/MpRestoreEffect.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item.effect;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

public record MpRestoreEffect(int amount) implements EatEffect {
    @Override
    public void applyTo(PlayerCharacter player) {
        player.heal(0, amount, 0);
    }
}
```

`src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/ApRestoreEffect.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item.effect;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

public record ApRestoreEffect(int amount) implements EatEffect {
    @Override
    public void applyTo(PlayerCharacter player) {
        player.heal(0, 0, amount);
    }
}
```

- [ ] **Step 4: 테스트 실행 → PASS 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.*"
```

Expected: BUILD SUCCESSFUL, 3개 테스트 통과.

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/ \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/effect/
git commit -m "feat(item): EatEffect 인터페이스 + Hp/Mp/Ap RestoreEffect record"
```

---

## Task 6: `ItemTemplate.requiresImmediateDeletion()` abstract 추가 + 모든 서브타입 구현

abstract 메서드 추가는 모든 서브타입에 동시에 구현을 넣어야 컴파일된다. 한 커밋으로 묶는다.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/FoodTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/AccessoryTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/MartialArtsBookTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/MissionItemTemplate.java`

- [ ] **Step 1: `ItemTemplate`에 abstract 메서드 추가**

`src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemTemplate.java` 의 `initializeAssociatedEntities()` 선언 바로 위(또는 아래)에 추가:

```java
    /**
     * 소비/사용 시점에 ItemInstance 행을 즉시 DB에서 삭제하고 player 상태도 즉시 저장해야 하는지 여부.
     * <p>true 반환 시 인벤토리에서 빠진 시점에 같은 트랜잭션 안에서 player save 까지 마쳐 — 서버 재기동 시
     * 부활(아이템 복사) 위험을 차단한다. 영구 효과를 부여하는 아이템(예: 무공서 학습)이 이 값을 true 로 둔다.</p>
     */
    public abstract boolean requiresImmediateDeletion();
```

- [ ] **Step 2: 모든 서브타입에 `false` 반환 구현 추가**

`FoodTemplate.java` 의 `initializeAssociatedEntities()` 아래:

```java
    @Override
    public boolean requiresImmediateDeletion() {
        return false;
    }
```

`WeaponTemplate.java`, `EquipmentTemplate.java`, `AccessoryTemplate.java`, `MartialArtsBookTemplate.java`, `MissionItemTemplate.java` 각각에 동일하게 `initializeAssociatedEntities()` 아래(또는 클래스 끝)에 동일 코드 블록 추가:

```java
    @Override
    public boolean requiresImmediateDeletion() {
        return false;
    }
```

(6개 서브타입 모두에 동일한 메서드 추가 — 빠짐없이.)

- [ ] **Step 3: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: 전체 테스트 회귀 확인**

```bash
./gradlew test
```

Expected: BUILD SUCCESSFUL. 기존 테스트 모두 통과.

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemTemplate.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/FoodTemplate.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTemplate.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentTemplate.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/AccessoryTemplate.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/MartialArtsBookTemplate.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/MissionItemTemplate.java
git commit -m "feat(item): ItemTemplate.requiresImmediateDeletion() — 모든 서브타입 false"
```

---

## Task 7: `FoodTemplate.getEatEffects()` 추가

0인 회복은 리스트에서 제외.

**Files:**
- Create: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/FoodTemplateTest.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/FoodTemplate.java`

- [ ] **Step 1: 실패 테스트 작성 (신규 파일)**

`src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/FoodTemplateTest.java`:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.ApRestoreEffect;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.EatEffect;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.HpRestoreEffect;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.MpRestoreEffect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FoodTemplateTest {

    @Test
    void getEatEffects_includesAllNonZeroRecoveries() {
        FoodTemplate food = FoodTemplate.builder()
                .name("산해진미").description("진수성찬").weight(2).stackable(false)
                .hpRecovery(30).mpRecovery(20).apRecovery(10)
                .build();

        List<EatEffect> effects = food.getEatEffects();

        assertThat(effects).hasSize(3);
        assertThat(effects).contains(
                new HpRestoreEffect(30),
                new MpRestoreEffect(20),
                new ApRestoreEffect(10)
        );
    }

    @Test
    void getEatEffects_excludesZeroRecoveries() {
        FoodTemplate food = FoodTemplate.builder()
                .name("사과").description("빨간 사과").weight(1).stackable(true)
                .hpRecovery(30).mpRecovery(0).apRecovery(0)
                .build();

        List<EatEffect> effects = food.getEatEffects();

        assertThat(effects).containsExactly(new HpRestoreEffect(30));
    }

    @Test
    void getEatEffects_allZero_returnsEmptyList() {
        FoodTemplate food = FoodTemplate.builder()
                .name("물").description("맹물").weight(1).stackable(true)
                .hpRecovery(0).mpRecovery(0).apRecovery(0)
                .build();

        assertThat(food.getEatEffects()).isEmpty();
    }
}
```

- [ ] **Step 2: 테스트 실행 → FAIL 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.FoodTemplateTest"
```

Expected: `getEatEffects` 메서드 없음으로 컴파일 실패.

- [ ] **Step 3: `FoodTemplate.getEatEffects` 구현**

`src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/FoodTemplate.java` 상단에 import 추가:

```java
import com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.ApRestoreEffect;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.EatEffect;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.HpRestoreEffect;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.effect.MpRestoreEffect;

import java.util.ArrayList;
import java.util.List;
```

기존 `update` 메서드 아래(또는 `requiresImmediateDeletion()` 옆)에 메서드 추가:

```java
    public List<EatEffect> getEatEffects() {
        List<EatEffect> effects = new ArrayList<>();
        if (hpRecovery > 0) effects.add(new HpRestoreEffect(hpRecovery));
        if (mpRecovery > 0) effects.add(new MpRestoreEffect(mpRecovery));
        if (apRecovery > 0) effects.add(new ApRestoreEffect(apRecovery));
        return effects;
    }
```

- [ ] **Step 4: 테스트 실행 → PASS 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.item.FoodTemplateTest"
```

Expected: BUILD SUCCESSFUL, 3개 테스트 통과.

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/FoodTemplate.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/FoodTemplateTest.java
git commit -m "feat(item): FoodTemplate.getEatEffects — 0 회복 제외"
```

---

## Task 8: `CommandDictionary.EAT` 추가

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionary.java`

- [ ] **Step 1: enum 값 추가**

`src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionary.java` 의 `EQUIPMENT_VIEW` 라인 끝 세미콜론 직전에 새 enum 값 추가:

```java
    EQUIPMENT_VIEW("장비", "장비창"),
    EAT("먹다", "먹어", "먹");
```

(기존 마지막 enum 값에 붙어있던 세미콜론을 콤마로 바꾸고 새 라인에 `EAT(...)` + 세미콜론.)

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionary.java
git commit -m "feat(command): CommandDictionary.EAT 추가 — 먹다/먹어/먹"
```

---

## Task 9: `EatCommand` record 추가

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/EatCommand.java`

- [ ] **Step 1: record 작성**

`src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/EatCommand.java`:

```java
package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record EatCommand(
        Long userId,
        String itemName,
        int index
) implements Command {}
```

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/EatCommand.java
git commit -m "feat(command): EatCommand record"
```

---

## Task 10: `EatCommandParser` 추가

`EquipCommandParser`와 같은 정규식 구조.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EatCommandParser.java`
- Create: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EatCommandParserTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EatCommandParserTest.java`:

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EatCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EatCommandParserTest {

    private EatCommandParser parser;

    @BeforeEach
    void setUp() { parser = new EatCommandParser(); }

    @Test
    void parse_appleWith_먹어_returnsEatCommand() {
        Command c = parser.parse(1L, "사과 먹어");
        assertThat(c).isInstanceOf(EatCommand.class);
        EatCommand e = (EatCommand) c;
        assertThat(e.itemName()).isEqualTo("사과");
        assertThat(e.index()).isEqualTo(1);
        assertThat(e.userId()).isEqualTo(1L);
    }

    @Test
    void parse_meatWith_먹다_returnsEatCommand() {
        EatCommand e = (EatCommand) parser.parse(1L, "고기 먹다");
        assertThat(e.itemName()).isEqualTo("고기");
    }

    @Test
    void parse_withIndex_returnsCorrectIndex() {
        EatCommand e = (EatCommand) parser.parse(1L, "고기 2 먹다");
        assertThat(e.index()).isEqualTo(2);
    }

    @Test
    void parse_nonEatText_returnsNull() {
        assertThat(parser.parse(1L, "철검 장착")).isNull();
    }
}
```

- [ ] **Step 2: 테스트 실행 → FAIL 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser.EatCommandParserTest"
```

Expected: `EatCommandParser` 없음으로 컴파일 실패.

- [ ] **Step 3: `EatCommandParser` 구현**

`src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EatCommandParser.java`:

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionary;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EatCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EatCommandParser extends AbstractCommandParser {
    private static final Pattern EAT_PATTERN =
            Pattern.compile("(\\S+?)(?:\\s+(\\d+))?\\s+(" + CommandDictionary.EAT.toRegex() + ")");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = EAT_PATTERN.matcher(content);
        if (matcher.matches()) {
            String itemName = matcher.group(1);
            int index = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 1;
            return new EatCommand(userId, itemName, index);
        }
        return null;
    }
}
```

- [ ] **Step 4: 테스트 실행 → PASS 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser.EatCommandParserTest"
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EatCommandParser.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/EatCommandParserTest.java
git commit -m "feat(parser): EatCommandParser — 아이템명 + (인덱스) + 먹기 동사"
```

---

## Task 11: `EatUseCase` 인터페이스 추가

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/EatUseCase.java`

- [ ] **Step 1: 인터페이스 작성**

`src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/EatUseCase.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.EatCommand;

public interface EatUseCase {
    void eat(EatCommand command);
}
```

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/EatUseCase.java
git commit -m "feat(usecase): EatUseCase 인터페이스"
```

---

## Task 12: `EatCommandService` 구현

핵심 로직. 정상 흐름 + 에러 4종 + 동기화 분기.

**Files:**
- Create: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/EatCommandServiceTest.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/EatCommandService.java`

- [ ] **Step 1: 실패 테스트 작성 (신규 파일)**

`src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/EatCommandServiceTest.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.FoodTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemInstanceRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EatCommand;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EatCommandServiceTest {

    @Mock
    private ActivePlayerRepository players;

    @Mock
    private ItemInstanceRepository itemInstanceRepository;

    @Mock
    private PlayerCharacterRepository playerCharacterRepository;

    @Mock
    private SendMessageToUserPort sendMessageToUserPort;

    @InjectMocks
    private EatCommandService eatCommandService;

    private PlayerCharacter stubPlayer(Inventory inventory) {
        PlayerCharacter player = mock(PlayerCharacter.class);
        lenient().when(player.getInventory()).thenReturn(inventory);
        lenient().when(players.findByUserId(1L)).thenReturn(Optional.of(player));
        return player;
    }

    private FoodTemplate apple(int hp, int mp, int ap) {
        return FoodTemplate.builder()
                .name("사과").description("빨간 사과").weight(1).stackable(true)
                .hpRecovery(hp).mpRecovery(mp).apRecovery(ap)
                .build();
    }

    @Test
    void eat_invalidIndex_sendsIndexErrorMessage() {
        eatCommandService.eat(new EatCommand(1L, "사과", 0));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("올바른 번호"));
        verifyNoInteractions(players);
    }

    @Test
    void eat_itemNotFoundInInventory_sendsNotFoundMessage() {
        Inventory inventory = mock(Inventory.class);
        stubPlayer(inventory);
        when(inventory.findItemsByName("사과")).thenReturn(List.of());

        eatCommandService.eat(new EatCommand(1L, "사과", 1));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("가지고 있지 않습니다"));
        verify(inventory, never()).consumeOne(any());
        verifyNoInteractions(itemInstanceRepository);
    }

    @Test
    void eat_indexOutOfRange_sendsIndexErrorMessage() {
        ItemInstance item = new ItemInstance(apple(30, 0, 0), 1);
        Inventory inventory = mock(Inventory.class);
        stubPlayer(inventory);
        when(inventory.findItemsByName("사과")).thenReturn(List.of(item));

        eatCommandService.eat(new EatCommand(1L, "사과", 5));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("찾을 수 없습니다"));
        verify(inventory, never()).consumeOne(any());
    }

    @Test
    void eat_nonFoodItem_sendsRejectionMessage() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("평범한 철검").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD).statModifiers(List.of())
                .build();
        ItemInstance item = new ItemInstance(sword, 1);
        Inventory inventory = mock(Inventory.class);
        stubPlayer(inventory);
        when(inventory.findItemsByName("철검")).thenReturn(List.of(item));

        eatCommandService.eat(new EatCommand(1L, "철검", 1));

        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("먹을 수 없습니다"));
        verify(inventory, never()).consumeOne(any());
    }

    @Test
    void eat_quantityGreaterThanOne_appliesEffectsButDoesNotDeleteInstance() {
        ItemInstance item = new ItemInstance(apple(30, 10, 0), 5);
        Inventory inventory = mock(Inventory.class);
        PlayerCharacter player = stubPlayer(inventory);
        when(inventory.findItemsByName("사과")).thenReturn(List.of(item));
        when(inventory.consumeOne(item)).thenReturn(false);  // 수량 5→4, 제거 안 됨

        eatCommandService.eat(new EatCommand(1L, "사과", 1));

        verify(player).heal(30, 0, 0);
        verify(player).heal(0, 10, 0);
        verify(inventory).consumeOne(item);
        verify(itemInstanceRepository, never()).delete(any());
        verify(playerCharacterRepository, never()).save(any());
        verify(sendMessageToUserPort).messageToUser(eq(1L), contains("사과"));
    }

    @Test
    void eat_quantityOne_deletesInstanceImmediately() {
        ItemInstance item = new ItemInstance(apple(30, 0, 0), 1);
        Inventory inventory = mock(Inventory.class);
        PlayerCharacter player = stubPlayer(inventory);
        when(inventory.findItemsByName("사과")).thenReturn(List.of(item));
        when(inventory.consumeOne(item)).thenReturn(true);  // 수량 1→0, 제거됨

        eatCommandService.eat(new EatCommand(1L, "사과", 1));

        verify(player).heal(30, 0, 0);
        verify(inventory).consumeOne(item);
        verify(itemInstanceRepository).delete(item);
        verify(playerCharacterRepository, never()).save(any());  // FoodTemplate 은 false
    }

    @Test
    void eat_requiresImmediateDeletionTemplate_savesPlayerImmediately() {
        // immediate=true 인 가짜 ItemTemplate 으로 분기 검증
        ItemTemplate immediateTemplate = mock(ItemTemplate.class);
        when(immediateTemplate.getName()).thenReturn("귀한약");
        when(immediateTemplate.requiresImmediateDeletion()).thenReturn(true);
        // 단, EatCommandService 는 FoodTemplate 만 먹을 수 있으므로 — 이 케이스는
        // "FOOD 아닌 아이템은 거부" 흐름을 타게 된다. 즉 immediate save 분기는
        // FoodTemplate 이면서 requiresImmediateDeletion=true 인 경우에만 발생.
        // 따라서 anonymous subclass 로 FoodTemplate 을 확장해 immediate=true 를 강제한다.

        FoodTemplate persistentFood = new FoodTemplate(
                "선단", "내공 영구 증진 단약", 1, false, 0, 0, 0
        ) {
            @Override
            public boolean requiresImmediateDeletion() {
                return true;
            }
        };
        ItemInstance item = new ItemInstance(persistentFood, 1);

        Inventory inventory = mock(Inventory.class);
        PlayerCharacter player = stubPlayer(inventory);
        when(inventory.findItemsByName("선단")).thenReturn(List.of(item));
        when(inventory.consumeOne(item)).thenReturn(true);

        eatCommandService.eat(new EatCommand(1L, "선단", 1));

        verify(itemInstanceRepository).delete(item);
        verify(playerCharacterRepository).save(player);
    }
}
```

- [ ] **Step 2: 테스트 실행 → FAIL 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.command.EatCommandServiceTest"
```

Expected: `EatCommandService` 없음으로 컴파일 실패.

- [ ] **Step 3: `EatCommandService` 구현**

`src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/EatCommandService.java`:

```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.FoodTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemInstanceRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EatCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.provided.EatUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EatCommandService implements EatUseCase {

    private final ActivePlayerRepository players;
    private final ItemInstanceRepository itemInstanceRepository;
    private final PlayerCharacterRepository playerCharacterRepository;
    private final SendMessageToUserPort sendMessageToUserPort;

    public EatCommandService(
            ActivePlayerRepository players,
            ItemInstanceRepository itemInstanceRepository,
            PlayerCharacterRepository playerCharacterRepository,
            SendMessageToUserPort sendMessageToUserPort
    ) {
        this.players = players;
        this.itemInstanceRepository = itemInstanceRepository;
        this.playerCharacterRepository = playerCharacterRepository;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void eat(EatCommand command) {
        if (command.index() < 1) {
            sendMessageToUserPort.messageToUser(command.userId(), "올바른 번호를 입력해주세요.");
            return;
        }

        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
        Inventory inventory = player.getInventory();

        List<ItemInstance> matching = inventory.findItemsByName(command.itemName());
        if (matching.isEmpty()) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    command.itemName() + "(을)를 가지고 있지 않습니다.");
            return;
        }
        if (command.index() > matching.size()) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    command.itemName() + " " + command.index() + "번째 아이템을 찾을 수 없습니다.");
            return;
        }

        ItemInstance instance = matching.get(command.index() - 1);
        ItemTemplate template = instance.getTemplate();
        if (!(template instanceof FoodTemplate food)) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    template.getName() + "은(는) 먹을 수 없습니다.");
            return;
        }

        food.getEatEffects().forEach(e -> e.applyTo(player));

        boolean removed = inventory.consumeOne(instance);
        if (removed) {
            itemInstanceRepository.delete(instance);
        }
        if (template.requiresImmediateDeletion()) {
            playerCharacterRepository.save(player);
        }

        sendMessageToUserPort.messageToUser(command.userId(),
                food.getName() + "을(를) 먹어 " + buildRecoveryMessage(food) + "이(가) 회복되었다.");
    }

    private String buildRecoveryMessage(FoodTemplate food) {
        List<String> parts = new ArrayList<>();
        if (food.getHpRecovery() > 0) parts.add("hp " + food.getHpRecovery());
        if (food.getMpRecovery() > 0) parts.add("mp " + food.getMpRecovery());
        if (food.getApRecovery() > 0) parts.add("ap " + food.getApRecovery());
        return String.join(", ", parts);
    }
}
```

- [ ] **Step 4: 테스트 실행 → PASS 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.command.EatCommandServiceTest"
```

Expected: BUILD SUCCESSFUL. 7개 테스트 통과.

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/EatCommandService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/EatCommandServiceTest.java
git commit -m "feat(eat): EatCommandService — 효과 적용 + 동기화 분기 + 거부 메시지"
```

---

## Task 13: `EatCommandExecutor` 추가

Chain의 일원. `EquipCommandExecutor`와 동형.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/EatCommandExecutor.java`

- [ ] **Step 1: Executor 작성**

`src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/EatCommandExecutor.java`:

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EatCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.EatUseCase;
import org.springframework.stereotype.Component;

@Component
public class EatCommandExecutor implements CommandExecutor {
    private final EatUseCase eatUseCase;

    public EatCommandExecutor(EatUseCase eatUseCase) {
        this.eatUseCase = eatUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof EatCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof EatCommand eatCommand)) {
            throw new IllegalArgumentException("Command must be an EatCommand");
        }
        eatUseCase.eat(eatCommand);
    }
}
```

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew compileJava
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: 전체 테스트 회귀 확인**

```bash
./gradlew test
```

Expected: BUILD SUCCESSFUL. 모든 기존 테스트 + 신규 테스트 통과.

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/EatCommandExecutor.java
git commit -m "feat(executor): EatCommandExecutor — Chain 등록"
```

---

## Task 14: 수동 통합 검증 (애플리케이션 실행)

자동 통합 테스트는 추가하지 않는다 (스펙 정책). 대신 로컬에서 음식 아이템을 만들어 한 번 먹어본다.

- [ ] **Step 1: 애플리케이션 기동**

```bash
./gradlew bootRun
```

- [ ] **Step 2: 관리자 페이지에서 FoodTemplate 생성**

웹 UI 또는 기존 admin 엔드포인트로 임의의 음식 (예: 사과, hp 30 회복) 생성 후 캐릭터 인벤토리에 1개 배치.

- [ ] **Step 3: 게임 클라이언트에서 명령 입력**

`사과 먹어` 입력. 다음을 눈으로 확인:
- 메시지 `사과을(를) 먹어 hp 30이(가) 회복되었다.` 출력
- 인벤토리에서 사과 사라짐
- DB `item_instance` 테이블에서 해당 행이 삭제됨 (직접 쿼리)

- [ ] **Step 4: 회귀 안전 — 다른 명령 점검**

`장비`, `소지품`, `철검 장착` 같은 인접 명령이 여전히 정상 동작하는지 확인.

- [ ] **Step 5: 검증 완료 표시 (커밋 없음 — 코드 변경 없음)**

문제가 있으면 해당 Task로 돌아가 수정 후 재실행.

---

## 완료 후 점검

- [ ] 전체 테스트 통과: `./gradlew test`
- [ ] 컴파일 경고 0: `./gradlew compileJava`
- [ ] Spec 의 "범위 외 (Out of Scope)" 항목들은 **이번 plan에 포함되지 않았음** 재확인
  - 버프 효과 미구현 ✓
  - JPA 매핑 변경 없음 ✓
  - 사망/기절 가드 미추가 ✓
  - 실제 영구효과 아이템 미도입 (분기 코드만 마련) ✓
