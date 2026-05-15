# GameWorldService 리팩토링 구현 계획 — Phase 7 (잔여 정리 + 최종 검증)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** spec 6.2가 명시한 도메인 모델 3곳(`ItemInstance`, `ItemTemplate`, `Inventory`)의 Javadoc에서 "GameWorldService 캐시 적재" 표현을 "인메모리 캐시 적재"로 갱신하고, Phase 6에서 식별된 `FakeCreatureLookupQuery` 의 case-sensitive/insensitive 계약 미세 불일치를 정리한 뒤, GameWorldService 리팩토링 전체의 최종 회귀 검증(코드 참조 0건, 리플렉션 0건, 어댑터 5개 부트스트랩/BatchSyncable, 빌드/테스트 그린)을 마무리한다. 코드 변경 분량은 매우 작다: 도메인 모델 주석 3줄 + 테스트 fake 메서드 4줄 수정. 새 코드 작성/패키지 신설/삭제 없음.

**Architecture:** 본 phase는 신규 컴포넌트 도입이 아닌 잔여 정리이므로 아키텍처 변경이 없다. (1) 도메인 모델 주석은 동작에 영향 없는 텍스트 변경이며 spec 6.2 예시 ("GameWorldService 캐시 적재 시점에 LAZY ..." → "인메모리 캐시 적재 시점에 LAZY ...") 를 그대로 적용한다. (2) `FakeCreatureLookupQuery`는 `SpeakCommandServiceTest` 내부 fake로, 현재 `Map<String, X>` exact match 매칭이라 프로덕션 `DefaultCreatureLookupQuery` 의 `StringUtils.equalsIgnoreCase` 매칭과 계약이 어긋난다. 테스트는 그린이지만 "동일 이름의 다른 케이싱"으로 호출하는 회귀 테스트가 추후 추가될 때 fake가 실패해 디버깅 비용이 발생하므로 지금 정리한다. fake 구조는 유지하고 `findPlayerByName`/`findNpcByName` 의 검색 로직만 `entrySet().stream().filter(... equalsIgnoreCase ...)` 패턴으로 교체한다. (3) 최종 검증은 5개 grep + `./gradlew test` 1회로 수행. 검증 task에는 코드 변경이 없다.

**Tech Stack:** Java 17, Spring Boot 3.x, JPA, JUnit 5, Mockito, AssertJ, Apache Commons Lang3 `StringUtils`

**Spec:** `docs/superpowers/specs/2026-05-13-gameworldservice-refactor-design.md` (섹션 6.2 `initializeAssociatedEntities` 주석 갱신, 섹션 8.3 step 7 잔여 정리)

**Prior phases:**
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-room.md` (Phase 1+2 완료)
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-player.md` (Phase 3 완료)
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-npc.md` (Phase 4 완료)
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-monster.md` (Phase 5 완료)
- `docs/superpowers/plans/2026-05-13-gameworldservice-refactor-party.md` (Phase 6 완료)

---

## 파일 구조

**수정 (main)**
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstance.java:34` — Javadoc 1줄 갱신
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemTemplate.java:51` — Javadoc 1줄 갱신
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Inventory.java:81` — Javadoc 1줄 갱신

**수정 (test)**
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java:244-269` — `FakeCreatureLookupQuery.findPlayerByName`/`findNpcByName` 의 매칭 로직을 case-insensitive 로 교체

**생성/삭제:** 없음

---

## Task 1: 도메인 모델 Javadoc 일괄 갱신 (3 파일, 1 commit)

