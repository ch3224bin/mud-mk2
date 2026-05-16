# Cache Invariant 이관 (옵션 ②): `Room.addFloorItem` / `Inventory.addItem` 에서 LAZY 초기화 강제

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** "인메모리 캐시에 들어가는 `ItemInstance` 는 항상 LAZY 그래프가 강제 초기화돼야 한다" 라는 invariant 를, 호출자 측 명시적 호출 (`ItemInstanceService.place()` 에서 1줄) 대신 도메인 애그리거트 루트 메서드 (`Room.addFloorItem`, `Inventory.addItem`) 자체가 항상 강제하도록 이관한다. 그 결과 미래의 새 진입점 (상인 구매, 제작, 이벤트 보상) 이 어떤 경로로 아이템을 추가하든 자동으로 보호된다.

**Architecture:** 현재 `ItemInstanceService.place()` 가 `save(...)` 직후 `instance.initializeAssociatedEntities()` 를 1회 호출해 invariant 를 지키지만, 이 책임이 호출자에게 있는 한 진입점이 늘어날 때마다 같은 패턴을 반복해야 하고 누락 시 `LazyInitializationException` 이 재발한다. 본 plan 은 invariant 의 enforcement 지점을 애그리거트 루트로 이동한다: `Room.addFloorItem(item)` 과 `Inventory.addItem(item)` 의 첫 줄에서 `item.initializeAssociatedEntities()` 를 호출. 이 호출은 idempotent (이미 초기화된 PersistentBag 에 `.size()` 를 부르는 것은 no-op) 이므로 기존 호출자 (`DropCommandService`, `TakeCommandService`) 의 동작은 변하지 않는다. 애그리거트가 invariant 를 책임지면 `ItemInstanceService.place()` 의 explicit 호출은 중복이 되어 제거 (DRY). Javadoc 으로 invariant 를 메서드 contract 에 명시해 미래 개발자가 의미를 이해할 수 있게 한다.

**Tech Stack:** Java 17, Spring Boot 3.x, JPA, JUnit 5, Mockito, AssertJ

**Related:**
- 직전 hotfix commit: `66c93e8` (`fix(gameplay): eagerly initialize ItemInstance LAZY relations in place()` — 본 plan 으로 대체될 옵션 A 의 explicit 호출)
- 원본 spec section 6.2: `docs/superpowers/specs/2026-05-13-gameworldservice-refactor-design.md` (인메모리 캐시 LAZY 초기화 invariant 의 정의)
- 도메인 모델 Javadoc: `ItemInstance.initializeAssociatedEntities()` (`gamedata/.../ItemInstance.java:33-39`), `Inventory.initializeAssociatedEntities()` (`gamedata/.../Inventory.java:80-88`)

---

## 파일 구조

**수정 (main)**
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/map/Room.java:144-146` — `addFloorItem` 의 시작점에서 init 호출 + Javadoc 추가
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Inventory.java:57-68` — `addItem` 의 시작점에서 init 호출 + Javadoc 추가
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceService.java:55` — 중복된 explicit init 호출 1줄 제거

**수정 (test)**
- `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/map/RoomFloorItemsTest.java` — `addFloorItem` 가 init 을 호출하는지 검증하는 회귀 테스트 1개 추가
- `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/InventoryTest.java` — `addItem` 가 init 을 호출하는지 검증하는 회귀 테스트 2개 (신규 추가 경로 + stackable merge 경로) 추가

**생성/삭제:** 없음. `ItemInstanceServiceTest.place_shouldInitializeAssociatedEntitiesOnInstance_beforeAddingToCache` 는 그대로 유지됨 — 실행 경로가 `place() → room.addFloorItem(spy)` 로 바뀌어도 spy 의 `initializeAssociatedEntities()` 가 결국 호출되므로 verify 가 그대로 통과.

---

## Task 1: `Room.addFloorItem` 에서 init 호출 (TDD)

`Room.addFloorItem(item)` 의 첫 줄에서 `item.initializeAssociatedEntities()` 를 호출하여, 이 애그리거트 루트가 "내가 담는 아이템은 항상 LAZY 그래프가 초기화돼 있다" 라는 invariant 를 강제하도록 한다.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/map/Room.java:144-146`
- Modify: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/map/RoomFloorItemsTest.java`

- [ ] **Step 1: 회귀 테스트 추가 (TDD red)**

`RoomFloorItemsTest.java` 본문에 다음 테스트 메서드를 기존 `@Test` 메서드들 사이 (예: `addFloorItem_addsItemToFloor` 바로 아래) 에 추가:

```java
    @Test
    void addFloorItem_callsInitializeAssociatedEntitiesOnItem() {
        // detached cache invariant: 애그리거트는 자기에 담기는 아이템의 LAZY 그래프를
        // 강제 초기화한다. 호출자가 까먹어도 invariant 유지.
        ItemInstance item = org.mockito.Mockito.spy(new ItemInstance(swordTemplate, 1));

        room.addFloorItem(item);

        org.mockito.Mockito.verify(item).initializeAssociatedEntities();
    }
