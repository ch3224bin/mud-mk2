# 무공(심법/외공) Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 심법 3종 / 외공 6종 무공 시스템의 템플릿·학습·장착 모델과 어드민 페이지를 추가한다 (전투 적용·경험치·필살기 트리거는 후속 페이즈).

**Architecture:** 기존 hexagonal DDD 구조를 따른다. `gamedata` 도메인에 `martialart` 패키지를 신설하고 `MentalMethodTemplate` / `ExternalArtTemplate` / `LearnedMentalMethod` / `LearnedExternalArt` / `EquippedMartialArts` 5개 엔티티를 둔다. 레벨별 효과는 JSON `AttributeConverter`로 단일 컬럼에 저장. 슬롯 규칙(심법 kind당 1, 외공 최대 6)은 `EquippedMartialArts` 엔티티에서 강제. 어드민은 `/api/v1/mental-method-templates`, `/api/v1/external-art-templates`, `/api/v1/player-characters/{pcId}/martial-arts` 3개 REST 컨트롤러 + Thymeleaf 페이지 3장.

**Tech Stack:** Java 21 / Spring Boot / Spring Data JPA / Jackson / Thymeleaf / Lombok / JUnit 5 / AssertJ / MockMvc.

**Spec:** `docs/superpowers/specs/2026-05-16-martial-arts-design.md`

---

## Conventions

- 작업 디렉토리: `/Users/ch3224bin/dev/apps/ch3223bin/ddd7/mud-mk2`
- 빌드: `./gradlew build` (테스트 실행 포함). 단위 테스트만은 `./gradlew test --tests <FQN>`.
- 새 패키지: `com.jefflife.mudmk2.gamedata.application.domain.model.martialart`
- 커밋 컨벤션: `feat(martial-arts): <설명>`, `test(martial-arts): <설명>`, `refactor(martial-arts): <설명>`
- 모든 도메인 엔티티는 Lombok `@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)` 사용 (기존 패턴).
- 테스트 위치: 단위 테스트는 동일 패키지 미러 (`src/test/java/...`)
- 매 단계 commit 빈도: Task 1개 = 커밋 1개 (테스트 + 구현 함께).

---

## Task 1: MentalMethodKind enum + MentalMethodLevelEffect record

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodKind.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodLevelEffect.java`

- [ ] **Step 1: Create the package directory**

```bash
mkdir -p src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart
```

- [ ] **Step 2: Create MentalMethodKind enum**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

public enum MentalMethodKind {
    INNER_POWER, LIGHT_STEP, SPECIAL_TECHNIQUE
}
```

- [ ] **Step 3: Create MentalMethodLevelEffect record**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;

import java.util.List;

public record MentalMethodLevelEffect(
        int level,
        List<StatModifier> statModifiers
) {
    public MentalMethodLevelEffect {
        if (level < 1) throw new IllegalArgumentException("level must be >= 1: " + level);
        statModifiers = statModifiers == null ? List.of() : List.copyOf(statModifiers);
    }
}
```

- [ ] **Step 4: Compile check**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodKind.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodLevelEffect.java
git commit -m "feat(martial-arts): MentalMethodKind enum + MentalMethodLevelEffect record"
```

---

## Task 2: MentalMethodLevelEffectsConverter (Jackson)

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodLevelEffectsConverter.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodLevelEffectsConverterTest.java`

- [ ] **Step 1: Write failing test**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MentalMethodLevelEffectsConverterTest {

    private final MentalMethodLevelEffectsConverter converter = new MentalMethodLevelEffectsConverter();

    @Test
    void roundTrip_preservesLevelsAndModifiers() {
        List<MentalMethodLevelEffect> input = List.of(
                new MentalMethodLevelEffect(1, List.of(new StatModifier(StatType.INNER_POWER, 5))),
                new MentalMethodLevelEffect(2, List.of(
                        new StatModifier(StatType.INNER_POWER, 10),
                        new StatModifier(StatType.MERIDIAN, 2)))
        );

        String json = converter.convertToDatabaseColumn(input);
        List<MentalMethodLevelEffect> restored = converter.convertToEntityAttribute(json);

        assertThat(restored).isEqualTo(input);
    }

    @Test
    void null_roundsTripToEmptyList() {
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo("[]");
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        assertThat(converter.convertToEntityAttribute("")).isEmpty();
    }
}
```

- [ ] **Step 2: Run test to confirm failure**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodLevelEffectsConverterTest"`
Expected: FAIL (class does not exist)

- [ ] **Step 3: Implement converter**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Converter
public class MentalMethodLevelEffectsConverter
        implements AttributeConverter<List<MentalMethodLevelEffect>, String> {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<MentalMethodLevelEffect>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<MentalMethodLevelEffect> attribute) {
        if (attribute == null) return "[]";
        return MAPPER.writeValueAsString(attribute);
    }

    @Override
    public List<MentalMethodLevelEffect> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return List.of();
        return MAPPER.readValue(dbData, TYPE_REF);
    }
}
```

- [ ] **Step 4: Run test to confirm pass**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodLevelEffectsConverterTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodLevelEffectsConverter.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodLevelEffectsConverterTest.java
git commit -m "feat(martial-arts): MentalMethodLevelEffectsConverter — JSON round-trip"
```

---

## Task 3: MentalMethodTemplate entity

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodTemplate.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodTemplateTest.java`

- [ ] **Step 1: Write failing test**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MentalMethodTemplateTest {

    @Test
    void create_buildsTemplate() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("천뢰신공")
                .description("내공 심법")
                .kind(MentalMethodKind.INNER_POWER)
                .maxLevel(2)
                .levelEffects(List.of(
                        new MentalMethodLevelEffect(1, List.of(new StatModifier(StatType.INNER_POWER, 3))),
                        new MentalMethodLevelEffect(2, List.of(new StatModifier(StatType.INNER_POWER, 6)))))
                .build();

        assertThat(t.getName()).isEqualTo("천뢰신공");
        assertThat(t.getKind()).isEqualTo(MentalMethodKind.INNER_POWER);
        assertThat(t.getMaxLevel()).isEqualTo(2);
        assertThat(t.getLevelEffects()).hasSize(2);
    }

    @Test
    void create_whenMaxLevelLessThan1_throws() {
        assertThatThrownBy(() -> MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(0).levelEffects(List.of()).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_whenLevelEffectsSizeNotEqMaxLevel_throws() {
        assertThatThrownBy(() -> MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(2)
                .levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_whenLevelsNotConsecutive_throws() {
        assertThatThrownBy(() -> MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(2)
                .levelEffects(List.of(
                        new MentalMethodLevelEffect(1, List.of()),
                        new MentalMethodLevelEffect(3, List.of())))
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_replacesAllFields() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("old").description("old").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(1).levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build();

        t.update("new", "new desc", MentalMethodKind.LIGHT_STEP, 1,
                List.of(new MentalMethodLevelEffect(1, List.of(new StatModifier(StatType.LIGHT_STEP, 4)))));

        assertThat(t.getName()).isEqualTo("new");
        assertThat(t.getKind()).isEqualTo(MentalMethodKind.LIGHT_STEP);
        assertThat(t.getLevelEffects().get(0).statModifiers()).hasSize(1);
    }
}
```

- [ ] **Step 2: Run test to confirm failure**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplateTest"`
Expected: FAIL (class does not exist)

- [ ] **Step 3: Implement entity**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "mental_method_template")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentalMethodTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MentalMethodKind kind;

    @Column(nullable = false)
    private int maxLevel;

    @Convert(converter = MentalMethodLevelEffectsConverter.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private List<MentalMethodLevelEffect> levelEffects;

    private MentalMethodTemplate(String name, String description, MentalMethodKind kind,
                                 int maxLevel, List<MentalMethodLevelEffect> levelEffects) {
        validate(maxLevel, levelEffects);
        this.name = name;
        this.description = description;
        this.kind = kind;
        this.maxLevel = maxLevel;
        this.levelEffects = List.copyOf(levelEffects);
    }

    public static Builder builder() { return new Builder(); }

    public void update(String name, String description, MentalMethodKind kind,
                       int maxLevel, List<MentalMethodLevelEffect> levelEffects) {
        validate(maxLevel, levelEffects);
        this.name = name;
        this.description = description;
        this.kind = kind;
        this.maxLevel = maxLevel;
        this.levelEffects = List.copyOf(levelEffects);
    }

    private static void validate(int maxLevel, List<MentalMethodLevelEffect> levelEffects) {
        if (maxLevel < 1) {
            throw new IllegalArgumentException("maxLevel must be >= 1: " + maxLevel);
        }
        if (levelEffects == null || levelEffects.size() != maxLevel) {
            throw new IllegalArgumentException(
                    "levelEffects.size must equal maxLevel: expected " + maxLevel
                            + ", actual " + (levelEffects == null ? 0 : levelEffects.size()));
        }
        for (int i = 0; i < levelEffects.size(); i++) {
            int expected = i + 1;
            if (levelEffects.get(i).level() != expected) {
                throw new IllegalArgumentException(
                        "levelEffects must be ordered 1..maxLevel; index " + i
                                + " expected level " + expected
                                + ", got " + levelEffects.get(i).level());
            }
        }
    }

    public static class Builder {
        private String name;
        private String description;
        private MentalMethodKind kind;
        private int maxLevel;
        private List<MentalMethodLevelEffect> levelEffects;

        public Builder name(String v) { this.name = v; return this; }
        public Builder description(String v) { this.description = v; return this; }
        public Builder kind(MentalMethodKind v) { this.kind = v; return this; }
        public Builder maxLevel(int v) { this.maxLevel = v; return this; }
        public Builder levelEffects(List<MentalMethodLevelEffect> v) { this.levelEffects = v; return this; }

        public MentalMethodTemplate build() {
            return new MentalMethodTemplate(name, description, kind, maxLevel, levelEffects);
        }
    }
}
```

- [ ] **Step 4: Run test to confirm pass**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplateTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodTemplate.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/MentalMethodTemplateTest.java
git commit -m "feat(martial-arts): MentalMethodTemplate entity + validation"
```

---

## Task 4: ExternalArtLevelEffect record + Converter

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtLevelEffect.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtLevelEffectsConverter.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtLevelEffectsConverterTest.java`

- [ ] **Step 1: Write failing test**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalArtLevelEffectsConverterTest {

    private final ExternalArtLevelEffectsConverter converter = new ExternalArtLevelEffectsConverter();

    @Test
    void roundTrip_preservesAllFields() {
        List<ExternalArtLevelEffect> input = List.of(
                new ExternalArtLevelEffect(1, 1.1, 6, 5, 0),
                new ExternalArtLevelEffect(2, 1.25, 5, 5, 3)
        );

        String json = converter.convertToDatabaseColumn(input);
        List<ExternalArtLevelEffect> restored = converter.convertToEntityAttribute(json);

        assertThat(restored).isEqualTo(input);
    }

    @Test
    void recordValidation_rejectsNegativeValues() {
        assertThatThrownBy(() -> new ExternalArtLevelEffect(1, -0.1, 5, 5, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExternalArtLevelEffect(1, 1.0, -1, 5, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExternalArtLevelEffect(1, 1.0, 5, -1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExternalArtLevelEffect(1, 1.0, 5, 5, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void null_or_blank_roundsTripToEmptyList() {
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        assertThat(converter.convertToEntityAttribute("")).isEmpty();
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo("[]");
    }
}
```

- [ ] **Step 2: Run test to confirm failure**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtLevelEffectsConverterTest"`
Expected: FAIL

- [ ] **Step 3: Create record**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

public record ExternalArtLevelEffect(
        int level,
        double damageMultiplier,
        int cooldownSeconds,
        int apCost,
        int mpCost
) {
    public ExternalArtLevelEffect {
        if (level < 1) throw new IllegalArgumentException("level must be >= 1: " + level);
        if (damageMultiplier < 0) throw new IllegalArgumentException("damageMultiplier must be >= 0: " + damageMultiplier);
        if (cooldownSeconds < 0) throw new IllegalArgumentException("cooldownSeconds must be >= 0: " + cooldownSeconds);
        if (apCost < 0) throw new IllegalArgumentException("apCost must be >= 0: " + apCost);
        if (mpCost < 0) throw new IllegalArgumentException("mpCost must be >= 0: " + mpCost);
    }
}
```

- [ ] **Step 4: Create converter**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Converter
public class ExternalArtLevelEffectsConverter
        implements AttributeConverter<List<ExternalArtLevelEffect>, String> {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<ExternalArtLevelEffect>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<ExternalArtLevelEffect> attribute) {
        if (attribute == null) return "[]";
        return MAPPER.writeValueAsString(attribute);
    }

    @Override
    public List<ExternalArtLevelEffect> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return List.of();
        return MAPPER.readValue(dbData, TYPE_REF);
    }
}
```

- [ ] **Step 5: Run tests, confirm pass, commit**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtLevelEffectsConverterTest"
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtLevelEffect.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtLevelEffectsConverter.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtLevelEffectsConverterTest.java
git commit -m "feat(martial-arts): ExternalArtLevelEffect record + converter"
```

---

## Task 5: ExternalArtTemplate entity

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtTemplate.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtTemplateTest.java`

- [ ] **Step 1: Write failing test**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalArtTemplateTest {

    @Test
    void create_buildsTemplate() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("발검술")
                .description("기초 검법")
                .weaponType(WeaponType.SWORD)
                .maxLevel(2)
                .levelEffects(List.of(
                        new ExternalArtLevelEffect(1, 1.1, 6, 5, 0),
                        new ExternalArtLevelEffect(2, 1.2, 5, 5, 0)))
                .build();

        assertThat(t.getWeaponType()).isEqualTo(WeaponType.SWORD);
        assertThat(t.getLevelEffects()).hasSize(2);
    }

    @Test
    void create_whenLevelEffectsSizeNotEqMaxLevel_throws() {
        assertThatThrownBy(() -> ExternalArtTemplate.builder()
                .name("x").description("x").weaponType(WeaponType.SWORD)
                .maxLevel(2)
                .levelEffects(List.of(new ExternalArtLevelEffect(1, 1.0, 1, 1, 0)))
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_replacesAllFields() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("old").description("old").weaponType(WeaponType.SWORD)
                .maxLevel(1).levelEffects(List.of(new ExternalArtLevelEffect(1, 1.0, 1, 1, 0)))
                .build();

        t.update("new", "new desc", WeaponType.FIST, 1,
                List.of(new ExternalArtLevelEffect(1, 1.5, 2, 2, 1)));

        assertThat(t.getName()).isEqualTo("new");
        assertThat(t.getWeaponType()).isEqualTo(WeaponType.FIST);
        assertThat(t.getLevelEffects().get(0).damageMultiplier()).isEqualTo(1.5);
    }
}
```

- [ ] **Step 2: Run, confirm failure**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplateTest"`
Expected: FAIL

