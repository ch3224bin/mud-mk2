# 무공 조회 — 상태창 확장 & `무공`/`무공창` 명령 설계

> Phase 1(템플릿/학습/장착 + 어드민) 완성 위에 **사용자 조회 기능**을 더한다.
> 본 페이즈 비포함: 경험치 누적, 다음 레벨까지 계산, 전투 적용, 외공 사용 명령, 무공서 폐기.

## 1. 범위

- **A. 상태창 확장** — 기존 `상태`/`상태창` 출력에 `[ 장착 무공 ]` 섹션 추가
  - 심법 3슬롯(`INNER_POWER`/`LIGHT_STEP`/`SPECIAL_TECHNIQUE`) + 외공 6슬롯, 빈 슬롯도 표시
  - 각 슬롯: 무공 이름 / 현재 레벨(MAX 표기) / 현재 레벨에서의 효과
- **B. 새 명령 `무공`/`무공창`** — 학습한 무공 전체 정보 (장착 안 한 것 포함)
  - 심법은 `MentalMethodKind`별, 외공은 `WeaponType`별로 그룹화
  - 각 항목: 이름, 현재 레벨/최대 레벨(MAX 표기), 현재 경험치, 현재 레벨 효과, 장착 마킹 `☆`
  - 학습 0건이면 "배운 무공이 없습니다." 한 줄 출력
- **C. 도메인 메서드** — `MentalMethodTemplate.effectAt(level)` / `ExternalArtTemplate.effectAt(level)`

## 2. 도메인 변경

위치: `gamedata/application/domain/model/martialart/`

```java
// MentalMethodTemplate
public MentalMethodLevelEffect effectAt(int level) {
    if (level < 1 || level > maxLevel) {
        throw new IllegalArgumentException(
                "level must be in 1.." + maxLevel + ", got " + level);
    }
    return levelEffects.get(level - 1);
}

// ExternalArtTemplate (동일 시그니처/검증)
public ExternalArtLevelEffect effectAt(int level) { ... }
```

- `levelEffects`는 생성/수정 시 1..maxLevel 검증이 끝나 있으므로 단순 인덱싱.
- IAE는 정상 경로에선 발생 X — `LearnedXxx.currentLevel` 값만 호출자가 넘김. 방어적 검증으로 둠.

## 3. 명령 등록

`gameplay/application/domain/model/command/CommandDictionary.java`에 항목 추가:

```java
MARTIAL_ART("무공", "무공창"),
```

새 명령 객체:

```java
// gameplay/application/domain/model/command/MartialArtViewCommand.java
public record MartialArtViewCommand(Long userId) implements Command {}
```

Parser / Executor는 `EquipmentView*` 패턴 그대로:

- `MartialArtViewCommandParser`: `Pattern.compile("(" + CommandDictionary.MARTIAL_ART.toRegex() + ")")`
- `MartialArtViewCommandExecutor`: `command instanceof MartialArtViewCommand` → `useCase.showMartialArts(cmd)`

## 4. Variables 구조

### 4.1 `StatusVariables` 확장

기존 record 마지막에 1개 필드 추가:

```java
public record StatusVariables(
    Long userId,
    /* ...기존 필드 그대로... */,
    String roomName,
    EquippedMartialArtsView equippedMartialArts   // 신규
) {
    public record EquippedMartialArtsView(
        List<MentalSlotLine> mentalSlots,    // 항상 size 3, MentalMethodKind 엔움 선언순
        List<ExternalSlotLine> externalSlots // 항상 size 6 (EXTERNAL_SLOT_MAX), 1..6 슬롯번호
    ) {}

    public record MentalSlotLine(
        String kindLabel,         // "내공" / "경공" / "특기"
        String name,              // null = 빈 슬롯 → 템플릿 "(없음)"
        Integer currentLevel,     // null = 빈 슬롯
        Integer maxLevel,         // null = 빈 슬롯, 그 외 atMax 판단용
        List<StatModLine> effects // 빈 슬롯이면 List.of()
    ) {}

    public record ExternalSlotLine(
        int slotNumber,           // 1..6, 항상 채움
        String name,              // null = 빈 슬롯
        String weaponLabel,       // null 가능
        Integer currentLevel,
        Integer maxLevel,
        Double damageMultiplier,
        Integer cooldownSeconds,
        Integer apCost,
        Integer mpCost
    ) {}

    public record StatModLine(String label, int value) {}  // 예: ("활력", 5)
}
```