```

기존 import 에 Mockito 가 없으므로 FQN 으로 인라인 사용. (한 테스트 파일에서 import 추가 vs FQN 의 trade-off — FQN 1회 사용은 import 추가 없이 처리하는 것이 작은 변경으로 깔끔.)

만약 같은 파일에 추가 Mockito 사용 테스트가 더 들어올 가능성이 있다면 다음 import 추가:
```java
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
```
이 경우 테스트 본문은 `Mockito.spy` / `Mockito.verify` 대신 `spy(...)` / `verify(...)` 로 단순화. **본 plan 에서는 FQN 인라인 1회로 진행** (다른 테스트 추가 계획 없음, YAGNI).

- [ ] **Step 2: 테스트 실패 확인 (TDD red 검증)**

Run:
```
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomFloorItemsTest.addFloorItem_callsInitializeAssociatedEntitiesOnItem"
```

Expected: FAIL. 메시지 예시:
```
org.mockito.exceptions.verification.WantedButNotInvoked:
Wanted but not invoked:
itemInstance.initializeAssociatedEntities();
```

만약 PASS 한다면 이미 `addFloorItem` 이 init 을 호출하고 있다는 뜻 (이 plan 전제와 어긋남). Stop and re-investigate.

- [ ] **Step 3: `Room.addFloorItem` 구현 + Javadoc 추가**

현재 코드 (`Room.java:144-146`):
```java
	public void addFloorItem(ItemInstance item) {
		this.floorItems.add(item);
	}
```

변경 후:
```java
	/**
	 * 방 바닥에 아이템을 추가한다. detached 캐시 invariant 를 위해 아이템의 LAZY 그래프를
	 * 강제 초기화한 뒤 컬렉션에 담는다 — 이 메서드를 통과한 아이템은 세션 없이도
	 * template / 하위 그래프에 안전히 접근할 수 있음이 보장된다.
	 */
	public void addFloorItem(ItemInstance item) {
		item.initializeAssociatedEntities();
		this.floorItems.add(item);
	}
```

Edit 도구 사용:
- `old_string`:
```
	public void addFloorItem(ItemInstance item) {
		this.floorItems.add(item);
	}
```
- `new_string`:
```
	/**
	 * 방 바닥에 아이템을 추가한다. detached 캐시 invariant 를 위해 아이템의 LAZY 그래프를
	 * 강제 초기화한 뒤 컬렉션에 담는다 — 이 메서드를 통과한 아이템은 세션 없이도
	 * template / 하위 그래프에 안전히 접근할 수 있음이 보장된다.
	 */
	public void addFloorItem(ItemInstance item) {
		item.initializeAssociatedEntities();
		this.floorItems.add(item);
	}
