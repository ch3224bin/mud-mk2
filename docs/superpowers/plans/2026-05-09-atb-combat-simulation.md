# ATB 전투 시스템 개편 및 시뮬레이션 환경 구현 플랜

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** D20 임시 전투를 ATB 게이지 기반 전투로 교체하고, Admin 페이지에서 커스텀 스탯 몬스터를 소환해 MUD 인게임으로 테스트할 수 있는 시뮬레이션 환경을 구축한다.

**Architecture:** `CombatFormulas`(순수 수식) → `ATBCombatParticipant`(상태 관리) → `ATBCombat`(틱 루프)의 계층 구조. `CombatNarrativeFormatter`가 `CombatLog`를 한국어 텍스트로 변환해 WebSocket으로 전달. `SimulationController`가 커스텀 스탯 몬스터를 `GameWorldService`에 인메모리로 소환하면, 기존 `때려` 명령어와 `CombatService`가 그대로 새 ATB 전투를 실행한다.

**Tech Stack:** Java 21, Spring Boot 3, JPA/Hibernate, Thymeleaf, Bootstrap 5, JUnit 5, MockMvc

---

## 파일 구조

| 동작 | 경로 |
|------|------|
| 신규 | `src/main/java/.../gameplay/application/domain/model/combat/CombatFormulas.java` |
| 신규 | `src/main/java/.../gameplay/application/domain/model/combat/ATBCombatParticipant.java` |
| 신규 | `src/main/java/.../gameplay/application/domain/model/combat/ATBCombat.java` |
| 신규 | `src/main/java/.../gameplay/application/service/CombatNarrativeFormatter.java` |
| 신규 | `src/main/java/.../gameplay/application/service/SimulationService.java` |
| 신규 | `src/main/java/.../gameplay/adapter/in/webapi/SimulationController.java` |
| 신규 | `src/main/java/.../gameplay/adapter/in/webapi/dto/SimulationSpawnRequest.java` |
| 신규 | `src/main/java/.../gameplay/adapter/in/webapi/dto/SpawnedMonsterResponse.java` |
| 신규 | `src/main/resources/templates/web/simulation-management.html` |
| 신규 (테스트) | `src/test/java/.../combat/CombatFormulasTest.java` |
| 신규 (테스트) | `src/test/java/.../combat/ATBCombatTest.java` |
| 신규 (테스트) | `src/test/java/.../combat/FixedRandomGenerator.java` |
| 신규 (테스트) | `src/test/java/.../service/CombatNarrativeFormatterTest.java` |
| 수정 | `src/main/java/.../gameplay/application/domain/model/combat/CombatLog.java` |
| 수정 | `src/main/java/.../gameplay/application/service/CombatService.java` |
| 수정 | `src/main/java/.../gamedata/application/domain/model/player/Monster.java` |
| 수정 | `src/main/java/.../gamedata/application/domain/model/map/Room.java` |
| 수정 | `src/main/java/.../web/IndexController.java` |
| 수정 | `src/main/resources/templates/web/admin.html` |
| 삭제/교체 | `src/test/java/.../combat/CombatTest.java` |

패키지 기본 경로: `com.jefflife.mudmk2`

---

## Task 1: CombatFormulas — 순수 전투 수식 클래스

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatFormulas.java`
- Create (test): `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatFormulasTest.java`
- Create (test helper): `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/FixedRandomGenerator.java`

- [ ] **Step 1: FixedRandomGenerator 작성**

```java
// src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/FixedRandomGenerator.java
package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

public class FixedRandomGenerator implements RandomGenerator {
    private final int fixedValue;

    public FixedRandomGenerator(int fixedValue) {
        this.fixedValue = fixedValue;
    }

    @Override
    public int nextInt(int bound) {
        return Math.min(fixedValue, bound - 1);
    }
}
```

- [ ] **Step 2: CombatFormulasTest 작성**

```java
// src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatFormulasTest.java
package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CombatFormulasTest {

    // 민첩 14, 경공 5
    private CharacterStats makeStats(int vigor, int physique, int agility, int intellect, int will, int meridian,
                                      int lightStep, int swordMethod) {
        return new CharacterStats(100, 50, 80, vigor, physique, agility, intellect, will, meridian,
                0, 0, lightStep, 0, swordMethod, 0, 0, 0, 0);
    }

    @Test
    void initiativeSpeed_민첩14_경공5() {
        CharacterStats stats = makeStats(12, 10, 14, 10, 8, 10, 5, 8);
        assertThat(CombatFormulas.initiativeSpeed(stats)).isCloseTo(16.5, within(0.001));
    }

    @Test
    void accuracy_지력10_검법8_장비0() {
        CharacterStats stats = makeStats(12, 10, 14, 10, 8, 10, 5, 8);
        assertThat(CombatFormulas.accuracy(stats, 8, 0)).isCloseTo(62.0, within(0.001));
    }

    @Test
    void evasion_민첩14_경공5() {
        CharacterStats stats = makeStats(12, 10, 14, 10, 8, 10, 5, 8);
        assertThat(CombatFormulas.evasion(stats)).isCloseTo(13.2, within(0.001));
    }

    @Test
    void evasionRate_회피가낮으면0() {
        // evasion 13.2 - accuracy 62.0 = -48.8 → clamp to 0
        assertThat(CombatFormulas.evasionRate(13.2, 62.0)).isEqualTo(0);
    }

    @Test
    void evasionRate_회피가높으면최대75() {
        // evasion 200 - accuracy 50 = 150 → clamp to 75
        assertThat(CombatFormulas.evasionRate(200.0, 50.0)).isEqualTo(75);
    }

    @Test
    void evasionRate_중간값() {
        // evasion 80 - accuracy 50 = 30 → 30
        assertThat(CombatFormulas.evasionRate(80.0, 50.0)).isEqualTo(30);
    }

    @Test
    void baseDamage_무기20_검법8_근력15_랜덤고정10() {
        CharacterStats stats = makeStats(15, 10, 14, 10, 8, 10, 5, 8);
        // base = 20 × (1 + 8×0.008) + 15×0.3 = 20×1.064 + 4.5 = 21.28 + 4.5 = 25.78
        // randomFactor = 0.9 + 10/100 = 1.0  → (int)(25.78 × 1.0) = 25
        FixedRandomGenerator rng = new FixedRandomGenerator(10); // nextInt(21)→10 → factor=1.0
        assertThat(CombatFormulas.baseDamage(stats, 20, 8, rng)).isEqualTo(25);
    }

    @Test
    void critRate_근력15() {
        CharacterStats stats = makeStats(15, 10, 14, 10, 8, 10, 5, 8);
        assertThat(CombatFormulas.critRate(stats)).isEqualTo(4); // (int)(15×0.3)=4
    }

    @Test
    void armor_장비5_의지8() {
        CharacterStats stats = makeStats(12, 10, 14, 10, 8, 10, 5, 8);
        assertThat(CombatFormulas.armor(stats, 5)).isEqualTo(8); // 5 + (int)(8×0.4)=5+3=8
    }

    @Test
    void armorPct_의지8_검법8_장비0_상한미달() {
        CharacterStats stats = makeStats(12, 10, 14, 10, 8, 10, 5, 8);
        assertThat(CombatFormulas.armorPct(stats, 0, 8)).isEqualTo(2);
        // (int)(0 + 8×0.2 + 8×0.15) = (int)(1.6+1.2) = (int)2.8 = 2
    }

    @Test
    void armorPct_상한75초과시75() {
        CharacterStats stats = makeStats(12, 10, 14, 10, 300, 10, 5, 200);
        assertThat(CombatFormulas.armorPct(stats, 50, 200)).isEqualTo(75);
    }

    @Test
    void applyDefense_피해30_방어8_방어율10() {
        // 30 × (1 - 0.10) = 27.0 → 27 - 8 = 19
        assertThat(CombatFormulas.applyDefense(30, 8, 10)).isEqualTo(19);
    }

    @Test
    void applyDefense_최솟값1() {
        // 피해가 방어보다 낮아도 최소 1
        assertThat(CombatFormulas.applyDefense(1, 100, 75)).isEqualTo(1);
    }
}
```

- [ ] **Step 3: 테스트 실행 — FAIL 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.domain.model.combat.CombatFormulasTest"
```

