# 무공 조회 (상태창 확장 + `무공`/`무공창` 명령) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 상태창(`상태`/`상태창`)에 `[ 장착 무공 ]` 섹션을 추가하고, 학습한 모든 무공을 보는 새 명령(`무공`/`무공창`)을 구현한다.

**Architecture:** 기존 `EquipmentView*` 4-component 패턴(parser → command → executor → service → variables → messageSender → thymeleaf)을 그대로 따른다. 상태창과 무공창이 공유하는 도메인 → Variables 매핑을 `MartialArtViewMapper` 한 클래스에 모은다. 현재 레벨에서의 효과 조회는 도메인 메서드 `template.effectAt(level)`로 노출.

**Tech Stack:** Spring Boot 4 (Java), JPA (Lombok), Thymeleaf, JUnit 5, Mockito, AssertJ, Gradle.

**Spec reference:** `docs/superpowers/specs/2026-05-17-martial-arts-view-design.md`

---

## Task 0: 작업 브랜치 생성

**Files:**
- (없음 — git 작업만)

- [ ] **Step 1: 현재 main에 깨끗한 상태인지 확인**

Run: `git status`
Expected: `nothing to commit, working tree clean`, branch `main`.

- [ ] **Step 2: feature 브랜치 생성 및 전환**

Run: `git checkout -b feature/martial-arts-view`
Expected: `Switched to a new branch 'feature/martial-arts-view'`

- [ ] **Step 3: 브랜치 확인**

Run: `git branch --show-current`
Expected: `feature/martial-arts-view`

---