```

- [ ] **Step 4: 새 테스트 + 기존 RoomFloorItemsTest 전체 통과 확인 (TDD green)**

Run:
```
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomFloorItemsTest"
```

Expected: BUILD SUCCESSFUL, 5 tests passed (기존 4개 + 신규 1개).

기존 테스트가 fail 한다면: `addFloorItem` 의 init 호출이 다른 동작에 영향을 줬다는 뜻 — diff 확인 후 root cause 분석. 가장 가능성 높은 원인은 ItemInstance 가 spy 가 아니라 실제 객체일 때 `template == null` 같은 빌더 누락. RoomFloorItemsTest 의 기존 테스트들은 `new ItemInstance(swordTemplate, 1)` 로 template 을 항상 넣고 있으므로 정상 작동해야 함.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/map/Room.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/map/RoomFloorItemsTest.java
git commit -m "$(cat <<'EOF'
refactor(domain): make Room.addFloorItem enforce LAZY-init invariant on incoming items

Move the detached-cache invariant ("items stored in cached Room have LAZY graph initialized")
from the caller (ItemInstanceService.place()) into the aggregate root method itself.
Idempotent — re-initialization of already-initialized collections is a no-op .size() call,
so existing callers (DropCommandService) are unaffected. Future entry points
(merchant buy, crafting, event rewards) become automatically safe.
EOF
)"
```

