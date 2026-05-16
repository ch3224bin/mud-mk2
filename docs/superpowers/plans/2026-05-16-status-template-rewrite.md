# Status Template Rewrite Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `status.html` 템플릿이 도메인 모델 변경 이후 갱신되지 않아 `상태창` 명령 실행 시 SpEL `null - Integer` 예외가 발생하는 문제를 해결한다. 무협 6속성/9무예 도메인에 맞춰 템플릿/Variables/Service를 전면 재작성하고, 각 능력치 옆에 장비로 인한 보너스 수치를 `base (+bonus)` 형식으로 표시한다.

**Architecture:**
- `PlayerCharacter`에 `getStatModifiers()` 메서드를 추가해 장비 보너스 맵을 노출.
- `StatusVariables`의 15개 `int` 능력치 필드를 nested `StatValue(int base, int bonus)` record로 교체.
- `StatusCommandService`가 base stats + modifier map을 조합해 `StatValue`로 매핑.
- `status.html`을 프로젝트 표준 CSS 클래스(`font-terminal`/`msg-room-title`/`line`/`label`/`line-spacer`)로 다시 작성. D&D 잔재 섹션 제거.

**Tech Stack:** Spring Boot, Thymeleaf, JUnit 5, Mockito, AssertJ

---

## File Structure

**Modify:**
- `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java` — `getStatModifiers()` 메서드 추가.
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/model/template/StatusVariables.java` — 15개 `int` → 15개 `StatValue`로 재정의.
- `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandService.java` — `StatusVariables` 생성 로직을 base + bonus 조합으로 변경.
- `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/eventpublisher/chat/StatusMessageSender.java` — `StatValue` 객체를 컨텍스트에 set.
- `src/main/resources/templates/gameplay/status.html` — 전면 재작성.

**Create:**
- `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacterStatModifiersTest.java` — `getStatModifiers()` 동작 검증.
- `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandServiceTest.java` — 서비스가 base + bonus를 정확히 매핑하는지 검증.

---

## Task 1: PlayerCharacter.getStatModifiers() 추가

**Files:**
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacterStatModifiersTest.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java`

- [ ] **Step 1: 테스트 파일 생성 (실패 테스트 작성)**

`src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacterStatModifiersTest.java` 신규 작성:

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerCharacterStatModifiersTest {

    private PlayerCharacter newPlayer(EquippedItems equipped) {
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
        return new PlayerCharacter(null, base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), Inventory.create(100), equipped);
    }

    @Test
    void getStatModifiers_emptyEquipment_returnsEmptyMap() {
        PlayerCharacter player = newPlayer(EquippedItems.create());

        Map<StatType, Integer> mods = player.getStatModifiers();

        assertThat(mods).isEmpty();
    }

    @Test
    void getStatModifiers_withWeapon_returnsAggregatedModifiers() {
        EquippedItems equipped = EquippedItems.create();
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(
                        new StatModifier(StatType.VIGOR, 2),
                        new StatModifier(StatType.SWORD_METHOD, 5)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));
        PlayerCharacter player = newPlayer(equipped);

        Map<StatType, Integer> mods = player.getStatModifiers();

        assertThat(mods).containsEntry(StatType.VIGOR, 2);
        assertThat(mods).containsEntry(StatType.SWORD_METHOD, 5);
    }
}
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacterStatModifiersTest"
```

Expected: 컴파일 에러 — `PlayerCharacter`에 `getStatModifiers()` 메서드 없음.

- [ ] **Step 3: PlayerCharacter에 메서드 추가**

`PlayerCharacter.java`에서 `getBaseStats()` 메서드(line 107~109) 바로 아래에 추가:

```java
    public java.util.Map<com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType, Integer> getStatModifiers() {
        return equippedItems.sumStatModifiers();
    }