### 4.2 `MartialArtViewVariables` (신규)

위치: `gameplay/application/service/model/template/MartialArtViewVariables.java`

```java
public record MartialArtViewVariables(
    Long userId,
    List<MentalGroup> mentalGroups,    // 학습한 게 있는 kind만, kind 엔움 선언순
    List<ExternalGroup> externalGroups // 학습한 게 있는 weaponType만, 엔움 선언순
) {
    public record MentalGroup(
        String kindLabel,                  // "내공"
        List<LearnedMentalLine> items      // 학습 순서(insertion order)
    ) {}

    public record LearnedMentalLine(
        String name,
        int currentLevel,
        int maxLevel,
        long currentExp,                   // 표시: "Exp 0" (Phase 1 항상 0)
        boolean atMax,                     // currentLevel == maxLevel
        boolean equipped,                  // 장착됐으면 ☆
        List<StatModLine> effects          // effectAt(currentLevel).statModifiers 매핑
    ) {}

    public record ExternalGroup(
        String weaponLabel,                // "검"
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

## 5. 서비스 & 매퍼

### 5.1 신규 유스케이스 / 포트

- `gameplay/application/service/provided/MartialArtViewUseCase.java`
  ```java
  public interface MartialArtViewUseCase {
      void showMartialArts(MartialArtViewCommand command);
  }
  ```
- `gameplay/application/service/required/SendMartialArtViewMessagePort.java`
  ```java
  public interface SendMartialArtViewMessagePort {
      void sendMessage(MartialArtViewVariables variables);
  }
  ```

### 5.2 공유 매퍼 — `MartialArtViewMapper`

위치: `gameplay/application/service/command/martialart/MartialArtViewMapper.java`

**왜 매퍼를 두는가**: 상태창과 무공창이 같은 `LearnedMartialArtFinder` + 템플릿 finder 의존성을 갖고 동일한 변환(장착 슬롯, 학습 목록 → Variables) 일부를 공유한다. 두 서비스에 중복하지 않도록 분리. 매퍼는 도메인 → Variables 변환만 담당(`multiline-message-template.md` 규칙 준수).

```java
@Component
public class MartialArtViewMapper {

    private final LearnedMartialArtFinder finder;
    private final MentalMethodTemplateFinder mentalTplFinder;
    private final ExternalArtTemplateFinder externalTplFinder;

    // A. 상태창용 — 항상 3+6 슬롯 고정
    public EquippedMartialArtsView toEquippedView(PlayerCharacter pc) {
        CharacterMartialArtView v = finder.findByCharacter(pc.getId());
        Map<UUID, LearnedMentalMethod> mentalById  = byId(v.learnedMentalMethods());
        Map<UUID, LearnedExternalArt>  externalById = byId(v.learnedExternalArts());

        List<MentalSlotLine> mentalSlots = new ArrayList<>();
        for (MentalMethodKind kind : MentalMethodKind.values()) {        // 엔움 선언순
            UUID learnedId = v.equipped().getMentalSlots().get(kind);
            mentalSlots.add(buildMentalSlot(kind, learnedId, mentalById));
        }

        List<ExternalSlotLine> externalSlots = new ArrayList<>();
        List<UUID> equippedExternals = v.equipped().getExternalSlots();
        for (int i = 0; i < EquippedMartialArts.EXTERNAL_SLOT_MAX; i++) {
            UUID learnedId = i < equippedExternals.size() ? equippedExternals.get(i) : null;
            externalSlots.add(buildExternalSlot(i + 1, learnedId, externalById));
        }
        return new EquippedMartialArtsView(mentalSlots, externalSlots);
    }