Expected: FAIL (CombatFormulas 클래스 없음)

- [ ] **Step 4: CombatFormulas 구현**

```java
// src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatFormulas.java
package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;

public final class CombatFormulas {

    private CombatFormulas() {}

    public static double initiativeSpeed(CharacterStats stats) {
        return stats.agility() * 1.0 + stats.lightStep() * 0.5;
    }

    public static double accuracy(CharacterStats stats, int weaponSkill, int equipAccuracy) {
        return stats.intellect() * 0.8 + weaponSkill * 0.5 + equipAccuracy + 50;
    }

    public static double evasion(CharacterStats stats) {
        return stats.agility() * 0.8 + stats.lightStep() * 0.4;
    }

    public static int evasionRate(double evasion, double accuracy) {
        return Math.max(0, Math.min(75, (int)(evasion - accuracy)));
    }

    public static int baseDamage(CharacterStats stats, int weaponBase, int weaponSkill, RandomGenerator rng) {
        double weaponMultiplier = 1.0 + weaponSkill * 0.008;
        double base = weaponBase * weaponMultiplier + stats.vigor() * 0.3;
        double randomFactor = 0.9 + rng.nextInt(21) / 100.0;
        return (int)(base * randomFactor);
    }

    public static int critRate(CharacterStats stats) {
        return (int)(stats.vigor() * 0.3);
    }

    public static int armor(CharacterStats stats, int equipArmor) {
        return equipArmor + (int)(stats.will() * 0.4);
    }

    public static int armorPct(CharacterStats stats, int equipArmorPct, int weaponSkill) {
        return Math.min(75, (int)(equipArmorPct + stats.will() * 0.2 + weaponSkill * 0.15));
    }

    public static int applyDefense(int rawDamage, int armor, int armorPct) {
        double reduced = rawDamage * (1.0 - armorPct / 100.0);
        return Math.max(1, (int)(reduced - armor));
    }
}
```

- [ ] **Step 5: 테스트 실행 — PASS 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.domain.model.combat.CombatFormulasTest"
```

Expected: BUILD SUCCESSFUL, 10 tests passed

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatFormulas.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatFormulasTest.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/FixedRandomGenerator.java
git commit -m "feat: CombatFormulas 구현 - ATB 전투 수식 정적 클래스"
```

---

## Task 2: CombatLog 필드 추가

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatLog.java`

- [ ] **Step 1: CombatLog 레코드에 필드 추가**

기존 파일의 `boolean targetDefeated` 다음에 추가:

```java
// src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatLog.java
package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import lombok.Builder;
import java.util.UUID;

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
    // 신규 필드
    boolean evaded,
    boolean isCrit,
    int attackerApAfter,
    int targetApAfter,
    String weaponTypeName
) {
    @Builder
    public CombatLog {
    }
}
```

- [ ] **Step 2: 전체 빌드 — 컴파일 오류 확인**

```bash
./gradlew compileJava 2>&1 | grep -E "error:|BUILD"
```

Expected: `CombatLog.builder()` 사용처에서 컴파일 오류 발생 (CombatTest.java 등). Task 10에서 해결.

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatLog.java
git commit -m "feat: CombatLog에 evaded/isCrit/attackerApAfter/targetApAfter/weaponTypeName 필드 추가"
```

---

## Task 3: ATBCombatParticipant — ATB 게이지 + 자원 관리

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/ATBCombatParticipant.java`

- [ ] **Step 1: ATBCombatParticipant 구현**

```java
// src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/ATBCombatParticipant.java
package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Combatable;
import lombok.Getter;

import java.util.stream.IntStream;

@Getter
public class ATBCombatParticipant {

    private final Combatable combatable;
    private final CombatGroupType group;
    private final int weaponBaseDamage;
    private final int equipArmor;
    private final int equipArmorPct;
    private final int weaponSkill;
    private final String weaponTypeName;
    private double atbGauge;
    private int currentMp;
    private int currentAp;

    public ATBCombatParticipant(Combatable combatable, CombatGroupType group) {
        this(combatable, group, 10, 0, 0);
    }

    public ATBCombatParticipant(Combatable combatable, CombatGroupType group,
                                 int weaponBaseDamage, int equipArmor, int equipArmorPct) {
        this.combatable = combatable;
        this.group = group;
        this.weaponBaseDamage = weaponBaseDamage;
        this.equipArmor = equipArmor;
        this.equipArmorPct = equipArmorPct;
        CharacterStats stats = combatable.getStats();
        this.weaponSkill = deriveWeaponSkill(stats);
        this.weaponTypeName = deriveWeaponTypeName(stats);
        this.currentMp = stats.mp();
        this.currentAp = stats.ap();
        this.atbGauge = 0.0;
    }

    public void advanceAtb(int speedDivisor) {
        double speed = CombatFormulas.initiativeSpeed(combatable.getStats());
        this.atbGauge += speed / speedDivisor;
    }

    public boolean isReadyToAct() {
        return atbGauge >= 100.0;
    }

    public void resetAtb() {
        this.atbGauge -= 100.0;
    }

    public void recoverResourcesOnAction() {
        CharacterStats stats = combatable.getStats();
        int apRecovery = (int)(stats.agility() * 0.5);
        int mpRecovery = (int)(stats.meridian() * 0.3);
        this.currentAp = Math.min(stats.maxAp(), this.currentAp + apRecovery);
        this.currentMp = Math.min(stats.maxMp(), this.currentMp + mpRecovery);
    }

    public void spendAp(int amount) {
        this.currentAp = Math.max(0, this.currentAp - amount);
    }

    public boolean isDefeated() {
        return !combatable.isAlive();
    }

    public int getCurrentHp() {
        return combatable.getStats().hp();
    }

    public void applyDamage(int damage) {
        combatable.damaged(damage);
    }

    private static int deriveWeaponSkill(CharacterStats stats) {
        return IntStream.of(
            stats.swordMethod(), stats.bladeMethod(), stats.fistsAndPalms(),
            stats.longWeapon(), stats.esotericWeapon(), stats.archery()
        ).max().orElse(0);
    }

    private static String deriveWeaponTypeName(CharacterStats stats) {
        int max = 0;
        String name = "권장";
        if (stats.swordMethod() > max) { max = stats.swordMethod(); name = "검법"; }
        if (stats.bladeMethod() > max) { max = stats.bladeMethod(); name = "도법"; }
        if (stats.fistsAndPalms() > max) { max = stats.fistsAndPalms(); name = "권장"; }
        if (stats.longWeapon() > max) { max = stats.longWeapon(); name = "장병"; }
        if (stats.esotericWeapon() > max) { max = stats.esotericWeapon(); name = "기문"; }
        if (stats.archery() > max) { name = "사술"; }
        return name;
    }
}
```

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew compileJava 2>&1 | grep -E "ATBCombatParticipant|error:"
```

