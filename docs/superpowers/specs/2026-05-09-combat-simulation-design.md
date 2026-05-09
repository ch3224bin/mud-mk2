# 전투 시스템 개편 및 시뮬레이션 환경 설계

**날짜:** 2026-05-09  
**범위:** ATB 기반 전투 코어 교체 + REST API 시뮬레이션 환경 구축  
**참조:** `docs/combat_system.md`

---

## 1. 배경 및 목표

현재 `Combat.java`는 D20 주사위 기반의 임시 구현(고정 방어값 10, 1d6 데미지)으로,
`docs/combat_system.md`에 정의된 ATB 게이지 / 이중 방어 / AP·MP 자원 관리 시스템과 전혀 다르다.

**목표:**
- `combat_system.md`의 ATB 기반 전투 시스템으로 도메인 모델 전면 교체
- Spring Boot REST API로 조작 가능한 시뮬레이션 환경 구축
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
| (없음) | `SimulationController.java` | REST API 신규 |
| (없음) | `SimulationService.java` | 시뮬레이션 오케스트레이션 신규 |
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
      SimulationBattleRequest.java
      CharacterSetup.java
      SimulationBattleResponse.java
      SimulationLogEntry.java

  application/service/
    SimulationService.java        (신규)
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

### 4-1. REST API

```
POST /api/v1/simulation/battle
Content-Type: application/json
→ 전투 시뮬레이션 실행, SimulationBattleResponse 반환

GET /api/v1/simulation/battle/{id}
→ 이전 시뮬레이션 결과 재조회
```

### 4-2. Request DTO

```json
{
  "attacker": {
    "name": "홍길동",
    "vigor": 15,
    "physique": 12,
    "agility": 14,
    "intellect": 10,
    "will": 8,
    "meridian": 10,
    "innerPower": 0,
    "specialTechnique": 0,
    "lightStep": 5,
    "swordMethod": 8,
    "bladeMethod": 0,
    "fistsAndPalms": 0,
    "longWeapon": 0,
    "esotericWeapon": 0,
    "archery": 0,
    "activeWeaponType": "SWORD",   // "SWORD"|"BLADE"|"FIST"|"LONG_WEAPON"|"ESOTERIC"|"ARCHERY"
    "weaponBaseDamage": 20,
    "equipmentAccuracy": 0,
    "equipmentEvasion": 0,
    "equipmentArmor": 5,
    "equipmentArmorPct": 0
  },
  "defender": { ... },
  "maxTicks": 300,
  "speedDivisor": 10
}
```

`speedDivisor`는 선택 필드 (기본값 10). 밸런스 튜닝 시 변경 가능.

`activeWeaponType`은 `CombatFormulas`의 `weaponSkill` 파라미터로 전달할 무예 수치를 결정한다.
`SimulationService`가 `activeWeaponType`에 따라 해당 무예 필드(예: SWORD → `swordMethod`)를 선택한다.

### 4-3. Response DTO

```json
{
  "id": "550e8400-...",
  "winner": "홍길동",
  "totalTicks": 87,
  "totalActions": 12,
  "attackerFinalHp": 45,
  "defenderFinalHp": 0,
  "battleText": [
    "[전투 시작] 홍길동과 산적두목의 전투가 시작되었습니다!",
    "홍길동이 검법으로 산적두목을 공격했다!",
    "  → 산적두목에게 42 데미지! (남은 HP: 108)",
    "산적두목이 권장으로 홍길동을 공격했지만 홍길동이 피했다!",
    "홍길동이 검법으로 산적두목에게 치명타를 날렸다!",
    "  → 산적두목에게 63 데미지! (남은 HP: 45)",
    "산적두목이 쓰러졌다!",
    "[전투 종료] 홍길동 승리! (남은 HP: 45)"
  ],
  "log": [
    {
      "tick": 10,
      "actorName": "홍길동",
      "targetName": "산적두목",
      "evaded": false,
      "baseDamage": 28,
      "isCrit": true,
      "finalDamage": 42,
      "targetRemainingHp": 108,
      "actorApAfter": 80
    }
  ]
}
```