```

(파일 상단에 `Map` / `StatType` import가 이미 있는지 확인하고 없으면 import 추가. 현재 `Map`은 `getStats()`에서 이미 import 되어 있음 — line 1~30의 import 섹션을 보고 추가 import 필요 여부 판단.)

- [ ] **Step 4: 테스트 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacterStatModifiersTest"
```

Expected: 2개 테스트 모두 PASS.

- [ ] **Step 5: 커밋**

```bash
git add src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacterStatModifiersTest.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java
git commit -m "$(cat <<'EOF'
feat(player): PlayerCharacter.getStatModifiers() — 장비 보너스 맵 노출

상태창에서 능력치 base/bonus 분리 표시를 위한 사전 작업.
EOF
)"
```

---

## Task 2: StatusVariables를 StatValue 구조로 재정의

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/model/template/StatusVariables.java`

이 Task는 record 정의 변경만 수행하며, 호출자(서비스/어댑터)는 Task 3/4에서 함께 고치므로 이 시점에는 컴파일이 깨진다. 다음 Task에서 즉시 복구한다.

- [ ] **Step 1: StatusVariables 전면 재작성**

`StatusVariables.java`를 다음 내용으로 교체:

```java
package com.jefflife.mudmk2.gameplay.application.service.model.template;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterState;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;

/**
 * Variables for the status message template.
 * 각 능력치/무예는 base 값과 장비/심법으로부터 누적된 bonus 값을 함께 보유한다.
 */
public record StatusVariables(
        Long userId,
        String playerName,
        CharacterClass characterClass,
        Gender gender,
        CharacterState state,
        int level,
        long experience,
        long nextLevelExp,
        // 현재 자원
        int hp,
        int maxHp,
        int mp,
        int maxMp,
        int ap,
        int maxAp,
        // 속성 6개
        StatValue vigor,
        StatValue physique,
        StatValue agility,
        StatValue intellect,
        StatValue will,
        StatValue meridian,
        // 무예 9개
        StatValue innerPower,
        StatValue specialTechnique,
        StatValue lightStep,
        StatValue fistsAndPalms,
        StatValue swordMethod,
        StatValue bladeMethod,
        StatValue longWeapon,
        StatValue esotericWeapon,
        StatValue archery,
        String roomName
) {
    public record StatValue(int base, int bonus) {
        public int total() {
            return base + bonus;
        }
    }
}
```

- [ ] **Step 2: 컴파일 깨졌음을 확인 (의도된 상태)**

```bash
./gradlew compileJava
```

Expected: `StatusCommandService.java`, `StatusMessageSender.java`에서 컴파일 에러. 다음 Task로 진행.

(Task 2는 단독 커밋하지 않음 — Task 3까지 묶어 commit 시 컴파일이 다시 통과한다.)

---

## Task 3: StatusMessageSender 어댑터 갱신

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/eventpublisher/chat/StatusMessageSender.java`

- [ ] **Step 1: 어댑터를 StatValue 객체로 set하도록 수정**

`StatusMessageSender.java`의 `sendMessage` 메서드를 다음으로 교체:

```java
    @Override
    public void sendMessage(final StatusVariables statusVariables) {
        Context context = new Context();
        context.setVariable("playerName", statusVariables.playerName());
        context.setVariable("characterClass", statusVariables.characterClass());
        context.setVariable("gender", statusVariables.gender());
        context.setVariable("state", statusVariables.state());
        context.setVariable("level", statusVariables.level());
        context.setVariable("experience", statusVariables.experience());
        context.setVariable("nextLevelExp", statusVariables.nextLevelExp());
        context.setVariable("hp", statusVariables.hp());
        context.setVariable("maxHp", statusVariables.maxHp());
        context.setVariable("mp", statusVariables.mp());
        context.setVariable("maxMp", statusVariables.maxMp());
        context.setVariable("ap", statusVariables.ap());
        context.setVariable("maxAp", statusVariables.maxAp());
        context.setVariable("vigor", statusVariables.vigor());
        context.setVariable("physique", statusVariables.physique());
        context.setVariable("agility", statusVariables.agility());
        context.setVariable("intellect", statusVariables.intellect());
        context.setVariable("will", statusVariables.will());
        context.setVariable("meridian", statusVariables.meridian());
        context.setVariable("innerPower", statusVariables.innerPower());
        context.setVariable("specialTechnique", statusVariables.specialTechnique());
        context.setVariable("lightStep", statusVariables.lightStep());
        context.setVariable("fistsAndPalms", statusVariables.fistsAndPalms());
        context.setVariable("swordMethod", statusVariables.swordMethod());
        context.setVariable("bladeMethod", statusVariables.bladeMethod());
        context.setVariable("longWeapon", statusVariables.longWeapon());
        context.setVariable("esotericWeapon", statusVariables.esotericWeapon());
        context.setVariable("archery", statusVariables.archery());
        context.setVariable("roomName", statusVariables.roomName());

        String htmlContent = templateEngine.process("gameplay/status", context);
        chatEventPublisher.messageToUser(statusVariables.userId(), htmlContent);
    }
```