    // B. 무공창용 — 학습 전체, 그룹화, 장착 마킹
    public MartialArtViewVariables toMartialArtVariables(Long userId, PlayerCharacter pc) {
        CharacterMartialArtView v = finder.findByCharacter(pc.getId());
        Set<UUID> equippedMentalIds  = Set.copyOf(v.equipped().getMentalSlots().values());
        Set<UUID> equippedExternalIds = Set.copyOf(v.equipped().getExternalSlots());

        List<MentalGroup>   mentalGroups   = groupMental(v.learnedMentalMethods(), equippedMentalIds);
        List<ExternalGroup> externalGroups = groupExternal(v.learnedExternalArts(), equippedExternalIds);
        return new MartialArtViewVariables(userId, mentalGroups, externalGroups);
    }
}
```

내부 헬퍼:
- `buildMentalSlot(kind, learnedId, mentalById)` — learnedId null이면 빈 슬롯 `MentalSlotLine`. 아니면 `LearnedMentalMethod` + `MentalMethodTemplate.effectAt(currentLevel)` 매핑.
- `buildExternalSlot(slotNumber, learnedId, externalById)` — 동일.
- `groupMental(learned, equippedIds)` — `kind`별 그룹화, 학습 0건인 `kind` 그룹은 생략. `MentalMethodKind` 엔움 선언순 정렬.
- `groupExternal(learned, equippedIds)` — `weaponType`별 그룹화. 학습 0건인 weaponType 그룹은 생략. `WeaponType` 엔움 선언순.
- `toStatModLines(List<StatModifier>)` — `ItemDisplayLabels.of(StatType)` 활용.

### 5.3 한글 라벨

`gameplay/application/service/command/look/ItemDisplayLabels.java`에 오버로드 추가:

```java
public static String of(MentalMethodKind kind) {
    return switch (kind) {
        case INNER_POWER       -> "내공";
        case LIGHT_STEP        -> "경공";
        case SPECIAL_TECHNIQUE -> "특기";
    };
}
```

### 5.4 서비스 변경

**`StatusCommandService`** — 의존성에 `MartialArtViewMapper` 추가, `StatusVariables` 생성 시 마지막 인자로 `martialArtViewMapper.toEquippedView(player)` 결과 전달.

**`MartialArtViewCommandService` (신규)**:
```java
@Service
public class MartialArtViewCommandService implements MartialArtViewUseCase {
    private final ActivePlayerRepository players;
    private final MartialArtViewMapper mapper;
    private final SendMartialArtViewMessagePort port;

    @Override
    public void showMartialArts(MartialArtViewCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
        port.sendMessage(mapper.toMartialArtVariables(command.userId(), player));
    }
}
```

## 6. Adapter (MessageSender) & 템플릿

### 6.1 `StatusMessageSender` 수정

`context.setVariable("equippedMartialArts", statusVariables.equippedMartialArts())` 한 줄 추가.

### 6.2 `MartialArtViewMessageSender` 신규

`gameplay/adapter/out/eventpublisher/chat/MartialArtViewMessageSender.java`

```java
@Component
public class MartialArtViewMessageSender implements SendMartialArtViewMessagePort {
    private final TemplateEngine templateEngine;
    private final ChatEventPublisher chatEventPublisher;

    @Override
    public void sendMessage(MartialArtViewVariables variables) {
        Context context = new Context();
        context.setVariable("mentalGroups", variables.mentalGroups());
        context.setVariable("externalGroups", variables.externalGroups());
        String html = templateEngine.process("gameplay/martial-art-view", context);
        chatEventPublisher.messageToUser(variables.userId(), html);
    }
}
```

### 6.3 `status.html` — `[ 무예 ]` 다음, `archery` 라인 끝에 이어붙임

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

### 6.4 `martial-art-view.html` (신규)

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

## 7. 데이터 흐름 (요약)

**A. 상태창**:
```
StatusCommandParser → StatusCommand → StatusCommandExecutor
  → StatusCommandService:
      1. player 조회 (userId → PlayerCharacter)
      2. CharacterStats / Room (기존)
      3. martialArtViewMapper.toEquippedView(player)  ← 신규
      4. StatusVariables 조립 → SendStatusMessagePort.sendMessage
```

**B. 무공창**:
```
MartialArtViewCommandParser → MartialArtViewCommand → MartialArtViewCommandExecutor
  → MartialArtViewCommandService:
      1. player 조회
      2. martialArtViewMapper.toMartialArtVariables(userId, player)
      3. SendMartialArtViewMessagePort.sendMessage(variables)