Expected: ATBCombatParticipant 관련 오류 없음.

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/ATBCombatParticipant.java
git commit -m "feat: ATBCombatParticipant 구현 - ATB 게이지 및 전투 자원 관리"
```

---

## Task 4: ATBCombat — ATB 틱 기반 전투 엔진

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/ATBCombat.java`
- Create (test): `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/ATBCombatTest.java`

- [ ] **Step 1: ATBCombatTest 작성**

```java
// src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/ATBCombatTest.java
package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ATBCombatTest {

    private ATBCombatParticipant attacker;
    private ATBCombatParticipant defender;

    @BeforeEach
    void setUp() {
        attacker = new ATBCombatParticipant(
            createMonster("공격자", 15, 10, 14, 10, 8, 10, 8, 20),
            CombatGroupType.ALLY, 20, 0, 0
        );
        defender = new ATBCombatParticipant(
            createMonster("방어자", 10, 12, 8, 8, 12, 8, 0, 15),
            CombatGroupType.ENEMY, 15, 5, 5
        );
    }

    @DisplayName("SPEED_DIVISOR=10, 틱을 반복하면 빠른 참가자가 먼저 행동한다")
    @Test
    void fastParticipantActsFirst() {
        // 공격자 speed = 14×1.0 + 8×0.5 = 18.0 → 게이지 100 도달에 ceil(100/1.8)=56틱
        // 방어자 speed = 8×1.0 + 0×0.5 = 8.0 → 게이지 100 도달에 ceil(100/0.8)=125틱
        ATBCombat combat = new ATBCombat(UUID.randomUUID(), List.of(attacker, defender),
            new FixedRandomGenerator(10));
        combat.start();

        CombatActionResult result = CombatActionResult.NOT_ACTED;
        for (int i = 0; i < 200 && !result.isActed(); i++) {
            result = combat.tick();
        }

        assertThat(result.isActed()).isTrue();
        assertThat(result.getLogs()).isNotEmpty();
        assertThat(result.getLogs().get(0).attackerName()).isEqualTo("공격자");
    }

    @DisplayName("전투 종료 — 방어자 HP가 0이 되면 FINISHED")
    @Test
    void combatFinishesWhenDefenderDefeated() {
        ATBCombat combat = new ATBCombat(UUID.randomUUID(), List.of(attacker, defender),
            new FixedRandomGenerator(10));
        combat.start();

        for (int i = 0; i < ATBCombat.MAX_TICKS && !combat.isFinished(); i++) {
            combat.tick();
        }

        assertThat(combat.isFinished()).isTrue();
    }

    @DisplayName("MAX_TICKS 초과 시 강제 종료")
    @Test
    void forcesFinishAtMaxTicks() {
        // 양쪽 다 공격력이 매우 낮아도 MAX_TICKS 이후 FINISHED
        ATBCombatParticipant weakAttacker = new ATBCombatParticipant(
            createMonster("약한공격자", 1, 100, 10, 1, 1, 1, 0, 1),
            CombatGroupType.ALLY, 1, 0, 0
        );
        ATBCombatParticipant toughDefender = new ATBCombatParticipant(
            createMonster("강한방어자", 1, 100, 10, 1, 1, 1, 0, 1),
            CombatGroupType.ENEMY, 1, 0, 0
        );
        ATBCombat combat = new ATBCombat(UUID.randomUUID(),
            List.of(weakAttacker, toughDefender), new FixedRandomGenerator(10));
        combat.start();

        for (int i = 0; i < ATBCombat.MAX_TICKS + 10; i++) {
            combat.tick();
        }

        assertThat(combat.isFinished()).isTrue();
    }

    private Monster createMonster(String name, int vigor, int physique, int agility,
                                   int intellect, int will, int meridian, int swordMethod,
                                   int weaponBase) {
        MonsterType type = MonsterType.builder()
            .name(name).description("테스트").baseHp(physique * 10)
            .baseMp(meridian * 5).baseVigor(vigor).basePhysique(physique)
            .baseAgility(agility).baseIntellect(intellect).baseWill(will)
            .baseMeridian(meridian).baseSwordMethod(swordMethod).build();
        return Monster.createFromType(type, 1, 1L);
    }
}
```

