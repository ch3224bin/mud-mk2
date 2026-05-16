# 무공(심법/외공) 시스템 — Phase 1 설계

> Phase 1 스코프: 템플릿 + 학습/장착 모델 + 어드민 페이지.
> 전투 적용 / 경험치 누적 / 필살기 트리거 / 명령어 사용은 후속 페이즈.

## 1. 용어 & 범위

- **심법 (MentalMethod)** — 상위 카테고리. 캐릭터는 종류별로 1개씩 장착(최대 3슬롯).
  종류(`MentalMethodKind`): `INNER_POWER`(내공), `LIGHT_STEP`(경공), `SPECIAL_TECHNIQUE`(절기)
- **외공 (ExternalArt)** — 스킬 개념. 최대 6개 장착, 슬롯 순서 무의미.
  분류는 기존 `WeaponType` 6종 재사용: `SWORD`, `BLADE`, `FIST`, `ARCHERY`, `ESOTERIC`, `LONG_WEAPON`
- 학습은 멀티 가능, 장착 슬롯과 별개로 학습 대장에 누적됨.
- 장착·해제는 학습 레코드를 보존(레벨/경험치 유지).

### 본 페이즈 비포함
- 전투 중 효과 적용
- 경험치 누적 / 레벨업 트리거
- 필살기(심법 액티브) 발동 조건
- 외공 사용 명령어 / 쿨다운 진행
- 학습 부여 이벤트 핸들러 (admin 직호출로 대체)
- 기존 `MartialArtsBookTemplate` 아이템 정리 (별도 폐기 페이즈)

## 2. 도메인 모델

위치: `com.jefflife.mudmk2.gamedata.application.domain.model.martialart`

### 2.1 enum

```java
public enum MentalMethodKind {
    INNER_POWER, LIGHT_STEP, SPECIAL_TECHNIQUE
}
```

(외공 분류는 `gamedata.application.domain.model.item.WeaponType` 그대로 사용.)

### 2.2 템플릿 엔티티

```java
@Entity @Table(name = "mental_method_template")
public class MentalMethodTemplate {
    @Id @GeneratedValue(strategy = IDENTITY) Long id;
    String name;
    @Column(length = 1000) String description;
    @Enumerated(EnumType.STRING) MentalMethodKind kind;
    int maxLevel;
    @Convert(converter = MentalMethodLevelEffectsConverter.class)
    @Column(columnDefinition = "TEXT")
    List<MentalMethodLevelEffect> levelEffects; // size == maxLevel
}

public record MentalMethodLevelEffect(
    int level,                          // 1..maxLevel
    List<StatModifier> statModifiers
) {}
```

```java
@Entity @Table(name = "external_art_template")
public class ExternalArtTemplate {
    @Id @GeneratedValue(strategy = IDENTITY) Long id;
    String name;
    @Column(length = 1000) String description;
    @Enumerated(EnumType.STRING) WeaponType weaponType;
    int maxLevel;
    @Convert(converter = ExternalArtLevelEffectsConverter.class)
    @Column(columnDefinition = "TEXT")
    List<ExternalArtLevelEffect> levelEffects;
}

public record ExternalArtLevelEffect(
    int level,
    double damageMultiplier,
    int cooldownSeconds,
    int apCost,
    int mpCost
) {}
```

JSON 컨버터 2종은 Jackson 기반 `AttributeConverter<List<X>, String>`.

### 2.3 학습 대장

```java
@Entity @Table(name = "learned_mental_method")
public class LearnedMentalMethod {
    @Id @GeneratedValue(strategy = UUID) UUID id;
    UUID playerCharacterId;          // FK (관계 매핑 X, ID-only)
    Long mentalMethodTemplateId;     // FK (관계 매핑 X)
    int currentLevel = 1;
    long currentExp = 0;
}

@Entity @Table(name = "learned_external_art")
public class LearnedExternalArt {
    @Id @GeneratedValue(strategy = UUID) UUID id;
    UUID playerCharacterId;
    Long externalArtTemplateId;
    int currentLevel = 1;
    long currentExp = 0;
}
```

각 학습 레코드의 unique 제약: `(playerCharacterId, templateId)`.