**IMPORTANT:** No Co-Authored-By trailer (this repo's convention).

---

## Task 2: `Inventory.addItem` 에서 init 호출 (TDD)

`Inventory.addItem(instance)` 의 첫 줄에서 `instance.initializeAssociatedEntities()` 를 호출. stackable merge 경로에서도 동일하게 호출되도록 메서드 최상단에 위치시킨다. (merge 후 instance 가 버려지더라도 호출은 idempotent 라 무해, 그리고 향후 merge 이전에 instance 의 template 을 검사해야 할 수도 있으므로 일관성 유지.)

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Inventory.java:57-68`
- Modify: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/InventoryTest.java`

- [ ] **Step 1: 회귀 테스트 2개 추가 (TDD red)**

`InventoryTest.java` 본문에 기존 `addItem_*` 테스트들 사이에 다음 두 테스트를 추가:

```java
    @Test
    void addItem_nonStackable_callsInitializeAssociatedEntitiesOnInstance() {
        // 새로 추가되는 경로에서 invariant 강제
        ItemInstance instance = org.mockito.Mockito.spy(new ItemInstance(swordTemplate, 1));

        inventory.addItem(instance);

        org.mockito.Mockito.verify(instance).initializeAssociatedEntities();
    }

    @Test
    void addItem_stackableMerge_callsInitializeAssociatedEntitiesOnIncomingInstance() {
        // stackable merge 경로에서도 invariant 강제 — merge 후 incoming 이 버려지더라도
        // idempotent 호출이라 무해. 일관성 유지가 목적.
        inventory.addItem(new ItemInstance(foodTemplate, 3));   // 첫 아이템 (merge target)
        ItemInstance incoming = org.mockito.Mockito.spy(new ItemInstance(foodTemplate, 2));

        inventory.addItem(incoming);

        org.mockito.Mockito.verify(incoming).initializeAssociatedEntities();
        // merge 결과도 함께 확인 — 기존 addItem_stackableItem_mergesQuantity 와 같은 동작
        assertThat(inventory.getItems()).hasSize(1);
        assertThat(inventory.getItems().get(0).getQuantity()).isEqualTo(5);
    }
```

(기존 InventoryTest 는 `static import org.assertj.core.api.Assertions.assertThat;` 가 있으므로 `assertThat(...)` 사용 가능. Mockito 는 FQN 으로 인라인 처리 — Room 테스트와 동일한 컨벤션.)

- [ ] **Step 2: 두 테스트 실패 확인 (TDD red 검증)**

Run:
```
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.InventoryTest.addItem_nonStackable_callsInitializeAssociatedEntitiesOnInstance" --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.InventoryTest.addItem_stackableMerge_callsInitializeAssociatedEntitiesOnIncomingInstance"
```

Expected: 두 테스트 모두 FAIL with `WantedButNotInvoked`.

만약 둘 중 하나만 fail 한다면 spy 셋업이 어긋난 것이므로 Step 1 의 코드와 정확히 비교.

- [ ] **Step 3: `Inventory.addItem` 구현 + Javadoc 추가**

현재 코드 (`Inventory.java:57-68`):
```java
    public void addItem(ItemInstance instance) {
        if (instance.getTemplate().isStackable()) {
            Optional<ItemInstance> existing = items.stream()
                    .filter(i -> i.getTemplate() == instance.getTemplate())
                    .findFirst();
            if (existing.isPresent()) {
                existing.get().addQuantity(instance.getQuantity());
                return;
            }
        }
        items.add(instance);
    }
```

변경 후 (메서드 시그니처 / merge 로직 / add 로직은 그대로 유지, 첫 줄에 init 호출 + Javadoc):
```java
    /**
     * 인벤토리에 아이템을 추가한다. detached 캐시 invariant 를 위해 아이템의 LAZY 그래프를
     * 강제 초기화한 뒤 처리한다 (stackable merge / 새 인스턴스 추가 두 경로 모두에서) —
     * 이 메서드를 통과한 아이템은 세션 없이도 template / 하위 그래프에 안전히 접근할 수 있음이 보장된다.
     */
    public void addItem(ItemInstance instance) {
        instance.initializeAssociatedEntities();
        if (instance.getTemplate().isStackable()) {
            Optional<ItemInstance> existing = items.stream()
                    .filter(i -> i.getTemplate() == instance.getTemplate())
                    .findFirst();
            if (existing.isPresent()) {
                existing.get().addQuantity(instance.getQuantity());
                return;
            }
        }
        items.add(instance);
    }
```

Edit 도구 사용:
- `old_string`:
```
    public void addItem(ItemInstance instance) {
        if (instance.getTemplate().isStackable()) {
            Optional<ItemInstance> existing = items.stream()
                    .filter(i -> i.getTemplate() == instance.getTemplate())
                    .findFirst();
            if (existing.isPresent()) {
                existing.get().addQuantity(instance.getQuantity());
                return;
            }
        }
        items.add(instance);
    }
```
- `new_string`:
```
    /**
     * 인벤토리에 아이템을 추가한다. detached 캐시 invariant 를 위해 아이템의 LAZY 그래프를
     * 강제 초기화한 뒤 처리한다 (stackable merge / 새 인스턴스 추가 두 경로 모두에서) —
     * 이 메서드를 통과한 아이템은 세션 없이도 template / 하위 그래프에 안전히 접근할 수 있음이 보장된다.
     */
    public void addItem(ItemInstance instance) {
        instance.initializeAssociatedEntities();
        if (instance.getTemplate().isStackable()) {
            Optional<ItemInstance> existing = items.stream()
                    .filter(i -> i.getTemplate() == instance.getTemplate())
                    .findFirst();
            if (existing.isPresent()) {
                existing.get().addQuantity(instance.getQuantity());
                return;
            }
        }
        items.add(instance);
    }
```

- [ ] **Step 4: 신규 + 기존 InventoryTest 전체 통과 확인 (TDD green)**

Run:
```
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.InventoryTest"
```

Expected: BUILD SUCCESSFUL. 기존 InventoryTest 의 모든 테스트 + 신규 2개 모두 통과.

기존 `addItem_stackableItem_mergesQuantity` 가 fail 한다면, init 호출이 stackable merge 경로에 영향을 준 것 — but init 은 idempotent 이므로 영향 없어야 함. 만약 fail 한다면 diff 확인.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Inventory.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/InventoryTest.java
git commit -m "$(cat <<'EOF'
refactor(domain): make Inventory.addItem enforce LAZY-init invariant on incoming items

Mirrors the change in Room.addFloorItem. The init call sits at the top of addItem so that
both the stackable-merge path and the new-instance path force LAZY graph initialization
before the item is observed by any downstream code that may run outside a Hibernate session.
Idempotent — existing callers (TakeCommandService) are unaffected.
EOF
)"
```

No Co-Authored-By trailer.

---

## Task 3: `ItemInstanceService.place()` 의 중복 explicit init 호출 제거

Task 1 + 2 에서 애그리거트 루트가 invariant 를 강제하므로, `place()` 의 explicit 호출은 DRY 위반이자 misleading (두 곳이 같은 책임을 갖는 것처럼 보임). 1줄 제거.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceService.java:55`

- [ ] **Step 1: `ItemInstanceService.place()` 의 init 호출 1줄 제거**