- [ ] **Step 2: 테스트 실행 — FAIL 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.domain.model.combat.ATBCombatTest"
```

Expected: FAIL (ATBCombat 없음)

- [ ] **Step 3: ATBCombat 구현**

```java
// src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/ATBCombat.java
package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ATBCombat {

    public static final int SPEED_DIVISOR = 10;
    public static final int MAX_TICKS = 500;

    private final UUID id;
    private final List<ATBCombatParticipant> participants;
    private final RandomGenerator randomGenerator;
    private CombatState state;
    private int tickCount;

    public ATBCombat(UUID id, List<ATBCombatParticipant> participants, RandomGenerator randomGenerator) {
        this.id = id;
        this.participants = new ArrayList<>(participants);
        this.randomGenerator = randomGenerator;
        this.state = CombatState.WAITING;
        this.tickCount = 0;
    }

    public void start() {
        state = CombatState.ACTIVE;
        participants.forEach(p -> p.getCombatable().enterCombatState());
    }

    public CombatActionResult tick() {
        if (state != CombatState.ACTIVE) {
            return CombatActionResult.NOT_ACTED;
        }
        if (tickCount >= MAX_TICKS) {
            state = CombatState.FINISHED;
            return CombatActionResult.NOT_ACTED;
        }
        tickCount++;

        participants.forEach(p -> p.advanceAtb(SPEED_DIVISOR));

        List<ATBCombatParticipant> readyList = participants.stream()
            .filter(p -> !p.isDefeated() && p.isReadyToAct())
            .sorted(Comparator.comparingDouble(ATBCombatParticipant::getAtbGauge).reversed()
                .thenComparingInt(p -> -p.getCombatable().getStats().agility()))
            .toList();

        CombatActionResult result = new CombatActionResult();
        for (ATBCombatParticipant actor : readyList) {
            if (actor.isDefeated()) continue;

            actor.recoverResourcesOnAction();
            actor.resetAtb();

            ATBCombatParticipant target = selectTarget(actor);
            if (target == null) {
                state = CombatState.FINISHED;
                break;
            }

            CombatLog log = executeAttack(actor, target);
            result.addLog(log);

            if (isGroupDefeated(actor.getGroup() == CombatGroupType.ALLY
                    ? CombatGroupType.ENEMY : CombatGroupType.ALLY)) {
                state = CombatState.FINISHED;
                break;
            }
        }
        return result;
    }

    private ATBCombatParticipant selectTarget(ATBCombatParticipant actor) {
        CombatGroupType targetGroup = actor.getGroup() == CombatGroupType.ALLY
            ? CombatGroupType.ENEMY : CombatGroupType.ALLY;
        return participants.stream()
            .filter(p -> p.getGroup() == targetGroup && !p.isDefeated())
            .min(Comparator.comparingInt(ATBCombatParticipant::getCurrentHp))
            .orElse(null);
    }

    private CombatLog executeAttack(ATBCombatParticipant actor, ATBCombatParticipant target) {
        CharacterStats actorStats = actor.getCombatable().getStats();
        CharacterStats targetStats = target.getCombatable().getStats();

        double accuracy = CombatFormulas.accuracy(actorStats, actor.getWeaponSkill(), 0);
        double evasion = CombatFormulas.evasion(targetStats);
        int evasionRate = CombatFormulas.evasionRate(evasion, accuracy);

        boolean evaded = (target.getCurrentAp() >= 5) && (randomGenerator.nextInt(100) < evasionRate);
        if (evaded) {
            target.spendAp(5);
            return buildEvadeLog(actor, target);
        }

        int raw = CombatFormulas.baseDamage(actorStats, actor.getWeaponBaseDamage(),
            actor.getWeaponSkill(), randomGenerator);
        boolean isCrit = randomGenerator.nextInt(100) < CombatFormulas.critRate(actorStats);
        if (isCrit) raw = (int)(raw * 1.5);

        int arm = CombatFormulas.armor(targetStats, target.getEquipArmor());
        int armPct = CombatFormulas.armorPct(targetStats, target.getEquipArmorPct(),
            target.getWeaponSkill());
        int finalDamage = CombatFormulas.applyDefense(raw, arm, armPct);

        target.applyDamage(finalDamage);

        return CombatLog.builder()
            .attackerId(actor.getCombatable().getId())
            .attackerName(actor.getCombatable().getName())
            .targetId(target.getCombatable().getId())
            .targetName(target.getCombatable().getName())
            .hitSuccess(true)
            .baseDamage(raw)
            .finalDamage(finalDamage)
            .defenseValue(arm)
            .targetRemainingHp(target.getCurrentHp())
            .targetDefeated(target.isDefeated())
            .evaded(false)
            .isCrit(isCrit)
            .attackerApAfter(actor.getCurrentAp())
            .targetApAfter(target.getCurrentAp())
            .weaponTypeName(actor.getWeaponTypeName())
            // 미사용 레거시 필드 기본값
            .attackRoll(0).attackModifier(0).attackTotal(0)
            .defenseRoll(0).defenseModifier(0).defenseTotal(0)
            .damageModifier(0).damageTotal(raw)
            .build();
    }

    private CombatLog buildEvadeLog(ATBCombatParticipant actor, ATBCombatParticipant target) {
        return CombatLog.builder()
            .attackerId(actor.getCombatable().getId())
            .attackerName(actor.getCombatable().getName())
            .targetId(target.getCombatable().getId())
            .targetName(target.getCombatable().getName())
            .hitSuccess(false)
            .evaded(true)
            .isCrit(false)
            .baseDamage(0).finalDamage(0).defenseValue(0)
            .targetRemainingHp(target.getCurrentHp())
            .targetDefeated(false)
            .attackerApAfter(actor.getCurrentAp())
            .targetApAfter(target.getCurrentAp())
            .weaponTypeName(actor.getWeaponTypeName())
            .attackRoll(0).attackModifier(0).attackTotal(0)
            .defenseRoll(0).defenseModifier(0).defenseTotal(0)
            .damageModifier(0).damageTotal(0)
            .build();
    }

    private boolean isGroupDefeated(CombatGroupType group) {
        return participants.stream()
            .filter(p -> p.getGroup() == group)
            .allMatch(ATBCombatParticipant::isDefeated);
    }

    public boolean isFinished() {
        return state == CombatState.FINISHED;
    }

    public UUID getId() {
        return id;
    }

    public List<Long> getAllyUserIds() {
        return participants.stream()
            .filter(p -> p.getGroup() == CombatGroupType.ALLY)
            .map(ATBCombatParticipant::getCombatable)
            .filter(c -> c instanceof PlayerCharacter)
            .map(c -> ((PlayerCharacter) c).getUserId())
            .toList();
    }

    public List<PlayerCharacter> getAllyUsers() {
        return participants.stream()
            .filter(p -> p.getGroup() == CombatGroupType.ALLY)
            .map(ATBCombatParticipant::getCombatable)
            .filter(c -> c instanceof PlayerCharacter)
            .map(c -> (PlayerCharacter) c)
            .toList();
    }
}
```

- [ ] **Step 4: CombatActionResult에 정적 상수 및 addLog 메서드 추가**

`CombatActionResult.java`를 다음으로 교체:

```java
package com.jefflife.mudmk2.gameplay.application.domain.model.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CombatActionResult {
    public static final CombatActionResult NOT_ACTED = new CombatActionResult();
    private List<CombatLog> logs = Collections.emptyList();
    private boolean acted = false;

    public void addLog(CombatLog log) {
        if (logs.isEmpty()) logs = new ArrayList<>();
        logs.add(log);
        acted = true;
    }

    public void addLogs(List<CombatLog> newLogs) {
        newLogs.forEach(this::addLog);
    }

    public List<CombatLog> getLogs() { return logs; }
    public boolean isActed() { return acted; }
}
```

- [ ] **Step 5: 테스트 실행 — PASS 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.domain.model.combat.ATBCombatTest"
```

Expected: BUILD SUCCESSFUL, 3 tests passed

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/ATBCombat.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatActionResult.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/ATBCombatTest.java
git commit -m "feat: ATBCombat 구현 - ATB 게이지 기반 전투 엔진"
```

---

## Task 5: CombatNarrativeFormatter — 전투 텍스트 생성

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatter.java`
- Create (test): `src/test/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatterTest.java`

- [ ] **Step 1: CombatNarrativeFormatterTest 작성**

```java
// src/test/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatterTest.java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.domain.model.combat.CombatLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CombatNarrativeFormatterTest {

    private CombatNarrativeFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new CombatNarrativeFormatter();
    }

    private CombatLog.CombatLogBuilder baseLog() {
        return CombatLog.builder()
            .attackerId(UUID.randomUUID()).attackerName("홍길동")
            .targetId(UUID.randomUUID()).targetName("산적두목")
            .attackRoll(0).attackModifier(0).attackTotal(0)
            .defenseRoll(0).defenseModifier(0).defenseTotal(0)
            .damageModifier(0).damageTotal(0).defenseValue(0);
    }

    @Test
    void 일반명중_메시지() {
        CombatLog log = baseLog()
            .hitSuccess(true).evaded(false).isCrit(false)
            .baseDamage(30).finalDamage(22)
            .targetRemainingHp(78).targetDefeated(false)
            .attackerApAfter(80).targetApAfter(112)
            .weaponTypeName("검법")
            .build();

        String message = formatter.format(log);

        assertThat(message).contains("홍길동").contains("검법").contains("산적두목");
        assertThat(message).contains("22").contains("78");
        assertThat(message).doesNotContain("치명타").doesNotContain("피했다");
    }

    @Test
    void 치명타_메시지() {
        CombatLog log = baseLog()
            .hitSuccess(true).evaded(false).isCrit(true)
            .baseDamage(30).finalDamage(33)
            .targetRemainingHp(45).targetDefeated(false)
            .attackerApAfter(80).targetApAfter(112)
            .weaponTypeName("검법")
            .build();

        assertThat(formatter.format(log)).contains("치명타");
    }

    @Test
    void 회피_메시지() {
        CombatLog log = baseLog()
            .hitSuccess(false).evaded(true).isCrit(false)
            .baseDamage(0).finalDamage(0)
            .targetRemainingHp(150).targetDefeated(false)
            .attackerApAfter(80).targetApAfter(107)
            .weaponTypeName("검법")
            .build();

        String message = formatter.format(log);
        assertThat(message).contains("피했다");
        assertThat(message).doesNotContain("데미지");
    }

    @Test
    void 사망_메시지가_포함된다() {
        CombatLog log = baseLog()
            .hitSuccess(true).evaded(false).isCrit(false)
            .baseDamage(100).finalDamage(90)
            .targetRemainingHp(0).targetDefeated(true)
            .attackerApAfter(80).targetApAfter(0)
            .weaponTypeName("도법")
            .build();

        String message = formatter.format(log);
        assertThat(message).contains("쓰러졌다");
    }
}
```