### 2.4 장착 정보

```java
@Entity @Table(name = "equipped_martial_arts")
public class EquippedMartialArts {
    @Id @GeneratedValue(strategy = UUID) UUID id;

    @ElementCollection
    @CollectionTable(name = "equipped_mental_slot",
                     joinColumns = @JoinColumn(name = "equipped_martial_arts_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "kind")
    @Column(name = "learned_mental_method_id")
    Map<MentalMethodKind, UUID> mentalSlots = new EnumMap<>(MentalMethodKind.class);

    @ElementCollection
    @CollectionTable(name = "equipped_external_slot",
                     joinColumns = @JoinColumn(name = "equipped_martial_arts_id"))
    @Column(name = "learned_external_art_id")
    List<UUID> externalSlots = new ArrayList<>(); // size ≤ EXTERNAL_SLOT_MAX

    public static final int EXTERNAL_SLOT_MAX = 6;

    public void equipMental(MentalMethodKind kind, UUID learnedId) {
        mentalSlots.put(kind, learnedId); // 자동 교체
    }

    public Optional<UUID> unequipMental(MentalMethodKind kind) {
        return Optional.ofNullable(mentalSlots.remove(kind));
    }

    public void equipExternal(UUID learnedId) {
        if (externalSlots.size() >= EXTERNAL_SLOT_MAX) {
            throw new MartialArtSlotFullException();
        }
        if (externalSlots.contains(learnedId)) return; // 중복 장착 방지
        externalSlots.add(learnedId);
    }

    public boolean unequipExternal(UUID learnedId) {
        return externalSlots.remove(learnedId);
    }
}
```

### 2.5 PlayerCharacter 변경

```java
@OneToOne(cascade = ALL, orphanRemoval = true, fetch = LAZY, optional = false)
@JoinColumn(name = "equipped_martial_arts_id")
private EquippedMartialArts equippedMartialArts;
```

`initializeAssociatedEntities()`에 `equippedMartialArts.initializeAssociatedEntities()` 추가
(컬렉션 size() 호출로 LAZY 강제 로딩).

## 3. 유스케이스 & 포트

위치: `gamedata.application.service.provided` / `required` / `model.request`

### 3.1 provided

**템플릿 CRUD (심법, 외공 각각)**
- `MentalMethodTemplateCreator.create(MentalMethodTemplateRequest)`
- `MentalMethodTemplateFinder.findById / findAll / findByKind`
- `MentalMethodTemplateModifier.update(id, request)`
- `MentalMethodTemplateRemover.delete(id)` — 학습 레코드 존재 시 거부
- `ExternalArtTemplateCreator / Finder / Modifier / Remover` (동일, `findByWeaponType`)

**학습**
- `MartialArtLearner.learnMentalMethod(UUID pcId, Long templateId)` — 중복 시 `AlreadyLearnedException`
- `MartialArtLearner.learnExternalArt(UUID pcId, Long templateId)` — 동일

**장착**
- `MartialArtEquipper.equipMentalMethod(UUID pcId, UUID learnedId)` — kind 자동 인식 → 같은 kind 슬롯 자동 교체
- `MartialArtEquipper.unequipMentalMethod(UUID pcId, MentalMethodKind kind)`
- `MartialArtEquipper.equipExternalArt(UUID pcId, UUID learnedId)` — 6슬롯 차면 `MartialArtSlotFullException`
- `MartialArtEquipper.unequipExternalArt(UUID pcId, UUID learnedId)`

**조회**
- `LearnedMartialArtFinder.findByCharacter(UUID pcId)` → `(List<LearnedMentalMethod>, List<LearnedExternalArt>, EquippedMartialArts)` 묶음 반환

### 3.2 required

- `MentalMethodTemplateRepository`, `ExternalArtTemplateRepository`
- `LearnedMentalMethodRepository`
  - `existsByPlayerCharacterIdAndMentalMethodTemplateId(UUID, Long)`
  - `findAllByPlayerCharacterId(UUID)`
  - `existsByMentalMethodTemplateId(Long)` — 템플릿 삭제 차단용