`battleText`는 MUD 게임 전투 메시지와 동일한 형태의 한국어 텍스트 목록이다.
구조화된 `log`는 밸런스 분석용, `battleText`는 전투 흐름 확인용으로 함께 제공한다.

### 4-4. `SimulationService` 책임

- `CharacterSetup` DTO → `CharacterStats` + `ATBCombatParticipant` 변환 (DB 조회 없음)
- `ATBCombat` 인스턴스 생성 → `tick()` 루프 (`maxTicks`까지 또는 전투 종료)
- 결과 UUID로 in-memory `ConcurrentHashMap`에 저장
- `CombatService`와 독립 — `ATBCombat` 도메인만 공유

---

## 4-5. MUD 인게임 시뮬레이션

REST API가 스탯 조작과 자동 전투 실행을 담당한다면, MUD 인게임 시뮬레이션은 **실제 게임 전투 흐름 그대로** 시뮬레이션 방에서 테스트하는 방식이다.

### 흐름

```
1. REST API로 시뮬레이션 방에 커스텀 스탯 NPC/몬스터 소환
   POST /api/v1/simulation/spawn
   { "roomId": <시뮬레이션 방 ID>, "target": { "name": "산적두목(시뮬)", "vigor": 20, ... } }

2. 플레이어가 시뮬레이션 방으로 이동
   > 북으로 이동
   시뮬레이션 훈련장에 들어왔다.
   산적두목(시뮬)이 있다.

3. 기존 명령어로 전투 시작
   > 때려 산적두목
   [전투] 홍길동과 산적두목(시뮬)의 전투가 시작되었습니다!
   홍길동이 검법으로 산적두목(시뮬)을 공격했다!
     → 42 데미지! (남은 HP: 108)
   ...

4. 전투 종료 처리
   - 플레이어 승리: 일반 전투 종료와 동일
   - 플레이어 사망: 전투 종료 → 즉시 부활 → 시작 지점으로 이동
     (시뮬레이션 방 사망은 패널티 없음)
```

### 추가되는 요소

| 요소 | 내용 |
|------|------|
| 시뮬레이션 방 | DB에 특수 플래그(`isSimulationRoom = true`) 있는 기존 Room |
| `POST /api/v1/simulation/spawn` | 시뮬레이션 방에 커스텀 스탯 Monster 소환. 기존 Monster 생성 로직 재사용 |
| 전투 실행 | 기존 `때려` 명령어 + `CombatService` 그대로 사용. `ATBCombat`으로 교체된 전투 엔진이 자동 적용 |
| 사망 처리 | 시뮬레이션 방 사망 시: 전투 종료 → HP 전량 회복 → 지정 부활 위치로 이동 |
| 출력 | 기존 WebSocket 채팅 시스템으로 전투 메시지 전달 (`CombatNarrativeFormatter` 공유) |

### REST API vs MUD 인게임 역할 비교

| 역할 | REST API (`/simulation/battle`) | MUD 인게임 |
|------|--------------------------------|-----------|
| 스탯 설정 | 요청 body에 직접 | REST API (`/simulation/spawn`)로 소환 |
| 전투 실행 | 자동 (전체 틱 루프) | 플레이어가 `때려`로 실시간 |
| 결과 확인 | JSON 응답 + `battleText` | 채팅창 텍스트 |
| 용도 | 빠른 밸런스 수치 검증 | 실제 플레이 감각 확인 |

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
| `SimulationController` | `@WebMvcTest` + MockMvc |
| `CombatService` | 기존 테스트 구조 유지, `ATBCombat` 교체만 반영 |

---

## 7. 구현 우선순위

| 단계 | 항목 |
|------|------|
| 1 | `CombatFormulas` 구현 + 단위 테스트 |
| 2 | `ATBCombatParticipant` 구현 |
| 3 | `ATBCombat` 구현 + 결정론적 시나리오 테스트 |
| 4 | `CombatNarrativeFormatter` 구현 |
| 5 | REST API: `SimulationController` + `SimulationService` + DTO 구현 |
| 6 | `CombatService` 수정 (ATBCombat 교체) |
| 7 | MUD 인게임: 시뮬레이션 방 설정 + `/simulation/spawn` API + 사망 시 부활 처리 |
| 8 | 기존 `CombatTest` 등 테스트 업데이트 |