- [ ] **Step 2: 테스트 실행 — FAIL 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.CombatNarrativeFormatterTest"
```

Expected: FAIL (CombatNarrativeFormatter 없음)

- [ ] **Step 3: CombatNarrativeFormatter 구현**

```java
// src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatter.java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gameplay.application.domain.model.combat.CombatLog;
import org.springframework.stereotype.Component;

@Component
public class CombatNarrativeFormatter {

    public String format(CombatLog log) {
        StringBuilder sb = new StringBuilder();

        if (log.evaded()) {
            sb.append(String.format("%s이(가) %s으로 공격했지만 %s이(가) 피했다!",
                log.attackerName(), log.weaponTypeName(), log.targetName()));
        } else if (log.isCrit()) {
            sb.append(String.format("%s이(가) %s으로 %s에게 치명타를 날렸다!",
                log.attackerName(), log.weaponTypeName(), log.targetName()));
            sb.append(String.format("\n  → %d 데미지! (남은 HP: %d)",
                log.finalDamage(), log.targetRemainingHp()));
        } else {
            sb.append(String.format("%s이(가) %s으로 %s을(를) 공격했다!",
                log.attackerName(), log.weaponTypeName(), log.targetName()));
            sb.append(String.format("\n  → %d 데미지! (남은 HP: %d)",
                log.finalDamage(), log.targetRemainingHp()));
        }

        if (log.targetDefeated()) {
            sb.append(String.format("\n%s이(가) 쓰러졌다!", log.targetName()));
        }

        return sb.toString();
    }
}
```

- [ ] **Step 4: 테스트 실행 — PASS 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.CombatNarrativeFormatterTest"
```

Expected: BUILD SUCCESSFUL, 4 tests passed

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatter.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/CombatNarrativeFormatterTest.java
git commit -m "feat: CombatNarrativeFormatter 구현 - CombatLog → 한국어 전투 텍스트"
```

---

## Task 6: CombatService 리팩토링 — ATBCombat 교체

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatService.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Monster.java`

- [ ] **Step 1: Monster에 @Transient 장비 필드 추가**

`Monster.java`의 `private int aggressiveness;` 다음에 추가:

```java
@Transient
private int weaponBaseDamage = 10;
@Transient
private int equipmentArmor = 0;
@Transient
private int equipmentArmorPct = 0;
```

그리고 `createSimulation()` 팩토리 메서드 추가:

```java
public static Monster createSimulation(String name, int vigor, int physique, int agility,
                                        int intellect, int will, int meridian,
                                        int innerPower, int specialTechnique, int lightStep,
                                        int fistsAndPalms, int swordMethod, int bladeMethod,
                                        int longWeapon, int esotericWeapon, int archery,
                                        int weaponBaseDamage, int equipmentArmor,
                                        int equipmentArmorPct, Long roomId) {
    int maxHp = physique * CharacterStats.HP_PER_PHYSIQUE + specialTechnique * CharacterStats.HP_PER_SPECIAL_TECHNIQUE;
    int maxMp = meridian * CharacterStats.MP_PER_MERIDIAN + innerPower * CharacterStats.MP_PER_INNER_POWER;
    int maxAp = agility * CharacterStats.AP_PER_AGILITY;

    BaseCharacter baseCharacterInfo = BaseCharacter.builder()
        .name(name).background("시뮬레이션 몬스터").gender(Gender.MALE)
        .hp(maxHp).mp(maxMp).ap(maxAp)
        .vigor(vigor).physique(physique).agility(agility)
        .intellect(intellect).will(will).meridian(meridian)
        .innerPower(innerPower).specialTechnique(specialTechnique).lightStep(lightStep)
        .fistsAndPalms(fistsAndPalms).swordMethod(swordMethod).bladeMethod(bladeMethod)
        .longWeapon(longWeapon).esotericWeapon(esotericWeapon).archery(archery)
        .roomId(roomId).alive(true).build();

    Monster monster = Monster.builder()
        .id(UUID.randomUUID()).description("시뮬레이션 몬스터").level(1)
        .monsterTypeId(null).experienceReward(0).aggressiveness(0).respawnTime(0)
        .baseCharacterInfo(baseCharacterInfo).build();
    monster.weaponBaseDamage = weaponBaseDamage;
    monster.equipmentArmor = equipmentArmor;
    monster.equipmentArmorPct = equipmentArmorPct;
    return monster;
}
```

참고: `@Transient` 필드는 `@Builder`로 설정이 안 되므로 직접 할당한다.

- [ ] **Step 2: CombatService 전면 교체**

```java
// src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatService.java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gameplay.application.domain.model.combat.*;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import com.jefflife.mudmk2.gameplay.application.tick.TickListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CombatService implements TickListener {

    private static final Map<UUID, ATBCombat> combatMap = new ConcurrentHashMap<>();

    private final GameWorldService gameWorldService;
    private final SendMessageToUserPort sendMessageToUserPort;
    private final CombatNarrativeFormatter narrativeFormatter;

    public CombatService(GameWorldService gameWorldService,
                         SendMessageToUserPort sendMessageToUserPort,
                         CombatNarrativeFormatter narrativeFormatter) {
        this.gameWorldService = gameWorldService;
        this.sendMessageToUserPort = sendMessageToUserPort;
        this.narrativeFormatter = narrativeFormatter;
    }

    @Override
    @Async("combatTaskExecutor")
    public void onTick(long tickCount) {
        combatMap.values().forEach(combat -> {
            CombatActionResult result = combat.tick();
            if (result.isActed()) {
                sendCombatMessages(combat, result);
            }
            if (combat.isFinished()) {
                handleCombatEnd(combat);
                combat.getAllyUsers().forEach(player ->
                    sendMessageToUserPort.messageToUser(player.getUserId(), "[전투 종료]"));
                combatMap.remove(combat.getId());
            }
        });
    }

    public void startCombat(PlayerCharacter attacker, Statable defender) {
        List<ATBCombatParticipant> participants = new ArrayList<>();

        Party party = gameWorldService.getPartyByPlayerId(attacker.getId()).orElse(null);
        if (party != null) {
            party.getMembers().getMembers().stream()
                .filter(m -> m instanceof Combatable)
                .map(m -> (Combatable) m)
                .forEach(c -> participants.add(new ATBCombatParticipant(c, CombatGroupType.ALLY)));
        } else {
            participants.add(new ATBCombatParticipant(attacker, CombatGroupType.ALLY));
        }

        Combatable enemy = (Combatable) defender;
        participants.add(createEnemyParticipant(enemy));

        ATBCombat combat = new ATBCombat(UUID.randomUUID(), participants, new DefaultRandomGenerator());
        combat.start();
        combatMap.put(combat.getId(), combat);

        combat.getAllyUsers().forEach(player ->
            sendMessageToUserPort.messageToUser(player.getUserId(),
                String.format("[전투 시작] %s과(와) %s의 전투가 시작되었습니다!",
                    player.getName(), enemy.getName())));
    }

    private ATBCombatParticipant createEnemyParticipant(Combatable combatable) {
        if (combatable instanceof Monster monster) {
            return new ATBCombatParticipant(combatable, CombatGroupType.ENEMY,
                monster.getWeaponBaseDamage(), monster.getEquipmentArmor(), monster.getEquipmentArmorPct());
        }
        return new ATBCombatParticipant(combatable, CombatGroupType.ENEMY);
    }

    private void sendCombatMessages(ATBCombat combat, CombatActionResult result) {
        List<PlayerCharacter> users = combat.getAllyUsers();
        result.getLogs().forEach(log -> {
            String message = narrativeFormatter.format(log);
            users.forEach(user ->
                sendMessageToUserPort.messageToUser(user.getUserId(), message));
        });
    }

    private void handleCombatEnd(ATBCombat combat) {
        combat.getAllyUsers().forEach(player -> {
            if (!player.isAlive()) {
                Long roomId = player.getCurrentRoomId();
                Room room = gameWorldService.getRoom(roomId).orElse(null);
                if (room != null && room.isSimulationRoom()) {
                    player.fullRestore();
                    sendMessageToUserPort.messageToUser(player.getUserId(),
                        "시뮬레이션 전투 종료. HP가 완전히 회복되었습니다.");
                }
            }
        });
    }
}
```