(기존 파일과 유일한 차이: 능력치/무예 15개 변수는 이제 `int`가 아닌 `StatValue` 객체가 컨텍스트에 들어간다. 시그니처/이름 변화 없음.)

- [ ] **Step 2: 컴파일 확인 (서비스만 남았음)**

```bash
./gradlew compileJava
```

Expected: `StatusCommandService.java`만 컴파일 에러. 다음 Task로.

---

## Task 4: StatusCommandService에서 base + bonus 매핑 + 테스트

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandService.java`
- Create: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandServiceTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandServiceTest.java` 신규 작성:

```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.StatusCommand;
import com.jefflife.mudmk2.gameplay.application.service.model.template.StatusVariables;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendStatusMessagePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StatusCommandServiceTest {

    private ActivePlayerRepository players;
    private ActiveRoomRepository rooms;
    private SendStatusMessagePort sender;
    private StatusCommandService service;
    private PlayerCharacter player;
    private EquippedItems equipped;

    @BeforeEach
    void setUp() {
        players = mock(ActivePlayerRepository.class);
        rooms = mock(ActiveRoomRepository.class);
        sender = mock(SendStatusMessagePort.class);
        service = new StatusCommandService(rooms, players, sender);

        equipped = EquippedItems.create();
        BaseCharacter base = BaseCharacter.builder()
                .name("철수").background("d").gender(Gender.MALE)
                .hp(100).mp(50).ap(80)
                .vigor(10).physique(12).agility(11).intellect(9).will(8).meridian(7)
                .innerPower(3).specialTechnique(2).lightStep(1)
                .fistsAndPalms(0).swordMethod(4).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(1L).alive(true).build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(5).experience(120).nextLevelExp(500).conversable(true).build();
        player = new PlayerCharacter(null, base, playable, 1L, "철수",
                CharacterClass.WARRIOR, true, LocalDateTime.now(), Inventory.create(100), equipped);

        Room room = mock(Room.class);
        when(room.getName()).thenReturn("훈련장");
        when(rooms.findById(1L)).thenReturn(Optional.of(room));
        when(players.findByUserId(1L)).thenReturn(Optional.of(player));
    }

    private StatusVariables capturedVariables() {
        ArgumentCaptor<StatusVariables> cap = ArgumentCaptor.forClass(StatusVariables.class);
        verify(sender).sendMessage(cap.capture());
        return cap.getValue();
    }

    @Test
    void showStatus_noEquipment_allStatsHaveZeroBonus() {
        service.showStatus(new StatusCommand(1L));

        StatusVariables v = capturedVariables();
        assertThat(v.userId()).isEqualTo(1L);
        assertThat(v.playerName()).isEqualTo("철수");
        assertThat(v.roomName()).isEqualTo("훈련장");
        assertThat(v.vigor().base()).isEqualTo(10);
        assertThat(v.vigor().bonus()).isEqualTo(0);
        assertThat(v.physique().base()).isEqualTo(12);
        assertThat(v.physique().bonus()).isEqualTo(0);
        assertThat(v.swordMethod().base()).isEqualTo(4);
        assertThat(v.swordMethod().bonus()).isEqualTo(0);
        assertThat(v.archery().base()).isEqualTo(0);
        assertThat(v.archery().bonus()).isEqualTo(0);
    }

    @Test
    void showStatus_withEquippedWeapon_appliesBonusToCorrectStats() {
        WeaponTemplate sword = WeaponTemplate.builder()
                .name("철검").description("d").weight(5).stackable(false)
                .weaponType(WeaponType.SWORD)
                .statModifiers(List.of(
                        new StatModifier(StatType.VIGOR, 2),
                        new StatModifier(StatType.SWORD_METHOD, 5)))
                .build();
        equipped.equip(EquipmentSlot.WEAPON, new ItemInstance(sword, 1));

        service.showStatus(new StatusCommand(1L));

        StatusVariables v = capturedVariables();
        assertThat(v.vigor().base()).isEqualTo(10);
        assertThat(v.vigor().bonus()).isEqualTo(2);
        assertThat(v.vigor().total()).isEqualTo(12);
        assertThat(v.swordMethod().base()).isEqualTo(4);
        assertThat(v.swordMethod().bonus()).isEqualTo(5);
        assertThat(v.swordMethod().total()).isEqualTo(9);
        // 보너스가 없는 스탯은 그대로
        assertThat(v.physique().bonus()).isEqualTo(0);
        assertThat(v.archery().bonus()).isEqualTo(0);
    }

    @Test
    void showStatus_resourcesFromBaseStats_notFromTotal() {
        service.showStatus(new StatusCommand(1L));

        StatusVariables v = capturedVariables();
        assertThat(v.hp()).isEqualTo(100);
        assertThat(v.mp()).isEqualTo(50);
        assertThat(v.ap()).isEqualTo(80);
        // maxHp = physique * 10 + specialTechnique * 3 = 12 * 10 + 2 * 3 = 126
        assertThat(v.maxHp()).isEqualTo(126);
        // maxMp = meridian * 5 + innerPower * 3 = 7 * 5 + 3 * 3 = 44
        assertThat(v.maxMp()).isEqualTo(44);
        // maxAp = agility * 8 = 11 * 8 = 88
        assertThat(v.maxAp()).isEqualTo(88);
    }
}
```

