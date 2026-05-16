# `먹어` 명령 — 설계 (spec)

**Date:** 2026-05-16
**Status:** Approved

## 목적

플레이어가 인벤토리의 음식 아이템(`FoodTemplate`)을 `먹어` 명령으로 소비하면 hp/mp/ap가 회복되도록 한다. 향후 일정 시간 버프 효과를 끼워 넣을 확장 포인트(`EatEffect` 인터페이스)를 미리 마련하되, 버프 자체는 이번 스펙 범위에 포함하지 않는다.

DB 동기화 정책은 아이템 종류에 따라 분기한다. 음식 같은 회복성 소비 아이템은 player 저장을 batch sync에 맡겨도 안전하지만, 향후 추가될 "영구 효과를 부여하는 아이템"은 인벤토리 → 영구효과 적용 사이에 서버가 죽으면 복사 위험이 생기므로 즉시 player 저장이 필요하다. `ItemTemplate`이 자기 정책을 선언한다.

## 아키텍처 흐름

```
사용자 입력 "사과 먹어"
  → EatCommandParser   (CommandDictionary.EAT 별칭 매칭)
  → EatCommand(userId, itemName, index)
  → EatCommandExecutor (CommandExecutorChain의 일원)
  → EatUseCase / EatCommandService
       ├─ PlayerCharacter 조회 (ActivePlayerRepository)
       ├─ Inventory.findItemsByName + index 검증
       ├─ FoodTemplate.getEatEffects().forEach(e -> e.applyTo(player))
       ├─ Inventory.consumeOne(instance)        // 수량 1 감소, 0이면 List에서 제거 (반환값=제거 여부)
       ├─ if (removed) ItemInstanceRepository.delete(instance)              // 가비지 행 방지
       └─ if (template.requiresImmediateDeletion()) PlayerCharacterRepository.save(player)
            // 이번 스펙에서는 FoodTemplate=false라 호출 안 됨. 분기 코드만 마련.
  → SendMessageToUserPort.messageToUser(userId, "사과를 먹어 hp 30, mp 10이(가) 회복되었다.")
```

기존 `EquipCommandParser` / `EquipCommandExecutor` / `EquipCommandService` 패턴을 그대로 따른다.

## 도메인 변경

### `EatEffect` 인터페이스 (신규)

위치: `gamedata.application.domain.model.item.effect`

```java
public interface EatEffect {
    void applyTo(PlayerCharacter player);
}
```

구체 구현 3종 (모두 record):
- `HpRestoreEffect(int amount)` — `applyTo(p)` → `p.heal(amount, 0, 0)`
- `MpRestoreEffect(int amount)` — `applyTo(p)` → `p.heal(0, amount, 0)`
- `ApRestoreEffect(int amount)` — `applyTo(p)` → `p.heal(0, 0, amount)`

### `FoodTemplate.getEatEffects()` (메서드 추가)

기존 컬럼(`hpRecovery`, `mpRecovery`, `apRecovery`)은 그대로 둔다. 호출 시점에 effect 리스트로 빌드 — 0인 수치는 제외.

```java
public List<EatEffect> getEatEffects() {
    var effects = new ArrayList<EatEffect>();
    if (hpRecovery > 0) effects.add(new HpRestoreEffect(hpRecovery));
    if (mpRecovery > 0) effects.add(new MpRestoreEffect(mpRecovery));
    if (apRecovery > 0) effects.add(new ApRestoreEffect(apRecovery));
    return effects;
}
```

향후 버프 효과는 `FoodTemplate`에 `buffDuration` / `buffStat` / `buffMagnitude` 컬럼을 추가하고 `BuffEffect`를 리스트에 더하는 식으로 확장한다 — 이번 스펙에서는 구현하지 않는다.

### `ItemTemplate.requiresImmediateDeletion()` (abstract 메서드 추가)

```java
public abstract boolean requiresImmediateDeletion();
```

기존 모든 서브타입(`FoodTemplate`, `WeaponTemplate`, `EquipmentTemplate`, `AccessoryTemplate`, `MissionItemTemplate`)은 `false` 반환. 향후 영구 효과 아이템 도입 시 그 타입만 `true`.

## 회복 로직

### `BaseCharacter` (메서드 추가)

부분 회복 메서드 3종 — max 클램프. max는 호출자가 넘긴다 (BaseCharacter는 stat 합산 책임 없음).

