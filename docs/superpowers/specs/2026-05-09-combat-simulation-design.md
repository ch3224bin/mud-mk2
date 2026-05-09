# 전투 시스템 개편 및 시뮬레이션 환경 설계

**날짜:** 2026-05-09  
**범위:** ATB 기반 전투 코어 교체 + MUD 인게임 시뮬레이션 환경 구축  
**참조:** `docs/combat_system.md`

---

## 1. 배경 및 목표

현재 `Combat.java`는 D20 주사위 기반의 임시 구현(고정 방어값 10, 1d6 데미지)으로,
`docs/combat_system.md`에 정의된 ATB 게이지 / 이중 방어 / AP·MP 자원 관리 시스템과 전혀 다르다.

**목표:**
- `combat_system.md`의 ATB 기반 전투 시스템으로 도메인 모델 전면 교체
- 시뮬레이션 방에서 커스텀 스탯 몬스터와 실제 전투로 새 시스템 검증
- 시뮬레이션과 실제 전투가 동일한 도메인 모델을 공유

**이번 단계 구현 범위 (Phase 1):**
- ATB 게이지 + 틱 루프
- 명중/회피 판정
- 기본 피해 계산 + 치명타
- 이중 방어 (방어율% + 방어 수치)
- AP/MP 자원 관리 (회피 AP 소모, 행동권 획득 시 회복)

**다음 단계 (Phase 2, 별도 스펙):**
- 연격/단련/반격/압제 시스템
- 6종 상태이상
- 무공 슬롯 시스템

---

## 2. 아키텍처

### 변경 요약

| 기존 | 변경 후 | 비고 |
|------|---------|------|
| `Combat.java` | `ATBCombat.java` | ATB 틱 루프로 전면 재작성 |
| `CombatParticipant.java` | `ATBCombatParticipant.java` | atbGauge, 전투 중 HP/MP/AP 추가 |
| `InitiativeSystem.java` | `CombatFormulas.java` | 모든 수식 정적 클래스로 통합 |
| (없음) | `SimulationController.java` | spawn API 신규 |
| (없음) | `CombatNarrativeFormatter.java` | 전투 텍스트 포맷터 신규 |
| `CombatService.java` | 수정 | `ATBCombat` 사용으로 변경 |

### 패키지 구조

```
gameplay/
  application/domain/model/combat/
    ATBCombat.java
    ATBCombatParticipant.java
    CombatFormulas.java
    CombatGroup.java              (유지)
    CombatLog.java                (필드 추가)
    CombatGroupType.java          (유지)
    CombatState.java              (유지)
    RandomGenerator.java          (유지)
    DefaultRandomGenerator.java   (유지)

  adapter/in/webapi/              (신규 패키지)
    SimulationController.java
    dto/
      SimulationSpawnRequest.java
      MonsterSetup.java

  application/service/
    SimulationService.java        (신규 — spawn 처리)
    CombatNarrativeFormatter.java (신규 — 전투 텍스트 생성)
    CombatService.java            (수정)
```

---

## 3. 도메인 모델

### 3-1. `ATBCombatParticipant`

전투 참가자. `Combatable`을 래핑하고 전투 중 상태(ATB 게이지, 현재 HP/MP/AP)를 관리한다.

```java
public class ATBCombatParticipant {
    private final Combatable participant;
    private final CombatGroupType group;
    private double atbGauge;       // 0 ~ 100+
    private int currentHp;
    private int currentMp;
    private int currentAp;

    // ATB 전진: atbGauge += initiativeSpeed / SPEED_DIVISOR
    public void advanceAtb(double initiativeSpeed, int speedDivisor)

    // 행동권 획득 여부
    public boolean isReadyToAct()  // atbGauge >= 100

    // 잔여 유지 방식 리셋: atbGauge -= 100
    public void resetAtb()

    // 행동권 획득 시 자동 회복 (ATB 100 도달 시마다)
    // AP += 민첩 × 0.5 / MP += 경맥 × 0.3
    public void recoverResourcesOnAction()

    public boolean isDefeated()    // currentHp <= 0
}
```

초기 HP/MP/AP는 `CharacterStats.maxHp()` / `maxMp()` / `maxAp()` 로 설정한다.

### 3-2. `ATBCombat`

ATB 틱 기반 전투 엔진. 시뮬레이션과 실제 전투 모두 이 클래스를 사용한다.

```java
public class ATBCombat {
    public static final int SPEED_DIVISOR = 10;
    public static final int MAX_TICKS = 500;  // 무한루프 방지

    private final UUID id;
    private final List<ATBCombatParticipant> participants;  // 아군+적군 통합
    private final RandomGenerator randomGenerator;
    private final List<CombatLog> logs;
    private CombatState state;
    private int tickCount;
}
```

**`tick()` 메서드 흐름:**