- [ ] **Step 2: 테스트가 실패(컴파일 에러)함을 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.command.StatusCommandServiceTest"
```

Expected: 컴파일 에러 — `StatusCommandService`가 아직 `StatValue`를 만들지 않음.

- [ ] **Step 3: StatusCommandService 구현 교체**

`StatusCommandService.java`의 `showStatus` 메서드를 다음으로 교체:

```java
    @Override
    public void showStatus(final StatusCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
        Long currentRoomId = player.getCurrentRoomId();
        Room currentRoom = rooms.findById(currentRoomId)
                .orElseThrow(() -> new RoomNotFoundException(currentRoomId));

        CharacterStats base = player.getBaseStats();
        CharacterStats total = player.getStats();

        StatusVariables statusVariables = new StatusVariables(
                player.getUserId(),
                player.getName(),
                player.getCharacterClass(),
                player.getBaseCharacterInfo().getGender(),
                player.getState(),
                player.getPlayableCharacterInfo().getLevel(),
                player.getPlayableCharacterInfo().getExperience(),
                player.getPlayableCharacterInfo().getNextLevelExp(),
                total.hp(),
                total.maxHp(),
                total.mp(),
                total.maxMp(),
                total.ap(),
                total.maxAp(),
                new StatusVariables.StatValue(base.vigor(),            total.vigor()            - base.vigor()),
                new StatusVariables.StatValue(base.physique(),         total.physique()         - base.physique()),
                new StatusVariables.StatValue(base.agility(),          total.agility()          - base.agility()),
                new StatusVariables.StatValue(base.intellect(),        total.intellect()        - base.intellect()),
                new StatusVariables.StatValue(base.will(),             total.will()             - base.will()),
                new StatusVariables.StatValue(base.meridian(),         total.meridian()         - base.meridian()),
                new StatusVariables.StatValue(base.innerPower(),       total.innerPower()       - base.innerPower()),
                new StatusVariables.StatValue(base.specialTechnique(), total.specialTechnique() - base.specialTechnique()),
                new StatusVariables.StatValue(base.lightStep(),        total.lightStep()        - base.lightStep()),
                new StatusVariables.StatValue(base.fistsAndPalms(),    total.fistsAndPalms()    - base.fistsAndPalms()),
                new StatusVariables.StatValue(base.swordMethod(),      total.swordMethod()      - base.swordMethod()),
                new StatusVariables.StatValue(base.bladeMethod(),      total.bladeMethod()      - base.bladeMethod()),
                new StatusVariables.StatValue(base.longWeapon(),       total.longWeapon()       - base.longWeapon()),
                new StatusVariables.StatValue(base.esotericWeapon(),   total.esotericWeapon()   - base.esotericWeapon()),
                new StatusVariables.StatValue(base.archery(),          total.archery()          - base.archery()),
                currentRoom.getName()
        );

        sendStatusMessagePort.sendMessage(statusVariables);
        logger.debug("Sent status message for player: {}", player.getName());
    }