```java
public void healHp(int amount, int maxHp) {
    this.hp = Math.min(this.hp + amount, maxHp);
}
public void healMp(int amount, int maxMp) { ... }
public void healAp(int amount, int maxAp) { ... }
```

### `PlayerCharacter.heal` (진입점, 신규)

`CharacterStats.maxHp()/maxMp()/maxAp()`로 장비 보너스 포함된 max를 계산해 `BaseCharacter`에 전달.

```java
public void heal(int hp, int mp, int ap) {
    CharacterStats stats = getStats();
    baseCharacterInfo.healHp(hp, stats.maxHp());
    baseCharacterInfo.healMp(mp, stats.maxMp());
    baseCharacterInfo.healAp(ap, stats.maxAp());
}
```

0 회복 호출은 max에 영향 없으므로 안전.

### `Inventory.consumeOne` (메서드 추가)

수량 1 감소, 0이면 List에서 제거. 반환값으로 "instance가 List에서 제거됐는지" 알린다 — 호출자(EatCommandService)가 DB delete를 호출할지 결정하는 데 사용.

```java
public boolean consumeOne(ItemInstance instance) {
    instance.decreaseQuantity(1);
    if (instance.getQuantity() <= 0) {
        items.remove(instance);
        return true;
    }
    return false;
}
```

### `ItemInstance.decreaseQuantity(int)` (메서드 추가)

기존 `addQuantity(int)`와 대칭. 음수가 되지 않도록 가드만.

## DB 동기화 정책

`Inventory.items`의 JPA 매핑(`@OneToMany(cascade={PERSIST,MERGE})`, `orphanRemoval=false`)은 **이번 스펙에서 건드리지 않는다**. 이유: `ItemInstance`는 `inventory_id`와 `equipped_items_id` 두 FK 컬럼을 가지며, equip/unequip 흐름에서 한 인스턴스가 두 컬렉션 사이를 이동한다. `orphanRemoval=true`를 켜면 JPA가 "inventory에서 빠짐 = orphan → DELETE"로 판정하여 장착된 아이템이 통째로 삭제되는 회귀 버그가 날 수 있다. JPA 매핑 일원화는 별도 스펙으로 분리.

대신 `EatCommandService`에서 정책별로 명시적 저장 호출:

| 케이스 | `ItemInstanceRepository.delete(instance)` | `PlayerCharacterRepository.save(player)` |
|-------|------------------------------------------|------------------------------------------|
| 수량 5→4 (음식, 인스턴스 유지) | ❌ (batch sync로 UPDATE) | ❌ (batch sync) |
| 수량 1→0 (음식, 인스턴스 제거) | ✅ **즉시** (orphan 행 방지) | ❌ (batch sync) |
| 영구효과 아이템 사용 | ✅ **즉시** | ✅ **즉시** (재기동 시 부활 차단) |

"음식은 batch OK"라는 사용자 의도가 실제로 의미하는 건 **player 저장(`inventory_id` FK 갱신)이 60초 뒤여도 안전**하다는 것이다. instance 행 자체의 delete는 둘 다 즉시 — 음식이라도 instance 행 누적은 의미 있는 트레이드오프가 아니다.

## 명령어 컴포넌트

### `CommandDictionary.EAT` (enum 값 추가)

```java
EAT("먹다", "먹어", "먹"),
```

### `EatCommand` (record, 신규)

```java
public record EatCommand(Long userId, String itemName, int index) implements Command {}
```

### `EatCommandParser` (신규)

`EquipCommandParser`와 동일한 정규식 구조 — `(item-name)(?:\s+(index))?\s+(verb)`. 매칭 예: `"사과 먹어"`, `"고기 2 먹다"`.

```java
private static final Pattern EAT_PATTERN =
        Pattern.compile("(\\S+?)(?:\\s+(\\d+))?\\s+(" + CommandDictionary.EAT.toRegex() + ")");
```

### `EatCommandExecutor` (신규)

`canExecute(cmd instanceof EatCommand)` → `eatUseCase.eat(cmd)`. `EquipCommandExecutor`와 동형.

### `EatUseCase` 인터페이스 (신규)

```java
public interface EatUseCase {
    void eat(EatCommand command);
}
```

### `EatCommandService` (신규, `implements EatUseCase`)

의존성:
- `ActivePlayerRepository players`
- `ItemInstanceRepository itemInstanceRepository`
- `PlayerCharacterRepository playerCharacterRepository`
- `SendMessageToUserPort sendMessageToUserPort`