- [ ] **Step 3: Implement entity**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "external_art_template")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalArtTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeaponType weaponType;

    @Column(nullable = false)
    private int maxLevel;

    @Convert(converter = ExternalArtLevelEffectsConverter.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private List<ExternalArtLevelEffect> levelEffects;

    private ExternalArtTemplate(String name, String description, WeaponType weaponType,
                                int maxLevel, List<ExternalArtLevelEffect> levelEffects) {
        validate(maxLevel, levelEffects);
        this.name = name;
        this.description = description;
        this.weaponType = weaponType;
        this.maxLevel = maxLevel;
        this.levelEffects = List.copyOf(levelEffects);
    }

    public static Builder builder() { return new Builder(); }

    public void update(String name, String description, WeaponType weaponType,
                       int maxLevel, List<ExternalArtLevelEffect> levelEffects) {
        validate(maxLevel, levelEffects);
        this.name = name;
        this.description = description;
        this.weaponType = weaponType;
        this.maxLevel = maxLevel;
        this.levelEffects = List.copyOf(levelEffects);
    }

    private static void validate(int maxLevel, List<ExternalArtLevelEffect> levelEffects) {
        if (maxLevel < 1) {
            throw new IllegalArgumentException("maxLevel must be >= 1: " + maxLevel);
        }
        if (levelEffects == null || levelEffects.size() != maxLevel) {
            throw new IllegalArgumentException(
                    "levelEffects.size must equal maxLevel: expected " + maxLevel
                            + ", actual " + (levelEffects == null ? 0 : levelEffects.size()));
        }
        for (int i = 0; i < levelEffects.size(); i++) {
            int expected = i + 1;
            if (levelEffects.get(i).level() != expected) {
                throw new IllegalArgumentException(
                        "levelEffects must be ordered 1..maxLevel; index " + i
                                + " expected level " + expected
                                + ", got " + levelEffects.get(i).level());
            }
        }
    }

    public static class Builder {
        private String name;
        private String description;
        private WeaponType weaponType;
        private int maxLevel;
        private List<ExternalArtLevelEffect> levelEffects;

        public Builder name(String v) { this.name = v; return this; }
        public Builder description(String v) { this.description = v; return this; }
        public Builder weaponType(WeaponType v) { this.weaponType = v; return this; }
        public Builder maxLevel(int v) { this.maxLevel = v; return this; }
        public Builder levelEffects(List<ExternalArtLevelEffect> v) { this.levelEffects = v; return this; }

        public ExternalArtTemplate build() {
            return new ExternalArtTemplate(name, description, weaponType, maxLevel, levelEffects);
        }
    }
}
```

- [ ] **Step 4: Run tests, confirm pass, commit**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplateTest"
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtTemplate.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/ExternalArtTemplateTest.java
git commit -m "feat(martial-arts): ExternalArtTemplate entity + validation"
```

---

## Task 6: LearnedMentalMethod entity

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/LearnedMentalMethod.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/LearnedMentalMethodTest.java`

- [ ] **Step 1: Write failing test**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LearnedMentalMethodTest {

    @Test
    void create_startsAtLevel1Exp0() {
        UUID pcId = UUID.randomUUID();
        LearnedMentalMethod l = LearnedMentalMethod.create(pcId, 42L);

        assertThat(l.getPlayerCharacterId()).isEqualTo(pcId);
        assertThat(l.getMentalMethodTemplateId()).isEqualTo(42L);
        assertThat(l.getCurrentLevel()).isEqualTo(1);
        assertThat(l.getCurrentExp()).isEqualTo(0L);
    }
}
```

- [ ] **Step 2: Run, confirm failure**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedMentalMethodTest"`
Expected: FAIL

- [ ] **Step 3: Implement entity**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "learned_mental_method",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_learned_mental_pc_template",
               columnNames = {"player_character_id", "mental_method_template_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearnedMentalMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "player_character_id", nullable = false)
    private UUID playerCharacterId;

    @Column(name = "mental_method_template_id", nullable = false)
    private Long mentalMethodTemplateId;

    @Column(nullable = false)
    private int currentLevel;

    @Column(nullable = false)
    private long currentExp;

    private LearnedMentalMethod(UUID playerCharacterId, Long mentalMethodTemplateId) {
        this.playerCharacterId = playerCharacterId;
        this.mentalMethodTemplateId = mentalMethodTemplateId;
        this.currentLevel = 1;
        this.currentExp = 0L;
    }

    public static LearnedMentalMethod create(UUID playerCharacterId, Long mentalMethodTemplateId) {
        return new LearnedMentalMethod(playerCharacterId, mentalMethodTemplateId);
    }
}
```

- [ ] **Step 4: Run tests, commit**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedMentalMethodTest"
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/LearnedMentalMethod.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/LearnedMentalMethodTest.java
git commit -m "feat(martial-arts): LearnedMentalMethod entity"
```

---

## Task 7: LearnedExternalArt entity

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/LearnedExternalArt.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/LearnedExternalArtTest.java`

- [ ] **Step 1: Write failing test**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LearnedExternalArtTest {

    @Test
    void create_startsAtLevel1Exp0() {
        UUID pcId = UUID.randomUUID();
        LearnedExternalArt l = LearnedExternalArt.create(pcId, 17L);

        assertThat(l.getPlayerCharacterId()).isEqualTo(pcId);
        assertThat(l.getExternalArtTemplateId()).isEqualTo(17L);
        assertThat(l.getCurrentLevel()).isEqualTo(1);
        assertThat(l.getCurrentExp()).isEqualTo(0L);
    }
}
```

- [ ] **Step 2: Run, confirm failure**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedExternalArtTest"`
Expected: FAIL

- [ ] **Step 3: Implement entity**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "learned_external_art",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_learned_external_pc_template",
               columnNames = {"player_character_id", "external_art_template_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearnedExternalArt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "player_character_id", nullable = false)
    private UUID playerCharacterId;

    @Column(name = "external_art_template_id", nullable = false)
    private Long externalArtTemplateId;

    @Column(nullable = false)
    private int currentLevel;

    @Column(nullable = false)
    private long currentExp;

    private LearnedExternalArt(UUID playerCharacterId, Long externalArtTemplateId) {
        this.playerCharacterId = playerCharacterId;
        this.externalArtTemplateId = externalArtTemplateId;
        this.currentLevel = 1;
        this.currentExp = 0L;
    }

    public static LearnedExternalArt create(UUID playerCharacterId, Long externalArtTemplateId) {
        return new LearnedExternalArt(playerCharacterId, externalArtTemplateId);
    }
}
```

- [ ] **Step 4: Run tests, commit**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedExternalArtTest"
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/LearnedExternalArt.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/LearnedExternalArtTest.java
git commit -m "feat(martial-arts): LearnedExternalArt entity"
```

---

## Task 8: Exception classes

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/exception/AlreadyLearnedException.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/exception/NotLearnedException.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/exception/MartialArtSlotFullException.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/exception/MartialArtTemplateInUseException.java`

- [ ] **Step 1: Create exceptions**

```java
package com.jefflife.mudmk2.gamedata.application.service.exception;

public class AlreadyLearnedException extends RuntimeException {
    public AlreadyLearnedException(String message) { super(message); }
}
```

```java
package com.jefflife.mudmk2.gamedata.application.service.exception;

public class NotLearnedException extends RuntimeException {
    public NotLearnedException(String message) { super(message); }
}
```

```java
package com.jefflife.mudmk2.gamedata.application.service.exception;

public class MartialArtSlotFullException extends RuntimeException {
    public MartialArtSlotFullException(String message) { super(message); }
}
```

```java
package com.jefflife.mudmk2.gamedata.application.service.exception;

public class MartialArtTemplateInUseException extends RuntimeException {
    public MartialArtTemplateInUseException(String message) { super(message); }
}
```

- [ ] **Step 2: Compile & commit**

```bash
./gradlew compileJava
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/exception/AlreadyLearnedException.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/service/exception/NotLearnedException.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/service/exception/MartialArtSlotFullException.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/service/exception/MartialArtTemplateInUseException.java
git commit -m "feat(martial-arts): domain-level exceptions"
```

---

## Task 9: EquippedMartialArts entity (slot rules)

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/EquippedMartialArts.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/EquippedMartialArtsTest.java`

- [ ] **Step 1: Write failing tests**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtSlotFullException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EquippedMartialArtsTest {

    @Test
    void equipMental_putsInKindSlot() {
        EquippedMartialArts e = EquippedMartialArts.create();
        UUID learned = UUID.randomUUID();

        e.equipMental(MentalMethodKind.INNER_POWER, learned);

        assertThat(e.getMentalSlots()).containsEntry(MentalMethodKind.INNER_POWER, learned);
    }

    @Test
    void equipMental_sameKindAgain_replacesPrevious() {
        EquippedMartialArts e = EquippedMartialArts.create();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        e.equipMental(MentalMethodKind.INNER_POWER, first);
        e.equipMental(MentalMethodKind.INNER_POWER, second);

        assertThat(e.getMentalSlots()).containsEntry(MentalMethodKind.INNER_POWER, second);
        assertThat(e.getMentalSlots()).hasSize(1);
    }

    @Test
    void unequipMental_removesAndReturnsPrevious() {
        EquippedMartialArts e = EquippedMartialArts.create();
        UUID learned = UUID.randomUUID();
        e.equipMental(MentalMethodKind.INNER_POWER, learned);

        assertThat(e.unequipMental(MentalMethodKind.INNER_POWER)).contains(learned);
        assertThat(e.getMentalSlots()).doesNotContainKey(MentalMethodKind.INNER_POWER);
    }

    @Test
    void equipExternal_addsToSlots() {
        EquippedMartialArts e = EquippedMartialArts.create();
        UUID learned = UUID.randomUUID();

        e.equipExternal(learned);

        assertThat(e.getExternalSlots()).containsExactly(learned);
    }

    @Test
    void equipExternal_duplicateIsNoOp() {
        EquippedMartialArts e = EquippedMartialArts.create();
        UUID learned = UUID.randomUUID();

        e.equipExternal(learned);
        e.equipExternal(learned);

        assertThat(e.getExternalSlots()).containsExactly(learned);
    }

    @Test
    void equipExternal_whenFull_throws() {
        EquippedMartialArts e = EquippedMartialArts.create();
        for (int i = 0; i < EquippedMartialArts.EXTERNAL_SLOT_MAX; i++) {
            e.equipExternal(UUID.randomUUID());
        }

        assertThatThrownBy(() -> e.equipExternal(UUID.randomUUID()))
                .isInstanceOf(MartialArtSlotFullException.class);
    }

    @Test
    void unequipExternal_removesById_returnsTrue() {
        EquippedMartialArts e = EquippedMartialArts.create();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        e.equipExternal(a);
        e.equipExternal(b);

        assertThat(e.unequipExternal(a)).isTrue();
        assertThat(e.getExternalSlots()).containsExactly(b);
    }

    @Test
    void unequipExternal_notPresent_returnsFalse() {
        EquippedMartialArts e = EquippedMartialArts.create();
        assertThat(e.unequipExternal(UUID.randomUUID())).isFalse();
    }
}
```

- [ ] **Step 2: Run, confirm failure**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.EquippedMartialArtsTest"`
Expected: FAIL

- [ ] **Step 3: Implement entity**

```java
package com.jefflife.mudmk2.gamedata.application.domain.model.martialart;

import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtSlotFullException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "equipped_martial_arts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquippedMartialArts {

    public static final int EXTERNAL_SLOT_MAX = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ElementCollection
    @CollectionTable(name = "equipped_mental_slot",
                     joinColumns = @JoinColumn(name = "equipped_martial_arts_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "kind")
    @Column(name = "learned_mental_method_id")
    private Map<MentalMethodKind, UUID> mentalSlots = new EnumMap<>(MentalMethodKind.class);

    @ElementCollection
    @CollectionTable(name = "equipped_external_slot",
                     joinColumns = @JoinColumn(name = "equipped_martial_arts_id"))
    @Column(name = "learned_external_art_id")
    private List<UUID> externalSlots = new ArrayList<>();

    public static EquippedMartialArts create() {
        return new EquippedMartialArts();
    }

    public void equipMental(MentalMethodKind kind, UUID learnedId) {
        mentalSlots.put(kind, learnedId);
    }

    public Optional<UUID> unequipMental(MentalMethodKind kind) {
        return Optional.ofNullable(mentalSlots.remove(kind));
    }

    public void equipExternal(UUID learnedId) {
        if (externalSlots.contains(learnedId)) return;
        if (externalSlots.size() >= EXTERNAL_SLOT_MAX) {
            throw new MartialArtSlotFullException(
                    "external slot full (max " + EXTERNAL_SLOT_MAX + ")");
        }
        externalSlots.add(learnedId);
    }

    public boolean unequipExternal(UUID learnedId) {
        return externalSlots.remove(learnedId);
    }

    public void initializeAssociatedEntities() {
        mentalSlots.size();
        externalSlots.size();
    }
}
```

- [ ] **Step 4: Run tests, commit**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.domain.model.martialart.EquippedMartialArtsTest"
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/EquippedMartialArts.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/martialart/EquippedMartialArtsTest.java
git commit -m "feat(martial-arts): EquippedMartialArts entity with slot rules"
```

---

## Task 10: Wire EquippedMartialArts into PlayerCharacter

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/PlayerCharacter.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/PlayerCharacterService.java`

PlayerCharacter constructor takes 10 args today (id, baseCharacterInfo, playableCharacterInfo, userId, nickname, characterClass, online, lastActiveAt, inventory, equippedItems). Add `equippedMartialArts` as the 11th. Existing callers: `PlayerCharacterService.createCharacter`.

- [ ] **Step 1: Add field + getter + constructor arg in PlayerCharacter**

Open `PlayerCharacter.java`. Below the `equippedItems` field add:

```java
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipped_martial_arts_id")
    private com.jefflife.mudmk2.gamedata.application.domain.model.martialart.EquippedMartialArts equippedMartialArts;
```

Modify the constructor signature to accept `EquippedMartialArts equippedMartialArts` as the final parameter and assign `this.equippedMartialArts = equippedMartialArts;`.

Update `initializeAssociatedEntities()`:

```java
    public void initializeAssociatedEntities() {
        this.inventory.initializeAssociatedEntities();
        this.equippedItems.initializeAssociatedEntities();
        this.equippedMartialArts.initializeAssociatedEntities();
    }
```

- [ ] **Step 2: Update PlayerCharacterService.createCharacter to construct EquippedMartialArts.create()**

In `PlayerCharacterService.createCharacter(...)`, change the `new PlayerCharacter(...)` call to include `EquippedMartialArts.create()` as the trailing argument. Add the import.

```java
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.EquippedMartialArts;
...
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
        EquippedItems.create(),
        EquippedMartialArts.create()
);
```

- [ ] **Step 3: Find every other PlayerCharacter constructor call**

Run: `grep -rn "new PlayerCharacter(" src/`
Expected callers (verify): `PlayerCharacterService`. For every other caller (tests, fixtures), append `, EquippedMartialArts.create()` to the constructor call.

Common fixtures to check: `src/test/java/com/jefflife/mudmk2/common/fixture/GameTestFixture.java` — patch if it constructs PlayerCharacter directly.

- [ ] **Step 4: Run full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL. If existing tests fail because they construct PlayerCharacter through a different path, patch them.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat(martial-arts): wire EquippedMartialArts into PlayerCharacter"
```

---

## Task 11: Repositories (4 interfaces)

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/MentalMethodTemplateRepository.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/ExternalArtTemplateRepository.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/LearnedMentalMethodRepository.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/LearnedExternalArtRepository.java`

- [ ] **Step 1: MentalMethodTemplateRepository**

```java
package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplate;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MentalMethodTemplateRepository extends CrudRepository<MentalMethodTemplate, Long> {
    List<MentalMethodTemplate> findAllBy();
    List<MentalMethodTemplate> findByKind(MentalMethodKind kind);
    List<MentalMethodTemplate> findByNameContaining(String name);
}
```