```

## 8. 에러 처리

- `PlayerNotFoundException` (기존): userId로 player 없으면 발생. 다른 명령과 동일 처리.
- 도메인 `effectAt` `IllegalArgumentException`: 정상 경로에선 발생 X (방어막). 발생 시 서비스에서 잡지 않고 상위 글로벌 예외 핸들러로 전파.
- 학습 0건: 정상 출력 ("배운 무공이 없습니다." 한 줄). 예외 아님.

## 9. 테스트 전략

### 9.1 도메인 단위 (no Spring)

- `MentalMethodTemplateTest.effectAt`
  - 정상: 1, 중간, maxLevel
  - 비정상: 0, -1, maxLevel+1 → IAE
- `ExternalArtTemplateTest.effectAt` — 동일

### 9.2 매퍼 단위 (Mockito)

`MartialArtViewMapperTest`

- `toEquippedView`:
  - 심법 0/1/2/3 슬롯 다양: `MentalSlotLine`은 항상 3개, 빈 슬롯은 `name == null`
  - 외공 0/1/3/6 슬롯 다양: 항상 6개, `slotNumber` 1..6, 장착 순서 그대로 채움
  - 효과 매핑 검증: `template.effectAt(currentLevel)` 결과가 `StatModLine` / 외공 필드로 변환
- `toMartialArtVariables`:
  - 학습 0건 → 두 그룹 리스트 모두 empty
  - 심법 일부 kind만 학습 → 해당 kind 그룹만, 엔움 선언순
  - 외공 일부 weaponType만 학습 → 해당 weaponType 그룹만, 엔움 선언순
  - `equipped=true`/`false` 분기
  - `atMax`: currentLevel == maxLevel일 때만 true

### 9.3 서비스 단위 (Mockito)

- `StatusCommandServiceTest` — 기존 케이스 유지, `StatusVariables.equippedMartialArts`에 mapper 결과가 들어가는지 ArgumentCaptor로 검증
- `MartialArtViewCommandServiceTest`
  - 정상: player 조회 → mapper 호출 → port.sendMessage(variables)
  - `PlayerNotFoundException` 전파

### 9.4 Parser / Executor 단위

- `MartialArtViewCommandParserTest` — "무공", "무공창" 매칭 / "무공창고" 등 오매칭 거부 (`EquipmentViewCommandParserTest` 패턴)
- `MartialArtViewCommandExecutorTest` — `canExecute` 분기, `execute` 시 useCase 호출 확인

### 9.5 MessageSender 단위

`MartialArtViewMessageSenderTest` (`EquipmentViewMessageSenderTest` 패턴)
- 학습 0건 / 심법만 / 외공만 / 둘 다 / 장착 마킹 / MAX 케이스
- ArgumentCaptor로 `ChatEventPublisher.messageToUser(userId, html)` 호출의 html 내 핵심 토큰 검증
  - "배운 무공이 없습니다.", "[ 심법 ]", "[ 외공 ]", "〈검〉", "☆", "[MAX]", 효과 라벨

`StatusMessageSender` 기존 테스트에 무공 섹션 토큰 검증 추가.

### 9.6 비포함

- 통합 테스트(`@SpringBootTest`)
- 전투/경험치/필살기 시스템 (후속 페이즈)

## 10. 브랜치 전략

- `main`에서 `feature/martial-arts-view` 새 브랜치 분기
- 구현 완료 후 main에 머지

## 11. 새/변경 파일 일람

| 파일 | 신규/변경 |
|---|---|
| `gamedata/.../domain/model/martialart/MentalMethodTemplate.java` | 변경(effectAt) |
| `gamedata/.../domain/model/martialart/ExternalArtTemplate.java` | 변경(effectAt) |
| `gameplay/.../domain/model/command/CommandDictionary.java` | 변경(MARTIAL_ART) |
| `gameplay/.../domain/model/command/MartialArtViewCommand.java` | 신규 |
| `gameplay/.../service/command/martialart/MartialArtViewMapper.java` | 신규 |
| `gameplay/.../service/command/MartialArtViewCommandService.java` | 신규 |
| `gameplay/.../service/command/StatusCommandService.java` | 변경 |
| `gameplay/.../service/command/look/ItemDisplayLabels.java` | 변경(of(MentalMethodKind)) |
| `gameplay/.../service/model/template/StatusVariables.java` | 변경 |
| `gameplay/.../service/model/template/MartialArtViewVariables.java` | 신규 |
| `gameplay/.../service/provided/MartialArtViewUseCase.java` | 신규 |
| `gameplay/.../service/required/SendMartialArtViewMessagePort.java` | 신규 |
| `gameplay/.../adapter/in/eventlistener/parser/MartialArtViewCommandParser.java` | 신규 |
| `gameplay/.../adapter/in/eventlistener/executor/MartialArtViewCommandExecutor.java` | 신규 |
| `gameplay/.../adapter/out/eventpublisher/chat/StatusMessageSender.java` | 변경 |
| `gameplay/.../adapter/out/eventpublisher/chat/MartialArtViewMessageSender.java` | 신규 |
| `resources/templates/gameplay/status.html` | 변경 |
| `resources/templates/gameplay/martial-art-view.html` | 신규 |
