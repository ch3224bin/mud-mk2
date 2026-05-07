# 무협 스탯 시스템 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** D&D 방식 스탯(STR/DEX/CON/INT/POW/CHA)을 무협 속성 6개 + 무예 9개로 전면 교체하고, 파생 스탯(HP/MP/AP) 공식을 상수 기반으로 계산한다.

**Architecture:** `CharacterStats` record가 속성·무예 원본값과 현재 자원(hp/mp/ap)을 담고, `maxHp()`·`maxMp()`·`maxAp()`를 공식 상수 기반으로 계산한다. `BaseCharacter`에 새 필드를 저장하고, 모든 DTO·서비스는 새 스탯 이름을 참조한다. 공식 상수는 `CharacterStats`에 `static final`로 분리해 코드 변경 없이 조정 가능하게 한다.

**Tech Stack:** Java 17, Spring Boot 3, JPA/Hibernate, Lombok, JUnit 5, AssertJ, Gradle

---

## 필드명 매핑 (Korean → Java)

| 한국어 | 자바 필드명 | 분류 |
|--------|-----------|------|
| 근력(筋力) | `vigor` | 속성 |
| 체질(體質) | `physique` | 속성 |
| 민첩(敏捷) | `agility` | 속성 |
| 지력(智力) | `intellect` | 속성 |
| 의지(意志) | `will` | 속성 |
| 경맥(經脈) | `meridian` | 속성 |
| 내공(內功) | `innerPower` | 무예 |
| 절기(絶技) | `specialTechnique` | 무예 |
| 경공(輕功) | `lightStep` | 무예 |
| 권장(拳掌) | `fistsAndPalms` | 무예 |
| 검법(劍法) | `swordMethod` | 무예 |
| 도법(刀法) | `bladeMethod` | 무예 |
| 장병(長兵) | `longWeapon` | 무예 |
| 기문(奇門) | `esotericWeapon` | 무예 |
| 사술(射術) | `archery` | 무예 |

## 파생 스탯 공식 상수

```java
HP_PER_PHYSIQUE          = 10   // 생명(HP) = 체질 × 10
HP_PER_SPECIAL_TECHNIQUE =  3   //         + 절기 × 3
MP_PER_MERIDIAN          =  5   // 내력(MP) = 경맥 × 5
MP_PER_INNER_POWER       =  3   //         + 내공 × 3
AP_PER_AGILITY           =  8   // 체력(AP) = 민첩 × 8
```

---

## File Map

| 파일 | 변경 |
|------|------|
| `gamedata/.../player/CharacterStats.java` | 전면 재작성 |
| `gamedata/.../player/BaseCharacter.java` | 전면 재작성 |
| `gamedata/.../player/MonsterType.java` | 스탯 필드 교체 |
| `gamedata/.../request/CreateNonPlayerCharacterRequest.java` | 스탯 필드 교체 |
| `gamedata/.../request/UpdateNonPlayerCharacterRequest.java` | 스탯 필드 교체 |
| `gamedata/.../request/CreateMonsterTypeRequest.java` | 스탯 필드 교체 |
| `gamedata/.../request/UpdateMonsterTypeRequest.java` | 스탯 필드 교체 |
| `gamedata/.../response/NonPlayerCharacterResponse.java` | 스탯 필드 교체 |
| `gamedata/.../response/MonsterTypeResponse.java` | 스탯 필드 교체 |
| `gameplay/.../template/StatusVariables.java` | 스탯 필드 교체 |
| `gameplay/.../command/StatusCommandService.java` | stats 매핑 수정 |
| `gamedata/.../service/PlayerCharacterService.java` | createBaseCharacter() 수정 |
| `common/fixture/GameTestFixture.java` | BaseCharacter builder 수정 |
| `gameplay/.../CharacterCreationServiceTest.java` | FakePlayerCharacterService 수정 |
| `docs/STATS.md` | 골격 생성 |

> **기준 패키지:** `src/main/java/com/jefflife/mudmk2/`  
> **테스트 패키지:** `src/test/java/com/jefflife/mudmk2/`

---

## Task 1: `CharacterStats` record 재작성