현재 코드 (`ItemInstanceService.java:54-55` 부근, commit `66c93e8` 에서 추가됨):
```java
        ItemInstance instance = itemInstanceRepository.save(new ItemInstance(template, request.quantity()));
        instance.initializeAssociatedEntities();

        if (request.locationType() == LocationType.ROOM) {
```

변경 후:
```java
        ItemInstance instance = itemInstanceRepository.save(new ItemInstance(template, request.quantity()));

        if (request.locationType() == LocationType.ROOM) {
```

Edit 도구 사용:
- `old_string`:
```
        ItemInstance instance = itemInstanceRepository.save(new ItemInstance(template, request.quantity()));
        instance.initializeAssociatedEntities();

        if (request.locationType() == LocationType.ROOM) {
```
- `new_string`:
```
        ItemInstance instance = itemInstanceRepository.save(new ItemInstance(template, request.quantity()));

        if (request.locationType() == LocationType.ROOM) {
```

- [ ] **Step 2: `ItemInstanceServiceTest` 전체 통과 확인**

기존 테스트 `place_shouldInitializeAssociatedEntitiesOnInstance_beforeAddingToCache` 는 spy 의 `initializeAssociatedEntities()` 가 결국 호출되는지를 verify 한다. 새 구현 경로:
- `place()` → `placeInRoom(spy, 1L)` → `room.addFloorItem(spy)` → **`spy.initializeAssociatedEntities()` 호출** (Task 1 의 추가)
- 따라서 verify 가 그대로 통과.

Run:
```
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.ItemInstanceServiceTest"
```

Expected: BUILD SUCCESSFUL, 7 tests passed (모두 그린).

만약 `place_shouldInitializeAssociatedEntitiesOnInstance_beforeAddingToCache` 가 fail 한다면:
- `Room.addFloorItem` 가 실제로 init 을 호출하는지 (Task 1 정상 완료 여부) 확인
- spy 가 정상 작동하는지 (mock 객체에 stub 안 된 메서드 호출 시 default behavior 확인)
- `r != room` 분기 조건이 어떻게 동작하는지 — `rooms.findById` 가 같은 Room 객체를 반환하므로 `r == room` 이라 두 번째 addFloorItem 은 skip 됨. 첫 번째 `roomRepository.findById(...)` 가 반환한 room 에 대한 addFloorItem 호출에서 spy 의 init 이 트리거됨.

- [ ] **Step 3: 전체 테스트 회귀 검증**

Run:
```
./gradlew test
```

Expected: BUILD SUCCESSFUL, 280 tests passed, 0 failures.

(직전 hotfix commit `66c93e8` 이후 280 tests 였음. Task 1, Task 2 에서 각 1, 2개 회귀 테스트가 추가되므로 최종 283 tests 가 예상됨.)

테스트 count 확인:
```
find /Users/ch3224bin/dev/apps/ch3223bin/ddd7/mud-mk2/build/test-results/test -name "TEST-*.xml" -exec grep -h "<testsuite " {} \; | grep -oE 'tests="[0-9]+" skipped="[0-9]+" failures="[0-9]+" errors="[0-9]+"' | awk -F'"' '{tests+=$2; skipped+=$4; failures+=$6; errors+=$8} END {print "tests="tests" skipped="skipped" failures="failures" errors="errors}'
```

Expected: `tests=283 skipped=0 failures=0 errors=0`

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceService.java
git commit -m "$(cat <<'EOF'
refactor(gameplay): remove redundant explicit init from ItemInstanceService.place()

Now that Room.addFloorItem and Inventory.addItem enforce the LAZY-init invariant at the
aggregate boundary, the explicit instance.initializeAssociatedEntities() call in
ItemInstanceService.place() is redundant and misleading (two places appear to share the
same responsibility). DRY: single source of truth lives in the aggregate methods.