```

(이전 버전과 차이점: `stats` 한 번 호출 → `base` + `total` 두 번 호출. `getStatModifiers()`도 가능하나 `getBaseStats()` + `getStats()` 차분이 가장 단순. 자원(hp/maxHp/...)은 `total`에서 가져온다 — `getStats()`는 hp/mp/ap는 base에서 그대로 유지하고 maxHp/maxMp/maxAp는 derived stat이므로 보너스 반영된 total 기준이 맞음.)

- [ ] **Step 4: 테스트 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.command.StatusCommandServiceTest"
```

Expected: 3개 테스트 모두 PASS.

- [ ] **Step 5: 전체 컴파일 + 영향받은 모든 테스트 통과 확인**

```bash
./gradlew compileTestJava
./gradlew test --tests "com.jefflife.mudmk2.*"
```

Expected: 모든 테스트 PASS. (`StatusVariables` 시그니처 변경으로 다른 곳이 깨졌을 수 있으니 전체 테스트 실행.)

- [ ] **Step 6: 커밋 (Task 2/3/4 통합)**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/model/template/StatusVariables.java \
        src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/eventpublisher/chat/StatusMessageSender.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandServiceTest.java
git commit -m "$(cat <<'EOF'
refactor(status): StatusVariables — 능력치마다 (base, bonus) 분리 — 장비 보너스 표시 준비