**Files:**
- Modify: `gamedata/application/domain/model/player/CharacterStats.java`
- Create: `src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/CharacterStatsTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
// src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/CharacterStatsTest.java
package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CharacterStatsTest {

    // helper: 모든 속성/무예를 0으로 채운 기본 스탯 생성
    private CharacterStats statsWithPhysique(int physique, int specialTechnique) {
        return new CharacterStats(
            100, 50, 80,                              // hp, mp, ap
            0, physique, 0, 0, 0, 0,                 // vigor, physique, agility, intellect, will, meridian
            0, specialTechnique, 0, 0, 0, 0, 0, 0, 0 // innerPower, specialTechnique, lightStep, ...
        );
    }

    private CharacterStats statsWithMeridian(int meridian, int innerPower) {
        return new CharacterStats(
            100, 50, 80,
            0, 0, 0, 0, 0, meridian,
            innerPower, 0, 0, 0, 0, 0, 0, 0, 0
        );
    }

    private CharacterStats statsWithAgility(int agility) {
        return new CharacterStats(
            100, 50, 80,
            0, 0, agility, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0
        );
    }

    @Test
    void maxHp_isComputedFromPhysiqueAndSpecialTechnique() {
        CharacterStats stats = statsWithPhysique(50, 20);
        // 50 × 10 + 20 × 3 = 560
        assertThat(stats.maxHp()).isEqualTo(560);
    }

    @Test
    void maxHp_withZeroSpecialTechnique_isPhysiqueOnly() {
        CharacterStats stats = statsWithPhysique(30, 0);
        assertThat(stats.maxHp()).isEqualTo(300);
    }

    @Test
    void maxMp_isComputedFromMeridianAndInnerPower() {
        CharacterStats stats = statsWithMeridian(40, 30);
        // 40 × 5 + 30 × 3 = 290
        assertThat(stats.maxMp()).isEqualTo(290);
    }

    @Test
    void maxAp_isComputedFromAgility() {
        CharacterStats stats = statsWithAgility(60);
        // 60 × 8 = 480
        assertThat(stats.maxAp()).isEqualTo(480);
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

```bash
./gradlew test --tests "*.CharacterStatsTest" 2>&1 | tail -20
```
Expected: 컴파일 오류 또는 NoSuchMethodError (새 생성자 없음)

- [ ] **Step 3: `CharacterStats` 재작성**

```java
// src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/CharacterStats.java
package com.jefflife.mudmk2.gamedata.application.domain.model.player;