- [ ] **Step 2: ExternalArtTemplateRepository**

```java
package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplate;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ExternalArtTemplateRepository extends CrudRepository<ExternalArtTemplate, Long> {
    List<ExternalArtTemplate> findAllBy();
    List<ExternalArtTemplate> findByWeaponType(WeaponType weaponType);
    List<ExternalArtTemplate> findByNameContaining(String name);
}
```

- [ ] **Step 3: LearnedMentalMethodRepository**

```java
package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedMentalMethod;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnedMentalMethodRepository extends CrudRepository<LearnedMentalMethod, UUID> {
    boolean existsByPlayerCharacterIdAndMentalMethodTemplateId(UUID pcId, Long templateId);
    boolean existsByMentalMethodTemplateId(Long templateId);
    List<LearnedMentalMethod> findAllByPlayerCharacterId(UUID pcId);
    Optional<LearnedMentalMethod> findByIdAndPlayerCharacterId(UUID id, UUID pcId);
}
```

- [ ] **Step 4: LearnedExternalArtRepository**

```java
package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedExternalArt;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnedExternalArtRepository extends CrudRepository<LearnedExternalArt, UUID> {
    boolean existsByPlayerCharacterIdAndExternalArtTemplateId(UUID pcId, Long templateId);
    boolean existsByExternalArtTemplateId(Long templateId);
    List<LearnedExternalArt> findAllByPlayerCharacterId(UUID pcId);
    Optional<LearnedExternalArt> findByIdAndPlayerCharacterId(UUID id, UUID pcId);
}
```

- [ ] **Step 5: Build & commit**

```bash
./gradlew compileJava
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/MentalMethodTemplateRepository.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/ExternalArtTemplateRepository.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/LearnedMentalMethodRepository.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/LearnedExternalArtRepository.java
git commit -m "feat(martial-arts): 4 required repositories (Spring Data)"
```

---

## Task 12: Repository integration test (@DataJpaTest)

**Files:**
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/service/required/MartialArtRepositoryDataJpaTest.java`

- [ ] **Step 1: Write test that exercises both converters and exists* queries**

```java
package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({MentalMethodLevelEffectsConverter.class, ExternalArtLevelEffectsConverter.class})
class MartialArtRepositoryDataJpaTest {

    @Autowired MentalMethodTemplateRepository mentalRepo;
    @Autowired ExternalArtTemplateRepository externalRepo;
    @Autowired LearnedMentalMethodRepository learnedMentalRepo;
    @Autowired LearnedExternalArtRepository learnedExternalRepo;

    @Test
    void mentalMethodTemplate_persistsLevelEffectsRoundTrip() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("천뢰신공").description("d").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(2)
                .levelEffects(List.of(
                        new MentalMethodLevelEffect(1, List.of(new StatModifier(StatType.INNER_POWER, 3))),
                        new MentalMethodLevelEffect(2, List.of(new StatModifier(StatType.INNER_POWER, 7)))))
                .build();
        Long id = mentalRepo.save(t).getId();

        MentalMethodTemplate loaded = mentalRepo.findById(id).orElseThrow();
        assertThat(loaded.getLevelEffects()).hasSize(2);
        assertThat(loaded.getLevelEffects().get(1).statModifiers().get(0).getValue()).isEqualTo(7);
    }

    @Test
    void externalArtTemplate_persistsLevelEffectsRoundTrip() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("발검술").description("d").weaponType(WeaponType.SWORD)
                .maxLevel(1)
                .levelEffects(List.of(new ExternalArtLevelEffect(1, 1.2, 5, 5, 0)))
                .build();
        Long id = externalRepo.save(t).getId();

        ExternalArtTemplate loaded = externalRepo.findById(id).orElseThrow();
        assertThat(loaded.getLevelEffects().get(0).damageMultiplier()).isEqualTo(1.2);
    }

    @Test
    void learnedMentalMethod_existsByPlayerAndTemplate() {
        UUID pcId = UUID.randomUUID();
        learnedMentalRepo.save(LearnedMentalMethod.create(pcId, 100L));

        assertThat(learnedMentalRepo.existsByPlayerCharacterIdAndMentalMethodTemplateId(pcId, 100L)).isTrue();
        assertThat(learnedMentalRepo.existsByPlayerCharacterIdAndMentalMethodTemplateId(pcId, 200L)).isFalse();
        assertThat(learnedMentalRepo.existsByMentalMethodTemplateId(100L)).isTrue();
    }

    @Test
    void learnedExternalArt_existsByPlayerAndTemplate() {
        UUID pcId = UUID.randomUUID();
        learnedExternalRepo.save(LearnedExternalArt.create(pcId, 555L));

        assertThat(learnedExternalRepo.existsByPlayerCharacterIdAndExternalArtTemplateId(pcId, 555L)).isTrue();
        assertThat(learnedExternalRepo.existsByExternalArtTemplateId(555L)).isTrue();
    }
}
```

- [ ] **Step 2: Run, confirm pass**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.service.required.MartialArtRepositoryDataJpaTest"`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/jefflife/mudmk2/gamedata/application/service/required/MartialArtRepositoryDataJpaTest.java
git commit -m "test(martial-arts): @DataJpaTest verifies converters and queries"
```

---

## Task 13: Provided interfaces (use cases)

**Files (10 interfaces):**
- Create: `service/provided/MentalMethodTemplateCreator.java`
- Create: `service/provided/MentalMethodTemplateFinder.java`
- Create: `service/provided/MentalMethodTemplateModifier.java`
- Create: `service/provided/MentalMethodTemplateRemover.java`
- Create: `service/provided/ExternalArtTemplateCreator.java`
- Create: `service/provided/ExternalArtTemplateFinder.java`
- Create: `service/provided/ExternalArtTemplateModifier.java`
- Create: `service/provided/ExternalArtTemplateRemover.java`
- Create: `service/provided/MartialArtLearner.java`
- Create: `service/provided/MartialArtEquipper.java`
- Create: `service/provided/LearnedMartialArtFinder.java`

Note: All paths below are under `src/main/java/com/jefflife/mudmk2/gamedata/application/`.

- [ ] **Step 1: Create the four MentalMethodTemplate interfaces**

```java
// MentalMethodTemplateCreator.java
package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplate;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodTemplateRequest;

public interface MentalMethodTemplateCreator {
    MentalMethodTemplate create(MentalMethodTemplateRequest request);
}
```

```java
// MentalMethodTemplateFinder.java
package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplate;

import java.util.List;

public interface MentalMethodTemplateFinder {
    MentalMethodTemplate findById(Long id);
    List<MentalMethodTemplate> findAll();
    List<MentalMethodTemplate> findByKind(MentalMethodKind kind);
    List<MentalMethodTemplate> findByNameContaining(String name);
}
```

```java
// MentalMethodTemplateModifier.java
package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplate;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodTemplateRequest;

public interface MentalMethodTemplateModifier {
    MentalMethodTemplate update(Long id, MentalMethodTemplateRequest request);
}
```

```java
// MentalMethodTemplateRemover.java
package com.jefflife.mudmk2.gamedata.application.service.provided;

public interface MentalMethodTemplateRemover {
    void delete(Long id);
}
```

- [ ] **Step 2: Create the four ExternalArtTemplate interfaces (mirror of above, using ExternalArtTemplate + WeaponType filter)**

```java
// ExternalArtTemplateCreator.java
package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplate;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtTemplateRequest;

public interface ExternalArtTemplateCreator {
    ExternalArtTemplate create(ExternalArtTemplateRequest request);
}
```

```java
// ExternalArtTemplateFinder.java
package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplate;

import java.util.List;

public interface ExternalArtTemplateFinder {
    ExternalArtTemplate findById(Long id);
    List<ExternalArtTemplate> findAll();
    List<ExternalArtTemplate> findByWeaponType(WeaponType weaponType);
    List<ExternalArtTemplate> findByNameContaining(String name);
}
```

```java
// ExternalArtTemplateModifier.java
package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplate;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtTemplateRequest;

public interface ExternalArtTemplateModifier {
    ExternalArtTemplate update(Long id, ExternalArtTemplateRequest request);
}
```

```java
// ExternalArtTemplateRemover.java
package com.jefflife.mudmk2.gamedata.application.service.provided;

public interface ExternalArtTemplateRemover {
    void delete(Long id);
}
```

- [ ] **Step 3: Create MartialArtLearner / MartialArtEquipper / LearnedMartialArtFinder**

```java
// MartialArtLearner.java
package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedExternalArt;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedMentalMethod;

import java.util.UUID;

public interface MartialArtLearner {
    LearnedMentalMethod learnMentalMethod(UUID playerCharacterId, Long templateId);
    LearnedExternalArt learnExternalArt(UUID playerCharacterId, Long templateId);
}
```

```java
// MartialArtEquipper.java
package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;

import java.util.UUID;

public interface MartialArtEquipper {
    void equipMentalMethod(UUID playerCharacterId, UUID learnedId);
    void unequipMentalMethod(UUID playerCharacterId, MentalMethodKind kind);
    void equipExternalArt(UUID playerCharacterId, UUID learnedId);
    void unequipExternalArt(UUID playerCharacterId, UUID learnedId);
}
```

```java
// LearnedMartialArtFinder.java
package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.EquippedMartialArts;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedExternalArt;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedMentalMethod;

import java.util.List;
import java.util.UUID;

public interface LearnedMartialArtFinder {

    record CharacterMartialArtView(
            UUID playerCharacterId,
            List<LearnedMentalMethod> learnedMentalMethods,
            List<LearnedExternalArt> learnedExternalArts,
            EquippedMartialArts equipped
    ) {}

    CharacterMartialArtView findByCharacter(UUID playerCharacterId);
}
```

- [ ] **Step 4: Compile (will fail until Task 14 creates request DTOs) — defer compile until Task 14**

- [ ] **Step 5: Stage files; commit after Task 14**

(no commit yet; combined with Task 14)

---

## Task 14: Request DTOs

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/model/request/MentalMethodTemplateRequest.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/model/request/MentalMethodLevelEffectRequest.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/model/request/ExternalArtTemplateRequest.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/model/request/ExternalArtLevelEffectRequest.java`

- [ ] **Step 1: MentalMethodLevelEffectRequest**

```java
package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodLevelEffect;

import java.util.List;

public record MentalMethodLevelEffectRequest(int level, List<StatModifierRequest> statModifiers) {
    public MentalMethodLevelEffect toDomain() {
        List<StatModifier> mods = statModifiers == null
                ? List.of()
                : statModifiers.stream().map(StatModifierRequest::toDomain).toList();
        return new MentalMethodLevelEffect(level, mods);
    }
}
```

- [ ] **Step 2: MentalMethodTemplateRequest**

```java
package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodLevelEffect;

import java.util.List;

public record MentalMethodTemplateRequest(
        String name,
        String description,
        MentalMethodKind kind,
        int maxLevel,
        List<MentalMethodLevelEffectRequest> levelEffects
) {
    public List<MentalMethodLevelEffect> levelEffectsDomain() {
        return levelEffects == null ? List.of()
                : levelEffects.stream().map(MentalMethodLevelEffectRequest::toDomain).toList();
    }
}
```

- [ ] **Step 3: ExternalArtLevelEffectRequest**

```java
package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtLevelEffect;

public record ExternalArtLevelEffectRequest(
        int level,
        double damageMultiplier,
        int cooldownSeconds,
        int apCost,
        int mpCost
) {
    public ExternalArtLevelEffect toDomain() {
        return new ExternalArtLevelEffect(level, damageMultiplier, cooldownSeconds, apCost, mpCost);
    }
}
```

- [ ] **Step 4: ExternalArtTemplateRequest**

```java
package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtLevelEffect;

import java.util.List;

public record ExternalArtTemplateRequest(
        String name,
        String description,
        WeaponType weaponType,
        int maxLevel,
        List<ExternalArtLevelEffectRequest> levelEffects
) {
    public List<ExternalArtLevelEffect> levelEffectsDomain() {
        return levelEffects == null ? List.of()
                : levelEffects.stream().map(ExternalArtLevelEffectRequest::toDomain).toList();
    }
}
```

- [ ] **Step 5: Compile, commit Tasks 13 + 14 together**

```bash
./gradlew compileJava
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/MentalMethodTemplate*.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/ExternalArtTemplate*.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/MartialArt*.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/LearnedMartialArtFinder.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/service/model/request/MentalMethod*.java \
        src/main/java/com/jefflife/mudmk2/gamedata/application/service/model/request/ExternalArt*.java
git commit -m "feat(martial-arts): provided use cases + request DTOs"
```

---

## Task 15: MentalMethodTemplateService

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/MentalMethodTemplateService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/service/MentalMethodTemplateServiceTest.java`

- [ ] **Step 1: Write failing service test (Mockito)**

```java
package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtTemplateInUseException;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodLevelEffectRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.StatModifierRequest;
import com.jefflife.mudmk2.gamedata.application.service.required.LearnedMentalMethodRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.MentalMethodTemplateRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MentalMethodTemplateServiceTest {

    private final MentalMethodTemplateRepository repo = mock(MentalMethodTemplateRepository.class);
    private final LearnedMentalMethodRepository learnedRepo = mock(LearnedMentalMethodRepository.class);
    private final MentalMethodTemplateService service = new MentalMethodTemplateService(repo, learnedRepo);

    private MentalMethodTemplateRequest req(int maxLevel) {
        return new MentalMethodTemplateRequest("천뢰신공", "d",
                MentalMethodKind.INNER_POWER, maxLevel,
                List.of(new MentalMethodLevelEffectRequest(1,
                        List.of(new StatModifierRequest(StatType.INNER_POWER, 3)))));
    }

    @Test
    void create_persistsTemplate() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(req(1));

        ArgumentCaptor<MentalMethodTemplate> captor = ArgumentCaptor.forClass(MentalMethodTemplate.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("천뢰신공");
    }

    @Test
    void update_modifiesExisting() {
        MentalMethodTemplate existing = MentalMethodTemplate.builder()
                .name("old").description("old").kind(MentalMethodKind.INNER_POWER)
                .maxLevel(1).levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build();
        when(repo.findById(1L)).thenReturn(Optional.of(existing));

        service.update(1L, req(1));

        assertThat(existing.getName()).isEqualTo("천뢰신공");
    }

    @Test
    void findById_whenMissing_throwsNoSuchElement() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void delete_whenLearned_throws() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER).maxLevel(1)
                .levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build();
        when(repo.findById(7L)).thenReturn(Optional.of(t));
        when(learnedRepo.existsByMentalMethodTemplateId(7L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOf(MartialArtTemplateInUseException.class);
        verify(repo, never()).delete(any());
    }

    @Test
    void delete_whenNotLearned_deletes() {
        MentalMethodTemplate t = MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.INNER_POWER).maxLevel(1)
                .levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build();
        when(repo.findById(7L)).thenReturn(Optional.of(t));
        when(learnedRepo.existsByMentalMethodTemplateId(7L)).thenReturn(false);

        service.delete(7L);

        verify(repo).delete(t);
    }
}
```

- [ ] **Step 2: Run, confirm failure**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.service.MentalMethodTemplateServiceTest"`
Expected: FAIL (class does not exist)

- [ ] **Step 3: Implement service**