## Task 1: `MentalMethodTemplate.effectAt` / `ExternalArtTemplate.effectAt` 추가

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtTemplate.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodTemplateTest.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtTemplateTest.java`

- [ ] **Step 1: `MentalMethodTemplateTest`에 effectAt 테스트 4개 추가**

`MentalMethodTemplateTest`의 마지막 `}` 직전에 다음 4개의 메서드를 추가:

```java
    @Test
    void effectAt_returnsEffectForGivenLevel() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("천뢰신공").description("d").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(3)
                .levelEffects(List.of(
                        new MentalMethodLevelEffect(1, List.of(new StatModifier(StatType.INNER_POWER, 1))),
                        new MentalMethodLevelEffect(2, List.of(new StatModifier(StatType.INNER_POWER, 3))),
                        new MentalMethodLevelEffect(3, List.of(new StatModifier(StatType.INNER_POWER, 6)))))
                .build();

        assertThat(t.effectAt(1).statModifiers().get(0).getValue()).isEqualTo(1);
        assertThat(t.effectAt(2).statModifiers().get(0).getValue()).isEqualTo(3);
        assertThat(t.effectAt(3).statModifiers().get(0).getValue()).isEqualTo(6);
    }

    @Test
    void effectAt_whenLevelBelow1_throws() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(1)
                .levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build();

        assertThatThrownBy(() -> t.effectAt(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> t.effectAt(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void effectAt_whenLevelAboveMaxLevel_throws() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(2)
                .levelEffects(List.of(
                        new MentalMethodLevelEffect(1, List.of()),
                        new MentalMethodLevelEffect(2, List.of())))
                .build();

        assertThatThrownBy(() -> t.effectAt(3)).isInstanceOf(IllegalArgumentException.class);
    }
```

Note: `StatModifier`의 value getter는 `getValue()` (`@Getter` 기반).
사용 import 추가 필요 여부 확인 — 기존 테스트는 이미 `assertThat`, `assertThatThrownBy`, `StatModifier`, `StatType`, `List`를 import하고 있음.

- [ ] **Step 2: 테스트가 컴파일 실패하는지 확인**

Run: `./gradlew test --tests MentalMethodTemplateTest`
Expected: 컴파일 오류 또는 `cannot find symbol method effectAt`로 실패.

- [ ] **Step 3: `MentalMethodTemplate`에 effectAt 메서드 추가**

`MentalMethodTemplate.java`의 `Builder` 정적 inner class 정의 바로 위(즉, 마지막 인스턴스 메서드 자리)에 추가:

```java
    public MentalMethodLevelEffect effectAt(int level) {
        if (level < 1 || level > maxLevel) {
            throw new IllegalArgumentException(
                    "level must be in 1.." + maxLevel + ", got " + level);
        }
        return levelEffects.get(level - 1);
    }
```

- [ ] **Step 4: MentalMethod 테스트 통과 확인**

Run: `./gradlew test --tests MentalMethodTemplateTest`
Expected: 모든 테스트 통과.

- [ ] **Step 5: `ExternalArtTemplateTest`에 effectAt 테스트 3개 추가**

`ExternalArtTemplateTest`의 마지막 `}` 직전에 추가:

```java
    @Test
    void effectAt_returnsEffectForGivenLevel() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("천뢰검법").description("d").weaponType(WeaponType.SWORD)
                .maxLevel(3)
                .levelEffects(List.of(
                        new ExternalArtLevelEffect(1, 1.0, 5, 3, 2),
                        new ExternalArtLevelEffect(2, 1.5, 4, 3, 2),
                        new ExternalArtLevelEffect(3, 2.0, 3, 3, 2)))
                .build();

        assertThat(t.effectAt(1).damageMultiplier()).isEqualTo(1.0);
        assertThat(t.effectAt(2).damageMultiplier()).isEqualTo(1.5);
        assertThat(t.effectAt(3).damageMultiplier()).isEqualTo(2.0);
    }

    @Test
    void effectAt_whenLevelBelow1_throws() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("x").description("x").weaponType(WeaponType.SWORD)
                .maxLevel(1)
                .levelEffects(List.of(new ExternalArtLevelEffect(1, 1.0, 1, 1, 1)))
                .build();

        assertThatThrownBy(() -> t.effectAt(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void effectAt_whenLevelAboveMaxLevel_throws() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("x").description("x").weaponType(WeaponType.SWORD)
                .maxLevel(1)
                .levelEffects(List.of(new ExternalArtLevelEffect(1, 1.0, 1, 1, 1)))
                .build();

        assertThatThrownBy(() -> t.effectAt(2)).isInstanceOf(IllegalArgumentException.class);
    }
```

기존 테스트가 `WeaponType`, `assertThatThrownBy`를 import 안 했다면 추가하라.

- [ ] **Step 6: 테스트 컴파일 실패 확인**

Run: `./gradlew test --tests ExternalArtTemplateTest`
Expected: `cannot find symbol method effectAt` 컴파일 오류.

- [ ] **Step 7: `ExternalArtTemplate.effectAt` 구현**

`ExternalArtTemplate.java`의 Builder inner class 바로 위에 추가:

```java
    public ExternalArtLevelEffect effectAt(int level) {
        if (level < 1 || level > maxLevel) {
            throw new IllegalArgumentException(
                    "level must be in 1.." + maxLevel + ", got " + level);
        }
        return levelEffects.get(level - 1);
    }
```

- [ ] **Step 8: 전체 도메인 테스트 통과 확인**

Run: `./gradlew test --tests MentalMethodTemplateTest --tests ExternalArtTemplateTest`
Expected: 모두 통과.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodTemplate.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtTemplate.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodTemplateTest.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtTemplateTest.java
git commit -m "feat(martial-arts): add effectAt(level) to templates

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 2: `ItemDisplayLabels.of(MentalMethodKind)` 추가

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/look/ItemDisplayLabels.java`

- [ ] **Step 1: ItemDisplayLabels에 오버로드 추가**

기존 `of(StatType)` 메서드 아래에 다음을 추가하고 클래스 상단 import에 `com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind`를 추가:

```java
    public static String of(MentalMethodKind kind) {
        return switch (kind) {
            case INNER_POWER       -> "내공";
            case LIGHT_STEP        -> "경공";
            case SPECIAL_TECHNIQUE -> "특기";
        };
    }
```

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/look/ItemDisplayLabels.java
git commit -m "feat(martial-arts): add Korean labels for MentalMethodKind

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 3: `CommandDictionary.MARTIAL_ART` + `MartialArtViewCommand` 추가

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionary.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/MartialArtViewCommand.java`

- [ ] **Step 1: `CommandDictionary`에 MARTIAL_ART 추가**

`CommandDictionary.java`의 `EAT` 항목 다음에 추가(EAT 라인 끝의 `;`를 `,`로 변경):

```java
    EAT("먹다", "먹어", "먹"),
    MARTIAL_ART("무공", "무공창");
```

- [ ] **Step 2: `MartialArtViewCommand` 신규**

`MartialArtViewCommand.java` 생성:

```java
package com.jefflife.mudmk2.gameplay.application.domain.model.command;

public record MartialArtViewCommand(Long userId) implements Command {}
```

- [ ] **Step 3: 컴파일 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/CommandDictionary.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/domain/model/command/MartialArtViewCommand.java
git commit -m "feat(martial-arts): add MARTIAL_ART command dictionary entry + command

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 4: `MartialArtViewVariables` 신규

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/model/template/MartialArtViewVariables.java`

- [ ] **Step 1: Variables record 생성**

```java
package com.jefflife.mudmk2.gameplay.application.service.model.template;

import java.util.List;

public record MartialArtViewVariables(
        Long userId,
        List<MentalGroup> mentalGroups,
        List<ExternalGroup> externalGroups
) {
    public record MentalGroup(
            String kindLabel,
            List<LearnedMentalLine> items
    ) {}

    public record LearnedMentalLine(
            String name,
            int currentLevel,
            int maxLevel,
            long currentExp,
            boolean atMax,
            boolean equipped,
            List<StatModLine> effects
    ) {}

    public record ExternalGroup(
            String weaponLabel,
            List<LearnedExternalLine> items
    ) {}

    public record LearnedExternalLine(
            String name,
            int currentLevel,
            int maxLevel,
            long currentExp,
            boolean atMax,
            boolean equipped,
            double damageMultiplier,
            int cooldownSeconds,
            int apCost,
            int mpCost
    ) {}

    public record StatModLine(String label, int value) {}
}
```

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/model/template/MartialArtViewVariables.java
git commit -m "feat(martial-arts): add MartialArtViewVariables template

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 5: `StatusVariables`에 `EquippedMartialArtsView` 필드 추가

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/model/template/StatusVariables.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandService.java` (컴파일 유지용 임시 수정)
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/eventpublisher/chat/StatusMessageSenderTemplateTest.java` (컴파일 유지용 임시 수정)

- [ ] **Step 1: `StatusVariables`에 필드 + 중첩 record 추가**

`StatusVariables.java`의 마지막 필드 `String roomName` 뒤에 콤마를 찍고 새 필드를 추가. 그리고 `StatValue` record 다음에 추가 record 정의:

수정된 전체 파일은 다음과 같다:

```java
package com.jefflife.mudmk2.gameplay.application.service.model.template;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterState;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;

import java.util.List;

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
        String roomName,
        EquippedMartialArtsView equippedMartialArts
) {
    public record StatValue(int base, int bonus) {
        public int total() {
            return base + bonus;
        }
    }

    public record EquippedMartialArtsView(
            List<MentalSlotLine> mentalSlots,
            List<ExternalSlotLine> externalSlots
    ) {}

    public record MentalSlotLine(
            String kindLabel,
            String name,
            Integer currentLevel,
            Integer maxLevel,
            List<StatModLine> effects
    ) {}

    public record ExternalSlotLine(
            int slotNumber,
            String name,
            String weaponLabel,
            Integer currentLevel,
            Integer maxLevel,
            Double damageMultiplier,
            Integer cooldownSeconds,
            Integer apCost,
            Integer mpCost
    ) {}

    public record StatModLine(String label, int value) {}
}
```

- [ ] **Step 2: `StatusCommandService`에 placeholder 인자 추가**

`StatusVariables` 생성자 호출(`new StatusVariables(...)`)의 마지막 인자 `currentRoom.getName()` 뒤에 콤마를 찍고 `null`을 추가하여 컴파일을 유지한다(다음 태스크에서 실제 값으로 교체):

```java
                currentRoom.getName(),
                null
```

- [ ] **Step 3: `StatusMessageSenderTemplateTest`에 빈 무공 view 헬퍼 추가 + 기존 생성자 호출 fixup**

`StatusMessageSenderTemplateTest`의 클래스 끝(맨 마지막 `}` 직전)에 헬퍼 메서드를 추가:

```java
    private StatusVariables.EquippedMartialArtsView emptyEquipped() {
        java.util.List<StatusVariables.MentalSlotLine> mental = java.util.List.of(
                new StatusVariables.MentalSlotLine("내공", null, null, null, java.util.List.of()),
                new StatusVariables.MentalSlotLine("경공", null, null, null, java.util.List.of()),
                new StatusVariables.MentalSlotLine("특기", null, null, null, java.util.List.of())
        );
        java.util.List<StatusVariables.ExternalSlotLine> external = new java.util.ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            external.add(new StatusVariables.ExternalSlotLine(
                    i, null, null, null, null, null, null, null, null));
        }
        return new StatusVariables.EquippedMartialArtsView(mental, external);
    }
```

그리고 `defaultVariables()`의 마지막 인자 `"수련장"` 다음에 콤마를 찍고 `emptyEquipped()`를 추가:

```java
                "수련장",         // roomName
                emptyEquipped()
```

또한 `withVigor()`의 마지막 인자 `d.archery(), d.roomName()` 다음에 콤마를 찍고 `d.equippedMartialArts()`를 추가:

```java
                d.archery(), d.roomName(), d.equippedMartialArts()
```

- [ ] **Step 4: 빌드 통과 확인**

Run: `./gradlew compileJava compileTestJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: 기존 status 관련 테스트 통과 확인 (회귀)**

Run: `./gradlew test --tests StatusCommandServiceTest --tests StatusMessageSenderTemplateTest`
Expected: 모든 테스트 통과 (기존 테스트는 `equippedMartialArts` 필드 검증을 안 함 → 영향 없음).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/model/template/StatusVariables.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/eventpublisher/chat/StatusMessageSenderTemplateTest.java
git commit -m "feat(martial-arts): extend StatusVariables with EquippedMartialArtsView

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 6: `MartialArtViewMapper.toEquippedView` 구현

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/martialart/MartialArtViewMapper.java`
- Create: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/martialart/MartialArtViewMapperTest.java`

- [ ] **Step 1: 테스트 클래스 골격 + 빈 슬롯 케이스 작성**

`MartialArtViewMapperTest.java` 생성:

```java
package com.jefflife.mudmk2.gameplay.application.service.command.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.provided.ExternalArtTemplateFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.LearnedMartialArtFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.MentalMethodTemplateFinder;
import com.jefflife.mudmk2.gameplay.application.service.model.template.StatusVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MartialArtViewMapperTest {

    private LearnedMartialArtFinder finder;
    private MentalMethodTemplateFinder mentalTplFinder;
    private ExternalArtTemplateFinder externalTplFinder;
    private MartialArtViewMapper mapper;

    private PlayerCharacter pc;
    private UUID pcId;

    @BeforeEach
    void setUp() {
        finder = mock(LearnedMartialArtFinder.class);
        mentalTplFinder = mock(MentalMethodTemplateFinder.class);
        externalTplFinder = mock(ExternalArtTemplateFinder.class);
        mapper = new MartialArtViewMapper(finder, mentalTplFinder, externalTplFinder);

        pcId = UUID.randomUUID();
        pc = mock(PlayerCharacter.class);
        when(pc.getId()).thenReturn(pcId);
    }

    private LearnedMartialArtFinder.CharacterMartialArtView view(
            List<LearnedMentalMethod> mentals,
            List<LearnedExternalArt> externals,
            EquippedMartialArts equipped) {
        return new LearnedMartialArtFinder.CharacterMartialArtView(pcId, mentals, externals, equipped);
    }

    // ---- toEquippedView ----

    @Test
    void toEquippedView_noEquipped_threeMentalSlotsAndSixExternalSlotsAllEmpty() {
        when(finder.findByCharacter(pcId))
                .thenReturn(view(List.of(), List.of(), EquippedMartialArts.create()));

        StatusVariables.EquippedMartialArtsView v = mapper.toEquippedView(pc);

        assertThat(v.mentalSlots()).hasSize(3);
        assertThat(v.mentalSlots()).allMatch(s -> s.name() == null);
        assertThat(v.mentalSlots().get(0).kindLabel()).isEqualTo("내공");
        assertThat(v.mentalSlots().get(1).kindLabel()).isEqualTo("경공");
        assertThat(v.mentalSlots().get(2).kindLabel()).isEqualTo("특기");

        assertThat(v.externalSlots()).hasSize(6);
        assertThat(v.externalSlots()).allMatch(s -> s.name() == null);
        assertThat(v.externalSlots().get(0).slotNumber()).isEqualTo(1);
        assertThat(v.externalSlots().get(5).slotNumber()).isEqualTo(6);
    }
}
```

- [ ] **Step 2: 테스트가 컴파일 실패 확인**

Run: `./gradlew test --tests MartialArtViewMapperTest`
Expected: `cannot find symbol class MartialArtViewMapper` 컴파일 오류.

- [ ] **Step 3: `MartialArtViewMapper` 최소 구현 (toEquippedView만)**

`MartialArtViewMapper.java` 생성:

```java
package com.jefflife.mudmk2.gameplay.application.service.command.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.provided.ExternalArtTemplateFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.LearnedMartialArtFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.MentalMethodTemplateFinder;
import com.jefflife.mudmk2.gameplay.application.service.command.look.ItemDisplayLabels;
import com.jefflife.mudmk2.gameplay.application.service.model.template.MartialArtViewVariables;
import com.jefflife.mudmk2.gameplay.application.service.model.template.StatusVariables;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MartialArtViewMapper {

    private final LearnedMartialArtFinder finder;
    private final MentalMethodTemplateFinder mentalTplFinder;
    private final ExternalArtTemplateFinder externalTplFinder;

    public MartialArtViewMapper(LearnedMartialArtFinder finder,
                                MentalMethodTemplateFinder mentalTplFinder,
                                ExternalArtTemplateFinder externalTplFinder) {
        this.finder = finder;
        this.mentalTplFinder = mentalTplFinder;
        this.externalTplFinder = externalTplFinder;
    }

    public StatusVariables.EquippedMartialArtsView toEquippedView(PlayerCharacter pc) {
        LearnedMartialArtFinder.CharacterMartialArtView v = finder.findByCharacter(pc.getId());

        Map<UUID, LearnedMentalMethod> mentalById = v.learnedMentalMethods().stream()
                .collect(Collectors.toMap(LearnedMentalMethod::getId, m -> m));
        Map<UUID, LearnedExternalArt> externalById = v.learnedExternalArts().stream()
                .collect(Collectors.toMap(LearnedExternalArt::getId, m -> m));

        List<StatusVariables.MentalSlotLine> mentalSlots = new ArrayList<>();
        for (MentalMethodKind kind : MentalMethodKind.values()) {
            UUID learnedId = v.equipped().getMentalSlots().get(kind);
            mentalSlots.add(buildMentalSlot(kind, learnedId, mentalById));
        }

        List<StatusVariables.ExternalSlotLine> externalSlots = new ArrayList<>();
        List<UUID> equippedExternals = v.equipped().getExternalSlots();
        for (int i = 0; i < EquippedMartialArts.EXTERNAL_SLOT_MAX; i++) {
            UUID learnedId = i < equippedExternals.size() ? equippedExternals.get(i) : null;
            externalSlots.add(buildExternalSlot(i + 1, learnedId, externalById));
        }
        return new StatusVariables.EquippedMartialArtsView(mentalSlots, externalSlots);
    }

    private StatusVariables.MentalSlotLine buildMentalSlot(
            MentalMethodKind kind, UUID learnedId, Map<UUID, LearnedMentalMethod> mentalById) {
        String label = ItemDisplayLabels.of(kind);
        if (learnedId == null) {
            return new StatusVariables.MentalSlotLine(label, null, null, null, List.of());
        }
        LearnedMentalMethod learned = mentalById.get(learnedId);
        MentalMethodTemplate tpl = mentalTplFinder.findById(learned.getMentalMethodTemplateId());
        MentalMethodLevelEffect effect = tpl.effectAt(learned.getCurrentLevel());
        List<StatusVariables.StatModLine> effects = effect.statModifiers().stream()
                .map(this::toStatusStatModLine)
                .toList();
        return new StatusVariables.MentalSlotLine(
                label, tpl.getName(), learned.getCurrentLevel(), tpl.getMaxLevel(), effects);
    }

    private StatusVariables.ExternalSlotLine buildExternalSlot(
            int slotNumber, UUID learnedId, Map<UUID, LearnedExternalArt> externalById) {
        if (learnedId == null) {
            return new StatusVariables.ExternalSlotLine(
                    slotNumber, null, null, null, null, null, null, null, null);
        }
        LearnedExternalArt learned = externalById.get(learnedId);
        ExternalArtTemplate tpl = externalTplFinder.findById(learned.getExternalArtTemplateId());
        ExternalArtLevelEffect effect = tpl.effectAt(learned.getCurrentLevel());
        return new StatusVariables.ExternalSlotLine(
                slotNumber,
                tpl.getName(),
                ItemDisplayLabels.of(tpl.getWeaponType()),
                learned.getCurrentLevel(),
                tpl.getMaxLevel(),
                effect.damageMultiplier(),
                effect.cooldownSeconds(),
                effect.apCost(),
                effect.mpCost()
        );
    }

    private StatusVariables.StatModLine toStatusStatModLine(StatModifier mod) {
        return new StatusVariables.StatModLine(
                ItemDisplayLabels.of(mod.getStatType()), mod.getValue());
    }
}
```

- [ ] **Step 4: 빈 슬롯 테스트 통과 확인**

Run: `./gradlew test --tests MartialArtViewMapperTest`
Expected: 통과.

- [ ] **Step 5: 채워진 슬롯 테스트 추가**

`MartialArtViewMapperTest`에 다음 메서드 추가:

```java
    @Test
    void toEquippedView_withEquippedMentalAndExternal_populatesSlots() {
        // 심법: INNER_POWER 슬롯에 천뢰신공 Lv.2 장착
        MentalMethodTemplate mentalTpl = mock(MentalMethodTemplate.class);
        when(mentalTpl.getName()).thenReturn("천뢰신공");
        when(mentalTpl.getMaxLevel()).thenReturn(3);
        when(mentalTpl.effectAt(2)).thenReturn(new MentalMethodLevelEffect(2,
                List.of(new StatModifier(StatType.INNER_POWER, 3))));

        UUID learnedMentalId = UUID.randomUUID();
        LearnedMentalMethod learnedMental = mock(LearnedMentalMethod.class);
        when(learnedMental.getId()).thenReturn(learnedMentalId);
        when(learnedMental.getMentalMethodTemplateId()).thenReturn(100L);
        when(learnedMental.getCurrentLevel()).thenReturn(2);

        when(mentalTplFinder.findById(100L)).thenReturn(mentalTpl);

        // 외공: 슬롯 1번에 천뢰검법 Lv.1
        ExternalArtTemplate externalTpl = mock(ExternalArtTemplate.class);
        when(externalTpl.getName()).thenReturn("천뢰검법");
        when(externalTpl.getWeaponType()).thenReturn(WeaponType.SWORD);
        when(externalTpl.getMaxLevel()).thenReturn(5);
        when(externalTpl.effectAt(1)).thenReturn(new ExternalArtLevelEffect(1, 1.5, 4, 3, 2));

        UUID learnedExternalId = UUID.randomUUID();
        LearnedExternalArt learnedExternal = mock(LearnedExternalArt.class);
        when(learnedExternal.getId()).thenReturn(learnedExternalId);
        when(learnedExternal.getExternalArtTemplateId()).thenReturn(200L);
        when(learnedExternal.getCurrentLevel()).thenReturn(1);

        when(externalTplFinder.findById(200L)).thenReturn(externalTpl);

        EquippedMartialArts equipped = EquippedMartialArts.create();
        equipped.equipMental(MentalMethodKind.INNER_POWER, learnedMentalId);
        equipped.equipExternal(learnedExternalId);

        when(finder.findByCharacter(pcId)).thenReturn(view(
                List.of(learnedMental), List.of(learnedExternal), equipped));

        StatusVariables.EquippedMartialArtsView v = mapper.toEquippedView(pc);

        // 심법 슬롯 0 (INNER_POWER): 채워짐
        StatusVariables.MentalSlotLine inner = v.mentalSlots().get(0);
        assertThat(inner.name()).isEqualTo("천뢰신공");
        assertThat(inner.currentLevel()).isEqualTo(2);
        assertThat(inner.maxLevel()).isEqualTo(3);
        assertThat(inner.effects()).hasSize(1);
        assertThat(inner.effects().get(0).label()).isEqualTo("내공");
        assertThat(inner.effects().get(0).value()).isEqualTo(3);

        // 심법 슬롯 1, 2: 빈 채로
        assertThat(v.mentalSlots().get(1).name()).isNull();
        assertThat(v.mentalSlots().get(2).name()).isNull();

        // 외공 슬롯 0 (slotNumber 1): 채워짐
        StatusVariables.ExternalSlotLine ext = v.externalSlots().get(0);
        assertThat(ext.slotNumber()).isEqualTo(1);
        assertThat(ext.name()).isEqualTo("천뢰검법");
        assertThat(ext.weaponLabel()).isEqualTo("검");
        assertThat(ext.currentLevel()).isEqualTo(1);
        assertThat(ext.maxLevel()).isEqualTo(5);
        assertThat(ext.damageMultiplier()).isEqualTo(1.5);
        assertThat(ext.cooldownSeconds()).isEqualTo(4);
        assertThat(ext.apCost()).isEqualTo(3);
        assertThat(ext.mpCost()).isEqualTo(2);

        // 외공 슬롯 1~5: 빈 채로
        for (int i = 1; i < 6; i++) {
            assertThat(v.externalSlots().get(i).name()).isNull();
            assertThat(v.externalSlots().get(i).slotNumber()).isEqualTo(i + 1);
        }
    }
```

- [ ] **Step 6: 채워진 슬롯 테스트 통과 확인**

Run: `./gradlew test --tests MartialArtViewMapperTest`
Expected: 모두 통과.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/martialart/MartialArtViewMapper.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/martialart/MartialArtViewMapperTest.java
git commit -m "feat(martial-arts): add MartialArtViewMapper.toEquippedView

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 7: `MartialArtViewMapper.toMartialArtVariables` 구현

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/martialart/MartialArtViewMapper.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/martialart/MartialArtViewMapperTest.java`

- [ ] **Step 1: 학습 0건 테스트 추가**

`MartialArtViewMapperTest`에 다음 메서드 추가:

```java
    @Test
    void toMartialArtVariables_noLearned_returnsEmptyGroups() {
        when(finder.findByCharacter(pcId))
                .thenReturn(view(List.of(), List.of(), EquippedMartialArts.create()));

        MartialArtViewVariables v = mapper.toMartialArtVariables(7L, pc);

        assertThat(v.userId()).isEqualTo(7L);
        assertThat(v.mentalGroups()).isEmpty();
        assertThat(v.externalGroups()).isEmpty();
    }
```

또한 import에 다음 추가:
```java
import com.jefflife.mudmk2.gameplay.application.service.model.template.MartialArtViewVariables;
```

- [ ] **Step 2: 테스트 컴파일 실패 확인 (메서드 미정의)**

Run: `./gradlew test --tests MartialArtViewMapperTest`
Expected: `cannot find symbol method toMartialArtVariables`.

- [ ] **Step 3: `MartialArtViewMapper`에 메서드 추가**

`MartialArtViewMapper`에 다음 public 메서드와 internal 헬퍼들을 추가:

```java
    public MartialArtViewVariables toMartialArtVariables(Long userId, PlayerCharacter pc) {
        LearnedMartialArtFinder.CharacterMartialArtView v = finder.findByCharacter(pc.getId());
        Set<UUID> equippedMentalIds = new HashSet<>(v.equipped().getMentalSlots().values());
        Set<UUID> equippedExternalIds = new HashSet<>(v.equipped().getExternalSlots());

        return new MartialArtViewVariables(
                userId,
                groupMental(v.learnedMentalMethods(), equippedMentalIds),
                groupExternal(v.learnedExternalArts(), equippedExternalIds));
    }

    private List<MartialArtViewVariables.MentalGroup> groupMental(
            List<LearnedMentalMethod> learned, Set<UUID> equippedIds) {
        // kind -> ordered items
        EnumMap<MentalMethodKind, List<MartialArtViewVariables.LearnedMentalLine>> buckets =
                new EnumMap<>(MentalMethodKind.class);
        for (LearnedMentalMethod m : learned) {
            MentalMethodTemplate tpl = mentalTplFinder.findById(m.getMentalMethodTemplateId());
            List<MartialArtViewVariables.StatModLine> effects = tpl.effectAt(m.getCurrentLevel())
                    .statModifiers().stream()
                    .map(this::toViewStatModLine)
                    .toList();
            MartialArtViewVariables.LearnedMentalLine line = new MartialArtViewVariables.LearnedMentalLine(
                    tpl.getName(),
                    m.getCurrentLevel(),
                    tpl.getMaxLevel(),
                    m.getCurrentExp(),
                    m.getCurrentLevel() == tpl.getMaxLevel(),
                    equippedIds.contains(m.getId()),
                    effects);
            buckets.computeIfAbsent(tpl.getKind(), k -> new ArrayList<>()).add(line);
        }
        List<MartialArtViewVariables.MentalGroup> groups = new ArrayList<>();
        for (MentalMethodKind kind : MentalMethodKind.values()) {
            List<MartialArtViewVariables.LearnedMentalLine> items = buckets.get(kind);
            if (items != null && !items.isEmpty()) {
                groups.add(new MartialArtViewVariables.MentalGroup(ItemDisplayLabels.of(kind), items));
            }
        }
        return groups;
    }

    private List<MartialArtViewVariables.ExternalGroup> groupExternal(
            List<LearnedExternalArt> learned, Set<UUID> equippedIds) {
        EnumMap<com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType,
                List<MartialArtViewVariables.LearnedExternalLine>> buckets =
                new EnumMap<>(com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType.class);
        for (LearnedExternalArt e : learned) {
            ExternalArtTemplate tpl = externalTplFinder.findById(e.getExternalArtTemplateId());
            ExternalArtLevelEffect ef = tpl.effectAt(e.getCurrentLevel());
            MartialArtViewVariables.LearnedExternalLine line = new MartialArtViewVariables.LearnedExternalLine(
                    tpl.getName(),
                    e.getCurrentLevel(),
                    tpl.getMaxLevel(),
                    e.getCurrentExp(),
                    e.getCurrentLevel() == tpl.getMaxLevel(),
                    equippedIds.contains(e.getId()),
                    ef.damageMultiplier(),
                    ef.cooldownSeconds(),
                    ef.apCost(),
                    ef.mpCost());
            buckets.computeIfAbsent(tpl.getWeaponType(), k -> new ArrayList<>()).add(line);
        }
        List<MartialArtViewVariables.ExternalGroup> groups = new ArrayList<>();
        for (com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType wt :
                com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType.values()) {
            List<MartialArtViewVariables.LearnedExternalLine> items = buckets.get(wt);
            if (items != null && !items.isEmpty()) {
                groups.add(new MartialArtViewVariables.ExternalGroup(ItemDisplayLabels.of(wt), items));
            }
        }
        return groups;
    }

    private MartialArtViewVariables.StatModLine toViewStatModLine(StatModifier mod) {
        return new MartialArtViewVariables.StatModLine(
                ItemDisplayLabels.of(mod.getStatType()), mod.getValue());
    }
```

추가로 import에 다음을 더한다:
```java
import com.jefflife.mudmk2.gameplay.application.service.model.template.MartialArtViewVariables;
```

(`WeaponType`은 fully-qualified로 두어 import 충돌을 피했다.)

- [ ] **Step 4: 학습 0건 테스트 통과 확인**

Run: `./gradlew test --tests MartialArtViewMapperTest`
Expected: 모두 통과.

- [ ] **Step 5: 그룹화 + 장착 마킹 + MAX 라벨 테스트 추가**

`MartialArtViewMapperTest`에 추가:

```java
    @Test
    void toMartialArtVariables_groupsByKindAndWeaponType_equippedAndAtMaxFlagged() {
        // 심법 학습: 천뢰신공(내공, Lv 3/3 = MAX, 장착) + 풍림보(경공, Lv 1/2)
        UUID innerLearnedId = UUID.randomUUID();
        LearnedMentalMethod innerLearned = mock(LearnedMentalMethod.class);
        when(innerLearned.getId()).thenReturn(innerLearnedId);
        when(innerLearned.getMentalMethodTemplateId()).thenReturn(11L);
        when(innerLearned.getCurrentLevel()).thenReturn(3);
        when(innerLearned.getCurrentExp()).thenReturn(0L);
        MentalMethodTemplate innerTpl = mock(MentalMethodTemplate.class);
        when(innerTpl.getKind()).thenReturn(MentalMethodKind.INNER_POWER);
        when(innerTpl.getName()).thenReturn("천뢰신공");
        when(innerTpl.getMaxLevel()).thenReturn(3);
        when(innerTpl.effectAt(3)).thenReturn(new MentalMethodLevelEffect(3,
                List.of(new StatModifier(StatType.INNER_POWER, 6))));
        when(mentalTplFinder.findById(11L)).thenReturn(innerTpl);

        UUID lightLearnedId = UUID.randomUUID();
        LearnedMentalMethod lightLearned = mock(LearnedMentalMethod.class);
        when(lightLearned.getId()).thenReturn(lightLearnedId);
        when(lightLearned.getMentalMethodTemplateId()).thenReturn(12L);
        when(lightLearned.getCurrentLevel()).thenReturn(1);
        when(lightLearned.getCurrentExp()).thenReturn(0L);
        MentalMethodTemplate lightTpl = mock(MentalMethodTemplate.class);
        when(lightTpl.getKind()).thenReturn(MentalMethodKind.LIGHT_STEP);
        when(lightTpl.getName()).thenReturn("풍림보");
        when(lightTpl.getMaxLevel()).thenReturn(2);
        when(lightTpl.effectAt(1)).thenReturn(new MentalMethodLevelEffect(1,
                List.of(new StatModifier(StatType.LIGHT_STEP, 2))));
        when(mentalTplFinder.findById(12L)).thenReturn(lightTpl);

        // 외공 학습: 천뢰검법(검, Lv 1/2, 장착)
        UUID swordLearnedId = UUID.randomUUID();
        LearnedExternalArt swordLearned = mock(LearnedExternalArt.class);
        when(swordLearned.getId()).thenReturn(swordLearnedId);
        when(swordLearned.getExternalArtTemplateId()).thenReturn(21L);
        when(swordLearned.getCurrentLevel()).thenReturn(1);
        when(swordLearned.getCurrentExp()).thenReturn(0L);
        ExternalArtTemplate swordTpl = mock(ExternalArtTemplate.class);
        when(swordTpl.getName()).thenReturn("천뢰검법");
        when(swordTpl.getWeaponType()).thenReturn(WeaponType.SWORD);
        when(swordTpl.getMaxLevel()).thenReturn(2);
        when(swordTpl.effectAt(1)).thenReturn(new ExternalArtLevelEffect(1, 1.5, 4, 3, 2));
        when(externalTplFinder.findById(21L)).thenReturn(swordTpl);

        EquippedMartialArts equipped = EquippedMartialArts.create();
        equipped.equipMental(MentalMethodKind.INNER_POWER, innerLearnedId);  // 장착
        equipped.equipExternal(swordLearnedId);                              // 장착
        // lightLearnedId 는 장착 안 함

        when(finder.findByCharacter(pcId)).thenReturn(view(
                List.of(innerLearned, lightLearned),
                List.of(swordLearned),
                equipped));

        MartialArtViewVariables v = mapper.toMartialArtVariables(7L, pc);

        // 심법: 내공 + 경공 두 그룹 (특기 학습 0 → 그룹 없음), 엔움 선언순
        assertThat(v.mentalGroups()).hasSize(2);
        assertThat(v.mentalGroups().get(0).kindLabel()).isEqualTo("내공");
        assertThat(v.mentalGroups().get(0).items()).hasSize(1);
        MartialArtViewVariables.LearnedMentalLine innerLine = v.mentalGroups().get(0).items().get(0);
        assertThat(innerLine.name()).isEqualTo("천뢰신공");
        assertThat(innerLine.currentLevel()).isEqualTo(3);
        assertThat(innerLine.maxLevel()).isEqualTo(3);
        assertThat(innerLine.atMax()).isTrue();
        assertThat(innerLine.equipped()).isTrue();
        assertThat(innerLine.effects()).hasSize(1);
        assertThat(innerLine.effects().get(0).label()).isEqualTo("내공");
        assertThat(innerLine.effects().get(0).value()).isEqualTo(6);

        assertThat(v.mentalGroups().get(1).kindLabel()).isEqualTo("경공");
        MartialArtViewVariables.LearnedMentalLine lightLine = v.mentalGroups().get(1).items().get(0);
        assertThat(lightLine.atMax()).isFalse();
        assertThat(lightLine.equipped()).isFalse();

        // 외공: 검 그룹만
        assertThat(v.externalGroups()).hasSize(1);
        assertThat(v.externalGroups().get(0).weaponLabel()).isEqualTo("검");
        MartialArtViewVariables.LearnedExternalLine swordLine =
                v.externalGroups().get(0).items().get(0);
        assertThat(swordLine.name()).isEqualTo("천뢰검법");
        assertThat(swordLine.currentLevel()).isEqualTo(1);
        assertThat(swordLine.maxLevel()).isEqualTo(2);
        assertThat(swordLine.atMax()).isFalse();
        assertThat(swordLine.equipped()).isTrue();
        assertThat(swordLine.damageMultiplier()).isEqualTo(1.5);
        assertThat(swordLine.cooldownSeconds()).isEqualTo(4);
        assertThat(swordLine.apCost()).isEqualTo(3);
        assertThat(swordLine.mpCost()).isEqualTo(2);
    }
```

- [ ] **Step 6: 모든 매퍼 테스트 통과 확인**

Run: `./gradlew test --tests MartialArtViewMapperTest`
Expected: 모두 통과.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/martialart/MartialArtViewMapper.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/martialart/MartialArtViewMapperTest.java
git commit -m "feat(martial-arts): add MartialArtViewMapper.toMartialArtVariables

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 8: UseCase / Port 인터페이스 + `MartialArtViewCommandService`

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/MartialArtViewUseCase.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/required/SendMartialArtViewMessagePort.java`
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/MartialArtViewCommandService.java`
- Create: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/MartialArtViewCommandServiceTest.java`

- [ ] **Step 1: UseCase 인터페이스 생성**

```java
package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.MartialArtViewCommand;

public interface MartialArtViewUseCase {
    void showMartialArts(MartialArtViewCommand command);
}
```

- [ ] **Step 2: Port 인터페이스 생성**

```java
package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gameplay.application.service.model.template.MartialArtViewVariables;

public interface SendMartialArtViewMessagePort {
    void sendMessage(MartialArtViewVariables variables);
}
```

- [ ] **Step 3: 서비스 테스트 작성**

```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.MartialArtViewCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.command.martialart.MartialArtViewMapper;
import com.jefflife.mudmk2.gameplay.application.service.model.template.MartialArtViewVariables;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMartialArtViewMessagePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MartialArtViewCommandServiceTest {

    private ActivePlayerRepository players;
    private MartialArtViewMapper mapper;
    private SendMartialArtViewMessagePort sender;
    private MartialArtViewCommandService service;

    @BeforeEach
    void setUp() {
        players = mock(ActivePlayerRepository.class);
        mapper = mock(MartialArtViewMapper.class);
        sender = mock(SendMartialArtViewMessagePort.class);
        service = new MartialArtViewCommandService(players, mapper, sender);
    }

    @Test
    void showMartialArts_callsMapperAndSendsVariables() {
        PlayerCharacter pc = mock(PlayerCharacter.class);
        when(players.findByUserId(1L)).thenReturn(Optional.of(pc));
        MartialArtViewVariables vars =
                new MartialArtViewVariables(1L, List.of(), List.of());
        when(mapper.toMartialArtVariables(1L, pc)).thenReturn(vars);

        service.showMartialArts(new MartialArtViewCommand(1L));

        verify(sender).sendMessage(vars);
    }

    @Test
    void showMartialArts_whenPlayerNotFound_throws() {
        when(players.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.showMartialArts(new MartialArtViewCommand(99L)))
                .isInstanceOf(PlayerNotFoundException.class);
    }
}
```

- [ ] **Step 4: 테스트 컴파일 실패 확인**

Run: `./gradlew test --tests MartialArtViewCommandServiceTest`
Expected: `cannot find symbol class MartialArtViewCommandService`.

- [ ] **Step 5: 서비스 구현**

```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.MartialArtViewCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.command.martialart.MartialArtViewMapper;
import com.jefflife.mudmk2.gameplay.application.service.provided.MartialArtViewUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMartialArtViewMessagePort;
import org.springframework.stereotype.Service;

@Service
public class MartialArtViewCommandService implements MartialArtViewUseCase {

    private final ActivePlayerRepository players;
    private final MartialArtViewMapper mapper;
    private final SendMartialArtViewMessagePort port;

    public MartialArtViewCommandService(ActivePlayerRepository players,
                                        MartialArtViewMapper mapper,
                                        SendMartialArtViewMessagePort port) {
        this.players = players;
        this.mapper = mapper;
        this.port = port;
    }

    @Override
    public void showMartialArts(MartialArtViewCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
        port.sendMessage(mapper.toMartialArtVariables(command.userId(), player));
    }
}
```

- [ ] **Step 6: 테스트 통과 확인**

Run: `./gradlew test --tests MartialArtViewCommandServiceTest`
Expected: 모두 통과.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/provided/MartialArtViewUseCase.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/required/SendMartialArtViewMessagePort.java \
        src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/MartialArtViewCommandService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/MartialArtViewCommandServiceTest.java
git commit -m "feat(martial-arts): add MartialArtViewCommandService + ports

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 9: `StatusCommandService`에 mapper 통합 + 기존 테스트 보강

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandService.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandServiceTest.java`

- [ ] **Step 1: 기존 `StatusCommandServiceTest`의 setUp + 무공 검증 테스트 추가**

`StatusCommandServiceTest`를 다음과 같이 수정:

- 클래스 필드에 mapper 추가:
  ```java
  private com.jefflife.mudmk2.gameplay.application.service.command.martialart.MartialArtViewMapper mapper;
  ```
- `setUp()`에서 mapper mock 생성 + service 생성자 인자에 추가:
  ```java
  mapper = mock(com.jefflife.mudmk2.gameplay.application.service.command.martialart.MartialArtViewMapper.class);
  service = new StatusCommandService(rooms, players, sender, mapper);
  ```
- 모든 기존 테스트에 영향이 없도록 mapper 기본 stubbing:
  - setUp에서 mapper가 `toEquippedView(any())`에 호출되었을 때 mock 결과를 반환하도록 한다. 다음 한 줄을 setUp 끝에 추가하라:
  ```java
  when(mapper.toEquippedView(any(PlayerCharacter.class))).thenReturn(
          new StatusVariables.EquippedMartialArtsView(java.util.List.of(), java.util.List.of()));
  ```
- import에 `import static org.mockito.ArgumentMatchers.any;` 추가.
- 클래스의 마지막에 무공 통합 검증 테스트 추가:

```java
    @Test
    void showStatus_includesEquippedMartialArtsViewFromMapper() {
        StatusVariables.EquippedMartialArtsView expected =
                new StatusVariables.EquippedMartialArtsView(
                        java.util.List.of(new StatusVariables.MentalSlotLine("내공", "천뢰신공", 1, 3, java.util.List.of())),
                        java.util.List.of());
        when(mapper.toEquippedView(any(PlayerCharacter.class))).thenReturn(expected);

        service.showStatus(new StatusCommand(1L));

        StatusVariables v = capturedVariables();
        assertThat(v.equippedMartialArts()).isSameAs(expected);
    }
```

- [ ] **Step 2: 테스트 컴파일 실패 확인 (생성자 시그니처 변경)**

Run: `./gradlew test --tests StatusCommandServiceTest`
Expected: `constructor StatusCommandService cannot be applied to given types` 오류.

- [ ] **Step 3: `StatusCommandService`에 mapper 주입 + 호출 추가**

전체를 다음으로 수정:

```java
package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.StatusCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.exception.RoomNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.command.martialart.MartialArtViewMapper;
import com.jefflife.mudmk2.gameplay.application.service.model.template.StatusVariables;
import com.jefflife.mudmk2.gameplay.application.service.provided.StatusUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendStatusMessagePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StatusCommandService implements StatusUseCase {
    private static final Logger logger = LoggerFactory.getLogger(StatusCommandService.class);

    private final ActiveRoomRepository rooms;
    private final ActivePlayerRepository players;
    private final SendStatusMessagePort sendStatusMessagePort;
    private final MartialArtViewMapper martialArtViewMapper;

    public StatusCommandService(
            final ActiveRoomRepository rooms,
            final ActivePlayerRepository players,
            final SendStatusMessagePort sendStatusMessagePort,
            final MartialArtViewMapper martialArtViewMapper
    ) {
        this.rooms = rooms;
        this.players = players;
        this.sendStatusMessagePort = sendStatusMessagePort;
        this.martialArtViewMapper = martialArtViewMapper;
    }

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
                currentRoom.getName(),
                martialArtViewMapper.toEquippedView(player)
        );

        sendStatusMessagePort.sendMessage(statusVariables);
        logger.debug("Sent status message for player: {}", player.getName());
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests StatusCommandServiceTest`
Expected: 모두 통과.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/service/command/StatusCommandServiceTest.java
git commit -m "feat(martial-arts): wire MartialArtViewMapper into StatusCommandService

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 10: `MartialArtViewCommandParser`

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/MartialArtViewCommandParser.java`
- Create: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/MartialArtViewCommandParserTest.java`

- [ ] **Step 1: Parser 테스트 작성**

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.MartialArtViewCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MartialArtViewCommandParserTest {

    private MartialArtViewCommandParser parser;

    @BeforeEach
    void setUp() { parser = new MartialArtViewCommandParser(); }

    @Test
    void parse_무공_returnsCommand() {
        Command c = parser.parse(1L, "무공");
        assertThat(c).isInstanceOf(MartialArtViewCommand.class);
    }

    @Test
    void parse_무공창_returnsCommand() {
        assertThat(parser.parse(1L, "무공창")).isInstanceOf(MartialArtViewCommand.class);
    }

    @Test
    void parse_other_returnsNull() {
        assertThat(parser.parse(1L, "장비")).isNull();
        assertThat(parser.parse(1L, "무공창고")).isNull();
        assertThat(parser.parse(1L, "철검 장착")).isNull();
    }
}
```

- [ ] **Step 2: 테스트 컴파일 실패 확인**

Run: `./gradlew test --tests MartialArtViewCommandParserTest`
Expected: `cannot find symbol class MartialArtViewCommandParser`.

- [ ] **Step 3: Parser 구현**

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.parser;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.CommandDictionary;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.MartialArtViewCommand;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MartialArtViewCommandParser extends AbstractCommandParser {
    private static final Pattern VIEW_PATTERN =
            Pattern.compile("(" + CommandDictionary.MARTIAL_ART.toRegex() + ")");

    @Override
    protected Command parseCommand(Long userId, String content) {
        Matcher matcher = VIEW_PATTERN.matcher(content);
        if (matcher.matches()) {
            return new MartialArtViewCommand(userId);
        }
        return null;
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests MartialArtViewCommandParserTest`
Expected: 모두 통과.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/MartialArtViewCommandParser.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/parser/MartialArtViewCommandParserTest.java
git commit -m "feat(martial-arts): add MartialArtViewCommandParser

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 11: `MartialArtViewCommandExecutor`

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/MartialArtViewCommandExecutor.java`
- Create: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/MartialArtViewCommandExecutorTest.java`

- [ ] **Step 1: Executor 테스트 작성**

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.MartialArtViewCommand;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.StatusCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.MartialArtViewUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MartialArtViewCommandExecutorTest {

    private MartialArtViewUseCase useCase;
    private MartialArtViewCommandExecutor executor;

    @BeforeEach
    void setUp() {
        useCase = mock(MartialArtViewUseCase.class);
        executor = new MartialArtViewCommandExecutor(useCase);
    }

    @Test
    void canExecute_returnsTrueForMartialArtViewCommand() {
        assertThat(executor.canExecute(new MartialArtViewCommand(1L))).isTrue();
    }

    @Test
    void canExecute_returnsFalseForOtherCommand() {
        assertThat(executor.canExecute(new StatusCommand(1L))).isFalse();
    }

    @Test
    void execute_callsUseCase() {
        MartialArtViewCommand cmd = new MartialArtViewCommand(1L);

        executor.execute(cmd);

        verify(useCase).showMartialArts(cmd);
    }

    @Test
    void execute_whenWrongCommand_throws() {
        assertThatThrownBy(() -> executor.execute(new StatusCommand(1L)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: 테스트 컴파일 실패 확인**

Run: `./gradlew test --tests MartialArtViewCommandExecutorTest`
Expected: `cannot find symbol class MartialArtViewCommandExecutor`.

- [ ] **Step 3: Executor 구현**

```java
package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.MartialArtViewCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.MartialArtViewUseCase;
import org.springframework.stereotype.Component;

@Component
public class MartialArtViewCommandExecutor implements CommandExecutor {
    private final MartialArtViewUseCase useCase;

    public MartialArtViewCommandExecutor(MartialArtViewUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof MartialArtViewCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof MartialArtViewCommand viewCommand)) {
            throw new IllegalArgumentException("Command must be a MartialArtViewCommand");
        }
        useCase.showMartialArts(viewCommand);
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests MartialArtViewCommandExecutorTest`
Expected: 모두 통과.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/MartialArtViewCommandExecutor.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/in/eventlistener/executor/MartialArtViewCommandExecutorTest.java
git commit -m "feat(martial-arts): add MartialArtViewCommandExecutor

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 12: `martial-art-view.html` 템플릿

**Files:**
- Create: `src/main/resources/templates/gameplay/martial-art-view.html`

- [ ] **Step 1: 템플릿 작성**

```html
<div th:fragment="martialArtView" class="martial-art-view font-terminal">
    <div th:if="${#lists.isEmpty(mentalGroups) and #lists.isEmpty(externalGroups)}">
        <div class="line">배운 무공이 없습니다.</div>
    </div>

    <div th:if="${not #lists.isEmpty(mentalGroups)}">
        <div class="msg-room-title">[ 심법 ]</div>
        <div th:each="g : ${mentalGroups}">
            <div class="line">〈<span th:text="${g.kindLabel}"></span>〉</div>
            <div class="line" th:each="m : ${g.items}">
                &nbsp;&nbsp;- <span th:text="${m.name}"></span>
                Lv.<span th:text="${m.currentLevel}"></span>/<span th:text="${m.maxLevel}"></span>
                <span th:if="${m.atMax}">[MAX]</span>
                &nbsp;&nbsp;Exp <span th:text="${m.currentExp}"></span>
                <span th:if="${not #lists.isEmpty(m.effects)}">
                    &nbsp;&nbsp;<th:block th:each="e, st : ${m.effects}"
                        ><span th:text="${(e.value >= 0 ? '+' : '') + e.value + ' ' + e.label}"></span
                        ><span th:if="${not st.last}">, </span></th:block>
                </span>
                <span th:if="${m.equipped}"> ☆</span>
            </div>
        </div>
    </div>

    <div th:if="${not #lists.isEmpty(externalGroups)}">
        <div class="line-spacer"></div>
        <div class="msg-room-title">[ 외공 ]</div>
        <div th:each="g : ${externalGroups}">
            <div class="line">〈<span th:text="${g.weaponLabel}"></span>〉</div>
            <div class="line" th:each="x : ${g.items}">
                &nbsp;&nbsp;- <span th:text="${x.name}"></span>
                Lv.<span th:text="${x.currentLevel}"></span>/<span th:text="${x.maxLevel}"></span>
                <span th:if="${x.atMax}">[MAX]</span>
                &nbsp;&nbsp;Exp <span th:text="${x.currentExp}"></span>
                &nbsp;&nbsp;×<span th:text="${#numbers.formatDecimal(x.damageMultiplier, 1, 2)}"></span>
                &nbsp;쿨<span th:text="${x.cooldownSeconds}"></span>s
                &nbsp;AP<span th:text="${x.apCost}"></span>
                &nbsp;MP<span th:text="${x.mpCost}"></span>
                <span th:if="${x.equipped}"> ☆</span>
            </div>
        </div>
    </div>
</div>
```

- [ ] **Step 2: Commit (sender 미사용 — 다음 태스크에서 wiring + 렌더 검증)**

```bash
git add src/main/resources/templates/gameplay/martial-art-view.html
git commit -m "feat(martial-arts): add martial-art-view.html template

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 13: `MartialArtViewMessageSender` + 템플릿 렌더 테스트

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/eventpublisher/chat/MartialArtViewMessageSender.java`
- Create: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/eventpublisher/chat/MartialArtViewMessageSenderTemplateTest.java`

- [ ] **Step 1: Sender 구현**

```java
package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher.chat;

import com.jefflife.mudmk2.gameplay.application.service.model.template.MartialArtViewVariables;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMartialArtViewMessagePort;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class MartialArtViewMessageSender implements SendMartialArtViewMessagePort {

    private final TemplateEngine templateEngine;
    private final ChatEventPublisher chatEventPublisher;

    public MartialArtViewMessageSender(final TemplateEngine templateEngine,
                                       final ChatEventPublisher chatEventPublisher) {
        this.templateEngine = templateEngine;
        this.chatEventPublisher = chatEventPublisher;
    }

    @Override
    public void sendMessage(final MartialArtViewVariables variables) {
        Context context = new Context();
        context.setVariable("mentalGroups", variables.mentalGroups());
        context.setVariable("externalGroups", variables.externalGroups());

        String html = templateEngine.process("gameplay/martial-art-view", context);
        chatEventPublisher.messageToUser(variables.userId(), html);
    }
}
```

- [ ] **Step 2: 템플릿 렌더 테스트 작성**

`StatusMessageSenderTemplateTest`와 동일한 SpringTemplateEngine 설정 패턴을 따른다.

```java
package com.jefflife.mudmk2.gameplay.adapter.out.eventpublisher.chat;

import com.jefflife.mudmk2.gameplay.application.service.model.template.MartialArtViewVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticApplicationContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MartialArtViewMessageSenderTemplateTest {

    @Mock
    private ChatEventPublisher chatEventPublisher;

    private MartialArtViewMessageSender sender;

    @BeforeEach
    void setUp() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(new StaticApplicationContext());
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        doNothing().when(chatEventPublisher).messageToUser(anyLong(), anyString());

        sender = new MartialArtViewMessageSender(engine, chatEventPublisher);
    }

    private String captureHtml(MartialArtViewVariables vars) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        sender.sendMessage(vars);
        verify(chatEventPublisher).messageToUser(anyLong(), captor.capture());
        return captor.getValue();
    }

    @Test
    void render_noLearned_emitsEmptyMessage() {
        String html = captureHtml(new MartialArtViewVariables(1L, List.of(), List.of()));
        assertThat(html).contains("배운 무공이 없습니다.");
        assertThat(html).doesNotContain("[ 심법 ]");
        assertThat(html).doesNotContain("[ 외공 ]");
    }

    @Test
    void render_withMentalAndExternal_emitsBothSections() {
        MartialArtViewVariables.MentalGroup mental = new MartialArtViewVariables.MentalGroup(
                "내공",
                List.of(new MartialArtViewVariables.LearnedMentalLine(
                        "천뢰신공", 3, 3, 0L, true, true,
                        List.of(new MartialArtViewVariables.StatModLine("내공", 6)))));

        MartialArtViewVariables.ExternalGroup external = new MartialArtViewVariables.ExternalGroup(
                "검",
                List.of(new MartialArtViewVariables.LearnedExternalLine(
                        "천뢰검법", 1, 2, 0L, false, false, 1.5, 4, 3, 2)));

        String html = captureHtml(new MartialArtViewVariables(
                1L, List.of(mental), List.of(external)));

        assertThat(html).contains("[ 심법 ]");
        assertThat(html).contains("〈내공〉");
        assertThat(html).contains("천뢰신공");
        assertThat(html).contains("Lv.3/3");
        assertThat(html).contains("[MAX]");
        assertThat(html).contains("☆");
        assertThat(html).contains("+6 내공");
        assertThat(html).contains("Exp 0");

        assertThat(html).contains("[ 외공 ]");
        assertThat(html).contains("〈검〉");
        assertThat(html).contains("천뢰검법");
        assertThat(html).contains("Lv.1/2");
        assertThat(html).contains("×1.50");
        assertThat(html).contains("쿨4s");
        assertThat(html).contains("AP3");
        assertThat(html).contains("MP2");
    }

    @Test
    void render_negativeStatMod_rendersMinusPrefix() {
        MartialArtViewVariables.MentalGroup mental = new MartialArtViewVariables.MentalGroup(
                "내공",
                List.of(new MartialArtViewVariables.LearnedMentalLine(
                        "이름", 1, 1, 0L, true, false,
                        List.of(new MartialArtViewVariables.StatModLine("활력", -2)))));

        String html = captureHtml(new MartialArtViewVariables(
                1L, List.of(mental), List.of()));

        assertThat(html).contains("-2 활력");
        assertThat(html).doesNotContain("+-2");
    }
}
```

- [ ] **Step 3: 테스트 통과 확인**

Run: `./gradlew test --tests MartialArtViewMessageSenderTemplateTest`
Expected: 모두 통과.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/eventpublisher/chat/MartialArtViewMessageSender.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/eventpublisher/chat/MartialArtViewMessageSenderTemplateTest.java
git commit -m "feat(martial-arts): add MartialArtViewMessageSender + template test

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 14: `status.html`에 `[ 장착 무공 ]` 섹션 추가 + StatusMessageSender 변수 set + 템플릿 회귀 테스트

**Files:**
- Modify: `src/main/resources/templates/gameplay/status.html`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/eventpublisher/chat/StatusMessageSender.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/eventpublisher/chat/StatusMessageSenderTemplateTest.java`

- [ ] **Step 1: `status.html`에 무공 섹션 추가**

`status.html`의 마지막 `</div>` (fragment 끝) 바로 위, archery 라인 다음에 추가:

```html
    <div class="line-spacer"></div>
    <div class="msg-room-title">[ 장착 무공 ]</div>

    <div class="line"><span class="label">심법</span></div>
    <div class="line" th:each="slot : ${equippedMartialArts.mentalSlots}">
        &nbsp;&nbsp;<span class="label" th:text="${slot.kindLabel + ': '}"></span>
        <span th:if="${slot.name == null}">(없음)</span>
        <span th:if="${slot.name != null}">
            <span th:text="${slot.name}"></span>
            Lv.<span th:text="${slot.currentLevel}"></span>
            <span th:if="${slot.currentLevel == slot.maxLevel}"> MAX</span>
            <span th:if="${not #lists.isEmpty(slot.effects)}">
                &nbsp;&nbsp;<th:block th:each="e, st : ${slot.effects}"
                    ><span th:text="${(e.value >= 0 ? '+' : '') + e.value + ' ' + e.label}"></span
                    ><span th:if="${not st.last}">, </span></th:block>
            </span>
        </span>
    </div>

    <div class="line"><span class="label">외공</span></div>
    <div class="line" th:each="slot : ${equippedMartialArts.externalSlots}">
        &nbsp;&nbsp;<span class="label" th:text="${slot.slotNumber + '. '}"></span>
        <span th:if="${slot.name == null}">(없음)</span>
        <span th:if="${slot.name != null}">
            <span th:text="${slot.name}"></span>
            (<span th:text="${slot.weaponLabel}"></span>)
            Lv.<span th:text="${slot.currentLevel}"></span>
            <span th:if="${slot.currentLevel == slot.maxLevel}"> MAX</span>
            &nbsp;&nbsp;배율 <span th:text="${#numbers.formatDecimal(slot.damageMultiplier, 1, 2)}"></span>
            &nbsp;쿨<span th:text="${slot.cooldownSeconds}"></span>s
            &nbsp;AP<span th:text="${slot.apCost}"></span>
            &nbsp;MP<span th:text="${slot.mpCost}"></span>
        </span>
    </div>
```

- [ ] **Step 2: `StatusMessageSender`에 변수 set 한 줄 추가**

기존 `StatusMessageSender.sendMessage`의 `context.setVariable("roomName", ...)` 다음 줄에 추가:

```java
        context.setVariable("equippedMartialArts", statusVariables.equippedMartialArts());
```

- [ ] **Step 3: 무공 회귀 테스트 4개 추가**

(`emptyEquipped()` 헬퍼와 `defaultVariables()`/`withVigor()` 시그니처 보정은 Task 5에서 이미 완료. 여기서는 무공 섹션 렌더 검증 테스트만 추가한다.)

`StatusMessageSenderTemplateTest` 클래스 끝(`emptyEquipped()` 헬퍼 정의 위 또는 아래의 적절한 위치)에 새 테스트 메서드 4개를 추가:

```java
    @Test
    void render_emptyEquippedMartialArts_emitsSectionHeaderAndEmptySlots() {
        String html = captureHtml(defaultVariables());
        assertThat(html).contains("[ 장착 무공 ]");
        assertThat(html).contains("심법");
        assertThat(html).contains("외공");
        // 빈 슬롯은 모두 (없음)
        assertThat(html).contains("내공:");
        assertThat(html).contains("경공:");
        assertThat(html).contains("특기:");
        assertThat(html).contains("(없음)");
    }

    @Test
    void render_withEquippedMental_emitsNameLevelAndEffect() {
        StatusVariables d = defaultVariables();
        StatusVariables.EquippedMartialArtsView populated = new StatusVariables.EquippedMartialArtsView(
                java.util.List.of(
                        new StatusVariables.MentalSlotLine("내공", "천뢰신공", 2, 3,
                                java.util.List.of(new StatusVariables.StatModLine("내공", 3))),
                        new StatusVariables.MentalSlotLine("경공", null, null, null, java.util.List.of()),
                        new StatusVariables.MentalSlotLine("특기", null, null, null, java.util.List.of())),
                d.equippedMartialArts().externalSlots());
        StatusVariables vars = new StatusVariables(
                d.userId(), d.playerName(), d.characterClass(), d.gender(), d.state(),
                d.level(), d.experience(), d.nextLevelExp(),
                d.hp(), d.maxHp(), d.mp(), d.maxMp(), d.ap(), d.maxAp(),
                d.vigor(), d.physique(), d.agility(), d.intellect(), d.will(), d.meridian(),
                d.innerPower(), d.specialTechnique(), d.lightStep(), d.fistsAndPalms(),
                d.swordMethod(), d.bladeMethod(), d.longWeapon(), d.esotericWeapon(),
                d.archery(), d.roomName(), populated);

        String html = captureHtml(vars);

        assertThat(html).contains("천뢰신공");
        assertThat(html).contains("Lv.2");
        assertThat(html).contains("+3 내공");
        assertThat(html).doesNotContain(" MAX");  // currentLevel != maxLevel
    }

    @Test
    void render_atMaxMental_emitsMaxLabel() {
        StatusVariables d = defaultVariables();
        StatusVariables.EquippedMartialArtsView populated = new StatusVariables.EquippedMartialArtsView(
                java.util.List.of(
                        new StatusVariables.MentalSlotLine("내공", "천뢰신공", 3, 3, java.util.List.of()),
                        new StatusVariables.MentalSlotLine("경공", null, null, null, java.util.List.of()),
                        new StatusVariables.MentalSlotLine("특기", null, null, null, java.util.List.of())),
                d.equippedMartialArts().externalSlots());
        StatusVariables vars = new StatusVariables(
                d.userId(), d.playerName(), d.characterClass(), d.gender(), d.state(),
                d.level(), d.experience(), d.nextLevelExp(),
                d.hp(), d.maxHp(), d.mp(), d.maxMp(), d.ap(), d.maxAp(),
                d.vigor(), d.physique(), d.agility(), d.intellect(), d.will(), d.meridian(),
                d.innerPower(), d.specialTechnique(), d.lightStep(), d.fistsAndPalms(),
                d.swordMethod(), d.bladeMethod(), d.longWeapon(), d.esotericWeapon(),
                d.archery(), d.roomName(), populated);

        String html = captureHtml(vars);

        assertThat(html).contains("Lv.3");
        assertThat(html).contains("MAX");
    }

    @Test
    void render_withEquippedExternal_emitsWeaponAndStats() {
        StatusVariables d = defaultVariables();
        java.util.List<StatusVariables.ExternalSlotLine> externals = new java.util.ArrayList<>();
        externals.add(new StatusVariables.ExternalSlotLine(
                1, "천뢰검법", "검", 1, 2, 1.5, 4, 3, 2));
        for (int i = 2; i <= 6; i++) {
            externals.add(new StatusVariables.ExternalSlotLine(
                    i, null, null, null, null, null, null, null, null));
        }
        StatusVariables.EquippedMartialArtsView populated = new StatusVariables.EquippedMartialArtsView(
                d.equippedMartialArts().mentalSlots(), externals);
        StatusVariables vars = new StatusVariables(
                d.userId(), d.playerName(), d.characterClass(), d.gender(), d.state(),
                d.level(), d.experience(), d.nextLevelExp(),
                d.hp(), d.maxHp(), d.mp(), d.maxMp(), d.ap(), d.maxAp(),
                d.vigor(), d.physique(), d.agility(), d.intellect(), d.will(), d.meridian(),
                d.innerPower(), d.specialTechnique(), d.lightStep(), d.fistsAndPalms(),
                d.swordMethod(), d.bladeMethod(), d.longWeapon(), d.esotericWeapon(),
                d.archery(), d.roomName(), populated);

        String html = captureHtml(vars);

        assertThat(html).contains("1.");
        assertThat(html).contains("천뢰검법");
        assertThat(html).contains("(검)");
        assertThat(html).contains("Lv.1");
        assertThat(html).contains("배율 1.50");
        assertThat(html).contains("쿨4s");
        assertThat(html).contains("AP3");
        assertThat(html).contains("MP2");
    }
```

- [ ] **Step 4: 모든 변경 컴파일 및 전체 status sender 테스트 통과 확인**

Run: `./gradlew test --tests StatusMessageSenderTemplateTest`
Expected: 모든 기존 + 신규 테스트 통과.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/templates/gameplay/status.html \
        src/main/java/com/jefflife/mudmk2/gameplay/adapter/out/eventpublisher/chat/StatusMessageSender.java \
        src/test/java/com/jefflife/mudmk2/gameplay/adapter/out/eventpublisher/chat/StatusMessageSenderTemplateTest.java
git commit -m "feat(martial-arts): add [ 장착 무공 ] section to status template

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>"
```

---

## Task 15: 전체 빌드 검증 + 수동 점검 가이드

**Files:** (없음 — 검증 단계)

- [ ] **Step 1: 전체 테스트 실행**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, 모든 테스트 통과.

- [ ] **Step 2: 전체 빌드 실행 (compile + test + assemble)**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: 수동 점검 가이드**

(자동화 없음 — 다음 항목을 사용자가 직접 확인하도록 안내)
1. `./gradlew bootRun` 으로 서버 기동.
2. 로그인 후 캐릭터 진입.
3. `상태`/`상태창` 입력 → 새 `[ 장착 무공 ]` 섹션이 보여야 한다(빈 슬롯은 `(없음)`).
4. 어드민 페이지에서 캐릭터에 심법/외공을 학습·장착시킨 뒤 다시 상태창 입력 → 무공 이름/레벨/효과가 표시되어야 한다.
5. `무공`/`무공창` 입력:
   - 학습이 없으면 "배운 무공이 없습니다." 한 줄.
   - 학습 후에는 종류별 그룹 출력, 장착된 항목에 `☆`.

- [ ] **Step 4: 브랜치 푸시 안내**

머지 또는 PR 전, 다음 명령으로 원격에 브랜치를 푸시한다:
```
git push -u origin feature/martial-arts-view
```

---

## Self-Review Notes

- **Spec coverage**: 스펙 §1~§9 모든 항목이 태스크에 매핑되었다.
  - §2 도메인 effectAt → Task 1
  - §3 명령 등록 (CommandDictionary/Command/Parser/Executor) → Task 3, 10, 11
  - §4.1 StatusVariables 확장 → Task 5
  - §4.2 MartialArtViewVariables → Task 4
  - §5.1 UseCase/Port + §5.4 두 서비스 → Task 8, 9
  - §5.2 MartialArtViewMapper → Task 6, 7
  - §5.3 ItemDisplayLabels(MentalMethodKind) → Task 2
  - §6.1/6.2 MessageSender 변경 + 신규 → Task 13, 14
  - §6.3 status.html / §6.4 martial-art-view.html → Task 12, 14
  - §9 테스트 전략 → 각 태스크의 테스트 단계
  - §10 브랜치 전략 → Task 0
- **Type consistency**: `EquippedMartialArtsView`, `MentalSlotLine`, `ExternalSlotLine`, `StatModLine`(StatusVariables 내부), `LearnedMentalLine`/`LearnedExternalLine`/`MentalGroup`/`ExternalGroup`/`StatModLine`(MartialArtViewVariables 내부) — 모두 일관됨.
- **Placeholders**: 본 plan에는 추후 채워야 할 항목 없음.