각 6속성/9무예를 nested StatValue(base, bonus) record로 보유.
서비스는 PlayerCharacter base/total 차분으로 bonus 산출.
EOF
)"
```

---

## Task 5: status.html 템플릿 전면 재작성

**Files:**
- Modify: `src/main/resources/templates/gameplay/status.html`

이 Task는 템플릿 파일 변경만 — 자동화 테스트 없음. 끝에서 수동 verify.

- [ ] **Step 1: status.html을 다음 내용으로 완전 교체**

```html
<div th:fragment="statusDetails" class="status-info font-terminal">
    <div class="msg-room-title">[ 상태창 ]</div>

    <div class="line">
        <span class="label">이름: </span><span th:text="${playerName}"></span>
        &nbsp;&nbsp;
        <span class="label">레벨: </span><span th:text="${level}"></span>
    </div>
    <div class="line">
        <span class="label">직업: </span><span th:text="${characterClass}"></span>
        &nbsp;&nbsp;
        <span class="label">성별: </span><span th:text="${gender == T(com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender).MALE ? '남성' : '여성'}"></span>
    </div>
    <div class="line">
        <span class="label">상태: </span><span th:text="${state.description()}"></span>
        &nbsp;&nbsp;
        <span class="label">위치: </span><span th:text="${roomName}"></span>
    </div>
    <div class="line">
        <span class="label">경험치: </span><span th:text="${experience + ' / ' + nextLevelExp}"></span>
    </div>

    <div class="line-spacer"></div>
    <div class="msg-room-title">[ 자원 ]</div>
    <div class="line">
        <span class="label">체력 (HP): </span><span th:text="${hp + ' / ' + maxHp}"></span>
    </div>
    <div class="line">
        <span class="label">내력 (MP): </span><span th:text="${mp + ' / ' + maxMp}"></span>
    </div>
    <div class="line">
        <span class="label">활동력 (AP): </span><span th:text="${ap + ' / ' + maxAp}"></span>
    </div>

    <div class="line-spacer"></div>
    <div class="msg-room-title">[ 속성 ]</div>
    <div class="line">
        <span class="label">활력: </span><span th:text="${vigor.base}"></span><span th:if="${vigor.bonus != 0}" th:text="${' (' + (vigor.bonus > 0 ? '+' : '') + vigor.bonus + ')'}"></span>
    </div>
    <div class="line">
        <span class="label">체력: </span><span th:text="${physique.base}"></span><span th:if="${physique.bonus != 0}" th:text="${' (' + (physique.bonus > 0 ? '+' : '') + physique.bonus + ')'}"></span>
    </div>
    <div class="line">
        <span class="label">민첩: </span><span th:text="${agility.base}"></span><span th:if="${agility.bonus != 0}" th:text="${' (' + (agility.bonus > 0 ? '+' : '') + agility.bonus + ')'}"></span>
    </div>
    <div class="line">
        <span class="label">지력: </span><span th:text="${intellect.base}"></span><span th:if="${intellect.bonus != 0}" th:text="${' (' + (intellect.bonus > 0 ? '+' : '') + intellect.bonus + ')'}"></span>
    </div>
    <div class="line">
        <span class="label">의지: </span><span th:text="${will.base}"></span><span th:if="${will.bonus != 0}" th:text="${' (' + (will.bonus > 0 ? '+' : '') + will.bonus + ')'}"></span>
    </div>
    <div class="line">
        <span class="label">기맥: </span><span th:text="${meridian.base}"></span><span th:if="${meridian.bonus != 0}" th:text="${' (' + (meridian.bonus > 0 ? '+' : '') + meridian.bonus + ')'}"></span>
    </div>

    <div class="line-spacer"></div>
    <div class="msg-room-title">[ 무예 ]</div>
    <div class="line">
        <span class="label">내공: </span><span th:text="${innerPower.base}"></span><span th:if="${innerPower.bonus != 0}" th:text="${' (' + (innerPower.bonus > 0 ? '+' : '') + innerPower.bonus + ')'}"></span>
    </div>
    <div class="line">
        <span class="label">특기: </span><span th:text="${specialTechnique.base}"></span><span th:if="${specialTechnique.bonus != 0}" th:text="${' (' + (specialTechnique.bonus > 0 ? '+' : '') + specialTechnique.bonus + ')'}"></span>
    </div>
    <div class="line">
        <span class="label">경공: </span><span th:text="${lightStep.base}"></span><span th:if="${lightStep.bonus != 0}" th:text="${' (' + (lightStep.bonus > 0 ? '+' : '') + lightStep.bonus + ')'}"></span>
    </div>
    <div class="line">
        <span class="label">권장: </span><span th:text="${fistsAndPalms.base}"></span><span th:if="${fistsAndPalms.bonus != 0}" th:text="${' (' + (fistsAndPalms.bonus > 0 ? '+' : '') + fistsAndPalms.bonus + ')'}"></span>
    </div>
    <div class="line">
        <span class="label">검술: </span><span th:text="${swordMethod.base}"></span><span th:if="${swordMethod.bonus != 0}" th:text="${' (' + (swordMethod.bonus > 0 ? '+' : '') + swordMethod.bonus + ')'}"></span>
    </div>
    <div class="line">
        <span class="label">도법: </span><span th:text="${bladeMethod.base}"></span><span th:if="${bladeMethod.bonus != 0}" th:text="${' (' + (bladeMethod.bonus > 0 ? '+' : '') + bladeMethod.bonus + ')'}"></span>
    </div>
    <div class="line">
        <span class="label">장병기술: </span><span th:text="${longWeapon.base}"></span><span th:if="${longWeapon.bonus != 0}" th:text="${' (' + (longWeapon.bonus > 0 ? '+' : '') + longWeapon.bonus + ')'}"></span>
    </div>
    <div class="line">
        <span class="label">암기술: </span><span th:text="${esotericWeapon.base}"></span><span th:if="${esotericWeapon.bonus != 0}" th:text="${' (' + (esotericWeapon.bonus > 0 ? '+' : '') + esotericWeapon.bonus + ')'}"></span>
    </div>
    <div class="line">
        <span class="label">궁술: </span><span th:text="${archery.base}"></span><span th:if="${archery.bonus != 0}" th:text="${' (' + (archery.bonus > 0 ? '+' : '') + archery.bonus + ')'}"></span>
    </div>