Test behavior unchanged: ItemInstanceServiceTest.place_shouldInitializeAssociatedEntitiesOnInstance_beforeAddingToCache
still verifies that initializeAssociatedEntities() is invoked, now via the aggregate chain.
EOF
)"
```

No Co-Authored-By trailer.

---

## Task 4: 다른 진입점 grep + 최종 검증 (코드 변경 없음)

invariant 가 정말로 한 곳에 모였는지 확인. 다른 `addFloorItem` / `addItem` 호출자가 init 을 별도로 부르고 있다면 그것도 중복이 되므로 식별 (제거는 본 plan 범위 밖이지만 보고).

**Files:** 없음 (검증 전용, commit 없음)

- [ ] **Step 1: 모든 `addFloorItem` / `addItem` 호출처 매핑**

Run:
```
grep -rn "\.addFloorItem(\|\.addItem(" /Users/ch3224bin/dev/apps/ch3223bin/ddd7/mud-mk2/src/main/java --include="*.java"
```

Expected 호출처 (현재 시점):
- `gameplay/application/service/ItemInstanceService.java:81` (`room.addFloorItem(instance)`)
- `gameplay/application/service/ItemInstanceService.java:85` (`r.addFloorItem(instance)` — 캐시 측)
- `gameplay/application/service/ItemInstanceService.java:93` (`character.getInventory().addItem(instance)`)
- `gameplay/application/service/ItemInstanceService.java:97` (`p.getInventory().addItem(instance)` — 캐시 측)
- `gameplay/application/service/command/DropCommandService.java:59` (`room.addFloorItem(item)`)
- `gameplay/application/service/command/TakeCommandService.java:66` (`inventory.addItem(item)`)

이 6 호출 모두 Task 1, Task 2 의 변경으로 자동 보호됨.

- [ ] **Step 2: 다른 explicit `initializeAssociatedEntities()` 호출 잔존 여부 확인**

Run:
```
grep -rn "\.initializeAssociatedEntities()" /Users/ch3224bin/dev/apps/ch3223bin/ddd7/mud-mk2/src/main/java --include="*.java"
```

Expected 출력 (도메인 내부 호출 + 신규 추가된 애그리거트 호출):
- `gamedata/.../map/Room.java:101` (부트스트랩 시점 `initializeAssociatedEntities` 정의 내부에서 floor item 순회)
- `gamedata/.../map/Room.java:121` (위 정의 내부 — item 별 init 호출)
- `gamedata/.../map/Room.java:145` 부근 (Task 1 신규 — `addFloorItem` 첫 줄)
- `gamedata/.../player/PlayerCharacter.java:72` (PlayerCharacter 부트스트랩)
- `gamedata/.../player/Inventory.java:58` 부근 (Task 2 신규 — `addItem` 첫 줄)
- `gamedata/.../player/Inventory.java:86` (Inventory 내부 — 부트스트랩 시점 item 순회)
- `gamedata/.../item/ItemInstance.java:38` (ItemInstance 자기 자신 — template 위임)

**없어야 할 것 (= invariant 가 한 곳에 모인 증거):**
- `gameplay/application/service/ItemInstanceService.java` 에서 직접 호출 (Task 3 에서 제거됨)
- 기타 `gameplay/` 모듈 호출자 측 (있다면 다음 plan 으로 정리 후보)

만약 `gameplay/` 모듈에 추가 explicit 호출이 남아있다면 파일·라인을 사용자에게 보고 (제거는 본 plan 범위 밖, 별도 결정 필요).

- [ ] **Step 3: Idempotency 회귀 검증 — Drop/Take 경로**

`DropCommandService.addFloorItem(item)` 과 `TakeCommandService.inventory.addItem(item)` 의 기존 단위 테스트가 모두 통과하는지 재확인 (이미 Step 3 of Task 3 의 전체 테스트로 커버되지만 명시적으로 확인):

Run:
```
./gradlew test --tests "*DropCommandServiceTest" --tests "*TakeCommandServiceTest"
```

Expected: BUILD SUCCESSFUL, 모든 테스트 통과.

만약 fail 한다면, init 호출이 idempotent 가정을 깨뜨린 것 — 가장 가능한 원인은 mock ItemInstance 의 `initializeAssociatedEntities()` 가 stub 안 돼있어 default 동작 (return void) 이 아닌 예외를 던지는 경우. 발견 시 사용자에게 보고.

- [ ] **Step 4: Hibernate 동작 회귀 검증 — 컴파일 + 전체 테스트**

Run:
```
./gradlew clean test
```

Expected:
- `BUILD SUCCESSFUL`
- `tests=283 skipped=0 failures=0 errors=0`

- [ ] **Step 5: 최종 보고 (commit 없음)**

검증 결과를 다음 형태로 사용자에게 보고:

```
옵션 ② 적용 완료. invariant 가 도메인 애그리거트로 이관됨.