```
1. 전체 참가자 ATB 전진 (initiativeSpeed / SPEED_DIVISOR)
2. atbGauge ≥ 100인 참가자 수집
   → atbGauge DESC 정렬 (동점 시 agility DESC)
3. 각 참가자 순서대로 행동:
   a. recoverResourcesOnAction() — AP/MP 자동 회복
   b. 대상 선택 (적 그룹에서 HP 가장 낮은 생존자)
   c. 회피 판정 → 회피 시 AP 5 소모, CombatLog 기록 후 다음 참가자
   d. 명중 시: 피해 계산 → 방어 적용 → HP 차감 → CombatLog 기록
   e. resetAtb()
4. 승리 조건 확인: 한 그룹 전멸 → CombatState.FINISHED
5. tickCount 증가
```

### 3-3. `CombatFormulas`

모든 전투 수식을 담는 순수 정적 유틸리티 클래스. 사이드 이펙트 없음.

```java
public final class CombatFormulas {

    // 선제 속도 = 민첩 × 1.0 + 경공 × 0.5
    public static double initiativeSpeed(CharacterStats stats)

    // 명중치 = 지력 × 0.8 + 해당외공 × 0.5 + 장비 명중 + 50
    public static double accuracy(CharacterStats stats, int weaponSkill, int equipAccuracy)

    // 회피치 = 민첩 × 0.8 + 경공 × 0.4
    public static double evasion(CharacterStats stats)

    // 회피율(%) = clamp(회피치 - 명중치, 0, 75)
    public static int evasionRate(double evasion, double accuracy)

    // 기본 피해 = 무기치 × (1 + 외공 × 0.008) + 근력 × 0.3
    //           × random(0.9, 1.1)
    public static int baseDamage(CharacterStats stats, int weaponBase,
                                  int weaponSkill, RandomGenerator rng)

    // 치명타율(%) = 근력 × 0.3
    public static int critRate(CharacterStats stats)

    // 방어 수치 = 장비방어 + 의지 × 0.4
    public static int armor(CharacterStats stats, int equipArmor)

    // 방어율(%) = 장비방어율 + 의지 × 0.2 + 외공 × 0.15  [상한 75%]
    public static int armorPct(CharacterStats stats, int equipArmorPct, int weaponSkill)

    // 최종 피해 = max(피해 × (1 - armorPct/100) - armor, 1)
    public static int applyDefense(int rawDamage, int armor, int armorPct)
}
```

### 3-4. `CombatLog` 필드 추가

기존 필드에 다음을 추가한다:

```java
boolean evaded          // 회피 성공 여부
boolean isCrit          // 치명타 여부
int attackerApAfter     // 행동 후 공격자 AP
int targetApAfter       // 행동 후 대상 AP
```

---

## 4. 시뮬레이션 레이어

### 4-1. 개요

시뮬레이션은 **MUD 인게임 전투** 방식으로만 진행한다.
REST API는 커스텀 스탯 몬스터를 시뮬레이션 방에 소환하는 설정 역할만 담당한다.
실제 전투는 기존 `때려` 명령어로 실행하며, 결과는 WebSocket 채팅으로 전달된다.

### 4-2. 흐름

```
1. REST API로 시뮬레이션 방에 커스텀 스탯 몬스터 소환
   POST /api/v1/simulation/spawn
   {
     "name": "산적두목(시뮬)",
     "vigor": 20, "physique": 15, "agility": 10,
     "intellect": 8, "will": 12, "meridian": 6,
     "weaponBaseDamage": 25,
     "equipmentArmor": 8, "equipmentArmorPct": 10
   }

2. 플레이어가 시뮬레이션 방으로 이동
   > 북으로 이동
   시뮬레이션 훈련장에 들어왔다.
   산적두목(시뮬)이 있다.

3. 기존 명령어로 전투 시작
   > 때려 산적두목
   [전투] 홍길동과 산적두목(시뮬)의 전투가 시작되었습니다!
   홍길동이 검법으로 산적두목(시뮬)을 공격했다!
     → 산적두목(시뮬)에게 42 데미지! (남은 HP: 108)
   산적두목(시뮬)이 권장으로 홍길동을 공격했지만 홍길동이 피했다!
   홍길동이 검법으로 산적두목(시뮬)에게 치명타를 날렸다!
     → 63 데미지! (남은 HP: 45)
   산적두목(시뮬)이 쓰러졌다!
   [전투 종료] 홍길동 승리!

4. 전투 종료 처리
   - 플레이어 승리: 일반 전투 종료와 동일
   - 플레이어 사망: 전투 종료 → HP 전량 회복 → 지정 부활 위치로 이동
     (시뮬레이션 방 사망은 패널티 없음)
```

### 4-3. 추가되는 요소

| 요소 | 내용 |
|------|------|
| 시뮬레이션 방 | DB에 특수 플래그(`simulationRoom = true`) 있는 기존 Room. 직접 생성 |
| `POST /api/v1/simulation/spawn` | 시뮬레이션 방에 커스텀 스탯 Monster를 인메모리로 소환 |
| `SimulationSpawnRequest` | name, 6속성, 무예, 장비값을 받는 DTO |
| 전투 실행 | 기존 `때려` 명령어 + `CombatService` 그대로 사용 |
| 사망 처리 | 시뮬레이션 방 사망 감지 → HP 전량 회복 → 부활 위치 이동 |
| `CombatNarrativeFormatter` | `CombatLog` → 한국어 텍스트 변환. WebSocket 메시지 전송에 사용 |