- `LearnedExternalArtRepository` — 대응 메서드
- `EquippedMartialArts`는 PlayerCharacter cascade로 영속화 — 별도 리포지토리 불요

### 3.3 서비스 위치

`gamedata.application.service`에 4개 서비스 클래스:
- `MentalMethodTemplateService` — 템플릿 4개 인터페이스 구현
- `ExternalArtTemplateService` — 동일
- `MartialArtLearningService` — `MartialArtLearner` 구현
- `MartialArtEquipService` — `MartialArtEquipper`, `LearnedMartialArtFinder` 구현

Phase 1의 학습 호출 진입점은 어드민 컨트롤러뿐. 후속 페이즈의 이벤트 리스너는 동일 인터페이스(`MartialArtLearner`)를 재사용.

## 4. 어드민 UI & REST

### 4.1 REST 컨트롤러 (3개)

```
MentalMethodTemplateController     /api/v1/mental-method-templates
  POST   /                  생성       201
  GET    /?kind=...         목록(필터) 200
  GET    /{id}              단건       200 / 404
  PUT    /{id}              수정       200 / 404
  DELETE /{id}              삭제       204 / 404 / 409(학습됨)

ExternalArtTemplateController      /api/v1/external-art-templates
  (동일, ?weaponType=...)

CharacterMartialArtController      /api/v1/player-characters/{pcId}/martial-arts
  GET    /                                현재 학습/장착 상태   200 / 404
  POST   /mental-methods/learn            { templateId }       201 / 404 / 409(이미 학습)
  POST   /external-arts/learn             { templateId }       201 / 404 / 409
  POST   /mental-methods/equip            { learnedId }        200 / 400(미학습) / 404
  POST   /external-arts/equip             { learnedId }        200 / 400 / 404 / 409(슬롯 만석)
  DELETE /mental-methods/{kind}           슬롯 해제           204 / 404
  DELETE /external-arts/{learnedId}       슬롯 해제           204 / 404
```

### 4.2 Thymeleaf 페이지 3장

`templates/web/`에 추가, `admin.html` 메뉴에 링크 3개 추가:

1. **`mental-method-template-management.html`** — 심법 템플릿 CRUD
   - 폼: name / description / kind 셀렉트 / maxLevel
   - 레벨별 효과 표(동적 행): level | StatType 셀렉트 + value 입력 (한 레벨에 여러 StatModifier 추가 가능)
2. **`external-art-template-management.html`** — 외공 템플릿 CRUD
   - 폼: name / description / weaponType 셀렉트 / maxLevel
   - 레벨별 효과 표: level | damageMultiplier | cooldownSeconds | apCost | mpCost
3. **`character-martial-art-management.html`** — 캐릭터 무공 관리
   - 좌측: 캐릭터 검색(닉네임) → 선택 시 학습 목록, 장착 슬롯(심법 3 + 외공 6) 표시
   - 우측: 템플릿 검색 → "이 캐릭터에게 학습시키기" 버튼 → 학습 후 "장착" 버튼 활성화

### 4.3 응답 DTO

```java
record MentalMethodTemplateResponse(
    Long id, String name, String description,
    MentalMethodKind kind, int maxLevel,
    List<MentalMethodLevelEffect> levelEffects
) {}

record ExternalArtTemplateResponse(
    Long id, String name, String description,
    WeaponType weaponType, int maxLevel,
    List<ExternalArtLevelEffect> levelEffects
) {}

record LearnedMentalMethodView(UUID id, Long templateId, String templateName,
                               MentalMethodKind kind, int currentLevel, long currentExp) {}
record LearnedExternalArtView(UUID id, Long templateId, String templateName,
                              WeaponType weaponType, int currentLevel, long currentExp) {}

record CharacterMartialArtResponse(
    UUID playerCharacterId,
    List<LearnedMentalMethodView> learnedMentalMethods,
    List<LearnedExternalArtView> learnedExternalArts,
    Map<MentalMethodKind, UUID> equippedMentalSlots,  // 학습 ID
    List<UUID> equippedExternalSlots
) {}
```

## 5. 오류 처리 & 검증

### 5.1 새 예외 (`gamedata.application.service.exception`)