- [ ] **Step 3: GameWorldService에 getRoom 메서드 확인 / 추가**

`GameWorldService.java`에서 `getRoom(Long roomId)` 메서드가 없으면 추가:

```java
public Optional<Room> getRoom(Long roomId) {
    return Optional.ofNullable(rooms.get(roomId));
}
```

- [ ] **Step 4: PlayerCharacter에 fullRestore, isAlive 메서드 확인**

`PlayerCharacter.java`에서 `isAlive()`와 `fullRestore()` 메서드가 있는지 확인. 없으면 `BaseCharacter`에 위임하는 메서드를 추가:

```java
// PlayerCharacter.java 에 추가 (BaseCharacter 위임)
public boolean isAlive() {
    return baseCharacterInfo.isAlive();
}

public void fullRestore() {
    baseCharacterInfo.fullRestore();
}
```

`BaseCharacter`에 `fullRestore()` 없으면 추가:

```java
public void fullRestore() {
    this.hp = maxHp();  // BaseCharacter에 maxHp() 위임 메서드 추가
    this.mp = maxMp();
    this.ap = maxAp();
    this.state = CharacterState.NORMAL;
}

private int maxHp() {
    return physique * CharacterStats.HP_PER_PHYSIQUE + specialTechnique * CharacterStats.HP_PER_SPECIAL_TECHNIQUE;
}
private int maxMp() {
    return meridian * CharacterStats.MP_PER_MERIDIAN + innerPower * CharacterStats.MP_PER_INNER_POWER;
}
private int maxAp() {
    return agility * CharacterStats.AP_PER_AGILITY;
}
```

- [ ] **Step 5: Room에 simulationRoom 필드 추가**

`Room.java`에 추가:

```java
@Column(name = "simulation_room", nullable = false)
@Builder.Default
private boolean simulationRoom = false;
```

`Room.builder()` 패턴이 기존에 있으므로 `@Builder.Default` 어노테이션을 추가한다.

- [ ] **Step 6: 전체 빌드**

```bash
./gradlew build -x test 2>&1 | tail -5
```

Expected: BUILD SUCCESSFUL (컴파일 오류 없음)

- [ ] **Step 7: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/CombatService.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/Monster.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/map/Room.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java
git commit -m "feat: CombatService ATBCombat으로 교체 + Monster 장비 필드 + Room simulationRoom 플래그"
```

---

## Task 7: SimulationService + SimulationController — Spawn API

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/webapi/dto/SimulationSpawnRequest.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/webapi/dto/SpawnedMonsterResponse.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/SimulationService.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/webapi/SimulationController.java`

- [ ] **Step 1: DTO 작성**

```java
// src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/webapi/dto/SimulationSpawnRequest.java
package com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SimulationSpawnRequest {
    private String name;
    private int vigor = 10;
    private int physique = 10;
    private int agility = 10;
    private int intellect = 10;
    private int will = 10;
    private int meridian = 10;
    private int innerPower = 0;
    private int specialTechnique = 0;
    private int lightStep = 0;
    private int fistsAndPalms = 0;
    private int swordMethod = 0;
    private int bladeMethod = 0;
    private int longWeapon = 0;
    private int esotericWeapon = 0;
    private int archery = 0;
    private int weaponBaseDamage = 10;
    private int equipmentArmor = 0;
    private int equipmentArmorPct = 0;
}
```

```java
// src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/webapi/dto/SpawnedMonsterResponse.java
package com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import java.util.UUID;

public record SpawnedMonsterResponse(
    UUID id,
    String name,
    int vigor,
    int physique,
    int agility,
    int maxHp,
    int weaponBaseDamage,
    int equipmentArmor,
    int equipmentArmorPct
) {
    public static SpawnedMonsterResponse from(Monster monster) {
        var stats = monster.getStats();
        return new SpawnedMonsterResponse(
            monster.getId(), monster.getName(),
            stats.vigor(), stats.physique(), stats.agility(),
            stats.maxHp(),
            monster.getWeaponBaseDamage(),
            monster.getEquipmentArmor(),
            monster.getEquipmentArmorPct()
        );
    }
}
```

- [ ] **Step 2: SimulationService 구현**

```java
// src/main/java/com/jefflife/mudmk2/gameplay/application/service/SimulationService.java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto.SimulationSpawnRequest;
import com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto.SpawnedMonsterResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimulationService {

    private final GameWorldService gameWorldService;
    private final Map<UUID, Monster> spawnedMonsters = new ConcurrentHashMap<>();

    @Value("${simulation.room-id:1}")
    private Long simulationRoomId;

    public SimulationService(GameWorldService gameWorldService) {
        this.gameWorldService = gameWorldService;
    }

    public SpawnedMonsterResponse spawn(SimulationSpawnRequest request) {
        Monster monster = Monster.createSimulation(
            request.getName(),
            request.getVigor(), request.getPhysique(), request.getAgility(),
            request.getIntellect(), request.getWill(), request.getMeridian(),
            request.getInnerPower(), request.getSpecialTechnique(), request.getLightStep(),
            request.getFistsAndPalms(), request.getSwordMethod(), request.getBladeMethod(),
            request.getLongWeapon(), request.getEsotericWeapon(), request.getArchery(),
            request.getWeaponBaseDamage(), request.getEquipmentArmor(), request.getEquipmentArmorPct(),
            simulationRoomId
        );
        spawnedMonsters.put(monster.getId(), monster);
        gameWorldService.addMonster(monster);
        return SpawnedMonsterResponse.from(monster);
    }

    public List<SpawnedMonsterResponse> listSpawned() {
        return spawnedMonsters.values().stream()
            .map(SpawnedMonsterResponse::from)
            .toList();
    }

    public boolean remove(UUID monsterId) {
        Monster removed = spawnedMonsters.remove(monsterId);
        if (removed != null) {
            gameWorldService.removeMonster(monsterId.toString());
            return true;
        }
        return false;
    }
}
```

- [ ] **Step 3: application.properties에 simulation.room-id 추가**