```java
package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplate;
import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtTemplateInUseException;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import com.jefflife.mudmk2.gamedata.application.service.required.LearnedMentalMethodRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.MentalMethodTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class MentalMethodTemplateService implements
        MentalMethodTemplateCreator, MentalMethodTemplateFinder,
        MentalMethodTemplateModifier, MentalMethodTemplateRemover {

    private final MentalMethodTemplateRepository repo;
    private final LearnedMentalMethodRepository learnedRepo;

    public MentalMethodTemplateService(MentalMethodTemplateRepository repo,
                                       LearnedMentalMethodRepository learnedRepo) {
        this.repo = repo;
        this.learnedRepo = learnedRepo;
    }

    @Override
    public MentalMethodTemplate create(MentalMethodTemplateRequest request) {
        MentalMethodTemplate template = MentalMethodTemplate.builder()
                .name(request.name())
                .description(request.description())
                .kind(request.kind())
                .maxLevel(request.maxLevel())
                .levelEffects(request.levelEffectsDomain())
                .build();
        return repo.save(template);
    }

    @Override
    @Transactional(readOnly = true)
    public MentalMethodTemplate findById(Long id) {
        return repo.findById(id).orElseThrow(
                () -> new NoSuchElementException("MentalMethodTemplate not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentalMethodTemplate> findAll() { return repo.findAllBy(); }

    @Override
    @Transactional(readOnly = true)
    public List<MentalMethodTemplate> findByKind(MentalMethodKind kind) { return repo.findByKind(kind); }

    @Override
    @Transactional(readOnly = true)
    public List<MentalMethodTemplate> findByNameContaining(String name) { return repo.findByNameContaining(name); }

    @Override
    public MentalMethodTemplate update(Long id, MentalMethodTemplateRequest request) {
        MentalMethodTemplate t = repo.findById(id).orElseThrow(
                () -> new NoSuchElementException("MentalMethodTemplate not found: " + id));
        t.update(request.name(), request.description(), request.kind(),
                request.maxLevel(), request.levelEffectsDomain());
        return t;
    }

    @Override
    public void delete(Long id) {
        MentalMethodTemplate t = repo.findById(id).orElseThrow(
                () -> new NoSuchElementException("MentalMethodTemplate not found: " + id));
        if (learnedRepo.existsByMentalMethodTemplateId(id)) {
            throw new MartialArtTemplateInUseException(
                    "MentalMethodTemplate " + id + " is in use by learned records");
        }
        repo.delete(t);
    }
}
```

- [ ] **Step 4: Run tests, commit**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.service.MentalMethodTemplateServiceTest"
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/MentalMethodTemplateService.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/service/MentalMethodTemplateServiceTest.java
git commit -m "feat(martial-arts): MentalMethodTemplateService"
```

---

## Task 16: ExternalArtTemplateService

Mirror of Task 15 for ExternalArtTemplate.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/ExternalArtTemplateService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/service/ExternalArtTemplateServiceTest.java`

- [ ] **Step 1: Write failing test**

```java
package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtTemplateInUseException;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtLevelEffectRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.required.ExternalArtTemplateRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.LearnedExternalArtRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ExternalArtTemplateServiceTest {

    private final ExternalArtTemplateRepository repo = mock(ExternalArtTemplateRepository.class);
    private final LearnedExternalArtRepository learnedRepo = mock(LearnedExternalArtRepository.class);
    private final ExternalArtTemplateService service = new ExternalArtTemplateService(repo, learnedRepo);

    private ExternalArtTemplateRequest req() {
        return new ExternalArtTemplateRequest("발검술", "d", WeaponType.SWORD, 1,
                List.of(new ExternalArtLevelEffectRequest(1, 1.1, 5, 5, 0)));
    }

    @Test
    void create_persists() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        service.create(req());
        verify(repo).save(any());
    }

    @Test
    void delete_whenLearned_throws() {
        ExternalArtTemplate t = ExternalArtTemplate.builder()
                .name("x").description("x").weaponType(WeaponType.SWORD).maxLevel(1)
                .levelEffects(List.of(new ExternalArtLevelEffect(1, 1.0, 1, 1, 0)))
                .build();
        when(repo.findById(5L)).thenReturn(Optional.of(t));
        when(learnedRepo.existsByExternalArtTemplateId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(5L))
                .isInstanceOf(MartialArtTemplateInUseException.class);
    }

    @Test
    void findById_whenMissing_throws() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(NoSuchElementException.class);
    }
}
```

- [ ] **Step 2: Implement service (mirror of Task 15)**

```java
package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplate;
import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtTemplateInUseException;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import com.jefflife.mudmk2.gamedata.application.service.required.ExternalArtTemplateRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.LearnedExternalArtRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ExternalArtTemplateService implements
        ExternalArtTemplateCreator, ExternalArtTemplateFinder,
        ExternalArtTemplateModifier, ExternalArtTemplateRemover {

    private final ExternalArtTemplateRepository repo;
    private final LearnedExternalArtRepository learnedRepo;

    public ExternalArtTemplateService(ExternalArtTemplateRepository repo,
                                      LearnedExternalArtRepository learnedRepo) {
        this.repo = repo;
        this.learnedRepo = learnedRepo;
    }

    @Override
    public ExternalArtTemplate create(ExternalArtTemplateRequest request) {
        return repo.save(ExternalArtTemplate.builder()
                .name(request.name())
                .description(request.description())
                .weaponType(request.weaponType())
                .maxLevel(request.maxLevel())
                .levelEffects(request.levelEffectsDomain())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public ExternalArtTemplate findById(Long id) {
        return repo.findById(id).orElseThrow(
                () -> new NoSuchElementException("ExternalArtTemplate not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalArtTemplate> findAll() { return repo.findAllBy(); }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalArtTemplate> findByWeaponType(WeaponType type) { return repo.findByWeaponType(type); }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalArtTemplate> findByNameContaining(String name) { return repo.findByNameContaining(name); }

    @Override
    public ExternalArtTemplate update(Long id, ExternalArtTemplateRequest request) {
        ExternalArtTemplate t = repo.findById(id).orElseThrow(
                () -> new NoSuchElementException("ExternalArtTemplate not found: " + id));
        t.update(request.name(), request.description(), request.weaponType(),
                request.maxLevel(), request.levelEffectsDomain());
        return t;
    }

    @Override
    public void delete(Long id) {
        ExternalArtTemplate t = repo.findById(id).orElseThrow(
                () -> new NoSuchElementException("ExternalArtTemplate not found: " + id));
        if (learnedRepo.existsByExternalArtTemplateId(id)) {
            throw new MartialArtTemplateInUseException(
                    "ExternalArtTemplate " + id + " is in use by learned records");
        }
        repo.delete(t);
    }
}
```

- [ ] **Step 3: Tests pass, commit**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.service.ExternalArtTemplateServiceTest"
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/ExternalArtTemplateService.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/service/ExternalArtTemplateServiceTest.java
git commit -m "feat(martial-arts): ExternalArtTemplateService"
```

---

## Task 17: MartialArtLearningService

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/MartialArtLearningService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/service/MartialArtLearningServiceTest.java`

- [ ] **Step 1: Write failing test**

```java
package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.service.exception.AlreadyLearnedException;
import com.jefflife.mudmk2.gamedata.application.service.required.*;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MartialArtLearningServiceTest {

    private final LearnedMentalMethodRepository mentalRepo = mock(LearnedMentalMethodRepository.class);
    private final LearnedExternalArtRepository externalRepo = mock(LearnedExternalArtRepository.class);
    private final MentalMethodTemplateRepository mentalTplRepo = mock(MentalMethodTemplateRepository.class);
    private final ExternalArtTemplateRepository externalTplRepo = mock(ExternalArtTemplateRepository.class);
    private final PlayerCharacterRepository pcRepo = mock(PlayerCharacterRepository.class);

    private final MartialArtLearningService service = new MartialArtLearningService(
            mentalRepo, externalRepo, mentalTplRepo, externalTplRepo, pcRepo);

    @Test
    void learnMentalMethod_savesNew() {
        UUID pc = UUID.randomUUID();
        when(pcRepo.existsById(pc)).thenReturn(true);
        when(mentalTplRepo.existsById(1L)).thenReturn(true);
        when(mentalRepo.existsByPlayerCharacterIdAndMentalMethodTemplateId(pc, 1L)).thenReturn(false);
        when(mentalRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.learnMentalMethod(pc, 1L);

        verify(mentalRepo).save(any(LearnedMentalMethod.class));
    }

    @Test
    void learnMentalMethod_whenDuplicate_throws() {
        UUID pc = UUID.randomUUID();
        when(pcRepo.existsById(pc)).thenReturn(true);
        when(mentalTplRepo.existsById(1L)).thenReturn(true);
        when(mentalRepo.existsByPlayerCharacterIdAndMentalMethodTemplateId(pc, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.learnMentalMethod(pc, 1L))
                .isInstanceOf(AlreadyLearnedException.class);
    }

    @Test
    void learnMentalMethod_whenPcMissing_throwsNoSuchElement() {
        UUID pc = UUID.randomUUID();
        when(pcRepo.existsById(pc)).thenReturn(false);
        assertThatThrownBy(() -> service.learnMentalMethod(pc, 1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void learnMentalMethod_whenTemplateMissing_throwsNoSuchElement() {
        UUID pc = UUID.randomUUID();
        when(pcRepo.existsById(pc)).thenReturn(true);
        when(mentalTplRepo.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.learnMentalMethod(pc, 99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void learnExternalArt_duplicate_throws() {
        UUID pc = UUID.randomUUID();
        when(pcRepo.existsById(pc)).thenReturn(true);
        when(externalTplRepo.existsById(2L)).thenReturn(true);
        when(externalRepo.existsByPlayerCharacterIdAndExternalArtTemplateId(pc, 2L)).thenReturn(true);

        assertThatThrownBy(() -> service.learnExternalArt(pc, 2L))
                .isInstanceOf(AlreadyLearnedException.class);
    }
}
```

- [ ] **Step 2: Run, confirm failure**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.service.MartialArtLearningServiceTest"`

- [ ] **Step 3: Implement service**

```java
package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedExternalArt;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.LearnedMentalMethod;
import com.jefflife.mudmk2.gamedata.application.service.exception.AlreadyLearnedException;
import com.jefflife.mudmk2.gamedata.application.service.provided.MartialArtLearner;
import com.jefflife.mudmk2.gamedata.application.service.required.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class MartialArtLearningService implements MartialArtLearner {

    private final LearnedMentalMethodRepository mentalRepo;
    private final LearnedExternalArtRepository externalRepo;
    private final MentalMethodTemplateRepository mentalTplRepo;
    private final ExternalArtTemplateRepository externalTplRepo;
    private final PlayerCharacterRepository pcRepo;

    public MartialArtLearningService(LearnedMentalMethodRepository mentalRepo,
                                     LearnedExternalArtRepository externalRepo,
                                     MentalMethodTemplateRepository mentalTplRepo,
                                     ExternalArtTemplateRepository externalTplRepo,
                                     PlayerCharacterRepository pcRepo) {
        this.mentalRepo = mentalRepo;
        this.externalRepo = externalRepo;
        this.mentalTplRepo = mentalTplRepo;
        this.externalTplRepo = externalTplRepo;
        this.pcRepo = pcRepo;
    }

    @Override
    public LearnedMentalMethod learnMentalMethod(UUID pcId, Long templateId) {
        requirePc(pcId);
        if (!mentalTplRepo.existsById(templateId)) {
            throw new NoSuchElementException("MentalMethodTemplate not found: " + templateId);
        }
        if (mentalRepo.existsByPlayerCharacterIdAndMentalMethodTemplateId(pcId, templateId)) {
            throw new AlreadyLearnedException(
                    "already learned MentalMethodTemplate " + templateId + " for character " + pcId);
        }
        return mentalRepo.save(LearnedMentalMethod.create(pcId, templateId));
    }

    @Override
    public LearnedExternalArt learnExternalArt(UUID pcId, Long templateId) {
        requirePc(pcId);
        if (!externalTplRepo.existsById(templateId)) {
            throw new NoSuchElementException("ExternalArtTemplate not found: " + templateId);
        }
        if (externalRepo.existsByPlayerCharacterIdAndExternalArtTemplateId(pcId, templateId)) {
            throw new AlreadyLearnedException(
                    "already learned ExternalArtTemplate " + templateId + " for character " + pcId);
        }
        return externalRepo.save(LearnedExternalArt.create(pcId, templateId));
    }

    private void requirePc(UUID pcId) {
        if (!pcRepo.existsById(pcId)) {
            throw new NoSuchElementException("PlayerCharacter not found: " + pcId);
        }
    }
}
```

Note: `PlayerCharacterRepository` must already have `existsById(UUID)` from CrudRepository — verify via `grep -n "PlayerCharacterRepository" src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/PlayerCharacterRepository.java`. If interface does not extend CrudRepository/JpaRepository, add `boolean existsById(UUID id);` explicitly.

- [ ] **Step 4: Tests pass, commit**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.service.MartialArtLearningServiceTest"
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/MartialArtLearningService.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/service/MartialArtLearningServiceTest.java
git commit -m "feat(martial-arts): MartialArtLearningService"
```

---

## Task 18: MartialArtEquipService (Equipper + LearnedMartialArtFinder)

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/MartialArtEquipService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/service/MartialArtEquipServiceTest.java`

The service must:
1. Load `PlayerCharacter` (for its `EquippedMartialArts`)
2. Verify the `learnedId` belongs to this character (`findByIdAndPlayerCharacterId`)
3. For mental: look up the `LearnedMentalMethod`'s template → read `kind` → call `EquippedMartialArts.equipMental(kind, learnedId)`
4. For external: call `equipExternal(learnedId)` (entity enforces slot limit)
5. `LearnedMartialArtFinder.findByCharacter` returns the bundle

- [ ] **Step 1: Write failing test**