- `AlreadyLearnedException` → 409 Conflict
- `NotLearnedException` → 400 Bad Request
- `MartialArtSlotFullException` → 409 Conflict
- `MartialArtTemplateInUseException` → 409 (템플릿 삭제 거부)

### 5.2 도메인 검증

- `MentalMethodTemplate` / `ExternalArtTemplate` 생성·수정 시
  - `maxLevel ≥ 1`
  - `levelEffects.size() == maxLevel`
  - 각 `level` 값이 1..maxLevel을 빠짐없이 한 번씩 커버
- `ExternalArtLevelEffect`: `damageMultiplier ≥ 0`, `cooldownSeconds ≥ 0`, `apCost ≥ 0`, `mpCost ≥ 0`
- `EquippedMartialArts.equipExternal`: 6슬롯 초과 시 예외, 중복 장착은 no-op

검증 실패 → 400 Bad Request.

### 5.3 템플릿 삭제 정책

- 학습 레코드가 1건이라도 존재 → `MartialArtTemplateInUseException` (409)
- 어드민이 명시적으로 학습 레코드 청소 후 재시도해야 함
- soft-delete / cascade는 도입하지 않음

### 5.4 컨트롤러 예외 처리

기존 `ItemTemplateController`와 동일하게 `try / catch (NoSuchElementException) → 404`. 새 예외 4종은 `@RestControllerAdvice` 또는 각 컨트롤러 try-catch (기존 패턴 확인 후 일관성 유지).

## 6. 테스트 전략

### 6.1 도메인 단위 테스트 (no Spring)
- `EquippedMartialArtsTest`
  - 외공 6개 차면 7번째 추가 시 `MartialArtSlotFullException`
  - 심법 같은 kind 두 번 장착하면 두 번째가 자동 교체
  - 외공 같은 learnedId 두 번 장착 시 멱등 (slots size 변화 없음)
- `MentalMethodTemplateTest`, `ExternalArtTemplateTest`
  - levelEffects.size ≠ maxLevel 또는 level 결번 시 검증 실패
  - 음수 cooldown/multiplier/cost 거부

### 6.2 서비스 단위 테스트 (Mockito)
- `MartialArtLearningServiceTest`
  - 첫 학습 OK, 중복 학습 시 `AlreadyLearnedException`
- `MartialArtEquipServiceTest`
  - 미학습 learnedId 장착 시 `NotLearnedException`
  - 심법 자동 교체 / 외공 만석 예외
- `MentalMethodTemplateServiceTest`, `ExternalArtTemplateServiceTest`
  - 학습 레코드 있는 템플릿 삭제 시 `MartialArtTemplateInUseException`

### 6.3 Repository `@DataJpaTest`
- `MentalMethodLevelEffectsConverter` / `ExternalArtLevelEffectsConverter` round-trip
- `LearnedXxxRepository.existsByPlayerCharacterIdAndTemplateId`
- `LearnedXxxRepository.existsByTemplateId`

### 6.4 Controller `@WebMvcTest`
- 3개 컨트롤러 각각: 정상 케이스 + 매핑된 상태 코드(200/201/204/400/404/409)

### 6.5 비포함
- 전투 통합 테스트 (Phase 1 범위 외)

## 7. 인계 (후속 페이즈 인터페이스)

후속 페이즈가 본 페이즈 결과물 위에서 다음 작업을 하게 됨:
- **전투 적용**: `ATBCombatParticipant`가 장착된 `MentalMethodTemplate.levelEffects[level-1].statModifiers`를 합산 → `getStats()`에 반영. 외공은 액티브 사용 명령어로 호출.
- **경험치**: `LearnedMentalMethod.gainExp(int)` / `LearnedExternalArt.gainExp(int)` 메서드 추가, 전투 종료/공격 성공 훅에서 호출.
- **필살기**: `MentalMethodTemplate`에 `triggerCondition`/`activeEffect` 컬럼 추가 (스키마 호환).
- **학습 이벤트**: NPC 가르침, 보상 등에서 `MartialArtLearner` 직접 호출.
- **무공서 폐기**: 별도 페이즈에서 `MartialArtsBookTemplate` 및 관련 코드 제거.