`src/main/resources/application.properties` (또는 `application-local.properties`)에 추가:

```properties
simulation.room-id=1
```

실제 시뮬레이션 방 ID가 생성되면 이 값을 업데이트한다.

- [ ] **Step 4: SimulationController 구현**

```java
// src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/webapi/SimulationController.java
package com.jefflife.mudmk2.gameplay.adapter.in.webapi;

import com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto.SimulationSpawnRequest;
import com.jefflife.mudmk2.gameplay.adapter.in.webapi.dto.SpawnedMonsterResponse;
import com.jefflife.mudmk2.gameplay.application.service.SimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/simulation")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/spawn")
    public ResponseEntity<SpawnedMonsterResponse> spawn(@RequestBody SimulationSpawnRequest request) {
        return ResponseEntity.ok(simulationService.spawn(request));
    }

    @GetMapping("/room")
    public ResponseEntity<List<SpawnedMonsterResponse>> listSpawned() {
        return ResponseEntity.ok(simulationService.listSpawned());
    }

    @DeleteMapping("/spawn/{id}")
    public ResponseEntity<Void> remove(@PathVariable UUID id) {
        boolean removed = simulationService.remove(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
```

- [ ] **Step 5: 빌드 확인**

```bash
./gradlew build -x test 2>&1 | tail -5
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/webapi/ \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/SimulationService.java
git commit -m "feat: SimulationController + SimulationService - 커스텀 스탯 몬스터 spawn API"
```

---

## Task 8: Admin 페이지 — simulation-management.html

**Files:**
- Create: `src/main/resources/templates/web/simulation-management.html`
- Modify: `src/main/java/com/jefflife/mudmk2/web/IndexController.java`
- Modify: `src/main/resources/templates/web/admin.html`

- [ ] **Step 1: IndexController에 라우트 추가**

`IndexController.java`의 마지막 `@GetMapping` 메서드 뒤에 추가:

```java
@GetMapping("/simulation-management")
public String simulationManagement(Model model, @LoginUser SessionUser user) {
    if (user != null) {
        model.addAttribute("userName", user.getName());
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("userPicture", user.getPicture());
    }
    return "web/simulation-management";
}
```

- [ ] **Step 2: admin.html에 시뮬레이션 관리 카드 추가**

`admin.html`에서 기존 `.admin-grid` 안의 마지막 `.admin-card` 다음에 추가:

```html
<div class="admin-card" onclick="location.href='/simulation-management'">
    <div class="admin-icon">⚔️</div>
    <h3 class="admin-card-title">시뮬레이션 관리</h3>
    <p class="admin-card-description">커스텀 스탯 몬스터 소환 및 전투 시스템 검증</p>
    <a href="/simulation-management" class="btn-admin">관리하기</a>
</div>
```

- [ ] **Step 3: simulation-management.html 작성**

```html
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>시뮬레이션 관리</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Mono:wght@400;600&family=Inter:wght@400;600&display=swap" rel="stylesheet">
    <style>
        body { background: linear-gradient(135deg, #1a1c24 0%, #2d2f3b 100%); color: #e4e4e7; min-height: 100vh; padding: 20px 0; font-family: 'Inter', sans-serif; }
        .terminal-card { background: rgba(20,21,26,0.95); border-radius: 16px; border: 1px solid rgba(255,255,255,0.1); overflow: hidden; margin-bottom: 24px; }
        .card-header { background: linear-gradient(90deg, rgba(0,255,0,0.05) 0%, transparent 100%); border-bottom: 1px solid rgba(0,255,0,0.2); padding: 16px 24px; }
        .card-header h2 { font-family: 'IBM Plex Mono', monospace; color: #00ff00; margin: 0; }
        .card-body { padding: 24px; }
        label { color: #9ca3af; font-size: 0.85rem; }
        input[type=text], input[type=number] { background: rgba(0,0,0,0.4); border: 1px solid rgba(0,255,0,0.3); color: #e4e4e7; border-radius: 6px; padding: 6px 10px; width: 100%; }
        input[type=text]:focus, input[type=number]:focus { outline: none; border-color: #00ff00; }
        .section-title { color: #00ff00; font-family: 'IBM Plex Mono', monospace; font-size: 0.9rem; margin: 16px 0 8px; border-bottom: 1px solid rgba(0,255,0,0.2); padding-bottom: 4px; }
        .btn-green { background: rgba(0,255,0,0.1); border: 1px solid rgba(0,255,0,0.5); color: #00ff00; padding: 8px 20px; border-radius: 8px; cursor: pointer; }
        .btn-green:hover { background: rgba(0,255,0,0.2); }
        .btn-danger-sm { background: rgba(255,50,50,0.1); border: 1px solid rgba(255,50,50,0.4); color: #ff6b6b; padding: 2px 10px; border-radius: 6px; cursor: pointer; font-size: 0.8rem; }
        .monster-row { border-bottom: 1px solid rgba(255,255,255,0.05); padding: 10px 0; font-family: 'IBM Plex Mono', monospace; font-size: 0.85rem; }
        #message { font-family: 'IBM Plex Mono', monospace; font-size: 0.85rem; margin-top: 12px; min-height: 20px; }
    </style>
</head>
<body>
<div class="container" style="max-width: 900px;">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 style="font-family: 'IBM Plex Mono', monospace; color: #00ff00;">⚔️ 시뮬레이션 관리</h1>
        <a href="/admin" style="color: #9ca3af; font-size: 0.9rem;">← 관리자 메뉴</a>
    </div>

    <!-- 소환 폼 -->
    <div class="terminal-card">
        <div class="card-header"><h2>몬스터 소환</h2></div>
        <div class="card-body">
            <div class="row mb-3">
                <div class="col">
                    <label>이름</label>
                    <input type="text" id="name" value="시뮬몬스터">
                </div>
            </div>

            <div class="section-title">── 속성 ──</div>
            <div class="row g-2 mb-3">
                <div class="col"><label>근력</label><input type="number" id="vigor" value="10"></div>
                <div class="col"><label>체질</label><input type="number" id="physique" value="10"></div>
                <div class="col"><label>민첩</label><input type="number" id="agility" value="10"></div>
                <div class="col"><label>지력</label><input type="number" id="intellect" value="10"></div>
                <div class="col"><label>의지</label><input type="number" id="will" value="10"></div>
                <div class="col"><label>경맥</label><input type="number" id="meridian" value="10"></div>
            </div>

            <div class="section-title">── 무예 ──</div>
            <div class="row g-2 mb-3">
                <div class="col"><label>내공</label><input type="number" id="innerPower" value="0"></div>
                <div class="col"><label>절기</label><input type="number" id="specialTechnique" value="0"></div>
                <div class="col"><label>경공</label><input type="number" id="lightStep" value="0"></div>
                <div class="col"><label>권장</label><input type="number" id="fistsAndPalms" value="0"></div>
                <div class="col"><label>검법</label><input type="number" id="swordMethod" value="0"></div>
                <div class="col"><label>도법</label><input type="number" id="bladeMethod" value="0"></div>
                <div class="col"><label>장병</label><input type="number" id="longWeapon" value="0"></div>
                <div class="col"><label>기문</label><input type="number" id="esotericWeapon" value="0"></div>
                <div class="col"><label>사술</label><input type="number" id="archery" value="0"></div>
            </div>

            <div class="section-title">── 장비 ──</div>
            <div class="row g-2 mb-4">
                <div class="col"><label>무기 기본치</label><input type="number" id="weaponBaseDamage" value="20"></div>
                <div class="col"><label>방어수치</label><input type="number" id="equipmentArmor" value="0"></div>
                <div class="col"><label>방어율(%)</label><input type="number" id="equipmentArmorPct" value="0"></div>
            </div>

            <button class="btn-green" onclick="spawnMonster()">소환하기</button>
            <div id="message"></div>
        </div>
    </div>

    <!-- 현재 방 상태 -->
    <div class="terminal-card">
        <div class="card-header">
            <h2 style="display:flex;justify-content:space-between;">
                <span>현재 시뮬레이션 방 상태</span>
                <button class="btn-green" style="font-size:0.8rem;padding:4px 12px;" onclick="loadMonsters()">새로고침</button>
            </h2>
        </div>
        <div class="card-body" id="monsterList">
            <div style="color:#9ca3af;">로딩 중...</div>
        </div>
    </div>
</div>

<script>
async function spawnMonster() {
    const body = {
        name: v('name'), vigor: n('vigor'), physique: n('physique'), agility: n('agility'),
        intellect: n('intellect'), will: n('will'), meridian: n('meridian'),
        innerPower: n('innerPower'), specialTechnique: n('specialTechnique'), lightStep: n('lightStep'),
        fistsAndPalms: n('fistsAndPalms'), swordMethod: n('swordMethod'), bladeMethod: n('bladeMethod'),
        longWeapon: n('longWeapon'), esotericWeapon: n('esotericWeapon'), archery: n('archery'),
        weaponBaseDamage: n('weaponBaseDamage'), equipmentArmor: n('equipmentArmor'),
        equipmentArmorPct: n('equipmentArmorPct')
    };
    const res = await fetch('/api/v1/simulation/spawn', {method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
    const data = await res.json();
    document.getElementById('message').innerHTML = `<span style="color:#00ff00;">✓ ${data.name} 소환 완료 (HP: ${data.maxHp})</span>`;
    loadMonsters();
}

async function loadMonsters() {
    const res = await fetch('/api/v1/simulation/room');
    const list = await res.json();
    const el = document.getElementById('monsterList');
    if (list.length === 0) { el.innerHTML = '<div style="color:#9ca3af;">소환된 몬스터 없음</div>'; return; }
    el.innerHTML = list.map(m => `
        <div class="monster-row d-flex justify-content-between align-items-center">
            <span><span style="color:#00ff00;">${m.name}</span>　근력:${m.vigor} 민첩:${m.agility} HP:${m.maxHp} 무기:${m.weaponBaseDamage}</span>
            <button class="btn-danger-sm" onclick="removeMonster('${m.id}')">제거</button>
        </div>`).join('');
}

async function removeMonster(id) {
    await fetch('/api/v1/simulation/spawn/' + id, {method:'DELETE'});
    loadMonsters();
}

const v = id => document.getElementById(id).value;
const n = id => parseInt(document.getElementById(id).value) || 0;

loadMonsters();
</script>
</body>
</html>
```