```java
package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.exception.NotLearnedException;
import com.jefflife.mudmk2.gamedata.application.service.provided.LearnedMartialArtFinder.CharacterMartialArtView;
import com.jefflife.mudmk2.gamedata.application.service.required.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MartialArtEquipServiceTest {

    private final LearnedMentalMethodRepository mentalRepo = mock(LearnedMentalMethodRepository.class);
    private final LearnedExternalArtRepository externalRepo = mock(LearnedExternalArtRepository.class);
    private final MentalMethodTemplateRepository mentalTplRepo = mock(MentalMethodTemplateRepository.class);
    private final ExternalArtTemplateRepository externalTplRepo = mock(ExternalArtTemplateRepository.class);
    private final PlayerCharacterRepository pcRepo = mock(PlayerCharacterRepository.class);

    private final MartialArtEquipService service = new MartialArtEquipService(
            mentalRepo, externalRepo, mentalTplRepo, externalTplRepo, pcRepo);

    private PlayerCharacter pcWithEquipped(UUID pcId, EquippedMartialArts equipped) {
        PlayerCharacter pc = mock(PlayerCharacter.class);
        when(pc.getId()).thenReturn(pcId);
        when(pc.getEquippedMartialArts()).thenReturn(equipped);
        return pc;
    }

    @Test
    void equipMentalMethod_lookUpKind_andCallsEquipMental() {
        UUID pc = UUID.randomUUID();
        UUID learnedId = UUID.randomUUID();
        EquippedMartialArts eq = EquippedMartialArts.create();
        when(pcRepo.findById(pc)).thenReturn(Optional.of(pcWithEquipped(pc, eq)));

        LearnedMentalMethod learned = LearnedMentalMethod.create(pc, 10L);
        when(mentalRepo.findByIdAndPlayerCharacterId(learnedId, pc)).thenReturn(Optional.of(learned));
        MentalMethodTemplate tpl = MentalMethodTemplate.builder()
                .name("x").description("x").kind(MentalMethodKind.LIGHT_STEP).maxLevel(1)
                .levelEffects(List.of(new MentalMethodLevelEffect(1, List.of())))
                .build();
        when(mentalTplRepo.findById(10L)).thenReturn(Optional.of(tpl));

        service.equipMentalMethod(pc, learnedId);

        // we can't see private id assignment; verify entity slot state
        assertThat(eq.getMentalSlots()).containsKey(MentalMethodKind.LIGHT_STEP);
    }

    @Test
    void equipMentalMethod_notLearned_throws() {
        UUID pc = UUID.randomUUID();
        UUID learnedId = UUID.randomUUID();
        when(pcRepo.findById(pc)).thenReturn(Optional.of(pcWithEquipped(pc, EquippedMartialArts.create())));
        when(mentalRepo.findByIdAndPlayerCharacterId(learnedId, pc)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.equipMentalMethod(pc, learnedId))
                .isInstanceOf(NotLearnedException.class);
    }

    @Test
    void equipExternalArt_appendsToSlots() {
        UUID pc = UUID.randomUUID();
        UUID learnedId = UUID.randomUUID();
        EquippedMartialArts eq = EquippedMartialArts.create();
        when(pcRepo.findById(pc)).thenReturn(Optional.of(pcWithEquipped(pc, eq)));
        when(externalRepo.findByIdAndPlayerCharacterId(learnedId, pc))
                .thenReturn(Optional.of(LearnedExternalArt.create(pc, 1L)));

        service.equipExternalArt(pc, learnedId);

        assertThat(eq.getExternalSlots()).hasSize(1);
    }

    @Test
    void unequipMentalMethod_removesSlot() {
        UUID pc = UUID.randomUUID();
        EquippedMartialArts eq = EquippedMartialArts.create();
        UUID learned = UUID.randomUUID();
        eq.equipMental(MentalMethodKind.INNER_POWER, learned);

        when(pcRepo.findById(pc)).thenReturn(Optional.of(pcWithEquipped(pc, eq)));

        service.unequipMentalMethod(pc, MentalMethodKind.INNER_POWER);

        assertThat(eq.getMentalSlots()).doesNotContainKey(MentalMethodKind.INNER_POWER);
    }

    @Test
    void findByCharacter_returnsBundle() {
        UUID pc = UUID.randomUUID();
        EquippedMartialArts eq = EquippedMartialArts.create();
        when(pcRepo.findById(pc)).thenReturn(Optional.of(pcWithEquipped(pc, eq)));
        when(mentalRepo.findAllByPlayerCharacterId(pc)).thenReturn(List.of(LearnedMentalMethod.create(pc, 1L)));
        when(externalRepo.findAllByPlayerCharacterId(pc)).thenReturn(List.of());

        CharacterMartialArtView view = service.findByCharacter(pc);
        assertThat(view.learnedMentalMethods()).hasSize(1);
        assertThat(view.learnedExternalArts()).isEmpty();
        assertThat(view.equipped()).isEqualTo(eq);
    }

    @Test
    void equipMentalMethod_pcMissing_throwsNoSuchElement() {
        when(pcRepo.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.equipMentalMethod(UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(NoSuchElementException.class);
    }
}
```

- [ ] **Step 2: Confirm failure**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.service.MartialArtEquipServiceTest"`

- [ ] **Step 3: Implement service**

```java
package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.exception.NotLearnedException;
import com.jefflife.mudmk2.gamedata.application.service.provided.LearnedMartialArtFinder;
import com.jefflife.mudmk2.gamedata.application.service.provided.MartialArtEquipper;
import com.jefflife.mudmk2.gamedata.application.service.required.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class MartialArtEquipService implements MartialArtEquipper, LearnedMartialArtFinder {

    private final LearnedMentalMethodRepository mentalRepo;
    private final LearnedExternalArtRepository externalRepo;
    private final MentalMethodTemplateRepository mentalTplRepo;
    private final ExternalArtTemplateRepository externalTplRepo;
    private final PlayerCharacterRepository pcRepo;

    public MartialArtEquipService(LearnedMentalMethodRepository mentalRepo,
                                  LearnedExternalArtRepository externalRepo,
                                  MentalMethodTemplateRepository mentalTplRepo,
                                  ExternalArtTemplateRepository externalTplRepo,
                                  PlayerCharacterRepository pcRepo) {
        this.mentalRepo = mentalRepo;
        this.externalRepo = externalRepo;
        this.mentalTplRepo = mentalTplRepo;
        this.externalTplRepo = externalTplRepo;
        this.pcRepo = pcRepo;
    }

    @Override
    public void equipMentalMethod(UUID pcId, UUID learnedId) {
        PlayerCharacter pc = requirePc(pcId);
        LearnedMentalMethod learned = mentalRepo.findByIdAndPlayerCharacterId(learnedId, pcId)
                .orElseThrow(() -> new NotLearnedException(
                        "LearnedMentalMethod " + learnedId + " not learned by character " + pcId));
        MentalMethodTemplate tpl = mentalTplRepo.findById(learned.getMentalMethodTemplateId())
                .orElseThrow(() -> new NoSuchElementException(
                        "MentalMethodTemplate not found: " + learned.getMentalMethodTemplateId()));
        pc.getEquippedMartialArts().equipMental(tpl.getKind(), learned.getId());
    }

    @Override
    public void unequipMentalMethod(UUID pcId, MentalMethodKind kind) {
        PlayerCharacter pc = requirePc(pcId);
        pc.getEquippedMartialArts().unequipMental(kind);
    }

    @Override
    public void equipExternalArt(UUID pcId, UUID learnedId) {
        PlayerCharacter pc = requirePc(pcId);
        LearnedExternalArt learned = externalRepo.findByIdAndPlayerCharacterId(learnedId, pcId)
                .orElseThrow(() -> new NotLearnedException(
                        "LearnedExternalArt " + learnedId + " not learned by character " + pcId));
        pc.getEquippedMartialArts().equipExternal(learned.getId());
    }

    @Override
    public void unequipExternalArt(UUID pcId, UUID learnedId) {
        PlayerCharacter pc = requirePc(pcId);
        pc.getEquippedMartialArts().unequipExternal(learnedId);
    }

    @Override
    @Transactional(readOnly = true)
    public CharacterMartialArtView findByCharacter(UUID pcId) {
        PlayerCharacter pc = requirePc(pcId);
        return new CharacterMartialArtView(
                pcId,
                mentalRepo.findAllByPlayerCharacterId(pcId),
                externalRepo.findAllByPlayerCharacterId(pcId),
                pc.getEquippedMartialArts()
        );
    }

    private PlayerCharacter requirePc(UUID pcId) {
        return pcRepo.findById(pcId)
                .orElseThrow(() -> new NoSuchElementException("PlayerCharacter not found: " + pcId));
    }
}
```

- [ ] **Step 4: Tests pass, commit**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.service.MartialArtEquipServiceTest"
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/MartialArtEquipService.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/service/MartialArtEquipServiceTest.java
git commit -m "feat(martial-arts): MartialArtEquipService"
```

---

## Task 19: Response DTOs

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/response/MentalMethodTemplateResponse.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/response/ExternalArtTemplateResponse.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/response/CharacterMartialArtResponse.java`

- [ ] **Step 1: MentalMethodTemplateResponse**

```java
package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodLevelEffect;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplate;

import java.util.List;

public record MentalMethodTemplateResponse(
        Long id, String name, String description,
        MentalMethodKind kind, int maxLevel,
        List<MentalMethodLevelEffect> levelEffects
) {
    public static MentalMethodTemplateResponse from(MentalMethodTemplate t) {
        return new MentalMethodTemplateResponse(
                t.getId(), t.getName(), t.getDescription(),
                t.getKind(), t.getMaxLevel(), t.getLevelEffects());
    }
}
```

- [ ] **Step 2: ExternalArtTemplateResponse**

```java
package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtLevelEffect;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplate;

import java.util.List;

public record ExternalArtTemplateResponse(
        Long id, String name, String description,
        WeaponType weaponType, int maxLevel,
        List<ExternalArtLevelEffect> levelEffects
) {
    public static ExternalArtTemplateResponse from(ExternalArtTemplate t) {
        return new ExternalArtTemplateResponse(
                t.getId(), t.getName(), t.getDescription(),
                t.getWeaponType(), t.getMaxLevel(), t.getLevelEffects());
    }
}
```

- [ ] **Step 3: CharacterMartialArtResponse**

```java
package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.*;
import com.jefflife.mudmk2.gamedata.application.service.provided.LearnedMartialArtFinder.CharacterMartialArtView;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public record CharacterMartialArtResponse(
        UUID playerCharacterId,
        List<LearnedMentalView> learnedMentalMethods,
        List<LearnedExternalView> learnedExternalArts,
        Map<MentalMethodKind, UUID> equippedMentalSlots,
        List<UUID> equippedExternalSlots
) {
    public record LearnedMentalView(UUID id, Long templateId, String templateName,
                                    MentalMethodKind kind, int currentLevel, long currentExp) {}
    public record LearnedExternalView(UUID id, Long templateId, String templateName,
                                      WeaponType weaponType, int currentLevel, long currentExp) {}

    public static CharacterMartialArtResponse from(
            CharacterMartialArtView view,
            Function<Long, MentalMethodTemplate> mentalTpl,
            Function<Long, ExternalArtTemplate> externalTpl
    ) {
        List<LearnedMentalView> ms = view.learnedMentalMethods().stream().map(l -> {
            MentalMethodTemplate t = mentalTpl.apply(l.getMentalMethodTemplateId());
            return new LearnedMentalView(l.getId(), t.getId(), t.getName(),
                    t.getKind(), l.getCurrentLevel(), l.getCurrentExp());
        }).toList();
        List<LearnedExternalView> es = view.learnedExternalArts().stream().map(l -> {
            ExternalArtTemplate t = externalTpl.apply(l.getExternalArtTemplateId());
            return new LearnedExternalView(l.getId(), t.getId(), t.getName(),
                    t.getWeaponType(), l.getCurrentLevel(), l.getCurrentExp());
        }).toList();
        return new CharacterMartialArtResponse(
                view.playerCharacterId(), ms, es,
                view.equipped().getMentalSlots(),
                view.equipped().getExternalSlots());
    }
}
```

- [ ] **Step 4: Compile & commit**

```bash
./gradlew compileJava
git add src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/response/MentalMethodTemplateResponse.java \
        src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/response/ExternalArtTemplateResponse.java \
        src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/response/CharacterMartialArtResponse.java
git commit -m "feat(martial-arts): response DTOs"
```

---

## Task 20: MentalMethodTemplateController

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/MentalMethodTemplateController.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/MentalMethodTemplateControllerSystemTest.java`

- [ ] **Step 1: Write failing system test (modeled on ItemTemplateControllerSystemTest)**

```java
package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.config.SecurityMockMvcConfiguration;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.MentalMethodTemplateResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodLevelEffectRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.StatModifierRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
class MentalMethodTemplateControllerSystemTest {

    private static final String BASE_URL = "/api/v1/mental-method-templates";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private MentalMethodTemplateRequest sampleReq(String name) {
        return new MentalMethodTemplateRequest(name, "desc", MentalMethodKind.INNER_POWER, 1,
                List.of(new MentalMethodLevelEffectRequest(1,
                        List.of(new StatModifierRequest(StatType.INNER_POWER, 3)))));
    }

    @Test
    void create_returns201WithBody() throws Exception {
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleReq("천뢰신공"))))
                .andExpect(status().isCreated())
                .andReturn();

        MentalMethodTemplateResponse resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), MentalMethodTemplateResponse.class);
        assertThat(resp.id()).isNotNull();
        assertThat(resp.name()).isEqualTo("천뢰신공");
        assertThat(resp.kind()).isEqualTo(MentalMethodKind.INNER_POWER);
        assertThat(result.getResponse().getHeader("Location")).isEqualTo(BASE_URL + "/" + resp.id());
    }

    @Test
    void getById_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/-999")).andExpect(status().isNotFound());
    }

    @Test
    void delete_whenNotFound_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/-999")).andExpect(status().isNotFound());
    }

    @Test
    void update_andDelete_flow() throws Exception {
        MvcResult created = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleReq("심법1"))))
                .andExpect(status().isCreated()).andReturn();
        Long id = objectMapper.readValue(created.getResponse().getContentAsString(),
                MentalMethodTemplateResponse.class).id();

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleReq("심법2"))))
                .andExpect(status().isOk());

        mockMvc.perform(delete(BASE_URL + "/" + id)).andExpect(status().isNoContent());
    }

    @Test
    void list_withKindFilter() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleReq("심법A")))).andExpect(status().isCreated());
        mockMvc.perform(get(BASE_URL + "?kind=INNER_POWER")).andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: Confirm failure**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.adapter.webapi.MentalMethodTemplateControllerSystemTest"`

- [ ] **Step 3: Implement controller**

```java
package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.adapter.webapi.response.MentalMethodTemplateResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodTemplate;
import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtTemplateInUseException;
import com.jefflife.mudmk2.gamedata.application.service.model.request.MentalMethodTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(MentalMethodTemplateController.BASE_PATH)
public class MentalMethodTemplateController {
    public static final String BASE_PATH = "/api/v1/mental-method-templates";

    private final MentalMethodTemplateCreator creator;
    private final MentalMethodTemplateFinder finder;
    private final MentalMethodTemplateModifier modifier;
    private final MentalMethodTemplateRemover remover;

    public MentalMethodTemplateController(MentalMethodTemplateCreator creator,
                                          MentalMethodTemplateFinder finder,
                                          MentalMethodTemplateModifier modifier,
                                          MentalMethodTemplateRemover remover) {
        this.creator = creator; this.finder = finder;
        this.modifier = modifier; this.remover = remover;
    }

    @PostMapping
    public ResponseEntity<MentalMethodTemplateResponse> create(
            @RequestBody MentalMethodTemplateRequest request) {
        try {
            MentalMethodTemplate t = creator.create(request);
            MentalMethodTemplateResponse resp = MentalMethodTemplateResponse.from(t);
            return ResponseEntity.created(URI.create(BASE_PATH + "/" + resp.id())).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<MentalMethodTemplateResponse>> list(
            @RequestParam(required = false) MentalMethodKind kind,
            @RequestParam(required = false) String name) {
        List<MentalMethodTemplate> templates;
        if (name != null && !name.isBlank()) templates = finder.findByNameContaining(name);
        else if (kind != null) templates = finder.findByKind(kind);
        else templates = finder.findAll();
        return ResponseEntity.ok(templates.stream().map(MentalMethodTemplateResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentalMethodTemplateResponse> get(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(MentalMethodTemplateResponse.from(finder.findById(id)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MentalMethodTemplateResponse> update(@PathVariable Long id,
            @RequestBody MentalMethodTemplateRequest request) {
        try {
            return ResponseEntity.ok(MentalMethodTemplateResponse.from(modifier.update(id, request)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            remover.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (MartialArtTemplateInUseException e) {
            return ResponseEntity.status(409).build();
        }
    }
}
```