spec 6.2 가 명시한 도메인 모델 3곳의 Javadoc 에서 "GameWorldService" 결합 흔적을 제거. 어휘는 spec 6.2 예시("인메모리 캐시 적재 시점에 LAZY ...") 를 그대로 따른다. 동작 변경 없음 — 텍스트 갱신만.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstance.java:34`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemTemplate.java:51`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Inventory.java:81`

- [ ] **Step 1: `ItemInstance.java` Javadoc 갱신**

대상 라인 (현재):
```java
    /**
     * GameWorldService 캐시 적재 시점에 LAZY 관계를 강제 초기화한다.
     * detached 상태에서 template 접근 시 LazyInitializationException을 방지.
     */
    public void initializeAssociatedEntities() {
```

변경 후:
```java
    /**
     * 인메모리 캐시 적재 시점에 LAZY 관계를 강제 초기화한다.
     * detached 상태에서 template 접근 시 LazyInitializationException을 방지.
     */
    public void initializeAssociatedEntities() {
```

Edit 도구 사용:
- `old_string`: ` * GameWorldService 캐시 적재 시점에 LAZY 관계를 강제 초기화한다.`
- `new_string`: ` * 인메모리 캐시 적재 시점에 LAZY 관계를 강제 초기화한다.`

- [ ] **Step 2: `ItemTemplate.java` Javadoc 갱신**

대상 라인 (현재):
```java
    /**
     * GameWorldService 캐시 적재 시점에 서브클래스별 LAZY 컬렉션을 강제 초기화한다.
     * Hibernate 프록시 호출 시 실제 서브클래스 구현이 polymorphic dispatch 된다.
     */
    public abstract void initializeAssociatedEntities();
```

변경 후:
```java
    /**
     * 인메모리 캐시 적재 시점에 서브클래스별 LAZY 컬렉션을 강제 초기화한다.
     * Hibernate 프록시 호출 시 실제 서브클래스 구현이 polymorphic dispatch 된다.
     */
    public abstract void initializeAssociatedEntities();
```

Edit 도구 사용:
- `old_string`: ` * GameWorldService 캐시 적재 시점에 서브클래스별 LAZY 컬렉션을 강제 초기화한다.`
- `new_string`: ` * 인메모리 캐시 적재 시점에 서브클래스별 LAZY 컬렉션을 강제 초기화한다.`

- [ ] **Step 3: `Inventory.java` Javadoc 갱신**

대상 라인 (현재):
```java
    /**
     * GameWorldService 캐시 적재 시점에 LAZY 컬렉션 + 각 아이템의 template 그래프를 강제 초기화한다.
     */
    public void initializeAssociatedEntities() {
```

변경 후:
```java
    /**
     * 인메모리 캐시 적재 시점에 LAZY 컬렉션 + 각 아이템의 template 그래프를 강제 초기화한다.
     */
    public void initializeAssociatedEntities() {
```

Edit 도구 사용:
- `old_string`: ` * GameWorldService 캐시 적재 시점에 LAZY 컬렉션 + 각 아이템의 template 그래프를 강제 초기화한다.`
- `new_string`: ` * 인메모리 캐시 적재 시점에 LAZY 컬렉션 + 각 아이템의 template 그래프를 강제 초기화한다.`

- [ ] **Step 4: 잔존 참조 확인 (코드 변경 없음)**

Run:
```bash
grep -rn "GameWorldService" /Users/ch3224bin/dev/apps/ch3223bin/ddd7/mud-mk2/src/main --include="*.java"
```

Expected: 출력 없음 (3개 주석 모두 갱신됐는지 확인)

만약 출력이 있다면 위 step 1~3 중 어떤 Edit이 누락된 것이므로 누락된 파일을 다시 갱신한다.

- [ ] **Step 5: 컴파일 검증**

Run:
```bash
./gradlew compileJava -q
```

Expected: BUILD SUCCESSFUL. (주석만 변경했으므로 컴파일러가 영향받지 않아야 함)

만약 실패하면 Edit 도구가 인접 코드를 의도치 않게 건드린 것이므로 diff 확인 후 복구한다.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemInstance.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemTemplate.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Inventory.java
git commit -m "refactor(domain): update cache loading comments — replace GameWorldService references with neutral wording"
```

---

## Task 2: `FakeCreatureLookupQuery` 매칭 로직을 case-insensitive 로 통일 (1 commit)

Phase 6 final reviewer가 식별한 계약 미세 불일치 해결. `SpeakCommandServiceTest` 내부 `FakeCreatureLookupQuery` 가 `playersByName.get(name)` / `npcsByName.get(name)` 으로 exact match 매칭이라 프로덕션 `DefaultCreatureLookupQuery` 의 `StringUtils.equalsIgnoreCase` 매칭과 계약이 어긋난다. 현재 모든 테스트 케이스가 동일 케이싱을 사용해 그린이지만, 추후 회귀 테스트 추가 시 디버깅 비용이 발생하므로 미리 정리한다.

**Files:**
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java:244-269`

- [ ] **Step 1: 기존 매칭 동작을 보호하는 회귀 테스트 추가 (TDD red)**

`SpeakCommandServiceTest` 는 inner static class `FakePlayerCharacter(UUID id, String name, Long userId, Long roomId)` 와 `FakeNonPlayerCharacter(UUID id, String name, Long roomId)` (`PlayerCharacter`/`NonPlayerCharacter` 의 testing subclass) 로 객체를 생성한다 — 동일 패턴을 그대로 따른다. 별도 팩토리/빌더는 만들지 않음.

테스트 클래스 본문 (다른 top-level `@Test` 메서드들과 같은 위치, 즉 `class SpeakCommandServiceTest { ... }` 내부이되 inner `@Nested` 블록 바깥, 모든 fake inner class 정의 위) 에 다음 테스트 두 개를 추가:

```java
    @Nested
    @DisplayName("FakeCreatureLookupQuery 계약 회귀 테스트")
    class FakeCreatureLookupQueryContractTests {

        @Test
        @DisplayName("findPlayerByName 은 case-insensitive 매칭이어야 한다 (프로덕션 DefaultCreatureLookupQuery 와 일치)")
        void findPlayerByName_isCaseInsensitive() {
            FakePlayerCharacter player = new FakePlayerCharacter(PLAYER_ID, "Alice", USER_ID, ROOM_ID);
            FakeCreatureLookupQuery query = new FakeCreatureLookupQuery();
            query.indexPlayer(player);

            assertThat(query.findPlayerByName("alice")).contains(player);
            assertThat(query.findPlayerByName("ALICE")).contains(player);
            assertThat(query.findPlayerByName("Alice")).contains(player);
        }

        @Test
        @DisplayName("findNpcByName 은 case-insensitive 매칭이어야 한다 (프로덕션 DefaultCreatureLookupQuery 와 일치)")
        void findNpcByName_isCaseInsensitive() {
            FakeNonPlayerCharacter npc = new FakeNonPlayerCharacter(NPC_ID, "Goblin", ROOM_ID);
            FakeCreatureLookupQuery query = new FakeCreatureLookupQuery();
            query.addNpc(npc);

            assertThat(query.findNpcByName("goblin")).contains(npc);
            assertThat(query.findNpcByName("GOBLIN")).contains(npc);
            assertThat(query.findNpcByName("Goblin")).contains(npc);
        }
    }
```

임포트는 이미 모두 존재 확인됨 (`PLAYER_ID`, `USER_ID`, `ROOM_ID`, `NPC_ID` 는 같은 클래스 내 static field, `@Nested`/`@DisplayName`/`@Test` 도 기존 import 활용, `assertThat` 도 기존 import 활용).

추가 위치 결정 가이드:
- 파일 구조는 현재: 라인 1~16 import → 라인 17 `class SpeakCommandServiceTest {` → static field 선언 → `@BeforeEach setUp` → `@Nested SpeakTests` → (여기에 새 `@Nested` 블록 삽입) → 라인 244 부근 `static class FakeCreatureLookupQuery ...` 등 fake 정의들.
- 새 `@Nested` 블록은 마지막 `@Nested` 블록의 닫는 `}` 다음, 첫 `static class Fake...` 선언 이전에 삽입한다.

- [ ] **Step 2: 새 테스트 실패 확인 (TDD red 검증)**

Run:
```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.command.SpeakCommandServiceTest.fakeCreatureLookupQuery_findsPlayerCaseInsensitively" --tests "com.jefflife.mudmk2.gameplay.application.service.command.SpeakCommandServiceTest.fakeCreatureLookupQuery_findsNpcCaseInsensitively" -i
```

Expected: 두 테스트 모두 FAIL. 실패 메시지는 다음과 비슷해야 한다:
```
expected: Optional[Alice ...]
 but was: Optional.empty
```

만약 PASS 한다면 fake 가 이미 case-insensitive 거나 입력 케이싱이 같은 것이므로 Step 1 의 테스트 입력을 점검한다 ("alice"/"ALICE" 두 변형 모두 포함했는지).

- [ ] **Step 3: `FakeCreatureLookupQuery.findPlayerByName` 을 case-insensitive 로 교체**

대상 라인 (현재):
```java
        @Override
        public Optional<PlayerCharacter> findPlayerByName(String name) {
            return Optional.ofNullable(playersByName.get(name));
        }
```

변경 후 (`Map<String, PlayerCharacter> playersByName` 구조는 유지하고 검색 로직만 `entrySet().stream()` + `StringUtils.equalsIgnoreCase` 로 교체):
```java
        @Override
        public Optional<PlayerCharacter> findPlayerByName(String name) {
            return playersByName.entrySet().stream()
                    .filter(e -> org.apache.commons.lang3.StringUtils.equalsIgnoreCase(e.getKey(), name))
                    .map(java.util.Map.Entry::getValue)
                    .findFirst();
        }
```

(이미 `import org.apache.commons.lang3.StringUtils;` 가 있다면 `StringUtils.equalsIgnoreCase(...)` 로 간략화. import 가 없으면 FQN 형태로 인라인 사용해 추가 import 없이 처리.)

Edit 도구 사용:
- `old_string`:
```
        @Override
        public Optional<PlayerCharacter> findPlayerByName(String name) {
            return Optional.ofNullable(playersByName.get(name));
        }
```
- `new_string`:
```
        @Override
        public Optional<PlayerCharacter> findPlayerByName(String name) {
            return playersByName.entrySet().stream()
                    .filter(e -> org.apache.commons.lang3.StringUtils.equalsIgnoreCase(e.getKey(), name))
                    .map(java.util.Map.Entry::getValue)
                    .findFirst();
        }
```

- [ ] **Step 4: `FakeCreatureLookupQuery.findNpcByName` 도 동일 패턴으로 교체**

대상 라인 (현재):
```java
        @Override
        public Optional<NonPlayerCharacter> findNpcByName(String name) {
            return Optional.ofNullable(npcsByName.get(name));
        }
```

변경 후:
```java
        @Override
        public Optional<NonPlayerCharacter> findNpcByName(String name) {
            return npcsByName.entrySet().stream()
                    .filter(e -> org.apache.commons.lang3.StringUtils.equalsIgnoreCase(e.getKey(), name))
                    .map(java.util.Map.Entry::getValue)
                    .findFirst();
        }
```

Edit 도구 사용:
- `old_string`:
```
        @Override
        public Optional<NonPlayerCharacter> findNpcByName(String name) {
            return Optional.ofNullable(npcsByName.get(name));
        }
```
- `new_string`:
```
        @Override
        public Optional<NonPlayerCharacter> findNpcByName(String name) {
            return npcsByName.entrySet().stream()
                    .filter(e -> org.apache.commons.lang3.StringUtils.equalsIgnoreCase(e.getKey(), name))
                    .map(java.util.Map.Entry::getValue)
                    .findFirst();
        }
```

- [ ] **Step 5: 회귀 테스트 통과 확인 (TDD green)**

Run:
```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.command.SpeakCommandServiceTest" -i
```

Expected: BUILD SUCCESSFUL, `SpeakCommandServiceTest` 전체 PASS (Step 1의 새 테스트 2개 + 기존 테스트 모두).

만약 기존 테스트가 fail 한다면 inner class 구조나 import 가 어긋난 것이므로 diff 확인.

- [ ] **Step 6: Commit**

```bash
git add src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/SpeakCommandServiceTest.java
git commit -m "test(speak): align FakeCreatureLookupQuery matching with production case-insensitive contract"
```

---

## Task 3: 최종 회귀 검증 (코드 변경 없음, 1 commit 불필요)

리팩토링 전체의 최종 회귀 점검. 어떤 새 코드도 작성하지 않는다 — 5개 grep + 1회 빌드/테스트 실행으로 (a) `GameWorldService` 코드 참조 0건, (b) `PersistenceManager` 0건, (c) 리플렉션 0건, (d) 5개 InMemory* 어댑터가 self-bootstrap + BatchSyncable 모두 구현, (e) 277/277 테스트 그린을 확인. 검증 task에는 commit 이 없다.

**Files:** 없음 (검증 전용)

- [ ] **Step 1: `GameWorldService` 코드 참조 0건 확인 (주석 포함 0건)**

Run:
```bash
grep -rn "GameWorldService\|gameWorldService" /Users/ch3224bin/dev/apps/ch3223bin/ddd7/mud-mk2/src --include="*.java"
```

Expected: 출력 없음. (Task 1 이후 주석조차 0건이어야 함)

만약 출력이 있다면 Task 1 의 Edit 중 일부가 누락된 것이므로 누락된 파일을 식별해 Task 1 step 1~3 의 해당 Edit 을 재실행 후 Task 1 step 5(컴파일) + 신규 commit 추가.

- [ ] **Step 2: `PersistenceManager` 참조 0건 확인**

Run:
```bash
grep -rn "PersistenceManager" /Users/ch3224bin/dev/apps/ch3223bin/ddd7/mud-mk2/src --include="*.java"
```

Expected: 출력 없음. (Phase 6에서 파일 삭제됐으므로 import/import-as-comment/Javadoc 모두 0건이어야 함)

만약 출력이 있다면 어느 파일이 잔존 참조를 들고 있는지 확인 후 해당 파일에서 import 제거 + 컴파일 검증 + commit.

- [ ] **Step 3: gameplay 모듈 리플렉션 0건 확인**

Run:
```bash
grep -rn "java\.lang\.reflect\|getDeclaredField\|setAccessible" /Users/ch3224bin/dev/apps/ch3223bin/ddd7/mud-mk2/src/main/java/com/jefflife/mudmk2/gameplay --include="*.java"
```

Expected: 출력 없음. (Phase 4에서 `moveNpcToRoom` 리플렉션이 제거됐으므로 gameplay 전체에 리플렉션 0건)

- [ ] **Step 4: 5개 InMemory* 어댑터 self-bootstrap 확인**

Run:
```bash
grep -l "@EventListener(ApplicationReadyEvent.class)" /Users/ch3224bin/dev/apps/ch3223bin/ddd7/mud-mk2/src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/*.java
```

Expected: 정확히 다음 5개 파일이 나열:
```
InMemoryRoomRepository.java
InMemoryPlayerRepository.java
InMemoryNpcRepository.java
InMemoryMonsterRepository.java
InMemoryPartyRepository.java
```

(순서는 무관, 5줄/5파일 출력이 핵심.)

- [ ] **Step 5: 5개 InMemory* 어댑터 BatchSyncable 구현 확인**

Run:
```bash
grep -l "implements .*BatchSyncable\|, BatchSyncable" /Users/ch3224bin/dev/apps/ch3223bin/ddd7/mud-mk2/src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/cache/*.java
```

Expected: 정확히 5개 파일 (Step 4와 동일한 5개).

- [ ] **Step 6: 전체 빌드 + 테스트 그린 확인**

Run:
```bash
./gradlew clean test
```

Expected:
- `BUILD SUCCESSFUL`
- 277개 테스트 모두 통과 (Task 2 의 추가 테스트 2개 포함 시 279개 통과 가능 — Phase 6 종료 시점 277개에 +2)
- 0 failures, 0 errors, 0 skipped (skipped 가 0인지 확인하되 기존 phase 에서 skipped 가 있었다면 그 수치 유지)

만약 실패하면 실패한 테스트의 에러 메시지를 그대로 확인해 root cause 식별 (이 단계에서 코드 수정은 금지 — 사용자에게 보고하고 결정을 받는다).

- [ ] **Step 7: 리팩토링 완료 보고 (commit 없음)**

이 step에는 git operation 이 없다. 검증 결과를 한 줄씩 정리해 다음 형태로 사용자에게 보고:

```
Phase 7 완료. GameWorldService 리팩토링 전체 종료.

검증 결과
- GameWorldService 참조: 0건 (코드 + 주석 모두)
- PersistenceManager 참조: 0건
- gameplay 리플렉션: 0건
- InMemory* 어댑터 5개: self-bootstrap + BatchSyncable 모두 구현
- 빌드/테스트: BUILD SUCCESSFUL, 279/279 (또는 실제 수치)

도메인별 인메모리 리포지토리 5개, Query 3개, Service 3개로 책임이 분리됐고
GameWorldService.java / PersistenceManager.java / PartySyncService.java /
PlayerCharacterSyncService.java 4개 파일이 모두 제거됨.

다음 단계로 finishing-a-development-branch 스킬을 사용해 PR 또는 main merge
방향을 결정하시겠습니까?
```

(실제 보고 수치는 Step 6 결과로 치환. 테스트 수가 277/277이라면 그 값을 그대로 보고.)

---

## Self-Review

이 plan을 fresh eyes 로 다시 살펴 spec coverage / placeholder / type consistency 를 확인.

### 1. Spec coverage (spec 6.2, 8.3 step 7)

| spec 요구 | 본 plan 위치 |
|---|---|
| 6.2: `ItemInstance` 주석 "GameWorldService 캐시 적재" → "인메모리 캐시 적재" | Task 1 Step 1 |
| 6.2: `ItemTemplate` 동일 갱신 | Task 1 Step 2 |
| 6.2: `Inventory` 동일 갱신 | Task 1 Step 3 |
| 8.3 step 7: `GameWorldService.java` 삭제 | Phase 6 에서 이미 완료 — Task 3 Step 1 에서 0건 확인 |
| 8.3 step 7: `PersistenceManager.java` 삭제 | Phase 6 에서 이미 완료 — Task 3 Step 2 에서 0건 확인 |
| (추가) Fake/프로덕션 계약 일치 | Task 2 (사용자 승인됨) |

spec 6.2 의 `Room`, `PlayerCharacter`, `NonPlayerCharacter` 항목은 본문에 언급되지만 사전 grep 결과 해당 파일들의 `initializeAssociatedEntities` 메서드에는 "GameWorldService" 표현이 이미 없음을 확인 (조사 단계). 따라서 추가 task 불필요.

### 2. Placeholder scan

- "TBD" / "TODO" / "implement later": 없음
- "Add error handling" 형태: 없음
- "Write tests for the above" without code: Task 2 Step 1 에 실제 테스트 코드 포함
- "Similar to Task N" (코드 미포함 참조): 없음 — 각 step 이 자체 완결적인 Edit 명령과 코드 블록을 포함
- 미정의 type/method 참조: 없음. Task 2 Step 1 의 `FakePlayerCharacter` / `FakeNonPlayerCharacter` 시그니처는 `SpeakCommandServiceTest.java` 의 실제 inner class 정의 (라인 338, 378) 와 일치하도록 plan 작성 시점에 검증 완료

### 3. Type consistency

- `FakeCreatureLookupQuery.playersByName` / `npcsByName` 의 타입 (`Map<String, PlayerCharacter>` / `Map<String, NonPlayerCharacter>`): Task 2 Step 3, Step 4 모두 동일 시그니처 유지
- `StringUtils.equalsIgnoreCase` FQN 사용: Task 2 Step 3, Step 4 모두 동일 (`org.apache.commons.lang3.StringUtils.equalsIgnoreCase`)
- `Optional<PlayerCharacter>` / `Optional<NonPlayerCharacter>` 반환 타입: Step 3, Step 4 모두 일치
- Task 1 의 3개 Javadoc 변경은 "GameWorldService 캐시 적재" → "인메모리 캐시 적재" 일관 적용

이슈 없음.

---

## 실행 흐름 요약

| Task | 변경 분량 | Commit 수 | 적정 subagent |
|---|---|---|---|
| Task 1: 도메인 모델 Javadoc 갱신 | 3 파일 × 1 라인 = 3 라인 | 1 | haiku (mechanical) |
| Task 2: Fake 매칭 case-insensitive | 1 파일 × ~10 라인 (테스트 2개 + 메서드 2개) | 1 | sonnet (TDD) |
| Task 3: 최종 검증 | 0 (검증 전용) | 0 | sonnet (verification) |

총 2 commit. Task 3 종료 시 GameWorldService 리팩토링 전체 종료.