로직:
1. 입력 검증 (`index >= 1`, 아니면 `"올바른 번호를 입력해주세요."`)
2. player 조회 (`PlayerNotFoundException`)
3. `inventory.findItemsByName(name)` + index 범위 검증
4. `instance.getTemplate() instanceof FoodTemplate food` 분기 (아니면 거부)
5. `food.getEatEffects().forEach(e -> e.applyTo(player))`
6. `boolean removed = inventory.consumeOne(instance);`
7. `if (removed) itemInstanceRepository.delete(instance);`
8. `if (template.requiresImmediateDeletion()) playerCharacterRepository.save(player);`
9. 결과 메시지 송신

## 메시지

### 결과 (성공) — 한 줄

0이 아닌 회복 항목만 결합:

```
사과를 먹어 hp 30, mp 10이(가) 회복되었다.
육포를 먹어 hp 50이(가) 회복되었다.
```

Service 내부 작은 helper 메서드로 조립. 한 줄 출력이므로 `SendMessageToUserPort.messageToUser` 직접 사용 (CLAUDE.md 규칙: "결과 한 줄 통지" 허용 — Thymeleaf 템플릿 불필요).

### 거부 — 한 줄 4종

| 케이스 | 메시지 |
|--------|--------|
| 인벤토리에 없음 | `"사과(을)를 가지고 있지 않습니다."` |
| index가 가진 개수보다 큼 | `"사과 3번째 아이템을 찾을 수 없습니다."` |
| FOOD 아닌 아이템 | `"철검은(는) 먹을 수 없습니다."` |
| `index < 1` | `"올바른 번호를 입력해주세요."` |

플레이어 사망/기절 상태 체크는 이번 스펙에서 다루지 않음 — 다른 명령어들도 가드를 두지 않아 일관성 유지. 전체 명령 통일 사망 가드는 별도 스펙으로 분리.

## 테스트 전략

### `EatCommandServiceTest` (Mockito)

- 정상 흐름: 사과(hp 30) 먹기 → `player.heal(30,0,0)` 호출 + `inventory.consumeOne` 호출 + 메시지 송신 검증
- 수량 5→4: `itemInstanceRepository.delete` **호출 안 됨**
- 수량 1→0: `itemInstanceRepository.delete(instance)` **즉시 호출**
- `requiresImmediateDeletion()=true` 분기: `playerCharacterRepository.save(player)` 즉시 호출 (mock 템플릿으로 시뮬레이션)
- 에러 케이스 4종: 각각 거부 메시지 송신 검증

### 도메인 단위 테스트

- `BaseCharacterTest`: `healHp(30, 100)` 50→80, `healHp(80, 100)` 50→100 (clamp)
- `InventoryTest`: `consumeOne(instance)` — quantity 2→1 (false 반환), quantity 1→0 (true 반환 + 리스트 제거)
- `FoodTemplateTest`: `getEatEffects()` — 0인 회복은 리스트에서 제외
- `HpRestoreEffectTest` / `MpRestoreEffectTest` / `ApRestoreEffectTest`: `applyTo(player)` → `player.heal` 호출 검증

### `EatCommandParserTest`

- `"사과 먹어"` → `EatCommand("사과", 1)`
- `"고기 2 먹다"` → `EatCommand("고기", 2)`
- 다른 명령어 chain과 충돌 없음

### 통합 테스트

이번 스펙에서는 추가하지 않는다 — 기존 명령어 모두 service-level unit test로 커버하는 패턴 유지.

## 범위 외 (Out of Scope)

다음 항목은 이번 스펙에 포함하지 않는다. 각 항목은 후속 별도 스펙으로 다룬다.

- **버프 효과 (일정 시간)** — `BuffEffect` 구현, 시간 기반 만료 처리, `PlayerCharacter`의 활성 버프 컬렉션, tick 기반 만료 스케줄링.
- **JPA `Inventory.items` / `EquippedItems.slots` 매핑 일원화** — `orphanRemoval` 정책 정리는 equip/unequip/take/drop 흐름까지 함께 재설계해야 하므로 별도 스펙.
- **전체 명령어 통일 사망/기절 가드** — Eat뿐 아니라 모든 게임 명령에 적용되어야 일관성이 살아나므로 분리.
- **실제 영구 효과 아이템(예: 스킬북) 도입** — `requiresImmediateDeletion()=true` 분기는 이번에 코드로 마련하지만 실 사용 사례는 후속 스펙.