public record CharacterStats(
        // 현재 자원
        int hp,
        int mp,
        int ap,
        // 속성 (屬性) — 6개
        int vigor,            // 근력(筋力)
        int physique,         // 체질(體質)
        int agility,          // 민첩(敏捷)
        int intellect,        // 지력(智力)
        int will,             // 의지(意志)
        int meridian,         // 경맥(經脈)
        // 무예 (武藝) — 9개
        int innerPower,       // 내공(內功)
        int specialTechnique, // 절기(絶技)
        int lightStep,        // 경공(輕功)
        int fistsAndPalms,    // 권장(拳掌)
        int swordMethod,      // 검법(劍法)
        int bladeMethod,      // 도법(刀法)
        int longWeapon,       // 장병(長兵)
        int esotericWeapon,   // 기문(奇門)
        int archery           // 사술(射術)
) {
    // 파생 스탯 공식 상수 — 밸런싱 시 이 값만 수정
    public static final int HP_PER_PHYSIQUE = 10;
    public static final int HP_PER_SPECIAL_TECHNIQUE = 3;
    public static final int MP_PER_MERIDIAN = 5;
    public static final int MP_PER_INNER_POWER = 3;
    public static final int AP_PER_AGILITY = 8;

    public int maxHp() {
        return physique * HP_PER_PHYSIQUE + specialTechnique * HP_PER_SPECIAL_TECHNIQUE;
    }

    public int maxMp() {
        return meridian * MP_PER_MERIDIAN + innerPower * MP_PER_INNER_POWER;
    }

    public int maxAp() {
        return agility * AP_PER_AGILITY;
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

```bash
./gradlew test --tests "*.CharacterStatsTest" 2>&1 | tail -10
```
Expected: `4 tests completed, 0 failures`

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/CharacterStats.java \
        src/test/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/CharacterStatsTest.java
git commit -m "feat: CharacterStats를 무협 속성+무예 시스템으로 재작성"
```

---

## Task 2: `BaseCharacter` 재작성

**Files:**
- Modify: `gamedata/application/domain/model/player/BaseCharacter.java`

> Task 1 완료 후 진행. 이 변경으로 `BaseCharacter`를 참조하는 파일들이 컴파일 오류를 냄 — Task 3~8에서 순차 수정.

- [ ] **Step 1: `BaseCharacter` 전면 재작성**

```java
// src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/player/BaseCharacter.java
package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder @AllArgsConstructor
public class BaseCharacter {
    private String name;

    @Builder.Default
    private CharacterState state = CharacterState.NORMAL;

    @Column(length = 1000)
    private String background;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Gender gender = Gender.MALE;

    // 현재 자원
    private int hp;
    private int mp;
    private int ap;

    // 속성 (屬性) — 6개
    private int vigor;            // 근력(筋力)
    private int physique;         // 체질(體質)
    private int agility;          // 민첩(敏捷)
    private int intellect;        // 지력(智力)
    private int will;             // 의지(意志)
    private int meridian;         // 경맥(經脈)

    // 무예 (武藝) — 9개
    private int innerPower;       // 내공(內功)
    private int specialTechnique; // 절기(絶技)
    private int lightStep;        // 경공(輕功)
    private int fistsAndPalms;    // 권장(拳掌)
    private int swordMethod;      // 검법(劍法)
    private int bladeMethod;      // 도법(刀法)
    private int longWeapon;       // 장병(長兵)
    private int esotericWeapon;   // 기문(奇門)
    private int archery;          // 사술(射術)

    // 위치 정보
    private Long roomId;

    @Builder.Default
    private boolean alive = true;

    public void setRoomId(final Long roomId) {
        this.roomId = roomId;
    }

    public CharacterStats getStats() {
        return new CharacterStats(
                hp, mp, ap,
                vigor, physique, agility, intellect, will, meridian,
                innerPower, specialTechnique, lightStep,
                fistsAndPalms, swordMethod, bladeMethod, longWeapon, esotericWeapon, archery
        );
    }

    public void setState(CharacterState characterState) {
        this.state = characterState;
    }

    public void fullRestore() {
        CharacterStats stats = getStats();
        this.hp = stats.maxHp();
        this.mp = stats.maxMp();
        this.ap = stats.maxAp();
        this.alive = true;
    }

    public void decreaseHp(int amount) {
        this.hp -= amount;
        if (this.hp <= 0) {
            this.alive = false;
            this.state = CharacterState.DEAD;
        }
    }
}
```

- [ ] **Step 2: DB 스키마 초기화 (개발 환경)**

기존 DB 테이블에 구 스탯 컬럼(str, dex, con, intelligence, pow, cha, max_hp, max_mp)이 있으므로 개발 DB를 초기화한다.

```bash
docker-compose down -v && docker-compose up -d
```

`application.yml` (또는 `application-dev.yml`)에서 `spring.jpa.hibernate.ddl-auto=update` 또는 `create`인지 확인. `create`라면 재기동 시 자동 반영됨.

- [ ] **Step 3: 빌드 오류 목록 확인**

```bash
./gradlew compileJava 2>&1 | grep "error:" | head -30
```

Expected: Task 3~8에서 수정할 파일들의 오류 목록 출력.

- [ ] **Step 4: 커밋 (컴파일 오류 있는 상태로 커밋하지 말 것 — Task 8 완료 후 일괄 커밋)**

---

## Task 3: `MonsterType` 스탯 필드 교체

**Files:**
- Modify: `gamedata/application/domain/model/player/MonsterType.java`

- [ ] **Step 1: MonsterType 스탯 필드 교체**

`MonsterType.java`에서 아래 필드들을 교체한다.

제거:
```java
private int baseStr;
private int baseDex;
private int baseCon;
private int baseIntelligence;
private int basePow;
private int baseCha;
private int strPerLevel;
private int dexPerLevel;
private int conPerLevel;
private int intelligencePerLevel;
private int powPerLevel;
private int chaPerLevel;
```

추가 (기존 `baseHp`, `baseMp`, `hpPerLevel`, `baseExperience`, `expPerLevel` 유지):
```java
// 속성 기본값
private int baseVigor;
private int basePhysique;
private int baseAgility;
private int baseIntellect;
private int baseWill;
private int baseMeridian;
// 속성 레벨당 증가치
private int vigorPerLevel;
private int physiquePerLevel;
private int agilityPerLevel;
private int intellectPerLevel;
private int willPerLevel;
private int meridianPerLevel;
// 무예 기본값 (몬스터는 무예 레벨업 없음)
private int baseInnerPower;
private int baseSpecialTechnique;
private int baseLightStep;
private int baseFistsAndPalms;
private int baseSwordMethod;
private int baseBladeMethod;
private int baseLongWeapon;
private int baseEsotericWeapon;
private int baseArchery;
```

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew compileJava 2>&1 | grep "MonsterType" | head -10
```

Expected: MonsterType 관련 오류가 DTO/Response로 이동함.

---

## Task 4: NPC 요청/응답 DTO 교체

**Files:**
- Modify: `gamedata/application/service/model/request/CreateNonPlayerCharacterRequest.java`
- Modify: `gamedata/application/service/model/request/UpdateNonPlayerCharacterRequest.java`
- Modify: `gamedata/adapter/webapi/response/NonPlayerCharacterResponse.java`

- [ ] **Step 1: `CreateNonPlayerCharacterRequest` 재작성**

```java
package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gamedata.application.domain.service.NonPlayerCharacterFactory;

public record CreateNonPlayerCharacterRequest(
    String name,
    String background,
    Gender gender,
    // 현재 자원 (미입력 시 속성 기반 최대치로 초기화)
    int hp,
    int mp,
    int ap,
    // 속성
    int vigor,
    int physique,
    int agility,
    int intellect,
    int will,
    int meridian,
    // 무예
    int innerPower,
    int specialTechnique,
    int lightStep,
    int fistsAndPalms,
    int swordMethod,
    int bladeMethod,
    int longWeapon,
    int esotericWeapon,
    int archery,
    Long roomId,
    // PlayableCharacter
    int level,
    long experience,
    long nextLevelExp,
    boolean conversable,
    // NPC 전용
    String persona,
    NPCType npcType,
    Long spawnRoomId,
    boolean essential
) {
    public NonPlayerCharacter toDomain() {
        BaseCharacter base = BaseCharacter.builder()
                .name(name)
                .background(background)
                .gender(gender)
                .hp(hp)
                .mp(mp)
                .ap(ap)
                .vigor(vigor)
                .physique(physique)
                .agility(agility)
                .intellect(intellect)
                .will(will)
                .meridian(meridian)
                .innerPower(innerPower)
                .specialTechnique(specialTechnique)
                .lightStep(lightStep)
                .fistsAndPalms(fistsAndPalms)
                .swordMethod(swordMethod)
                .bladeMethod(bladeMethod)
                .longWeapon(longWeapon)
                .esotericWeapon(esotericWeapon)
                .archery(archery)
                .roomId(roomId)
                .alive(true)
                .build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(level)
                .experience(experience)
                .nextLevelExp(nextLevelExp)
                .conversable(conversable)
                .build();
        return NonPlayerCharacterFactory.builder()
                .baseCharacter(base)
                .playableCharacter(playable)
                .persona(persona)
                .npcType(npcType)
                .spawnRoomId(spawnRoomId)
                .essential(essential)
                .build();
    }
}
```

- [ ] **Step 2: `UpdateNonPlayerCharacterRequest` 재작성**

```java
package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.*;
import com.jefflife.mudmk2.gamedata.application.domain.service.NonPlayerCharacterFactory;

import java.util.UUID;

public record UpdateNonPlayerCharacterRequest(
    String name,
    String background,
    int hp,
    int mp,
    int ap,
    int vigor,
    int physique,
    int agility,
    int intellect,
    int will,
    int meridian,
    int innerPower,
    int specialTechnique,
    int lightStep,
    int fistsAndPalms,
    int swordMethod,
    int bladeMethod,
    int longWeapon,
    int esotericWeapon,
    int archery,
    Long roomId,
    boolean alive,
    int level,
    long experience,
    long nextLevelExp,
    boolean conversable,
    String persona,
    NPCType npcType,
    Long spawnRoomId,
    boolean essential
) {
    public NonPlayerCharacter toDomain(UUID id) {
        BaseCharacter base = BaseCharacter.builder()
                .name(name)
                .background(background)
                .hp(hp)
                .mp(mp)
                .ap(ap)
                .vigor(vigor)
                .physique(physique)
                .agility(agility)
                .intellect(intellect)
                .will(will)
                .meridian(meridian)
                .innerPower(innerPower)
                .specialTechnique(specialTechnique)
                .lightStep(lightStep)
                .fistsAndPalms(fistsAndPalms)
                .swordMethod(swordMethod)
                .bladeMethod(bladeMethod)
                .longWeapon(longWeapon)
                .esotericWeapon(esotericWeapon)
                .archery(archery)
                .roomId(roomId)
                .alive(alive)
                .build();
        PlayableCharacter playable = PlayableCharacter.builder()
                .level(level)
                .experience(experience)
                .nextLevelExp(nextLevelExp)
                .conversable(conversable)
                .build();
        return NonPlayerCharacterFactory.builder()
                .id(id)
                .baseCharacter(base)
                .playableCharacter(playable)
                .persona(persona)
                .npcType(npcType)
                .spawnRoomId(spawnRoomId)
                .essential(essential)
                .build();
    }
}
```

- [ ] **Step 3: `NonPlayerCharacterResponse` 재작성**

```java
package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NPCType;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;

import java.util.UUID;

public record NonPlayerCharacterResponse(
    UUID id,
    String name,
    String background,
    Gender gender,
    int hp,
    int maxHp,
    int mp,
    int maxMp,
    int ap,
    int maxAp,
    int vigor,
    int physique,
    int agility,
    int intellect,
    int will,
    int meridian,
    int innerPower,
    int specialTechnique,
    int lightStep,
    int fistsAndPalms,
    int swordMethod,
    int bladeMethod,
    int longWeapon,
    int esotericWeapon,
    int archery,
    Long roomId,
    boolean alive,
    int level,
    long experience,
    long nextLevelExp,
    boolean conversable,
    String persona,
    NPCType npcType,
    Long spawnRoomId,
    boolean essential
) {
    public static NonPlayerCharacterResponse of(NonPlayerCharacter npc) {
        var base = npc.getBaseCharacterInfo();
        var stats = base.getStats();
        var playable = npc.getPlayableCharacterInfo();
        return new NonPlayerCharacterResponse(
                npc.getId(),
                base.getName(),
                base.getBackground(),
                base.getGender(),
                stats.hp(),
                stats.maxHp(),
                stats.mp(),
                stats.maxMp(),
                stats.ap(),
                stats.maxAp(),
                stats.vigor(),
                stats.physique(),
                stats.agility(),
                stats.intellect(),
                stats.will(),
                stats.meridian(),
                stats.innerPower(),
                stats.specialTechnique(),
                stats.lightStep(),
                stats.fistsAndPalms(),
                stats.swordMethod(),
                stats.bladeMethod(),
                stats.longWeapon(),
                stats.esotericWeapon(),
                stats.archery(),
                base.getRoomId(),
                base.isAlive(),
                playable.getLevel(),
                playable.getExperience(),
                playable.getNextLevelExp(),
                playable.isConversable(),
                npc.getPersona(),
                npc.getNpcType(),
                npc.getSpawnRoomId(),
                npc.isEssential()
        );
    }
}
```

---

## Task 5: Monster 요청/응답 DTO 교체

**Files:**
- Modify: `gamedata/application/service/model/request/CreateMonsterTypeRequest.java`
- Modify: `gamedata/application/service/model/request/UpdateMonsterTypeRequest.java`
- Modify: `gamedata/adapter/webapi/response/MonsterTypeResponse.java`

- [ ] **Step 1: `CreateMonsterTypeRequest` 재작성**

```java
package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;

import java.util.List;

public record CreateMonsterTypeRequest(
    String name,
    String description,
    Gender gender,
    int baseHp,
    int baseMp,
    // 속성 기본값
    int baseVigor,
    int basePhysique,
    int baseAgility,
    int baseIntellect,
    int baseWill,
    int baseMeridian,
    // 속성 레벨당 증가
    int vigorPerLevel,
    int physiquePerLevel,
    int agilityPerLevel,
    int intellectPerLevel,
    int willPerLevel,
    int meridianPerLevel,
    // 무예 기본값
    int baseInnerPower,
    int baseSpecialTechnique,
    int baseLightStep,
    int baseFistsAndPalms,
    int baseSwordMethod,
    int baseBladeMethod,
    int baseLongWeapon,
    int baseEsotericWeapon,
    int baseArchery,
    long baseExperience,
    int hpPerLevel,
    int expPerLevel,
    List<MonsterSpawnRoomRequest> spawnRooms,
    int aggressiveness,
    int respawnTime
) {
    public MonsterType toDomain() {
        return MonsterType.builder()
                .name(name)
                .description(description)
                .gender(gender)
                .baseHp(baseHp)
                .baseMp(baseMp)
                .baseVigor(baseVigor)
                .basePhysique(basePhysique)
                .baseAgility(baseAgility)
                .baseIntellect(baseIntellect)
                .baseWill(baseWill)
                .baseMeridian(baseMeridian)
                .vigorPerLevel(vigorPerLevel)
                .physiquePerLevel(physiquePerLevel)
                .agilityPerLevel(agilityPerLevel)
                .intellectPerLevel(intellectPerLevel)
                .willPerLevel(willPerLevel)
                .meridianPerLevel(meridianPerLevel)
                .baseInnerPower(baseInnerPower)
                .baseSpecialTechnique(baseSpecialTechnique)
                .baseLightStep(baseLightStep)
                .baseFistsAndPalms(baseFistsAndPalms)
                .baseSwordMethod(baseSwordMethod)
                .baseBladeMethod(baseBladeMethod)
                .baseLongWeapon(baseLongWeapon)
                .baseEsotericWeapon(baseEsotericWeapon)
                .baseArchery(baseArchery)
                .baseExperience(baseExperience)
                .hpPerLevel(hpPerLevel)
                .expPerLevel(expPerLevel)
                .aggressiveness(aggressiveness)
                .respawnTime(respawnTime)
                .build();
    }
}
```

- [ ] **Step 2: `UpdateMonsterTypeRequest` 재작성**

```java
package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;

import java.util.List;

public record UpdateMonsterTypeRequest(
    String name,
    String description,
    Gender gender,
    int baseHp,
    int baseMp,
    int baseVigor,
    int basePhysique,
    int baseAgility,
    int baseIntellect,
    int baseWill,
    int baseMeridian,
    int vigorPerLevel,
    int physiquePerLevel,
    int agilityPerLevel,
    int intellectPerLevel,
    int willPerLevel,
    int meridianPerLevel,
    int baseInnerPower,
    int baseSpecialTechnique,
    int baseLightStep,
    int baseFistsAndPalms,
    int baseSwordMethod,
    int baseBladeMethod,
    int baseLongWeapon,
    int baseEsotericWeapon,
    int baseArchery,
    long baseExperience,
    int hpPerLevel,
    int expPerLevel,
    List<MonsterSpawnRoomRequest> spawnRooms,
    int aggressiveness,
    int respawnTime
) {}
```

- [ ] **Step 3: `MonsterTypeResponse` 재작성**

```java
package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record MonsterTypeResponse(
    Long id,
    String name,
    String description,
    Gender gender,
    int baseHp,
    int baseMp,
    int baseVigor,
    int basePhysique,
    int baseAgility,
    int baseIntellect,
    int baseWill,
    int baseMeridian,
    int vigorPerLevel,
    int physiquePerLevel,
    int agilityPerLevel,
    int intellectPerLevel,
    int willPerLevel,
    int meridianPerLevel,
    int baseInnerPower,
    int baseSpecialTechnique,
    int baseLightStep,
    int baseFistsAndPalms,
    int baseSwordMethod,
    int baseBladeMethod,
    int baseLongWeapon,
    int baseEsotericWeapon,
    int baseArchery,
    long baseExperience,
    int hpPerLevel,
    int expPerLevel,
    List<MonsterSpawnRoomResponse> spawnRooms,
    int aggressiveness,
    int respawnTime
) {
    public static MonsterTypeResponse from(MonsterType m) {
        List<MonsterSpawnRoomResponse> rooms = new ArrayList<>();
        if (m.getMonsterSpawnRooms() != null && m.getMonsterSpawnRooms().getSpawnRooms() != null) {
            rooms = m.getMonsterSpawnRooms().getSpawnRooms().stream()
                    .map(MonsterSpawnRoomResponse::from)
                    .collect(Collectors.toList());
        }
        return new MonsterTypeResponse(
                m.getId(), m.getName(), m.getDescription(), m.getGender(),
                m.getBaseHp(), m.getBaseMp(),
                m.getBaseVigor(), m.getBasePhysique(), m.getBaseAgility(),
                m.getBaseIntellect(), m.getBaseWill(), m.getBaseMeridian(),
                m.getVigorPerLevel(), m.getPhysiquePerLevel(), m.getAgilityPerLevel(),
                m.getIntellectPerLevel(), m.getWillPerLevel(), m.getMeridianPerLevel(),
                m.getBaseInnerPower(), m.getBaseSpecialTechnique(), m.getBaseLightStep(),
                m.getBaseFistsAndPalms(), m.getBaseSwordMethod(), m.getBaseBladeMethod(),
                m.getBaseLongWeapon(), m.getBaseEsotericWeapon(), m.getBaseArchery(),
                m.getBaseExperience(), m.getHpPerLevel(), m.getExpPerLevel(),
                rooms, m.getAggressiveness(), m.getRespawnTime()
        );
    }
}
```

---

## Task 6: `StatusVariables` + `StatusCommandService` 수정

**Files:**
- Modify: `gameplay/application/service/model/template/StatusVariables.java`
- Modify: `gameplay/application/service/command/StatusCommandService.java`

- [ ] **Step 1: `StatusVariables` 재작성**

```java
package com.jefflife.mudmk2.gameplay.application.service.model.template;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterState;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Gender;

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
    // 속성
    int vigor,
    int physique,
    int agility,
    int intellect,
    int will,
    int meridian,
    // 무예
    int innerPower,
    int specialTechnique,
    int lightStep,
    int fistsAndPalms,
    int swordMethod,
    int bladeMethod,
    int longWeapon,
    int esotericWeapon,
    int archery,
    String roomName
) {}
```

- [ ] **Step 2: `StatusCommandService.showStatus()` 수정**

`StatusCommandService.java`의 `showStatus()` 메서드에서 `StatusVariables` 생성 부분을 교체한다.

```java
@Override
public void showStatus(final StatusCommand command) {
    PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
    Room currentRoom = gameWorldService.getRoom(player.getCurrentRoomId());

    var stats = player.getStats();

    StatusVariables statusVariables = new StatusVariables(
            player.getUserId(),
            player.getName(),
            player.getCharacterClass(),
            player.getBaseCharacterInfo().getGender(),
            player.getState(),
            player.getPlayableCharacterInfo().getLevel(),
            player.getPlayableCharacterInfo().getExperience(),
            player.getPlayableCharacterInfo().getNextLevelExp(),
            stats.hp(),
            stats.maxHp(),
            stats.mp(),
            stats.maxMp(),
            stats.ap(),
            stats.maxAp(),
            stats.vigor(),
            stats.physique(),
            stats.agility(),
            stats.intellect(),
            stats.will(),
            stats.meridian(),
            stats.innerPower(),
            stats.specialTechnique(),
            stats.lightStep(),
            stats.fistsAndPalms(),
            stats.swordMethod(),
            stats.bladeMethod(),
            stats.longWeapon(),
            stats.esotericWeapon(),
            stats.archery(),
            currentRoom.getName()
    );

    sendStatusMessagePort.sendMessage(statusVariables);
    logger.debug("Sent status message for player: {}", player.getName());
}
```

---

## Task 7: `PlayerCharacterService.createBaseCharacter()` 수정

**Files:**
- Modify: `gamedata/application/service/PlayerCharacterService.java`

- [ ] **Step 1: `createBaseCharacter()` 수정**

`PlayerCharacterService.java`에서 `createBaseCharacter()` 메서드를 교체한다. 클래스 구분 없이 기본값으로 통일 (직업 시스템은 별도 작업에서 설계).

```java
private BaseCharacter createBaseCharacter(String name, CharacterClass characterClass, Gender gender) {
    int baseAttr = 10;
    int hp = baseAttr * CharacterStats.HP_PER_PHYSIQUE;           // 100
    int mp = baseAttr * CharacterStats.MP_PER_MERIDIAN;           //  50
    int ap = baseAttr * CharacterStats.AP_PER_AGILITY;            //  80

    return BaseCharacter.builder()
            .name(name)
            .background("새로운 강호인")
            .gender(gender)
            .hp(hp)
            .mp(mp)
            .ap(ap)
            .vigor(baseAttr)
            .physique(baseAttr)
            .agility(baseAttr)
            .intellect(baseAttr)
            .will(baseAttr)
            .meridian(baseAttr)
            .innerPower(0)
            .specialTechnique(0)
            .lightStep(0)
            .fistsAndPalms(0)
            .swordMethod(0)
            .bladeMethod(0)
            .longWeapon(0)
            .esotericWeapon(0)
            .archery(0)
            .roomId(1L)
            .alive(true)
            .build();
}
```

> `import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;` 추가 필요.

---

## Task 8: 테스트·픽스처 수정

**Files:**
- Modify: `src/test/java/com/jefflife/mudmk2/common/fixture/GameTestFixture.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/CharacterCreationServiceTest.java`

- [ ] **Step 1: `GameTestFixture.createTestPlayer()` 수정**

```java
public static PlayerCharacter createTestPlayer(Long userId, Long roomId) {
    // physique=10 → maxHp=100, meridian=10 → maxMp=50, agility=10 → maxAp=80
    BaseCharacter baseCharacter = BaseCharacter.builder()
            .name("TestPlayer")
            .roomId(roomId)
            .hp(100)
            .mp(50)
            .ap(80)
            .vigor(10)
            .physique(10)
            .agility(10)
            .intellect(10)
            .will(10)
            .meridian(10)
            .build();

    PlayableCharacter playableCharacter = PlayableCharacter.builder()
            .level(1)
            .experience(0)
            .nextLevelExp(100)
            .conversable(true)
            .build();

    return new PlayerCharacter(
            UUID.randomUUID(),
            baseCharacter,
            playableCharacter,
            userId,
            "TestPlayer",
            CharacterClass.WARRIOR,
            true,
            LocalDateTime.now());
}
```

- [ ] **Step 2: `CharacterCreationServiceTest.FakePlayerCharacterService.createCharacter()` 수정**

`CharacterCreationServiceTest.java`의 `FakePlayerCharacterService` 내부 클래스에서 `BaseCharacter` 빌더 호출을 수정한다.

```java
@Override
public PlayerCharacter createCharacter(Long userId, String name, CharacterClass characterClass, Gender gender) {
    BaseCharacter baseCharacter = BaseCharacter.builder()
            .name(name)
            .background("Test background")
            .hp(100)
            .mp(50)
            .ap(80)
            .vigor(10)
            .physique(10)
            .agility(10)
            .intellect(10)
            .will(10)
            .meridian(10)
            .roomId(1L)
            .gender(gender)
            .build();

    PlayableCharacter playableCharacter = PlayableCharacter.builder()
            .level(1)
            .experience(0)
            .nextLevelExp(100)
            .conversable(true)
            .build();

    // ... (PlayerCharacter 생성 코드 그대로 유지)
}
```

- [ ] **Step 3: 전체 테스트 통과 확인**

```bash
./gradlew test 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL` — 모든 기존 테스트 통과.

- [ ] **Step 4: 전체 커밋**

```bash
git add -u
git commit -m "feat: 무협 스탯 시스템 전면 적용 (속성 6개 + 무예 9개)"
```

---

## Task 9: `docs/STATS.md` 골격 생성

**Files:**
- Modify: `docs/STATS.md`

- [ ] **Step 1: STATS.md 골격 작성**

```markdown
# 스탯 정의 (Stats Reference)

## 파생 스탯 공식

| 파생 스탯 | 공식 |
|-----------|------|
| 생명(HP) 최대치 | 체질 × 10 + 절기 × 3 |
| 내력(MP) 최대치 | 경맥 × 5 + 내공 × 3 |
| 체력(AP) 최대치 | 민첩 × 8 |

---

## 속성 (屬性)

> 캐릭터의 기본 자질. **자질(質)**이 상한선이며 캐릭터 생성 시 40~80 범위에서 분배.

### 근력 (筋力)

_(설명 작성 예정)_

### 체질 (體質)

_(설명 작성 예정)_

### 민첩 (敏捷)

_(설명 작성 예정)_

### 지력 (智力)

_(설명 작성 예정)_

### 의지 (意志)

_(설명 작성 예정)_

### 경맥 (經脈)

_(설명 작성 예정)_

---

## 무예 (武藝)

> 무공을 배우고 사용할수록 숙련도가 오르는 스탯. 레벨업 시 연관 속성도 함께 증가.

### 내공 (內功)

_(설명 작성 예정)_

### 절기 (絶技)

_(설명 작성 예정)_

### 경공 (輕功)

_(설명 작성 예정)_

### 외공 (外功)

#### 권장 (拳掌)

_(설명 작성 예정)_

#### 검법 (劍法)

_(설명 작성 예정)_

#### 도법 (刀法)

_(설명 작성 예정)_

#### 장병 (長兵)

_(설명 작성 예정)_

#### 기문 (奇門)

_(설명 작성 예정)_

#### 사술 (射術)

_(설명 작성 예정)_
```

- [ ] **Step 2: 커밋**

```bash
git add docs/STATS.md
git commit -m "docs: STATS.md 골격 생성 (내용 작성 예정)"
```