- [ ] **Step 4: Tests pass, commit**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.adapter.webapi.MentalMethodTemplateControllerSystemTest"
git add src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/MentalMethodTemplateController.java \
        src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/MentalMethodTemplateControllerSystemTest.java
git commit -m "feat(martial-arts): MentalMethodTemplateController + system test"
```

---

## Task 21: ExternalArtTemplateController

Mirror of Task 20.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/ExternalArtTemplateController.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/ExternalArtTemplateControllerSystemTest.java`

- [ ] **Step 1: Write failing system test**

```java
package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.config.SecurityMockMvcConfiguration;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.ExternalArtTemplateResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtLevelEffectRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtTemplateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
class ExternalArtTemplateControllerSystemTest {

    private static final String BASE_URL = "/api/v1/external-art-templates";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private ExternalArtTemplateRequest sampleReq(String name) {
        return new ExternalArtTemplateRequest(name, "desc", WeaponType.SWORD, 1,
                List.of(new ExternalArtLevelEffectRequest(1, 1.1, 5, 5, 0)));
    }

    @Test
    void create_returns201() throws Exception {
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleReq("발검술"))))
                .andExpect(status().isCreated()).andReturn();

        ExternalArtTemplateResponse resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), ExternalArtTemplateResponse.class);
        assertThat(resp.id()).isNotNull();
        assertThat(resp.weaponType()).isEqualTo(WeaponType.SWORD);
    }

    @Test
    void getById_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/-999")).andExpect(status().isNotFound());
    }

    @Test
    void list_withWeaponTypeFilter() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleReq("외공1")))).andExpect(status().isCreated());
        mockMvc.perform(get(BASE_URL + "?weaponType=SWORD")).andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: Confirm failure**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.adapter.webapi.ExternalArtTemplateControllerSystemTest"`

- [ ] **Step 3: Implement controller (mirror of Task 20)**

```java
package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.adapter.webapi.response.ExternalArtTemplateResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.ExternalArtTemplate;
import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtTemplateInUseException;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ExternalArtTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(ExternalArtTemplateController.BASE_PATH)
public class ExternalArtTemplateController {
    public static final String BASE_PATH = "/api/v1/external-art-templates";

    private final ExternalArtTemplateCreator creator;
    private final ExternalArtTemplateFinder finder;
    private final ExternalArtTemplateModifier modifier;
    private final ExternalArtTemplateRemover remover;

    public ExternalArtTemplateController(ExternalArtTemplateCreator creator,
                                         ExternalArtTemplateFinder finder,
                                         ExternalArtTemplateModifier modifier,
                                         ExternalArtTemplateRemover remover) {
        this.creator = creator; this.finder = finder;
        this.modifier = modifier; this.remover = remover;
    }

    @PostMapping
    public ResponseEntity<ExternalArtTemplateResponse> create(@RequestBody ExternalArtTemplateRequest request) {
        try {
            ExternalArtTemplate t = creator.create(request);
            ExternalArtTemplateResponse resp = ExternalArtTemplateResponse.from(t);
            return ResponseEntity.created(URI.create(BASE_PATH + "/" + resp.id())).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ExternalArtTemplateResponse>> list(
            @RequestParam(required = false) WeaponType weaponType,
            @RequestParam(required = false) String name) {
        List<ExternalArtTemplate> templates;
        if (name != null && !name.isBlank()) templates = finder.findByNameContaining(name);
        else if (weaponType != null) templates = finder.findByWeaponType(weaponType);
        else templates = finder.findAll();
        return ResponseEntity.ok(templates.stream().map(ExternalArtTemplateResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExternalArtTemplateResponse> get(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ExternalArtTemplateResponse.from(finder.findById(id)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExternalArtTemplateResponse> update(@PathVariable Long id,
            @RequestBody ExternalArtTemplateRequest request) {
        try {
            return ResponseEntity.ok(ExternalArtTemplateResponse.from(modifier.update(id, request)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            remover.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (MartialArtTemplateInUseException e) {
            return ResponseEntity.status(409).build();
        }
    }
}
```

- [ ] **Step 4: Tests pass, commit**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.adapter.webapi.ExternalArtTemplateControllerSystemTest"
git add src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/ExternalArtTemplateController.java \
        src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/ExternalArtTemplateControllerSystemTest.java
git commit -m "feat(martial-arts): ExternalArtTemplateController + system test"
```

---

## Task 22: CharacterMartialArtController

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/CharacterMartialArtController.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/CharacterMartialArtControllerSystemTest.java`

This controller wires `MartialArtLearner` + `MartialArtEquipper` + `LearnedMartialArtFinder` + `MentalMethodTemplateFinder` + `ExternalArtTemplateFinder` to build full responses.

- [ ] **Step 1: Write failing system test (uses a Spring-created PlayerCharacter from existing fixture or creates one)**

Note: `PlayerCharacterControllerSystemTest` likely already creates characters; reuse the approach (POST `/api/v1/player-characters` or `PlayerCharacterService.createCharacter` via `@Autowired`).

```java
package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.config.SecurityMockMvcConfiguration;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.WeaponType;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.PlayerCharacterService;
import com.jefflife.mudmk2.gamedata.application.service.model.request.*;
import com.jefflife.mudmk2.gamedata.application.service.provided.ExternalArtTemplateCreator;
import com.jefflife.mudmk2.gamedata.application.service.provided.MentalMethodTemplateCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
class CharacterMartialArtControllerSystemTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PlayerCharacterService playerCharacterService;
    @Autowired MentalMethodTemplateCreator mentalCreator;
    @Autowired ExternalArtTemplateCreator externalCreator;

    private UUID createPc() {
        long userId = System.nanoTime();
        PlayerCharacter pc = playerCharacterService.createCharacter(userId, "테스터" + userId, CharacterClass.WARRIOR);
        return pc.getId();
    }

    private Long createMentalTpl() {
        return mentalCreator.create(new MentalMethodTemplateRequest("심법", "d", MentalMethodKind.INNER_POWER, 1,
                List.of(new MentalMethodLevelEffectRequest(1,
                        List.of(new StatModifierRequest(StatType.INNER_POWER, 3)))))).getId();
    }

    private Long createExternalTpl() {
        return externalCreator.create(new ExternalArtTemplateRequest("외공", "d", WeaponType.SWORD, 1,
                List.of(new ExternalArtLevelEffectRequest(1, 1.1, 5, 5, 0)))).getId();
    }

    @Test
    void learnMental_thenStatus_returnsLearned() throws Exception {
        UUID pc = createPc();
        Long tpl = createMentalTpl();
        String body = "{\"templateId\":" + tpl + "}";

        mockMvc.perform(post("/api/v1/player-characters/" + pc + "/martial-arts/mental-methods/learn")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/player-characters/" + pc + "/martial-arts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.learnedMentalMethods.length()").value(1));
    }

    @Test
    void learnMentalTwice_returns409() throws Exception {
        UUID pc = createPc();
        Long tpl = createMentalTpl();
        String body = "{\"templateId\":" + tpl + "}";

        mockMvc.perform(post("/api/v1/player-characters/" + pc + "/martial-arts/mental-methods/learn")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/player-characters/" + pc + "/martial-arts/mental-methods/learn")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
    }

    @Test
    void equipExternal_appendsSlot() throws Exception {
        UUID pc = createPc();
        Long tpl = createExternalTpl();

        // Learn first
        mockMvc.perform(post("/api/v1/player-characters/" + pc + "/martial-arts/external-arts/learn")
                .contentType(MediaType.APPLICATION_JSON).content("{\"templateId\":" + tpl + "}"))
                .andExpect(status().isCreated());

        // Read learnedId
        String json = mockMvc.perform(get("/api/v1/player-characters/" + pc + "/martial-arts"))
                .andReturn().getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
        String learnedId = node.path("learnedExternalArts").get(0).path("id").asText();

        // Equip
        mockMvc.perform(post("/api/v1/player-characters/" + pc + "/martial-arts/external-arts/equip")
                .contentType(MediaType.APPLICATION_JSON).content("{\"learnedId\":\"" + learnedId + "\"}"))
                .andExpect(status().isOk());

        // Verify slot
        mockMvc.perform(get("/api/v1/player-characters/" + pc + "/martial-arts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equippedExternalSlots.length()").value(1));
    }

    @Test
    void unknownCharacter_returns404OnGet() throws Exception {
        UUID pc = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/player-characters/" + pc + "/martial-arts"))
                .andExpect(status().isNotFound());
    }
}
```

- [ ] **Step 2: Confirm failure**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.adapter.webapi.CharacterMartialArtControllerSystemTest"`

- [ ] **Step 3: Implement controller**

```java
package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.adapter.webapi.response.CharacterMartialArtResponse;
import com.jefflife.mudmk2.gamedata.application.domain.model.martialart.MentalMethodKind;
import com.jefflife.mudmk2.gamedata.application.service.exception.AlreadyLearnedException;
import com.jefflife.mudmk2.gamedata.application.service.exception.MartialArtSlotFullException;
import com.jefflife.mudmk2.gamedata.application.service.exception.NotLearnedException;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/player-characters/{pcId}/martial-arts")
public class CharacterMartialArtController {

    private final MartialArtLearner learner;
    private final MartialArtEquipper equipper;
    private final LearnedMartialArtFinder finder;
    private final MentalMethodTemplateFinder mentalFinder;
    private final ExternalArtTemplateFinder externalFinder;

    public CharacterMartialArtController(MartialArtLearner learner,
                                         MartialArtEquipper equipper,
                                         LearnedMartialArtFinder finder,
                                         MentalMethodTemplateFinder mentalFinder,
                                         ExternalArtTemplateFinder externalFinder) {
        this.learner = learner;
        this.equipper = equipper;
        this.finder = finder;
        this.mentalFinder = mentalFinder;
        this.externalFinder = externalFinder;
    }

    public record LearnRequest(Long templateId) {}
    public record EquipRequest(UUID learnedId) {}

    @GetMapping
    public ResponseEntity<CharacterMartialArtResponse> status(@PathVariable UUID pcId) {
        try {
            return ResponseEntity.ok(CharacterMartialArtResponse.from(
                    finder.findByCharacter(pcId),
                    id -> mentalFinder.findById(id),
                    id -> externalFinder.findById(id)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/mental-methods/learn")
    public ResponseEntity<Void> learnMental(@PathVariable UUID pcId, @RequestBody LearnRequest req) {
        try {
            learner.learnMentalMethod(pcId, req.templateId());
            return ResponseEntity.status(201).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (AlreadyLearnedException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/external-arts/learn")
    public ResponseEntity<Void> learnExternal(@PathVariable UUID pcId, @RequestBody LearnRequest req) {
        try {
            learner.learnExternalArt(pcId, req.templateId());
            return ResponseEntity.status(201).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (AlreadyLearnedException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/mental-methods/equip")
    public ResponseEntity<Void> equipMental(@PathVariable UUID pcId, @RequestBody EquipRequest req) {
        try {
            equipper.equipMentalMethod(pcId, req.learnedId());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (NotLearnedException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/external-arts/equip")
    public ResponseEntity<Void> equipExternal(@PathVariable UUID pcId, @RequestBody EquipRequest req) {
        try {
            equipper.equipExternalArt(pcId, req.learnedId());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (NotLearnedException e) {
            return ResponseEntity.badRequest().build();
        } catch (MartialArtSlotFullException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @DeleteMapping("/mental-methods/{kind}")
    public ResponseEntity<Void> unequipMental(@PathVariable UUID pcId, @PathVariable MentalMethodKind kind) {
        try {
            equipper.unequipMentalMethod(pcId, kind);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/external-arts/{learnedId}")
    public ResponseEntity<Void> unequipExternal(@PathVariable UUID pcId, @PathVariable UUID learnedId) {
        try {
            equipper.unequipExternalArt(pcId, learnedId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
```

- [ ] **Step 4: Tests pass, commit**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.adapter.webapi.CharacterMartialArtControllerSystemTest"
git add src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/CharacterMartialArtController.java \
        src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/CharacterMartialArtControllerSystemTest.java
git commit -m "feat(martial-arts): CharacterMartialArtController + system test"
```

---

## Task 23: Web routes + admin.html menu links

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/web/IndexController.java`
- Modify: `src/main/resources/templates/web/admin.html`

- [ ] **Step 1: Add 3 GET mappings to IndexController**

Append below the existing `itemInstanceManagement` method:

```java
    @GetMapping("/mental-method-template-management")
    public String mentalMethodTemplateManagement(Model model, @LoginUser SessionUser user) {
        if (user != null) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("userPicture", user.getPicture());
        }
        return "web/mental-method-template-management";
    }

    @GetMapping("/external-art-template-management")
    public String externalArtTemplateManagement(Model model, @LoginUser SessionUser user) {
        if (user != null) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("userPicture", user.getPicture());
        }
        return "web/external-art-template-management";
    }

    @GetMapping("/character-martial-art-management")
    public String characterMartialArtManagement(Model model, @LoginUser SessionUser user) {
        if (user != null) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("userPicture", user.getPicture());
        }
        return "web/character-martial-art-management";
    }
```

- [ ] **Step 2: Add 3 admin cards to admin.html**

In `admin.html`, after the `<div class="admin-card">` for `ITEM INSTANCES` (the last admin-card before `</div>` closing `admin-grid`), insert:

```html
                            <div class="admin-card">
                                <div class="admin-icon">🧘</div>
                                <h4 class="admin-card-title">MENTAL METHODS</h4>
                                <p class="admin-card-description">
                                    심법 템플릿 CRUD (내공/경공/절기 3종).
                                </p>
                                <a href="/mental-method-template-management" class="btn btn-admin">MANAGE 심법</a>
                            </div>
                            <div class="admin-card">
                                <div class="admin-icon">🥋</div>
                                <h4 class="admin-card-title">EXTERNAL ARTS</h4>
                                <p class="admin-card-description">
                                    외공 템플릿 CRUD (검/도/권/사/기/장 6종).
                                </p>
                                <a href="/external-art-template-management" class="btn btn-admin">MANAGE 외공</a>
                            </div>
                            <div class="admin-card">
                                <div class="admin-icon">🎓</div>
                                <h4 class="admin-card-title">CHARACTER MARTIAL ARTS</h4>
                                <p class="admin-card-description">
                                    캐릭터에게 무공 학습 부여 및 장착 슬롯 관리.
                                </p>
                                <a href="/character-martial-art-management" class="btn btn-admin">MANAGE 무공</a>
                            </div>
```

- [ ] **Step 3: Build & commit**

```bash
./gradlew compileJava
git add src/main/java/com/jefflife/mudmk2/web/IndexController.java \
        src/main/resources/templates/web/admin.html
git commit -m "feat(martial-arts): admin routes + menu cards"
```

---

## Task 24: mental-method-template-management.html

**Files:**
- Create: `src/main/resources/templates/web/mental-method-template-management.html`

Reference structure: `item-template-management.html`. The page is a single-file Thymeleaf doc that uses fetch() against `/api/v1/mental-method-templates`. Below is the complete file — paste verbatim.