검증 결과
- Room.addFloorItem 의 init 호출: 신규 회귀 테스트 1개로 보호
- Inventory.addItem 의 init 호출: 신규 회귀 테스트 2개 (new + merge) 로 보호
- ItemInstanceService.place() 의 explicit 호출: 제거 완료
- 다른 진입점 (Drop, Take) 의 회귀: 0건
- 빌드/테스트: BUILD SUCCESSFUL, 283/283 tests, 0 failures

후속 작업 가능 (별도 결정):
- (만약 발견 시) gameplay/ 모듈에 잔존하는 explicit init 호출 정리
- 다음 진입점 (상인 구매, 제작, 이벤트 보상) 가 추가될 때 별도 작업 없이 자동 보호됨
```

---

## Self-Review

이 plan 을 fresh eyes 로 다시 살펴 spec coverage / placeholder / type consistency 를 확인.

### 1. Spec coverage

| 요구사항 | 본 plan 위치 |
|---|---|
| `Room.addFloorItem` 가 init 강제 (옵션 ② 의 핵심) | Task 1 |
| `Inventory.addItem` 가 init 강제 (옵션 ② 의 핵심) | Task 2 |
| 호출자 explicit 호출 제거 (DRY) | Task 3 |
| 다른 진입점 잔존 확인 | Task 4 Step 1, Step 2 |
| 기존 호출자 (Drop, Take) idempotency 회귀 검증 | Task 4 Step 3 |
| 도메인 메서드 contract Javadoc 명시 | Task 1 Step 3, Task 2 Step 3 |
| 신규 진입점 (상인 구매, 제작, 이벤트 보상) 자동 보호 | Task 1, Task 2 변경의 자연스러운 귀결 (별도 task 불필요) |

누락 없음.

### 2. Placeholder scan

- "TBD" / "TODO" / "implement later": 없음
- "Add error handling" 형태 모호한 지시: 없음
- "Write tests for the above" without code: 없음 — Task 1 Step 1, Task 2 Step 1 모두 실제 테스트 코드 포함
- "Similar to Task N" 코드 미포함 참조: 없음 — Task 2 도 자체 완결적 코드 블록
- 미정의 type/method 참조: 없음. `ItemInstance.initializeAssociatedEntities()`, `Room.addFloorItem`, `Inventory.addItem` 모두 현재 코드에 존재함을 plan 작성 시점에 확인

### 3. Type consistency

- `ItemInstance` (parameter type of both `addFloorItem` and `addItem`): 일관
- `initializeAssociatedEntities()` (no-arg, void return): Task 1, Task 2 모두 동일 시그니처
- Mockito FQN 사용 (`org.mockito.Mockito.spy`, `org.mockito.Mockito.verify`): Task 1, Task 2 동일 컨벤션
- 테스트 메서드 명명 (`<methodName>_<scenario>`): 기존 RoomFloorItemsTest / InventoryTest 컨벤션과 일치

이슈 없음.

---

## 실행 흐름 요약

| Task | 변경 분량 | Commit 수 | 적정 subagent |
|---|---|---|---|
| Task 1: `Room.addFloorItem` init + Javadoc | 1 파일 main + 1 파일 test, ~15 라인 | 1 | sonnet (TDD) |
| Task 2: `Inventory.addItem` init + Javadoc | 1 파일 main + 1 파일 test, ~25 라인 | 1 | sonnet (TDD) |
| Task 3: `place()` explicit 호출 제거 | 1 파일 main, 1 라인 제거 | 1 | haiku (mechanical) |
| Task 4: 최종 검증 | 0 (grep + test) | 0 | sonnet (verification) |

총 3 commit. Task 4 종료 시 옵션 ② 이관 완료.