</div>
```

(라벨은 `ItemDisplayLabels.of(StatType)`과 동일 — 일관성 유지. PHYSIQUE→"체력", HP는 위 "자원" 섹션의 라벨로 "체력 (HP)" 으로 표기되어 같은 단어가 겹치지만, 이는 의도된 도메인 용어 사용. 헷갈리면 추후 별도 PR에서 조정.)

- [ ] **Step 2: 컴파일/테스트 회귀 확인 (템플릿은 컴파일 대상 아님)**

```bash
./gradlew test
```

Expected: 모든 테스트 PASS.

- [ ] **Step 3: 수동 verify — 앱 기동하여 `상태창` 입력**

서버를 띄우고 클라이언트에서 `상태창` 입력. 다음을 눈으로 확인:

1. **예외가 발생하지 않는다.** (로그에 `TemplateInputException` / `SpelEvaluationException` 없음.)
2. 능력치 6개 + 무예 9개가 한글 라벨과 함께 출력된다.
3. 장비 미착용 시: `활력: 10` 처럼 보너스 표기 없음.
4. 무기 착용 시: `활력: 10 (+2)` 처럼 보너스 표기. (테스트 캐릭터에 `철검`을 장착시켜 확인.)

만약 출력이 비어 보이거나 라벨 일부가 누락되면 Task 5 Step 1로 돌아가 어긋난 변수명 확인.

- [ ] **Step 4: 커밋**

```bash
git add src/main/resources/templates/gameplay/status.html
git commit -m "$(cat <<'EOF'
fix(status): status.html 무협 도메인에 맞게 전면 재작성

기존 D&D식 STR/DEX/... 변수 참조로 인한 SpEL null 예외 해결.
6속성/9무예를 한글 라벨로 출력하고, 장비 보너스를 (+N)으로 표시.
EOF
)"
```

---

## Self-Review Notes (작성 후 점검 결과)

- **스펙 커버리지:** 1) SpEL null 예외 해결 → Task 5에서 변수 일치. 2) 보너스 표시 → Task 2~5. 3) 무협 도메인 정합 → Task 5 전면 재작성. 4) `ap`/`maxAp` 누락 보완 → Task 5 자원 섹션.
- **placeholder 없음.** 모든 step에 실행 가능한 코드/명령어 포함.
- **타입 일관성:** `StatValue` 이름·필드(`base`, `bonus`, `total()`)는 Task 2/4/5 모두 동일하게 사용.
- **컴파일 깨진 구간:** Task 2 종료 시점 ~ Task 4 종료 시점 사이에 컴파일이 깨진다. Task 4 Step 6에서 묶어 커밋해 master에는 깨진 상태가 들어가지 않게 한다.