- [ ] **Step 1: Create the file**

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Mental Method Template Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #0a0b10; color: #e4e4e7; font-family: 'IBM Plex Mono', monospace; }
        .page-header { background: #0d0e14; border-bottom: 1px solid #1e2030; padding: 12px 20px; }
        .table-dark { --bs-table-bg: #0d0e14; --bs-table-border-color: #1e2030; }
        .form-panel { background: #0d0e14; border: 1px solid #1e2030; border-radius: 6px; padding: 20px; }
        .level-block { border: 1px solid #1e3a5f; padding: 10px; margin-bottom: 6px; border-radius: 4px; background: #111320; }
        .stat-row { display: flex; gap: 8px; align-items: center; margin-bottom: 6px; }
        .btn-green { background: #4ade80; color: #000; border: none; font-weight: bold; }
        .btn-green:hover { background: #22c55e; color: #000; }
        input, select, textarea { background: #1a1c24 !important; color: #e4e4e7 !important; border-color: #333 !important; }
    </style>
</head>
<body>
<div class="page-header d-flex justify-content-between align-items-center">
    <span style="color:#4ade80;font-weight:bold;">🧘 MENTAL METHOD TEMPLATE MANAGEMENT</span>
    <a href="/admin" style="color:#aaa;font-size:12px;">← Admin 메뉴</a>
</div>

<div class="container-fluid p-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <button class="btn btn-green btn-sm" onclick="showCreateForm()">+ 새 심법</button>
        <select id="kindFilter" class="form-select form-select-sm" style="width:200px" onchange="loadList()">
            <option value="">모든 종류</option>
            <option value="INNER_POWER">내공 (INNER_POWER)</option>
            <option value="LIGHT_STEP">경공 (LIGHT_STEP)</option>
            <option value="SPECIAL_TECHNIQUE">절기 (SPECIAL_TECHNIQUE)</option>
        </select>
    </div>

    <table class="table table-dark table-sm table-bordered mb-4">
        <thead><tr style="color:#4ade80">
            <th>ID</th><th>이름</th><th>종류</th><th>최대 레벨</th><th>작업</th>
        </tr></thead>
        <tbody id="listBody"></tbody>
    </table>

    <div id="formPanel" class="form-panel" style="display:none">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <span style="color:#4ade80;font-weight:bold;" id="formTitle">◆ 새 심법</span>
            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="hideForm()">취소</button>
        </div>
        <input type="hidden" id="editId">

        <div class="row g-3 mb-3">
            <div class="col-md-4">
                <label class="form-label small">이름 *</label>
                <input id="name" class="form-control form-control-sm" placeholder="천뢰신공">
            </div>
            <div class="col-md-3">
                <label class="form-label small">종류 *</label>
                <select id="kind" class="form-select form-select-sm">
                    <option value="INNER_POWER">INNER_POWER (내공)</option>
                    <option value="LIGHT_STEP">LIGHT_STEP (경공)</option>
                    <option value="SPECIAL_TECHNIQUE">SPECIAL_TECHNIQUE (절기)</option>
                </select>
            </div>
            <div class="col-md-2">
                <label class="form-label small">최대 레벨 *</label>
                <input type="number" id="maxLevel" class="form-control form-control-sm" min="1" value="1" onchange="syncLevels()">
            </div>
        </div>

        <div class="mb-3">
            <label class="form-label small">설명</label>
            <textarea id="description" class="form-control form-control-sm" rows="2"></textarea>
        </div>

        <div id="levelsContainer"></div>

        <div class="mt-3">
            <button class="btn btn-green btn-sm" onclick="save()">저장</button>
        </div>
    </div>
</div>

<script>
const BASE = '/api/v1/mental-method-templates';
const STAT_TYPES = ['VIGOR','PHYSIQUE','AGILITY','INTELLECT','WILL','MERIDIAN',
    'INNER_POWER','SPECIAL_TECHNIQUE','LIGHT_STEP',
    'FISTS_AND_PALMS','SWORD_METHOD','BLADE_METHOD',
    'LONG_WEAPON','ESOTERIC_WEAPON','ARCHERY'];

async function loadList() {
    const kind = document.getElementById('kindFilter').value;
    const url = kind ? `${BASE}?kind=${kind}` : BASE;
    const res = await fetch(url);
    const items = await res.json();
    const tbody = document.getElementById('listBody');
    tbody.innerHTML = items.map(t => `
        <tr>
            <td>${t.id}</td><td>${t.name}</td><td>${t.kind}</td><td>${t.maxLevel}</td>
            <td>
                <button class="btn btn-sm btn-outline-info" onclick="loadForEdit(${t.id})">편집</button>
                <button class="btn btn-sm btn-outline-danger" onclick="del(${t.id})">삭제</button>
            </td>
        </tr>`).join('');
}

function showCreateForm() {
    document.getElementById('editId').value = '';
    document.getElementById('formTitle').textContent = '◆ 새 심법';
    document.getElementById('name').value = '';
    document.getElementById('description').value = '';
    document.getElementById('kind').value = 'INNER_POWER';
    document.getElementById('maxLevel').value = 1;
    syncLevels();
    document.getElementById('formPanel').style.display = 'block';
}

function hideForm() { document.getElementById('formPanel').style.display = 'none'; }

function syncLevels() {
    const max = parseInt(document.getElementById('maxLevel').value) || 1;
    const container = document.getElementById('levelsContainer');
    const current = container.children.length;
    if (max > current) {
        for (let l = current + 1; l <= max; l++) container.appendChild(makeLevelBlock(l));
    } else if (max < current) {
        for (let i = current - 1; i >= max; i--) container.removeChild(container.children[i]);
    }
}

function makeLevelBlock(level) {
    const div = document.createElement('div');
    div.className = 'level-block';
    div.innerHTML = `
        <div style="color:#4ade80;margin-bottom:6px">레벨 ${level}</div>
        <div class="stat-rows"></div>
        <button class="btn btn-sm btn-outline-success" onclick="addStat(this)">+ 스탯 추가</button>
    `;
    div.dataset.level = level;
    return div;
}

function addStat(btn) {
    const container = btn.previousElementSibling;
    const row = document.createElement('div');
    row.className = 'stat-row';
    row.innerHTML = `
        <select class="form-select form-select-sm" style="width:200px">
            ${STAT_TYPES.map(s => `<option>${s}</option>`).join('')}
        </select>
        <input type="number" class="form-control form-control-sm" style="width:100px" value="0">
        <button class="btn btn-sm btn-outline-danger" onclick="this.parentElement.remove()">x</button>`;
    container.appendChild(row);
}

function collectPayload() {
    const levels = Array.from(document.getElementById('levelsContainer').children).map(div => {
        const level = parseInt(div.dataset.level);
        const statModifiers = Array.from(div.querySelectorAll('.stat-row')).map(r => ({
            statType: r.children[0].value,
            value: parseInt(r.children[1].value) || 0
        }));
        return { level, statModifiers };
    });
    return {
        name: document.getElementById('name').value,
        description: document.getElementById('description').value,
        kind: document.getElementById('kind').value,
        maxLevel: parseInt(document.getElementById('maxLevel').value) || 1,
        levelEffects: levels
    };
}

async function save() {
    const id = document.getElementById('editId').value;
    const payload = collectPayload();
    const url = id ? `${BASE}/${id}` : BASE;
    const method = id ? 'PUT' : 'POST';
    const res = await fetch(url, {
        method, headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });
    if (!res.ok) { alert('저장 실패: ' + res.status); return; }
    hideForm();
    loadList();
}

async function loadForEdit(id) {
    const res = await fetch(`${BASE}/${id}`);
    const t = await res.json();
    document.getElementById('editId').value = t.id;
    document.getElementById('formTitle').textContent = `◆ 편집: ${t.name}`;
    document.getElementById('name').value = t.name;
    document.getElementById('description').value = t.description || '';
    document.getElementById('kind').value = t.kind;
    document.getElementById('maxLevel').value = t.maxLevel;
    document.getElementById('levelsContainer').innerHTML = '';
    syncLevels();
    t.levelEffects.forEach((le, idx) => {
        const block = document.getElementById('levelsContainer').children[idx];
        const rows = block.querySelector('.stat-rows');
        le.statModifiers.forEach(sm => {
            const row = document.createElement('div');
            row.className = 'stat-row';
            row.innerHTML = `
                <select class="form-select form-select-sm" style="width:200px">
                    ${STAT_TYPES.map(s => `<option ${s === sm.statType ? 'selected' : ''}>${s}</option>`).join('')}
                </select>
                <input type="number" class="form-control form-control-sm" style="width:100px" value="${sm.value}">
                <button class="btn btn-sm btn-outline-danger" onclick="this.parentElement.remove()">x</button>`;
            rows.appendChild(row);
        });
    });
    document.getElementById('formPanel').style.display = 'block';
}

async function del(id) {
    if (!confirm(`ID ${id} 삭제?`)) return;
    const res = await fetch(`${BASE}/${id}`, { method: 'DELETE' });
    if (res.status === 409) { alert('학습된 캐릭터가 있어 삭제할 수 없습니다.'); return; }
    if (!res.ok) { alert('삭제 실패: ' + res.status); return; }
    loadList();
}

loadList();
</script>
</body>
</html>
```

- [ ] **Step 2: Manual verification**

Start the app (`./gradlew bootRun`), open `/mental-method-template-management`, create a 2-level 심법 with 2 stat modifiers on level 2. Verify saved and reloaded with same data.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/web/mental-method-template-management.html
git commit -m "feat(martial-arts): mental method template admin page"
```

---

## Task 25: external-art-template-management.html

**Files:**
- Create: `src/main/resources/templates/web/external-art-template-management.html`

Mirror of Task 24, but per-level fields are `damageMultiplier / cooldownSeconds / apCost / mpCost` instead of StatModifier rows.

- [ ] **Step 1: Create the file**

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>External Art Template Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #0a0b10; color: #e4e4e7; font-family: 'IBM Plex Mono', monospace; }
        .page-header { background: #0d0e14; border-bottom: 1px solid #1e2030; padding: 12px 20px; }
        .form-panel { background: #0d0e14; border: 1px solid #1e2030; border-radius: 6px; padding: 20px; }
        .level-block { border: 1px solid #1e3a5f; padding: 10px; margin-bottom: 6px; border-radius: 4px; background: #111320; }
        .btn-green { background: #4ade80; color: #000; border: none; font-weight: bold; }
        input, select { background: #1a1c24 !important; color: #e4e4e7 !important; border-color: #333 !important; }
    </style>
</head>
<body>
<div class="page-header d-flex justify-content-between align-items-center">
    <span style="color:#4ade80;font-weight:bold;">🥋 EXTERNAL ART TEMPLATE MANAGEMENT</span>
    <a href="/admin" style="color:#aaa;font-size:12px;">← Admin 메뉴</a>
</div>

<div class="container-fluid p-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <button class="btn btn-green btn-sm" onclick="showCreateForm()">+ 새 외공</button>
        <select id="typeFilter" class="form-select form-select-sm" style="width:200px" onchange="loadList()">
            <option value="">모든 무기 타입</option>
            <option value="SWORD">SWORD (검)</option>
            <option value="BLADE">BLADE (도)</option>
            <option value="FIST">FIST (권/장)</option>
            <option value="ARCHERY">ARCHERY (사)</option>
            <option value="ESOTERIC">ESOTERIC (기문)</option>
            <option value="LONG_WEAPON">LONG_WEAPON (창/봉)</option>
        </select>
    </div>

    <table class="table table-dark table-sm table-bordered mb-4">
        <thead><tr style="color:#4ade80">
            <th>ID</th><th>이름</th><th>무기 타입</th><th>최대 레벨</th><th>작업</th>
        </tr></thead>
        <tbody id="listBody"></tbody>
    </table>

    <div id="formPanel" class="form-panel" style="display:none">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <span style="color:#4ade80;font-weight:bold;" id="formTitle">◆ 새 외공</span>
            <button class="btn btn-sm btn-outline-secondary" onclick="hideForm()">취소</button>
        </div>
        <input type="hidden" id="editId">

        <div class="row g-3 mb-3">
            <div class="col-md-4">
                <label class="form-label small">이름 *</label>
                <input id="name" class="form-control form-control-sm" placeholder="발검술">
            </div>
            <div class="col-md-3">
                <label class="form-label small">무기 타입 *</label>
                <select id="weaponType" class="form-select form-select-sm">
                    <option value="SWORD">SWORD</option><option value="BLADE">BLADE</option>
                    <option value="FIST">FIST</option><option value="ARCHERY">ARCHERY</option>
                    <option value="ESOTERIC">ESOTERIC</option><option value="LONG_WEAPON">LONG_WEAPON</option>
                </select>
            </div>
            <div class="col-md-2">
                <label class="form-label small">최대 레벨 *</label>
                <input type="number" id="maxLevel" class="form-control form-control-sm" min="1" value="1" onchange="syncLevels()">
            </div>
        </div>

        <div class="mb-3">
            <label class="form-label small">설명</label>
            <textarea id="description" class="form-control form-control-sm" rows="2"></textarea>
        </div>

        <div id="levelsContainer"></div>

        <div class="mt-3">
            <button class="btn btn-green btn-sm" onclick="save()">저장</button>
        </div>
    </div>
</div>

<script>
const BASE = '/api/v1/external-art-templates';

async function loadList() {
    const t = document.getElementById('typeFilter').value;
    const url = t ? `${BASE}?weaponType=${t}` : BASE;
    const items = await (await fetch(url)).json();
    document.getElementById('listBody').innerHTML = items.map(x => `
        <tr>
            <td>${x.id}</td><td>${x.name}</td><td>${x.weaponType}</td><td>${x.maxLevel}</td>
            <td>
                <button class="btn btn-sm btn-outline-info" onclick="loadForEdit(${x.id})">편집</button>
                <button class="btn btn-sm btn-outline-danger" onclick="del(${x.id})">삭제</button>
            </td>
        </tr>`).join('');
}

function showCreateForm() {
    document.getElementById('editId').value = '';
    document.getElementById('formTitle').textContent = '◆ 새 외공';
    document.getElementById('name').value = '';
    document.getElementById('description').value = '';
    document.getElementById('weaponType').value = 'SWORD';
    document.getElementById('maxLevel').value = 1;
    syncLevels();
    document.getElementById('formPanel').style.display = 'block';
}

function hideForm() { document.getElementById('formPanel').style.display = 'none'; }

function syncLevels() {
    const max = parseInt(document.getElementById('maxLevel').value) || 1;
    const c = document.getElementById('levelsContainer');
    const cur = c.children.length;
    if (max > cur) for (let l = cur + 1; l <= max; l++) c.appendChild(makeLevelBlock(l));
    else if (max < cur) for (let i = cur - 1; i >= max; i--) c.removeChild(c.children[i]);
}

function makeLevelBlock(level) {
    const div = document.createElement('div');
    div.className = 'level-block';
    div.dataset.level = level;
    div.innerHTML = `
        <div style="color:#4ade80;margin-bottom:6px">레벨 ${level}</div>
        <div class="row g-2">
            <div class="col-3"><label class="form-label small">damageMultiplier</label>
                <input type="number" step="0.01" class="form-control form-control-sm dmgmul" value="1.0"></div>
            <div class="col-3"><label class="form-label small">cooldownSeconds</label>
                <input type="number" class="form-control form-control-sm cdsec" value="5"></div>
            <div class="col-3"><label class="form-label small">apCost</label>
                <input type="number" class="form-control form-control-sm apc" value="5"></div>
            <div class="col-3"><label class="form-label small">mpCost</label>
                <input type="number" class="form-control form-control-sm mpc" value="0"></div>
        </div>`;
    return div;
}

function collectPayload() {
    const levels = Array.from(document.getElementById('levelsContainer').children).map(div => ({
        level: parseInt(div.dataset.level),
        damageMultiplier: parseFloat(div.querySelector('.dmgmul').value) || 0,
        cooldownSeconds: parseInt(div.querySelector('.cdsec').value) || 0,
        apCost: parseInt(div.querySelector('.apc').value) || 0,
        mpCost: parseInt(div.querySelector('.mpc').value) || 0
    }));
    return {
        name: document.getElementById('name').value,
        description: document.getElementById('description').value,
        weaponType: document.getElementById('weaponType').value,
        maxLevel: parseInt(document.getElementById('maxLevel').value) || 1,
        levelEffects: levels
    };
}

async function save() {
    const id = document.getElementById('editId').value;
    const url = id ? `${BASE}/${id}` : BASE;
    const method = id ? 'PUT' : 'POST';
    const res = await fetch(url, {
        method, headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(collectPayload())
    });
    if (!res.ok) { alert('저장 실패: ' + res.status); return; }
    hideForm(); loadList();
}

async function loadForEdit(id) {
    const t = await (await fetch(`${BASE}/${id}`)).json();
    document.getElementById('editId').value = t.id;
    document.getElementById('formTitle').textContent = `◆ 편집: ${t.name}`;
    document.getElementById('name').value = t.name;
    document.getElementById('description').value = t.description || '';
    document.getElementById('weaponType').value = t.weaponType;
    document.getElementById('maxLevel').value = t.maxLevel;
    document.getElementById('levelsContainer').innerHTML = '';
    syncLevels();
    t.levelEffects.forEach((le, idx) => {
        const b = document.getElementById('levelsContainer').children[idx];
        b.querySelector('.dmgmul').value = le.damageMultiplier;
        b.querySelector('.cdsec').value = le.cooldownSeconds;
        b.querySelector('.apc').value = le.apCost;
        b.querySelector('.mpc').value = le.mpCost;
    });
    document.getElementById('formPanel').style.display = 'block';
}

async function del(id) {
    if (!confirm(`ID ${id} 삭제?`)) return;
    const res = await fetch(`${BASE}/${id}`, { method: 'DELETE' });
    if (res.status === 409) { alert('학습된 캐릭터가 있어 삭제할 수 없습니다.'); return; }
    if (!res.ok) { alert('삭제 실패: ' + res.status); return; }
    loadList();
}

loadList();
</script>
</body>
</html>
```

- [ ] **Step 2: Manual verification & commit**

Start `./gradlew bootRun`, open `/external-art-template-management`, create a 2-level 외공. Verify edit/delete works.

```bash
git add src/main/resources/templates/web/external-art-template-management.html
git commit -m "feat(martial-arts): external art template admin page"
```

---

## Task 26: character-martial-art-management.html

**Files:**
- Create: `src/main/resources/templates/web/character-martial-art-management.html`

This page has 3 panes: (a) character search, (b) selected character's learned & equipped state, (c) template search + "이 캐릭터에게 학습시키기".

- [ ] **Step 1: Create the file**

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Character Martial Art Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #0a0b10; color: #e4e4e7; font-family: 'IBM Plex Mono', monospace; }
        .page-header { background: #0d0e14; border-bottom: 1px solid #1e2030; padding: 12px 20px; }
        .panel { background: #0d0e14; border: 1px solid #1e2030; border-radius: 6px; padding: 16px; height: 100%; }
        .table-dark { --bs-table-bg: #0d0e14; --bs-table-border-color: #1e2030; }
        .slot { display: inline-block; padding: 4px 8px; margin: 2px; background: #111320; border: 1px solid #1e3a5f; border-radius: 4px; }
        input, select { background: #1a1c24 !important; color: #e4e4e7 !important; border-color: #333 !important; }
        .btn-green { background: #4ade80; color: #000; border: none; font-weight: bold; }
    </style>
</head>
<body>
<div class="page-header d-flex justify-content-between align-items-center">
    <span style="color:#4ade80;font-weight:bold;">🎓 CHARACTER MARTIAL ART MANAGEMENT</span>
    <a href="/admin" style="color:#aaa;font-size:12px;">← Admin 메뉴</a>
</div>

<div class="container-fluid p-4">
    <div class="row g-3 mb-3">
        <div class="col-md-5">
            <div class="panel">
                <h6 style="color:#4ade80">캐릭터 검색</h6>
                <input id="charSearch" class="form-control form-control-sm mb-2" placeholder="닉네임 일부..." oninput="searchChars()">
                <table class="table table-dark table-sm">
                    <thead><tr><th>ID</th><th>닉네임</th><th>클래스</th><th></th></tr></thead>
                    <tbody id="charList"></tbody>
                </table>
            </div>
        </div>
        <div class="col-md-7">
            <div class="panel">
                <h6 style="color:#4ade80">선택 캐릭터 상태</h6>
                <div id="charStatus">캐릭터를 선택하세요.</div>
            </div>
        </div>
    </div>

    <div class="panel">
        <h6 style="color:#4ade80">템플릿 검색 → 학습 부여</h6>
        <ul class="nav nav-tabs mb-2">
            <li class="nav-item"><a class="nav-link active" href="#" onclick="setTab('mental');return false">심법</a></li>
            <li class="nav-item"><a class="nav-link" href="#" onclick="setTab('external');return false">외공</a></li>
        </ul>
        <input id="tplSearch" class="form-control form-control-sm mb-2" placeholder="이름 일부..." oninput="searchTpls()">
        <table class="table table-dark table-sm">
            <thead id="tplHead"></thead>
            <tbody id="tplList"></tbody>
        </table>
    </div>
</div>

<script>
let selectedPc = null;
let tab = 'mental';

async function searchChars() {
    const q = document.getElementById('charSearch').value;
    if (!q) { document.getElementById('charList').innerHTML = ''; return; }
    // PlayerCharacterController endpoint assumed at /api/v1/player-characters?name=
    const res = await fetch(`/api/v1/player-characters?name=${encodeURIComponent(q)}`);
    if (!res.ok) { document.getElementById('charList').innerHTML = '<tr><td colspan="4">검색 실패</td></tr>'; return; }
    const items = await res.json();
    document.getElementById('charList').innerHTML = items.map(c => `
        <tr>
            <td style="font-size:10px">${c.id}</td>
            <td>${c.nickname}</td>
            <td>${c.characterClass || ''}</td>
            <td><button class="btn btn-sm btn-green" onclick="selectChar('${c.id}','${c.nickname}')">선택</button></td>
        </tr>`).join('');
}

async function selectChar(id, nickname) {
    selectedPc = id;
    await refreshStatus(nickname);
}

async function refreshStatus(nickname) {
    if (!selectedPc) return;
    const res = await fetch(`/api/v1/player-characters/${selectedPc}/martial-arts`);
    if (!res.ok) { document.getElementById('charStatus').textContent = '조회 실패'; return; }
    const v = await res.json();
    const mentalRows = v.learnedMentalMethods.map(l => `
        <tr><td>${l.templateName}</td><td>${l.kind}</td><td>Lv${l.currentLevel}</td>
        <td>
            <button class="btn btn-sm btn-outline-info" onclick="equipMental('${l.id}')">장착</button>
        </td></tr>`).join('') || '<tr><td colspan="4">없음</td></tr>';
    const externalRows = v.learnedExternalArts.map(l => `
        <tr><td>${l.templateName}</td><td>${l.weaponType}</td><td>Lv${l.currentLevel}</td>
        <td>
            <button class="btn btn-sm btn-outline-info" onclick="equipExternal('${l.id}')">장착</button>
        </td></tr>`).join('') || '<tr><td colspan="4">없음</td></tr>';
    const mentalSlots = Object.entries(v.equippedMentalSlots || {}).map(([k, id]) => `
        <span class="slot">${k}: ${id.substring(0,8)}
            <button class="btn btn-sm btn-link p-0 ms-1" style="color:#f87171" onclick="unequipMental('${k}')">✕</button>
        </span>`).join('') || '<span class="text-muted">없음</span>';
    const externalSlots = (v.equippedExternalSlots || []).map(id => `
        <span class="slot">${id.substring(0,8)}
            <button class="btn btn-sm btn-link p-0 ms-1" style="color:#f87171" onclick="unequipExternal('${id}')">✕</button>
        </span>`).join('') || '<span class="text-muted">없음</span>';

    document.getElementById('charStatus').innerHTML = `
        <div class="mb-2"><b>${nickname || selectedPc}</b></div>
        <div class="mb-2"><b style="color:#4ade80">심법 슬롯:</b> ${mentalSlots}</div>
        <div class="mb-3"><b style="color:#4ade80">외공 슬롯 (${(v.equippedExternalSlots||[]).length}/6):</b> ${externalSlots}</div>
        <div class="mb-2"><b style="color:#60a5fa">학습한 심법</b>
            <table class="table table-dark table-sm"><thead><tr><th>이름</th><th>종류</th><th>레벨</th><th></th></tr></thead><tbody>${mentalRows}</tbody></table>
        </div>
        <div><b style="color:#60a5fa">학습한 외공</b>
            <table class="table table-dark table-sm"><thead><tr><th>이름</th><th>무기</th><th>레벨</th><th></th></tr></thead><tbody>${externalRows}</tbody></table>
        </div>
    `;
    window._lastNick = nickname;
}

function setTab(t) {
    tab = t;
    document.querySelectorAll('.nav-link').forEach(a => a.classList.remove('active'));
    event.target.classList.add('active');
    searchTpls();
}

async function searchTpls() {
    const q = document.getElementById('tplSearch').value;
    const base = tab === 'mental' ? '/api/v1/mental-method-templates' : '/api/v1/external-art-templates';
    const url = q ? `${base}?name=${encodeURIComponent(q)}` : base;
    const items = await (await fetch(url)).json();
    if (tab === 'mental') {
        document.getElementById('tplHead').innerHTML = '<tr><th>ID</th><th>이름</th><th>종류</th><th>최대레벨</th><th></th></tr>';
        document.getElementById('tplList').innerHTML = items.map(t => `
            <tr><td>${t.id}</td><td>${t.name}</td><td>${t.kind}</td><td>${t.maxLevel}</td>
            <td><button class="btn btn-sm btn-green" onclick="learnMental(${t.id})">학습시키기</button></td></tr>`).join('');
    } else {
        document.getElementById('tplHead').innerHTML = '<tr><th>ID</th><th>이름</th><th>무기</th><th>최대레벨</th><th></th></tr>';
        document.getElementById('tplList').innerHTML = items.map(t => `
            <tr><td>${t.id}</td><td>${t.name}</td><td>${t.weaponType}</td><td>${t.maxLevel}</td>
            <td><button class="btn btn-sm btn-green" onclick="learnExternal(${t.id})">학습시키기</button></td></tr>`).join('');
    }
}

async function postJson(url, body) {
    const res = await fetch(url, {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify(body || {})
    });
    return res;
}

async function learnMental(tplId) {
    if (!selectedPc) { alert('캐릭터를 먼저 선택하세요'); return; }
    const res = await postJson(`/api/v1/player-characters/${selectedPc}/martial-arts/mental-methods/learn`, {templateId: tplId});
    if (res.status === 409) { alert('이미 학습했습니다'); return; }
    if (!res.ok) { alert('실패: ' + res.status); return; }
    refreshStatus(window._lastNick);
}

async function learnExternal(tplId) {
    if (!selectedPc) { alert('캐릭터를 먼저 선택하세요'); return; }
    const res = await postJson(`/api/v1/player-characters/${selectedPc}/martial-arts/external-arts/learn`, {templateId: tplId});
    if (res.status === 409) { alert('이미 학습했습니다'); return; }
    if (!res.ok) { alert('실패: ' + res.status); return; }
    refreshStatus(window._lastNick);
}

async function equipMental(learnedId) {
    const res = await postJson(`/api/v1/player-characters/${selectedPc}/martial-arts/mental-methods/equip`, {learnedId});
    if (!res.ok) { alert('장착 실패: ' + res.status); return; }
    refreshStatus(window._lastNick);
}

async function equipExternal(learnedId) {
    const res = await postJson(`/api/v1/player-characters/${selectedPc}/martial-arts/external-arts/equip`, {learnedId});
    if (res.status === 409) { alert('슬롯이 가득 찼습니다 (최대 6)'); return; }
    if (!res.ok) { alert('장착 실패: ' + res.status); return; }
    refreshStatus(window._lastNick);
}

async function unequipMental(kind) {
    const res = await fetch(`/api/v1/player-characters/${selectedPc}/martial-arts/mental-methods/${kind}`, {method:'DELETE'});
    if (!res.ok) { alert('해제 실패: ' + res.status); return; }
    refreshStatus(window._lastNick);
}

async function unequipExternal(learnedId) {
    const res = await fetch(`/api/v1/player-characters/${selectedPc}/martial-arts/external-arts/${learnedId}`, {method:'DELETE'});
    if (!res.ok) { alert('해제 실패: ' + res.status); return; }
    refreshStatus(window._lastNick);
}

searchTpls();
</script>
</body>
</html>
```

- [ ] **Step 2: Verify PlayerCharacter REST endpoint for `?name=` search exists**

Run: `grep -n "name=" src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/PlayerCharacterController.java`
If `?name=` endpoint is missing, the easier fix is for the JS to call existing endpoints (check PlayerCharacterController for available search params). Adapt the JS `searchChars()` URL to whatever PlayerCharacterController supports (usually `?nickname=`).

- [ ] **Step 3: Manual verification**

`./gradlew bootRun` → `/character-martial-art-management`. Search for an existing character, learn a 심법, equip, unequip. Slot count chip should update.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/templates/web/character-martial-art-management.html
git commit -m "feat(martial-arts): character martial art admin page"
```

---

## Task 27: Final full build & end-to-end smoke

- [ ] **Step 1: Full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL. All tests pass.

- [ ] **Step 2: Smoke test via curl (optional sanity)**

```bash
# Create mental template
curl -s -X POST localhost:8080/api/v1/mental-method-templates \
  -H 'Content-Type: application/json' \
  -d '{"name":"천뢰신공","description":"내공","kind":"INNER_POWER","maxLevel":1,
       "levelEffects":[{"level":1,"statModifiers":[{"statType":"INNER_POWER","value":3}]}]}'

# List
curl -s localhost:8080/api/v1/mental-method-templates
```

- [ ] **Step 3: Inspect git log**

```bash
git log --oneline | head -30
```

Expected: ~26 commits forming a coherent feature stack.

---

## Notes on follow-on phases (NOT in this plan)

- **전투 적용**: `ATBCombatParticipant`가 `MentalMethodTemplate.levelEffects[level-1].statModifiers`를 합산하도록 확장. 외공은 액티브 사용 명령어로 호출 (`/skill <외공이름>`).
- **경험치/레벨업**: `LearnedMentalMethod.gainExp(amount)` / `LearnedExternalArt.gainExp(amount)` + level-up hook. 전투 종료 또는 공격 성공 시 호출.
- **필살기 트리거**: `MentalMethodTemplate`에 active trigger 필드 추가 (스키마 호환 가능).
- **학습 이벤트**: NPC 가르침, 보상 등에서 `MartialArtLearner` 직접 호출.
- **무공서 폐기**: `MartialArtsBookTemplate` 및 ItemType.MARTIAL_ARTS_BOOK 단계적 제거.