- [ ] **Step 4: 빌드 및 서버 기동 확인**

```bash
./gradlew build -x test 2>&1 | tail -5
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 커밋**

```bash
git add src/main/resources/templates/web/simulation-management.html \
        src/main/java/com/jefflife/mudmk2/web/IndexController.java \
        src/main/resources/templates/web/admin.html
git commit -m "feat: simulation-management admin 페이지 + 라우트 추가"
```

---

## Task 9: 시뮬레이션 방 DB 설정

**Files:**
- 없음 (DB 데이터 및 설정 변경)

- [ ] **Step 1: 시뮬레이션 방 생성 SQL 실행**

Docker MySQL에 접속해 시뮬레이션 방을 생성한다. Area가 먼저 존재해야 한다.

```sql
-- 시뮬레이션 전용 Area 생성 (없으면)
INSERT INTO area (name, area_type) VALUES ('훈련장', 'INSTANCE_MAP');

-- 생성된 area_id 확인
SELECT id FROM area WHERE name = '훈련장' ORDER BY id DESC LIMIT 1;

-- 시뮬레이션 방 생성 (area_id를 위에서 얻은 값으로 교체)
INSERT INTO room (name, summary, description, area_id, simulation_room)
VALUES ('시뮬레이션 훈련장', '전투 훈련을 위한 특별한 공간이다.', 
        '조용한 훈련장이다. 어떤 전투를 해도 실제 피해가 없다.', 
        1, true);  -- area_id = 1로 가정, 실제 값으로 교체

-- 생성된 방 ID 확인
SELECT id FROM room WHERE simulation_room = true;
```

- [ ] **Step 2: application.properties 업데이트**

위에서 확인한 방 ID로 `application.properties` 또는 `application-local.properties` 업데이트:

```properties
simulation.room-id=<위에서 확인한 방 ID>
```

- [ ] **Step 3: 서버 재기동 후 방 로드 확인**

```bash
./gradlew bootRun
```

서버 로그에서 `Loaded N rooms` 메시지 확인. N이 증가했으면 방이 로드된 것.

- [ ] **Step 4: 커밋**

```bash
git add src/main/resources/application.properties
git commit -m "config: 시뮬레이션 방 ID 설정"
```

---

## Task 10: 구 전투 시스템 테스트 정리

**Files:**
- Delete: `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatTest.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatGroupTest.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatParticipantTest.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/InitiativeSystemTest.java`

- [ ] **Step 1: CombatTest.java 삭제**

```bash
rm src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatTest.java
```

- [ ] **Step 2: InitiativeSystemTest.java 삭제**

`InitiativeSystem`이 제거되었으므로 테스트도 삭제:

```bash
rm src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/InitiativeSystemTest.java
```

- [ ] **Step 3: CombatParticipantTest.java 확인 및 삭제**

`CombatParticipant`가 더 이상 사용되지 않으면 삭제:

```bash
rm src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatParticipantTest.java
```

- [ ] **Step 4: CombatGroupTest.java 확인**

`CombatGroup`이 여전히 코드베이스에 있으면 테스트를 유지하거나 삭제. `ATBCombat`에서 `CombatGroup`을 사용하지 않으면:

```bash
rm src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/combat/CombatGroupTest.java
```

- [ ] **Step 5: 전체 테스트 실행**

```bash
./gradlew test 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL, 실패 테스트 없음

- [ ] **Step 6: 최종 커밋**

```bash
git add -A
git commit -m "refactor: 구 전투 시스템(Combat, CombatParticipant, InitiativeSystem) 테스트 정리"
```

---

## 검증 체크리스트

구현 완료 후 아래 항목을 순서대로 확인한다:

- [ ] `./gradlew test` — 전체 테스트 통과
- [ ] 서버 기동 후 `/admin` 접속 → "시뮬레이션 관리" 카드 노출
- [ ] `/simulation-management` 접속 → 소환 폼 노출
- [ ] 폼에 스탯 입력 후 "소환하기" → 방 상태에 몬스터 표시
- [ ] MUD 게임에서 시뮬레이션 방으로 이동 → 몬스터 보임
- [ ] `때려 <몬스터명>` → ATB 전투 시작, 채팅창에 "홍길동이 검법으로 ... 공격했다!" 출력
- [ ] 전투 종료 후 채팅창에 "[전투 종료]" 메시지 출력
- [ ] 플레이어 사망 시 HP 회복 메시지 출력