### 4-4. `CombatNarrativeFormatter`

`CombatLog`를 받아 MUD 전투 텍스트를 생성하는 단일 책임 클래스.

```java
public class CombatNarrativeFormatter {
    // 회피: "[이름]이 [무기]로 공격했지만 [대상]이 피했다!"
    // 치명타: "[이름]이 [무기]로 [대상]에게 치명타를 날렸다! → N 데미지! (남은 HP: X)"
    // 일반: "[이름]이 [무기]로 [대상]을 공격했다! → N 데미지! (남은 HP: X)"
    // 사망: "[대상]이 쓰러졌다!"
    public String format(CombatLog log)
}
```

`CombatService`(실제 전투)와 시뮬레이션 방 전투 모두 동일하게 사용한다.

---

## 5. `CombatService` 수정

- `Combat` → `ATBCombat`으로 교체
- `onTick()`: `ATBCombat.tick()` 호출 방식으로 변경
- 메시지 전송 로직은 `CombatLog`의 새 필드를 활용해 출력 포맷 보강
- `InitiativeSystem` 제거 (역할이 `CombatFormulas`로 통합)

---

## 6. 테스트 전략

| 대상 | 방법 |
|------|------|
| `CombatFormulas` | 순수 단위 테스트 (입력 → 기댓값 검증) |
| `ATBCombat` | `FixedRandomGenerator`로 결정론적 시나리오 테스트 |
| `SimulationController` (spawn) | `@WebMvcTest` + MockMvc |
| `CombatNarrativeFormatter` | 순수 단위 테스트 (CombatLog 입력 → 텍스트 검증) |
| `CombatService` | 기존 테스트 구조 유지, `ATBCombat` 교체만 반영 |

---

## 7. 구현 우선순위

| 단계 | 항목 |
|------|------|
| 1 | `CombatFormulas` 구현 + 단위 테스트 |
| 2 | `ATBCombatParticipant` 구현 |
| 3 | `ATBCombat` 구현 + 결정론적 시나리오 테스트 |
| 4 | `CombatNarrativeFormatter` 구현 + 단위 테스트 |
| 5 | `CombatService` 수정 (ATBCombat 교체) |
| 6 | `SimulationController` (spawn/room/delete API) + `SimulationService` + DTO 구현 |
| 7 | `simulation-management.html` admin 페이지 구현 |
| 8 | 시뮬레이션 방 DB 설정 + 사망 시 부활 처리 |
| 9 | 기존 `CombatTest` 등 테스트 업데이트 |

---

## 8. Admin 페이지: `simulation-management.html`

기존 admin 페이지 패턴(Thymeleaf + Bootstrap 5, 터미널 스타일 다크 테마)을 따른다.

### 페이지 구성

```
┌─ 시뮬레이션 관리 ──────────────────────────────────────┐
│                                                          │
│  ┌─ 몬스터 소환 ─────────────────────────────────────┐  │
│  │  이름: [____________]                              │  │
│  │                                                    │  │
│  │  ── 속성 ────────────────────────────────────     │  │
│  │  근력: [10]  체질: [10]  민첩: [10]               │  │
│  │  지력: [10]  의지: [10]  경맥: [10]               │  │
│  │                                                    │  │
│  │  ── 무예 ────────────────────────────────────     │  │
│  │  내공: [0]   절기: [0]   경공: [0]                │  │
│  │  권장: [0]   검법: [0]   도법: [0]                │  │
│  │  장병: [0]   기문: [0]   사술: [0]                │  │
│  │                                                    │  │
│  │  ── 장비 ────────────────────────────────────     │  │
│  │  무기 기본치: [20]  방어수치: [0]  방어율(%): [0] │  │
│  │                                                    │  │
│  │  [소환하기]                                        │  │
│  └────────────────────────────────────────────────┘  │
│                                                          │
│  ┌─ 현재 시뮬레이션 방 상태 ───────────────────────┐  │
│  │  산적두목(시뮬)  근력:20  민첩:10  HP:150  [제거] │  │
│  └────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

### REST API (SimulationController)

```
POST   /api/v1/simulation/spawn       → 커스텀 스탯 몬스터 소환
GET    /api/v1/simulation/room        → 시뮬레이션 방 현재 몬스터 목록
DELETE /api/v1/simulation/spawn/{id}  → 특정 몬스터 제거
```

소환된 몬스터는 `SimulationService` 내 in-memory Map에 보관한다.
서버 재시작 시 초기화되며, 영속화하지 않는다.

### admin 메뉴 연결

기존 `admin.html` 메뉴에 "시뮬레이션 관리" 링크 추가.
URL: `/admin/simulation`
