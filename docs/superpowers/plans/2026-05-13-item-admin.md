# Item Admin UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 아이템 템플릿 CRUD와 인스턴스 배치를 위한 Admin 웹 UI를 추가한다.

**Architecture:** UseCase 포트(provided) → Service → REST Controller 패턴. `ItemInstanceService`는 `GameWorldService`(gameplay 패키지) 의존성 때문에 `gameplay.application.service`에 배치하여 순환 의존성을 방지한다. 나머지 서비스는 `gamedata` 패키지에 위치한다.

**Tech Stack:** Spring Boot 3, JPA/Hibernate (JOINED inheritance), Thymeleaf, Bootstrap 5, 바닐라 JS (Fetch API)

---

## File Map

### 신규 생성

```
gamedata/application/service/provided/
  ItemTemplateCreator.java
  ItemTemplateFinder.java
  ItemTemplateModifier.java
  ItemTemplateRemover.java
  ItemInstancePlacer.java

gamedata/application/service/model/request/
  ItemTemplateRequest.java
  StatModifierRequest.java
  ItemInstancePlaceRequest.java

gamedata/adapter/webapi/response/
  ItemTemplateResponse.java
  StatModifierResponse.java
  PlayerCharacterSearchResponse.java

gamedata/adapter/webapi/
  ItemTemplateController.java
  ItemInstanceController.java
  PlayerCharacterController.java

gamedata/application/service/
  ItemTemplateService.java

gameplay/application/service/
  ItemInstanceService.java

resources/templates/web/
  item-template-management.html
  item-instance-management.html
```

### 수정

```
gamedata/application/domain/model/item/ItemTemplate.java      ← updateCommon() 추가
gamedata/application/domain/model/item/FoodTemplate.java      ← update() 추가
gamedata/application/domain/model/item/WeaponTemplate.java    ← update() 추가
gamedata/application/domain/model/item/EquipmentTemplate.java ← update() 추가
gamedata/application/domain/model/item/AccessoryTemplate.java ← update() 추가
gamedata/application/domain/model/item/MartialArtsBookTemplate.java ← update() 추가
gamedata/application/domain/model/item/MissionItemTemplate.java     ← update() 추가
gamedata/application/service/required/ItemTemplateRepository.java    ← 검색 메서드 추가
gamedata/application/service/required/PlayerCharacterRepository.java ← 닉네임 검색 추가
gameplay/application/service/GameWorldService.java             ← getPlayerById() 추가
web/IndexController.java                                       ← 2개 라우트 추가
resources/templates/web/admin.html                            ← 2개 카드 추가
```

---

## Task 1: UseCase 포트 인터페이스 5개

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/ItemTemplateCreator.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/ItemTemplateFinder.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/ItemTemplateModifier.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/ItemTemplateRemover.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/ItemInstancePlacer.java`

- [ ] **Step 1: 포트 인터페이스 5개 생성**

`ItemTemplateCreator.java`:
```java
package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;

public interface ItemTemplateCreator {
    ItemTemplate create(ItemTemplateRequest request);
}
```

`ItemTemplateFinder.java`:
```java
package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemType;

import java.util.List;

public interface ItemTemplateFinder {
    List<ItemTemplate> findAll();
    List<ItemTemplate> findByType(ItemType type);
    List<ItemTemplate> findByNameContaining(String name);
    ItemTemplate findById(Long id);
}
```

`ItemTemplateModifier.java`:
```java
package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;

public interface ItemTemplateModifier {
    ItemTemplate update(Long id, ItemTemplateRequest request);
}
```

`ItemTemplateRemover.java`:
```java
package com.jefflife.mudmk2.gamedata.application.service.provided;

public interface ItemTemplateRemover {
    void delete(Long id);
}
```

`ItemInstancePlacer.java`:
```java
package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemInstancePlaceRequest;

public interface ItemInstancePlacer {
    ItemInstance place(ItemInstancePlaceRequest request);
}
```

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew compileJava
```
Expected: BUILD SUCCESSFUL (인터페이스들이 아직 구현체 없으니 경고 없이 컴파일 성공)

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/ItemTemplateCreator.java
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/ItemTemplateFinder.java
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/ItemTemplateModifier.java
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/ItemTemplateRemover.java
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/provided/ItemInstancePlacer.java
git commit -m "feat: 아이템 Admin UseCase 포트 인터페이스 5개 추가"
```

---

## Task 2: DTO 레코드 (Request + Response)

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/model/request/StatModifierRequest.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/model/request/ItemTemplateRequest.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/model/request/ItemInstancePlaceRequest.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/response/StatModifierResponse.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/response/ItemTemplateResponse.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/response/PlayerCharacterSearchResponse.java`

- [ ] **Step 1: Request DTO 3개 생성**

`StatModifierRequest.java`:
```java
package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;

public record StatModifierRequest(StatType statType, int value) {
    public StatModifier toDomain() {
        return new StatModifier(statType, value);
    }
}
```

`ItemTemplateRequest.java`:
```java
package com.jefflife.mudmk2.gamedata.application.service.model.request;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;

import java.util.List;

public record ItemTemplateRequest(
    ItemType itemType,
    String name,
    String description,
    int weight,
    boolean stackable,
    // FOOD
    Integer hpRecovery,
    Integer mpRecovery,
    Integer apRecovery,
    // WEAPON
    WeaponType weaponType,
    // EQUIPMENT
    EquipmentSlot equipmentSlot,
    // ACCESSORY
    AccessoryType accessoryType,
    // WEAPON / EQUIPMENT / ACCESSORY
    List<StatModifierRequest> statModifiers,
    // MARTIAL_ARTS_BOOK
    String skillRef,
    // MISSION
    MissionItemType missionItemType,
    String targetRef
) {}
```

`ItemInstancePlaceRequest.java`:
```java
package com.jefflife.mudmk2.gamedata.application.service.model.request;

public record ItemInstancePlaceRequest(
    Long templateId,
    int quantity,
    LocationType locationType,
    String locationId
) {
    public enum LocationType { ROOM, CHARACTER }
}
```

- [ ] **Step 2: Response DTO 3개 생성**

`StatModifierResponse.java`:
```java
package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatModifier;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.StatType;

public record StatModifierResponse(StatType statType, int value) {
    public static StatModifierResponse from(StatModifier modifier) {
        return new StatModifierResponse(modifier.getStatType(), modifier.getValue());
    }
}
```

`ItemTemplateResponse.java`:
```java
package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;

import java.util.List;

public record ItemTemplateResponse(
    Long id,
    ItemType itemType,
    String name,
    String description,
    int weight,
    boolean stackable,
    Integer hpRecovery,
    Integer mpRecovery,
    Integer apRecovery,
    WeaponType weaponType,
    EquipmentSlot equipmentSlot,
    AccessoryType accessoryType,
    List<StatModifierResponse> statModifiers,
    String skillRef,
    MissionItemType missionItemType,
    String targetRef
) {
    public static ItemTemplateResponse from(ItemTemplate template) {
        if (template instanceof WeaponTemplate w) {
            return new ItemTemplateResponse(
                w.getId(), ItemType.WEAPON, w.getName(), w.getDescription(), w.getWeight(), w.isStackable(),
                null, null, null, w.getWeaponType(), null, null,
                w.getStatModifiers().stream().map(StatModifierResponse::from).toList(),
                null, null, null
            );
        } else if (template instanceof EquipmentTemplate e) {
            return new ItemTemplateResponse(
                e.getId(), ItemType.EQUIPMENT, e.getName(), e.getDescription(), e.getWeight(), e.isStackable(),
                null, null, null, null, e.getEquipmentSlot(), null,
                e.getStatModifiers().stream().map(StatModifierResponse::from).toList(),
                null, null, null
            );
        } else if (template instanceof AccessoryTemplate a) {
            return new ItemTemplateResponse(
                a.getId(), ItemType.ACCESSORY, a.getName(), a.getDescription(), a.getWeight(), a.isStackable(),
                null, null, null, null, null, a.getAccessoryType(),
                a.getStatModifiers().stream().map(StatModifierResponse::from).toList(),
                null, null, null
            );
        } else if (template instanceof FoodTemplate f) {
            return new ItemTemplateResponse(
                f.getId(), ItemType.FOOD, f.getName(), f.getDescription(), f.getWeight(), f.isStackable(),
                f.getHpRecovery(), f.getMpRecovery(), f.getApRecovery(),
                null, null, null, List.of(), null, null, null
            );
        } else if (template instanceof MartialArtsBookTemplate m) {
            return new ItemTemplateResponse(
                m.getId(), ItemType.MARTIAL_ARTS_BOOK, m.getName(), m.getDescription(), m.getWeight(), m.isStackable(),
                null, null, null, null, null, null, List.of(), m.getSkillRef(), null, null
            );
        } else if (template instanceof MissionItemTemplate mi) {
            return new ItemTemplateResponse(
                mi.getId(), ItemType.MISSION, mi.getName(), mi.getDescription(), mi.getWeight(), mi.isStackable(),
                null, null, null, null, null, null, List.of(), null, mi.getMissionItemType(), mi.getTargetRef()
            );
        }
        throw new IllegalArgumentException("Unknown ItemTemplate type: " + template.getClass());
    }
}
```

`PlayerCharacterSearchResponse.java`:
```java
package com.jefflife.mudmk2.gamedata.adapter.webapi.response;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;

import java.util.UUID;

public record PlayerCharacterSearchResponse(UUID id, String nickname) {
    public static PlayerCharacterSearchResponse from(PlayerCharacter character) {
        return new PlayerCharacterSearchResponse(character.getId(), character.getNickname());
    }
}
```

- [ ] **Step 3: 컴파일 확인**

```bash
./gradlew compileJava
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/model/request/StatModifierRequest.java
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/model/request/ItemTemplateRequest.java
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/model/request/ItemInstancePlaceRequest.java
git add src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/response/StatModifierResponse.java
git add src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/response/ItemTemplateResponse.java
git add src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/response/PlayerCharacterSearchResponse.java
git commit -m "feat: 아이템 Admin DTO 레코드 추가 (request/response)"
```

---

## Task 3: ItemTemplate 도메인 update 메서드 추가

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/ItemTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/FoodTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/WeaponTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/EquipmentTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/AccessoryTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/MartialArtsBookTemplate.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/MissionItemTemplate.java`

- [ ] **Step 1: ItemTemplate에 updateCommon() 추가**

`ItemTemplate.java`에 다음 메서드를 추가한다 (클래스 마지막에):
```java
protected void updateCommon(String name, String description, int weight, boolean stackable) {
    this.name = name;
    this.description = description;
    this.weight = weight;
    this.stackable = stackable;
}
```

- [ ] **Step 2: FoodTemplate에 update() 추가**

`FoodTemplate.java`에 다음 메서드를 추가한다:
```java
public void update(com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest request) {
    updateCommon(request.name(), request.description(), request.weight(), request.stackable());
    this.hpRecovery = request.hpRecovery() != null ? request.hpRecovery() : 0;
    this.mpRecovery = request.mpRecovery() != null ? request.mpRecovery() : 0;
    this.apRecovery = request.apRecovery() != null ? request.apRecovery() : 0;
}
```

- [ ] **Step 3: WeaponTemplate에 update() 추가**

`WeaponTemplate.java`에 다음 메서드를 추가한다:
```java
public void update(com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest request) {
    updateCommon(request.name(), request.description(), request.weight(), request.stackable());
    this.weaponType = request.weaponType();
    this.statModifiers.clear();
    if (request.statModifiers() != null) {
        request.statModifiers().forEach(sm -> this.statModifiers.add(sm.toDomain()));
    }
}
```

- [ ] **Step 4: EquipmentTemplate에 update() 추가**

`EquipmentTemplate.java`에 다음 메서드를 추가한다:
```java
public void update(com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest request) {
    updateCommon(request.name(), request.description(), request.weight(), request.stackable());
    this.equipmentSlot = request.equipmentSlot();
    this.statModifiers.clear();
    if (request.statModifiers() != null) {
        request.statModifiers().forEach(sm -> this.statModifiers.add(sm.toDomain()));
    }
}
```

- [ ] **Step 5: AccessoryTemplate에 update() 추가**

`AccessoryTemplate.java`에 다음 메서드를 추가한다:
```java
public void update(com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest request) {
    updateCommon(request.name(), request.description(), request.weight(), request.stackable());
    this.accessoryType = request.accessoryType();
    this.statModifiers.clear();
    if (request.statModifiers() != null) {
        request.statModifiers().forEach(sm -> this.statModifiers.add(sm.toDomain()));
    }
}
```

- [ ] **Step 6: MartialArtsBookTemplate에 update() 추가**

`MartialArtsBookTemplate.java`에 다음 메서드를 추가한다:
```java
public void update(com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest request) {
    updateCommon(request.name(), request.description(), request.weight(), request.stackable());
    this.skillRef = request.skillRef();
}
```

- [ ] **Step 7: MissionItemTemplate에 update() 추가**

`MissionItemTemplate.java`에 다음 메서드를 추가한다:
```java
public void update(com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest request) {
    updateCommon(request.name(), request.description(), request.weight(), request.stackable());
    this.missionItemType = request.missionItemType();
    this.targetRef = request.targetRef();
}
```

- [ ] **Step 8: 컴파일 확인**

```bash
./gradlew compileJava
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/domain/model/item/
git commit -m "feat: ItemTemplate 서브클래스에 update() 메서드 추가"
```

---

## Task 4: Repository 확장 + GameWorldService.getPlayerById() 추가

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/ItemTemplateRepository.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/PlayerCharacterRepository.java`
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java`

- [ ] **Step 1: ItemTemplateRepository에 검색 메서드 추가**

`ItemTemplateRepository.java`를 다음과 같이 수정한다:
```java
package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemType;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemTemplateRepository extends CrudRepository<ItemTemplate, Long> {
    List<ItemTemplate> findByName(String name);
    List<ItemTemplate> findByNameContaining(String name);
    List<ItemTemplate> findByItemType(ItemType itemType);
}
```

- [ ] **Step 2: PlayerCharacterRepository에 닉네임 검색 추가**

`PlayerCharacterRepository.java`에 다음을 추가한다:
```java
List<PlayerCharacter> findByNicknameContaining(String nickname);
```

최종 파일:
```java
package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerCharacterRepository extends CrudRepository<PlayerCharacter, UUID> {
    PlayerCharacter findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    boolean existsByNickname(String nickname);
    List<PlayerCharacter> findByNicknameContaining(String nickname);
}
```

- [ ] **Step 3: GameWorldService에 getPlayerById() 추가**

`GameWorldService.java`에 다음 메서드를 추가한다 (기존 메서드들 아래에):
```java
public Optional<PlayerCharacter> getPlayerById(UUID characterId) {
    return Optional.ofNullable(activePlayers.get(characterId));
}
```

- [ ] **Step 4: 컴파일 및 기존 테스트 통과 확인**

```bash
./gradlew test
```
Expected: 모든 기존 테스트 통과 (BUILD SUCCESSFUL)

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/ItemTemplateRepository.java
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/required/PlayerCharacterRepository.java
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java
git commit -m "feat: ItemTemplate/PlayerCharacter 검색 메서드 추가, GameWorldService.getPlayerById() 추가"
```

---

## Task 5: ItemTemplateService (단위 테스트 포함)

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/application/service/ItemTemplateService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/application/service/ItemTemplateServiceTest.java`

- [ ] **Step 1: 실패하는 단위 테스트 작성**

`ItemTemplateServiceTest.java`:
```java
package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.StatModifierRequest;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemTemplateServiceTest {

    @Mock
    private ItemTemplateRepository itemTemplateRepository;

    private ItemTemplateService service;

    @BeforeEach
    void setUp() {
        service = new ItemTemplateService(itemTemplateRepository);
    }

    @Test
    void create_food_shouldSaveAndReturnFoodTemplate() {
        ItemTemplateRequest request = new ItemTemplateRequest(
            ItemType.FOOD, "만두", "찐만두", 1, true,
            10, 0, 5,
            null, null, null, null, null, null, null
        );
        FoodTemplate saved = FoodTemplate.builder()
            .name("만두").description("찐만두").weight(1).stackable(true)
            .hpRecovery(10).mpRecovery(0).apRecovery(5).build();
        when(itemTemplateRepository.save(any())).thenReturn(saved);

        ItemTemplate result = service.create(request);

        assertThat(result).isInstanceOf(FoodTemplate.class);
        FoodTemplate food = (FoodTemplate) result;
        assertThat(food.getName()).isEqualTo("만두");
        assertThat(food.getHpRecovery()).isEqualTo(10);
        assertThat(food.getApRecovery()).isEqualTo(5);
        verify(itemTemplateRepository).save(any(FoodTemplate.class));
    }

    @Test
    void create_weapon_shouldSaveAndReturnWeaponTemplate() {
        ItemTemplateRequest request = new ItemTemplateRequest(
            ItemType.WEAPON, "철검", "날카로운 검", 5, false,
            null, null, null,
            WeaponType.SWORD, null, null,
            List.of(new StatModifierRequest(StatType.SWORD_METHOD, 5)),
            null, null, null
        );
        WeaponTemplate saved = WeaponTemplate.builder()
            .name("철검").description("날카로운 검").weight(5).stackable(false)
            .weaponType(WeaponType.SWORD)
            .statModifiers(List.of(new StatModifier(StatType.SWORD_METHOD, 5)))
            .build();
        when(itemTemplateRepository.save(any())).thenReturn(saved);

        ItemTemplate result = service.create(request);

        assertThat(result).isInstanceOf(WeaponTemplate.class);
        verify(itemTemplateRepository).save(any(WeaponTemplate.class));
    }

    @Test
    void findById_whenNotFound_shouldThrowNoSuchElementException() {
        when(itemTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void findByNameContaining_shouldDelegateToRepository() {
        when(itemTemplateRepository.findByNameContaining("검")).thenReturn(List.of());

        List<ItemTemplate> result = service.findByNameContaining("검");

        assertThat(result).isEmpty();
        verify(itemTemplateRepository).findByNameContaining("검");
    }

    @Test
    void delete_whenNotFound_shouldThrowNoSuchElementException() {
        when(itemTemplateRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(999L))
            .isInstanceOf(NoSuchElementException.class);
    }
}
```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.service.ItemTemplateServiceTest"
```
Expected: FAIL (ItemTemplateService가 없음)

- [ ] **Step 3: ItemTemplateService 구현**

`ItemTemplateService.java`:
```java
package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ItemTemplateService implements
    ItemTemplateCreator, ItemTemplateFinder, ItemTemplateModifier, ItemTemplateRemover {

    private final ItemTemplateRepository itemTemplateRepository;

    public ItemTemplateService(ItemTemplateRepository itemTemplateRepository) {
        this.itemTemplateRepository = itemTemplateRepository;
    }

    @Override
    public ItemTemplate create(ItemTemplateRequest request) {
        return itemTemplateRepository.save(buildTemplate(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemTemplate> findAll() {
        List<ItemTemplate> result = new ArrayList<>();
        itemTemplateRepository.findAll().forEach(result::add);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemTemplate> findByType(ItemType type) {
        return itemTemplateRepository.findByItemType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemTemplate> findByNameContaining(String name) {
        return itemTemplateRepository.findByNameContaining(name);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemTemplate findById(Long id) {
        return itemTemplateRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("ItemTemplate not found: " + id));
    }

    @Override
    public ItemTemplate update(Long id, ItemTemplateRequest request) {
        ItemTemplate template = itemTemplateRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("ItemTemplate not found: " + id));
        if (template instanceof FoodTemplate f) f.update(request);
        else if (template instanceof WeaponTemplate w) w.update(request);
        else if (template instanceof EquipmentTemplate e) e.update(request);
        else if (template instanceof AccessoryTemplate a) a.update(request);
        else if (template instanceof MartialArtsBookTemplate m) m.update(request);
        else if (template instanceof MissionItemTemplate mi) mi.update(request);
        return template;
    }

    @Override
    public void delete(Long id) {
        if (!itemTemplateRepository.existsById(id)) {
            throw new NoSuchElementException("ItemTemplate not found: " + id);
        }
        itemTemplateRepository.deleteById(id);
    }

    private ItemTemplate buildTemplate(ItemTemplateRequest request) {
        List<StatModifier> statModifiers = request.statModifiers() == null ? List.of() :
            request.statModifiers().stream().map(sm -> sm.toDomain()).toList();

        return switch (request.itemType()) {
            case FOOD -> FoodTemplate.builder()
                .name(request.name()).description(request.description())
                .weight(request.weight()).stackable(request.stackable())
                .hpRecovery(request.hpRecovery() != null ? request.hpRecovery() : 0)
                .mpRecovery(request.mpRecovery() != null ? request.mpRecovery() : 0)
                .apRecovery(request.apRecovery() != null ? request.apRecovery() : 0)
                .build();
            case WEAPON -> WeaponTemplate.builder()
                .name(request.name()).description(request.description())
                .weight(request.weight()).stackable(request.stackable())
                .weaponType(request.weaponType())
                .statModifiers(statModifiers)
                .build();
            case EQUIPMENT -> EquipmentTemplate.builder()
                .name(request.name()).description(request.description())
                .weight(request.weight()).stackable(request.stackable())
                .equipmentSlot(request.equipmentSlot())
                .statModifiers(statModifiers)
                .build();
            case ACCESSORY -> AccessoryTemplate.builder()
                .name(request.name()).description(request.description())
                .weight(request.weight()).stackable(request.stackable())
                .accessoryType(request.accessoryType())
                .statModifiers(statModifiers)
                .build();
            case MARTIAL_ARTS_BOOK -> MartialArtsBookTemplate.builder()
                .name(request.name()).description(request.description())
                .weight(request.weight()).stackable(request.stackable())
                .skillRef(request.skillRef())
                .build();
            case MISSION -> MissionItemTemplate.builder()
                .name(request.name()).description(request.description())
                .weight(request.weight()).stackable(request.stackable())
                .missionItemType(request.missionItemType())
                .targetRef(request.targetRef())
                .build();
        };
    }
}
```

- [ ] **Step 4: 단위 테스트 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.application.service.ItemTemplateServiceTest"
```
Expected: 5 tests, 0 failures

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/application/service/ItemTemplateService.java
git add src/test/java/com/jefflife/mudmk2/gamedata/application/service/ItemTemplateServiceTest.java
git commit -m "feat: ItemTemplateService 구현 (CRUD + 검색, 단위 테스트 포함)"
```

---

## Task 6: ItemInstanceService (gameplay 패키지, 단위 테스트 포함)

**Background:** `ItemInstanceService`는 `GameWorldService`를 의존하므로, gamedata→gameplay 순환 의존성을 피하기 위해 `gameplay.application.service` 패키지에 위치한다. 인터페이스(`ItemInstancePlacer`)는 gamedata에 있고, 구현체만 gameplay에 있다.

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceService.java`
- Test: `src/test/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceServiceTest.java`

- [ ] **Step 1: 실패하는 단위 테스트 작성**

`ItemInstanceServiceTest.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemInstancePlaceRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemInstancePlaceRequest.LocationType;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemInstanceRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemTemplateRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemInstanceServiceTest {

    @Mock private ItemTemplateRepository itemTemplateRepository;
    @Mock private ItemInstanceRepository itemInstanceRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private PlayerCharacterRepository playerCharacterRepository;
    @Mock private GameWorldService gameWorldService;

    private ItemInstanceService service;

    @BeforeEach
    void setUp() {
        service = new ItemInstanceService(
            itemTemplateRepository, itemInstanceRepository,
            roomRepository, playerCharacterRepository, gameWorldService
        );
    }

    @Test
    void place_toRoom_shouldSaveInstanceAndAddToRoomAndInMemory() {
        FoodTemplate template = FoodTemplate.builder()
            .name("만두").description("찐만두").weight(1).stackable(true)
            .hpRecovery(10).mpRecovery(0).apRecovery(0).build();
        Room room = Room.builder().id(1L).areaId(1L).name("동방 입구")
            .summary("입구").description("입구입니다").build();

        when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(itemInstanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(gameWorldService.getRoom(1L)).thenReturn(Optional.of(room));

        ItemInstancePlaceRequest request = new ItemInstancePlaceRequest(1L, 10, LocationType.ROOM, "1");
        ItemInstance result = service.place(request);

        assertThat(result.getQuantity()).isEqualTo(10);
        assertThat(room.getFloorItems()).contains(result);
        verify(roomRepository).save(room);
    }

    @Test
    void place_toRoom_whenRoomNotFound_shouldThrow() {
        FoodTemplate template = FoodTemplate.builder()
            .name("만두").description("찐만두").weight(1).stackable(true)
            .hpRecovery(10).mpRecovery(0).apRecovery(0).build();
        when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        ItemInstancePlaceRequest request = new ItemInstancePlaceRequest(1L, 1, LocationType.ROOM, "999");
        assertThatThrownBy(() -> service.place(request))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void place_toCharacter_whenCharacterNotFound_shouldThrow() {
        FoodTemplate template = FoodTemplate.builder()
            .name("만두").description("찐만두").weight(1).stackable(true)
            .hpRecovery(10).mpRecovery(0).apRecovery(0).build();
        UUID characterId = UUID.randomUUID();
        when(itemTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(playerCharacterRepository.findById(characterId)).thenReturn(Optional.empty());

        ItemInstancePlaceRequest request = new ItemInstancePlaceRequest(1L, 1, LocationType.CHARACTER, characterId.toString());
        assertThatThrownBy(() -> service.place(request))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void place_whenTemplateNotFound_shouldThrow() {
        when(itemTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        ItemInstancePlaceRequest request = new ItemInstancePlaceRequest(999L, 1, LocationType.ROOM, "1");
        assertThatThrownBy(() -> service.place(request))
            .isInstanceOf(NoSuchElementException.class);
    }
}
```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.ItemInstanceServiceTest"
```
Expected: FAIL (ItemInstanceService가 없음)

- [ ] **Step 3: ItemInstanceService 구현**

`GameWorldService`에서 `getRoom(Long)` 메서드가 이미 있는지 확인한다. 없다면 추가한다:
```java
public Optional<Room> getRoom(Long roomId) {
    return Optional.ofNullable(rooms.get(roomId));
}
```

`ItemInstanceService.java`:
```java
package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemInstancePlaceRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemInstancePlaceRequest.LocationType;
import com.jefflife.mudmk2.gamedata.application.service.provided.ItemInstancePlacer;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemInstanceRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemTemplateRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class ItemInstanceService implements ItemInstancePlacer {

    private final ItemTemplateRepository itemTemplateRepository;
    private final ItemInstanceRepository itemInstanceRepository;
    private final RoomRepository roomRepository;
    private final PlayerCharacterRepository playerCharacterRepository;
    private final GameWorldService gameWorldService;

    public ItemInstanceService(
        ItemTemplateRepository itemTemplateRepository,
        ItemInstanceRepository itemInstanceRepository,
        RoomRepository roomRepository,
        PlayerCharacterRepository playerCharacterRepository,
        GameWorldService gameWorldService
    ) {
        this.itemTemplateRepository = itemTemplateRepository;
        this.itemInstanceRepository = itemInstanceRepository;
        this.roomRepository = roomRepository;
        this.playerCharacterRepository = playerCharacterRepository;
        this.gameWorldService = gameWorldService;
    }

    @Override
    public ItemInstance place(ItemInstancePlaceRequest request) {
        ItemTemplate template = itemTemplateRepository.findById(request.templateId())
            .orElseThrow(() -> new NoSuchElementException("ItemTemplate not found: " + request.templateId()));

        ItemInstance instance = itemInstanceRepository.save(new ItemInstance(template, request.quantity()));

        if (request.locationType() == LocationType.ROOM) {
            placeInRoom(instance, Long.parseLong(request.locationId()));
        } else {
            placeInCharacter(instance, UUID.fromString(request.locationId()));
        }

        return instance;
    }

    private void placeInRoom(ItemInstance instance, Long roomId) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new NoSuchElementException("Room not found: " + roomId));
        room.addFloorItem(instance);
        roomRepository.save(room);
        gameWorldService.getRoom(roomId).ifPresent(r -> r.addFloorItem(instance));
    }

    private void placeInCharacter(ItemInstance instance, UUID characterId) {
        PlayerCharacter character = playerCharacterRepository.findById(characterId)
            .orElseThrow(() -> new NoSuchElementException("PlayerCharacter not found: " + characterId));
        character.getInventory().addItem(instance);
        playerCharacterRepository.save(character);
        gameWorldService.getPlayerById(characterId).ifPresent(p -> p.getInventory().addItem(instance));
    }
}
```

- [ ] **Step 4: 단위 테스트 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.service.ItemInstanceServiceTest"
```
Expected: 4 tests, 0 failures

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceService.java
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java
git add src/test/java/com/jefflife/mudmk2/gameplay/application/service/ItemInstanceServiceTest.java
git commit -m "feat: ItemInstanceService 구현 (ROOM/CHARACTER 배치, 인메모리 즉시 반영)"
```

---

## Task 7: ItemTemplateController + 시스템 테스트

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/ItemTemplateController.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/ItemTemplateControllerSystemTest.java`

- [ ] **Step 1: 실패하는 시스템 테스트 작성**

`ItemTemplateControllerSystemTest.java`:
```java
package com.jefflife.mudmk2.gamedata.adapter.webapi;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.config.SecurityMockMvcConfiguration;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.StatModifierRequest;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.ItemTemplateResponse;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
class ItemTemplateControllerSystemTest {

    private static final String BASE_URL = "/api/v1/item-templates";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void createFood_shouldReturn201WithBody() throws Exception {
        ItemTemplateRequest request = new ItemTemplateRequest(
            ItemType.FOOD, "만두", "찐만두", 1, true,
            10, 0, 5, null, null, null, null, null, null, null
        );

        MvcResult result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        ItemTemplateResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(), ItemTemplateResponse.class);
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("만두");
        assertThat(response.itemType()).isEqualTo(ItemType.FOOD);
        assertThat(response.hpRecovery()).isEqualTo(10);
        assertThat(result.getResponse().getHeader("Location"))
            .isEqualTo(BASE_URL + "/" + response.id());
    }

    @Test
    void createWeapon_shouldReturn201WithStatModifiers() throws Exception {
        ItemTemplateRequest request = new ItemTemplateRequest(
            ItemType.WEAPON, "철검", "날카로운 검", 5, false,
            null, null, null,
            WeaponType.SWORD, null, null,
            List.of(new StatModifierRequest(StatType.SWORD_METHOD, 5)),
            null, null, null
        );

        MvcResult result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        ItemTemplateResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(), ItemTemplateResponse.class);
        assertThat(response.weaponType()).isEqualTo(WeaponType.SWORD);
        assertThat(response.statModifiers()).hasSize(1);
        assertThat(response.statModifiers().get(0).statType()).isEqualTo(StatType.SWORD_METHOD);
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/-999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        createFoodTemplate("국밥");
        createFoodTemplate("비빔밥");

        MvcResult result = mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andReturn();

        List<ItemTemplateResponse> list = objectMapper.readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(list.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void getAll_withTypeFilter_shouldReturnOnlyMatchingType() throws Exception {
        createFoodTemplate("국밥");
        createWeaponTemplate("도끼");

        MvcResult result = mockMvc.perform(get(BASE_URL + "?type=FOOD"))
            .andExpect(status().isOk())
            .andReturn();

        List<ItemTemplateResponse> list = objectMapper.readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(list).allMatch(r -> r.itemType() == ItemType.FOOD);
    }

    @Test
    void getAll_withNameFilter_shouldReturnMatchingNames() throws Exception {
        createFoodTemplate("만두");
        createFoodTemplate("만두피");
        createFoodTemplate("국밥");

        MvcResult result = mockMvc.perform(get(BASE_URL + "?name=만두"))
            .andExpect(status().isOk())
            .andReturn();

        List<ItemTemplateResponse> list = objectMapper.readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(list).allMatch(r -> r.name().contains("만두"));
    }

    @Test
    void update_shouldReturn200WithUpdatedData() throws Exception {
        ItemTemplateResponse created = createFoodTemplate("만두");
        ItemTemplateRequest updateRequest = new ItemTemplateRequest(
            ItemType.FOOD, "왕만두", "큰 만두", 2, true,
            20, 5, 10, null, null, null, null, null, null, null
        );

        MvcResult result = mockMvc.perform(put(BASE_URL + "/" + created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andReturn();

        ItemTemplateResponse updated = objectMapper.readValue(
            result.getResponse().getContentAsString(), ItemTemplateResponse.class);
        assertThat(updated.name()).isEqualTo("왕만두");
        assertThat(updated.hpRecovery()).isEqualTo(20);
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        ItemTemplateResponse created = createFoodTemplate("지울만두");

        mockMvc.perform(delete(BASE_URL + "/" + created.id()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + created.id()))
            .andExpect(status().isNotFound());
    }

    private ItemTemplateResponse createFoodTemplate(String name) throws Exception {
        ItemTemplateRequest request = new ItemTemplateRequest(
            ItemType.FOOD, name, name + " 설명", 1, true,
            10, 0, 0, null, null, null, null, null, null, null
        );
        MvcResult result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), ItemTemplateResponse.class);
    }

    private ItemTemplateResponse createWeaponTemplate(String name) throws Exception {
        ItemTemplateRequest request = new ItemTemplateRequest(
            ItemType.WEAPON, name, name + " 설명", 5, false,
            null, null, null, WeaponType.SWORD, null, null,
            List.of(), null, null, null
        );
        MvcResult result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), ItemTemplateResponse.class);
    }
}
```

- [ ] **Step 2: 테스트 실행 — 실패 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.adapter.webapi.ItemTemplateControllerSystemTest"
```
Expected: FAIL (controller 없음)

- [ ] **Step 3: ItemTemplateController 구현**

`ItemTemplateController.java`:
```java
package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemType;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.ItemTemplateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(ItemTemplateController.BASE_PATH)
public class ItemTemplateController {
    public static final String BASE_PATH = "/api/v1/item-templates";

    private final ItemTemplateCreator itemTemplateCreator;
    private final ItemTemplateFinder itemTemplateFinder;
    private final ItemTemplateModifier itemTemplateModifier;
    private final ItemTemplateRemover itemTemplateRemover;

    public ItemTemplateController(
        ItemTemplateCreator itemTemplateCreator,
        ItemTemplateFinder itemTemplateFinder,
        ItemTemplateModifier itemTemplateModifier,
        ItemTemplateRemover itemTemplateRemover
    ) {
        this.itemTemplateCreator = itemTemplateCreator;
        this.itemTemplateFinder = itemTemplateFinder;
        this.itemTemplateModifier = itemTemplateModifier;
        this.itemTemplateRemover = itemTemplateRemover;
    }

    @PostMapping
    public ResponseEntity<ItemTemplateResponse> create(@RequestBody ItemTemplateRequest request) {
        ItemTemplate template = itemTemplateCreator.create(request);
        ItemTemplateResponse response = ItemTemplateResponse.from(template);
        return ResponseEntity
            .created(URI.create(BASE_PATH + "/" + response.id()))
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ItemTemplateResponse>> getAll(
        @RequestParam(required = false) ItemType type,
        @RequestParam(required = false) String name
    ) {
        List<ItemTemplate> templates;
        if (name != null && !name.isBlank()) {
            templates = itemTemplateFinder.findByNameContaining(name);
        } else if (type != null) {
            templates = itemTemplateFinder.findByType(type);
        } else {
            templates = itemTemplateFinder.findAll();
        }
        return ResponseEntity.ok(templates.stream().map(ItemTemplateResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemTemplateResponse> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ItemTemplateResponse.from(itemTemplateFinder.findById(id)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemTemplateResponse> update(
        @PathVariable Long id,
        @RequestBody ItemTemplateRequest request
    ) {
        try {
            return ResponseEntity.ok(ItemTemplateResponse.from(itemTemplateModifier.update(id, request)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            itemTemplateRemover.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
```

- [ ] **Step 4: 시스템 테스트 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.adapter.webapi.ItemTemplateControllerSystemTest"
```
Expected: 7 tests, 0 failures

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/ItemTemplateController.java
git add src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/ItemTemplateControllerSystemTest.java
git commit -m "feat: ItemTemplateController REST API 구현 (CRUD + 검색 필터, 시스템 테스트 포함)"
```

---

## Task 8: ItemInstanceController + PlayerCharacterController + 시스템 테스트

**Files:**
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/ItemInstanceController.java`
- Create: `src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/PlayerCharacterController.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/ItemInstanceControllerSystemTest.java`
- Test: `src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/PlayerCharacterControllerSystemTest.java`

- [ ] **Step 1: PlayerCharacterController 시스템 테스트 작성**

`PlayerCharacterControllerSystemTest.java`:
```java
package com.jefflife.mudmk2.gamedata.adapter.webapi;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.jefflife.mudmk2.config.SecurityMockMvcConfiguration;
import com.jefflife.mudmk2.gamedata.adapter.webapi.response.PlayerCharacterSearchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
class PlayerCharacterControllerSystemTest {

    private static final String BASE_URL = "/api/v1/player-characters";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void search_withEmptyNickname_shouldReturn200WithList() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL + "/search?nickname="))
            .andExpect(status().isOk())
            .andReturn();

        List<PlayerCharacterSearchResponse> list = objectMapper.readValue(
            result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(list).isNotNull();
    }

    @Test
    void search_withNickname_shouldReturn200() throws Exception {
        mockMvc.perform(get(BASE_URL + "/search?nickname=홍길동"))
            .andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: PlayerCharacterController 구현**

`PlayerCharacterController.java`:
```java
package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.adapter.webapi.response.PlayerCharacterSearchResponse;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/player-characters")
public class PlayerCharacterController {

    private final PlayerCharacterRepository playerCharacterRepository;

    public PlayerCharacterController(PlayerCharacterRepository playerCharacterRepository) {
        this.playerCharacterRepository = playerCharacterRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlayerCharacterSearchResponse>> search(
        @RequestParam(defaultValue = "") String nickname
    ) {
        List<PlayerCharacterSearchResponse> result = playerCharacterRepository
            .findByNicknameContaining(nickname)
            .stream()
            .map(PlayerCharacterSearchResponse::from)
            .toList();
        return ResponseEntity.ok(result);
    }
}
```

- [ ] **Step 3: ItemInstanceController 구현**

`ItemInstanceController.java`:
```java
package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemInstancePlaceRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.ItemInstancePlacer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/item-instances")
public class ItemInstanceController {

    private final ItemInstancePlacer itemInstancePlacer;

    public ItemInstanceController(ItemInstancePlacer itemInstancePlacer) {
        this.itemInstancePlacer = itemInstancePlacer;
    }

    @PostMapping("/place")
    public ResponseEntity<Map<String, Object>> place(@RequestBody ItemInstancePlaceRequest request) {
        try {
            ItemInstance instance = itemInstancePlacer.place(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "instanceId", instance.getId()
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

```bash
./gradlew test --tests "com.jefflife.mudmk2.gamedata.adapter.webapi.PlayerCharacterControllerSystemTest"
```
Expected: 2 tests, 0 failures

- [ ] **Step 5: 전체 테스트 통과 확인**

```bash
./gradlew test
```
Expected: BUILD SUCCESSFUL, 모든 테스트 통과

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/ItemInstanceController.java
git add src/main/java/com/jefflife/mudmk2/gamedata/adapter/webapi/PlayerCharacterController.java
git add src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/ItemInstanceControllerSystemTest.java
git add src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/PlayerCharacterControllerSystemTest.java
git commit -m "feat: ItemInstanceController, PlayerCharacterController 구현 (시스템 테스트 포함)"
```

---

## Task 9: 웹 라우팅 + admin.html 카드 추가

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/web/IndexController.java`
- Modify: `src/main/resources/templates/web/admin.html`

- [ ] **Step 1: IndexController에 두 라우트 추가**

`IndexController.java`에서 마지막 `@GetMapping` 메서드 뒤에 다음을 추가한다:
```java
@GetMapping("/item-template-management")
public String itemTemplateManagement(Model model, @LoginUser SessionUser user) {
    if (user != null) {
        model.addAttribute("userName", user.getName());
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("userPicture", user.getPicture());
    }
    return "web/item-template-management";
}

@GetMapping("/item-instance-management")
public String itemInstanceManagement(Model model, @LoginUser SessionUser user) {
    if (user != null) {
        model.addAttribute("userName", user.getName());
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("userPicture", user.getPicture());
    }
    return "web/item-instance-management";
}
```

- [ ] **Step 2: admin.html에 카드 2개 추가**

`admin.html`에서 기존 마지막 `admin-card` div 뒤에 다음 두 카드를 추가한다:
```html
<div class="admin-card">
    <div class="admin-card-icon">📦</div>
    <h4 class="admin-card-title">ITEM TEMPLATES</h4>
    <p class="admin-card-description">
        아이템 템플릿 CRUD. 6종 타입 (FOOD/WEAPON/EQUIPMENT/ACCESSORY/MARTIAL_ARTS_BOOK/MISSION).
    </p>
    <a href="/item-template-management" class="btn btn-admin">MANAGE TEMPLATES</a>
</div>
<div class="admin-card">
    <div class="admin-card-icon">🗺️</div>
    <h4 class="admin-card-title">ITEM INSTANCES</h4>
    <p class="admin-card-description">
        아이템 인스턴스 생성 및 배치. 방 바닥 또는 캐릭터 소지품에 즉시 반영.
    </p>
    <a href="/item-instance-management" class="btn btn-admin">PLACE ITEMS</a>
</div>
```

- [ ] **Step 3: 컴파일 확인**

```bash
./gradlew compileJava
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/web/IndexController.java
git add src/main/resources/templates/web/admin.html
git commit -m "feat: Admin 웹 라우팅 추가 (item-template-management, item-instance-management)"
```

---

## Task 10: item-template-management.html

**Files:**
- Create: `src/main/resources/templates/web/item-template-management.html`

- [ ] **Step 1: HTML 페이지 생성**

기존 admin 페이지 패턴(dark theme, Bootstrap 5, 바닐라 JS Fetch API)을 따른다.

`item-template-management.html`:
```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Item Template Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #0a0b10; color: #e4e4e7; font-family: 'IBM Plex Mono', monospace; }
        .page-header { background: #0d0e14; border-bottom: 1px solid #1e2030; padding: 12px 20px; }
        .table-dark { --bs-table-bg: #0d0e14; --bs-table-border-color: #1e2030; }
        .badge-FOOD { background: #1a3320; color: #4ade80; }
        .badge-WEAPON { background: #1e3a5f; color: #60a5fa; }
        .badge-EQUIPMENT { background: #3b2a1a; color: #f59e0b; }
        .badge-ACCESSORY { background: #2a1a3b; color: #c084fc; }
        .badge-MARTIAL_ARTS_BOOK { background: #1a2a3b; color: #38bdf8; }
        .badge-MISSION { background: #3b1a1a; color: #f87171; }
        .form-panel { background: #0d0e14; border: 1px solid #1e2030; border-radius: 6px; padding: 20px; }
        .type-section { display: none; background: #111320; border: 1px solid #1e3a5f; border-radius: 4px; padding: 14px; margin-top: 10px; }
        .type-section.active { display: block; }
        .stat-row { display: flex; gap: 8px; align-items: center; margin-bottom: 6px; }
        .btn-green { background: #4ade80; color: #000; border: none; font-weight: bold; }
        .btn-green:hover { background: #22c55e; color: #000; }
        input, select, textarea { background: #1a1c24 !important; color: #e4e4e7 !important; border-color: #333 !important; }
        input:focus, select:focus, textarea:focus { border-color: #4ade80 !important; box-shadow: none !important; }
    </style>
</head>
<body>
<div class="page-header d-flex justify-content-between align-items-center">
    <span style="color:#4ade80;font-weight:bold;">◆ ITEM TEMPLATE MANAGEMENT</span>
    <a href="/admin" style="color:#aaa;font-size:12px;">← Admin 메뉴</a>
</div>

<div class="container-fluid p-4">
    <!-- 목록 -->
    <div class="d-flex justify-content-between align-items-center mb-3">
        <button class="btn btn-green btn-sm" onclick="showCreateForm()">+ 새 템플릿</button>
        <div class="d-flex gap-2 align-items-center">
            <input type="text" id="nameFilter" class="form-control form-control-sm" style="width:200px" placeholder="이름 검색..." oninput="loadTemplates()">
            <select id="typeFilter" class="form-select form-select-sm" style="width:160px" onchange="loadTemplates()">
                <option value="">모든 타입</option>
                <option>FOOD</option><option>WEAPON</option><option>EQUIPMENT</option>
                <option>ACCESSORY</option><option>MARTIAL_ARTS_BOOK</option><option>MISSION</option>
            </select>
        </div>
    </div>

    <table class="table table-dark table-sm table-bordered mb-4">
        <thead><tr style="color:#4ade80">
            <th>ID</th><th>이름</th><th>타입</th><th>무게</th><th>스택</th><th>작업</th>
        </tr></thead>
        <tbody id="templateTableBody"></tbody>
    </table>

    <hr style="border-color:#1e2030">

    <!-- 생성/편집 폼 -->
    <div id="formPanel" class="form-panel" style="display:none">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <span style="color:#4ade80;font-weight:bold;" id="formTitle">◆ 새 아이템 템플릿</span>
            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="hideForm()">취소</button>
        </div>
        <input type="hidden" id="editId">

        <div class="row g-3 mb-3">
            <div class="col-md-3">
                <label class="form-label small text-secondary">아이템 타입 *</label>
                <select id="itemType" class="form-select form-select-sm" onchange="onTypeChange()">
                    <option value="">선택...</option>
                    <option>FOOD</option><option>WEAPON</option><option>EQUIPMENT</option>
                    <option>ACCESSORY</option><option>MARTIAL_ARTS_BOOK</option><option>MISSION</option>
                </select>
            </div>
            <div class="col-md-4">
                <label class="form-label small text-secondary">이름 *</label>
                <input type="text" id="name" class="form-control form-control-sm" placeholder="철검">
            </div>
            <div class="col-md-2">
                <label class="form-label small text-secondary">무게 *</label>
                <input type="number" id="weight" class="form-control form-control-sm" min="0" placeholder="5">
            </div>
            <div class="col-md-3 d-flex align-items-end">
                <div class="form-check mb-1">
                    <input type="checkbox" id="stackable" class="form-check-input">
                    <label for="stackable" class="form-check-label small">stackable</label>
                </div>
            </div>
        </div>

        <div class="mb-3">
            <label class="form-label small text-secondary">설명</label>
            <textarea id="description" class="form-control form-control-sm" rows="3" style="resize:vertical" placeholder="아이템 설명..."></textarea>
        </div>

        <!-- FOOD -->
        <div id="section-FOOD" class="type-section">
            <div style="color:#4ade80;font-size:12px;font-weight:bold;margin-bottom:8px">🍚 FOOD 설정</div>
            <div class="row g-2">
                <div class="col-4"><label class="form-label small text-secondary">HP 회복</label>
                    <input type="number" id="hpRecovery" class="form-control form-control-sm" value="0"></div>
                <div class="col-4"><label class="form-label small text-secondary">MP 회복</label>
                    <input type="number" id="mpRecovery" class="form-control form-control-sm" value="0"></div>
                <div class="col-4"><label class="form-label small text-secondary">AP 회복</label>
                    <input type="number" id="apRecovery" class="form-control form-control-sm" value="0"></div>
            </div>
        </div>

        <!-- WEAPON -->
        <div id="section-WEAPON" class="type-section">
            <div style="color:#60a5fa;font-size:12px;font-weight:bold;margin-bottom:8px">⚔ WEAPON 설정</div>
            <div class="mb-2">
                <label class="form-label small text-secondary">무기 타입</label>
                <select id="weaponType" class="form-select form-select-sm" style="width:220px">
                    <option>SWORD</option><option>BLADE</option><option>FIST</option>
                    <option>ARCHERY</option><option>ESOTERIC</option><option>LONG_WEAPON</option>
                </select>
            </div>
            <div id="weaponStatModifiers"></div>
            <button type="button" class="btn btn-sm btn-outline-success mt-1" onclick="addStatRow('weaponStatModifiers')">+ 스탯 추가</button>
        </div>

        <!-- EQUIPMENT -->
        <div id="section-EQUIPMENT" class="type-section">
            <div style="color:#f59e0b;font-size:12px;font-weight:bold;margin-bottom:8px">🛡 EQUIPMENT 설정</div>
            <div class="mb-2">
                <label class="form-label small text-secondary">장착 슬롯</label>
                <select id="equipmentSlot" class="form-select form-select-sm" style="width:220px">
                    <option>HELMET</option><option>UPPER_ARMOR</option><option>LOWER_ARMOR</option>
                    <option>GLOVES</option><option>BOOTS</option><option>BELT</option>
                </select>
            </div>
            <div id="equipmentStatModifiers"></div>
            <button type="button" class="btn btn-sm btn-outline-warning mt-1" onclick="addStatRow('equipmentStatModifiers')">+ 스탯 추가</button>
        </div>

        <!-- ACCESSORY -->
        <div id="section-ACCESSORY" class="type-section">
            <div style="color:#c084fc;font-size:12px;font-weight:bold;margin-bottom:8px">💍 ACCESSORY 설정</div>
            <div class="mb-2">
                <label class="form-label small text-secondary">악세서리 타입</label>
                <select id="accessoryType" class="form-select form-select-sm" style="width:220px">
                    <option>NECKLACE</option><option>RING</option>
                </select>
            </div>
            <div id="accessoryStatModifiers"></div>
            <button type="button" class="btn btn-sm btn-outline-secondary mt-1" onclick="addStatRow('accessoryStatModifiers')">+ 스탯 추가</button>
        </div>

        <!-- MARTIAL_ARTS_BOOK -->
        <div id="section-MARTIAL_ARTS_BOOK" class="type-section">
            <div style="color:#38bdf8;font-size:12px;font-weight:bold;margin-bottom:8px">📖 MARTIAL ARTS BOOK 설정</div>
            <label class="form-label small text-secondary">스킬 참조 (skillRef)</label>
            <input type="text" id="skillRef" class="form-control form-control-sm" placeholder="skill_id">
        </div>

        <!-- MISSION -->
        <div id="section-MISSION" class="type-section">
            <div style="color:#f87171;font-size:12px;font-weight:bold;margin-bottom:8px">🎯 MISSION 설정</div>
            <div class="mb-2">
                <label class="form-label small text-secondary">미션 아이템 타입</label>
                <select id="missionItemType" class="form-select form-select-sm" style="width:220px">
                    <option>KEY</option><option>QUEST_COMPLETION</option>
                </select>
            </div>
            <label class="form-label small text-secondary">대상 참조 (targetRef)</label>
            <input type="text" id="targetRef" class="form-control form-control-sm" placeholder="target_id">
        </div>

        <div class="mt-3 d-flex gap-2">
            <button type="button" class="btn btn-green btn-sm" onclick="saveTemplate()">저장</button>
        </div>
        <div id="formMessage" class="mt-2" style="font-size:12px"></div>
    </div>
</div>

<script>
const STAT_TYPES = ['VIGOR','PHYSIQUE','AGILITY','INTELLECT','WILL','MERIDIAN',
    'INNER_POWER','SPECIAL_TECHNIQUE','LIGHT_STEP','FISTS_AND_PALMS',
    'SWORD_METHOD','BLADE_METHOD','LONG_WEAPON','ESOTERIC_WEAPON','ARCHERY'];

const TYPE_COLORS = {
    FOOD:'#4ade80', WEAPON:'#60a5fa', EQUIPMENT:'#f59e0b',
    ACCESSORY:'#c084fc', MARTIAL_ARTS_BOOK:'#38bdf8', MISSION:'#f87171'
};

function onTypeChange() {
    const type = document.getElementById('itemType').value;
    document.querySelectorAll('.type-section').forEach(s => s.classList.remove('active'));
    if (type) document.getElementById('section-' + type)?.classList.add('active');
}

function addStatRow(containerId) {
    const container = document.getElementById(containerId);
    const row = document.createElement('div');
    row.className = 'stat-row';
    row.innerHTML = `
        <select class="form-select form-select-sm stat-type" style="width:180px">
            ${STAT_TYPES.map(t => `<option>${t}</option>`).join('')}
        </select>
        <input type="number" class="form-control form-control-sm stat-value" style="width:80px" value="0">
        <button type="button" class="btn btn-sm btn-outline-danger" onclick="this.parentElement.remove()">✕</button>`;
    container.appendChild(row);
}

function collectStatModifiers(containerId) {
    return Array.from(document.querySelectorAll(`#${containerId} .stat-row`)).map(row => ({
        statType: row.querySelector('.stat-type').value,
        value: parseInt(row.querySelector('.stat-value').value) || 0
    }));
}

async function loadTemplates() {
    const name = document.getElementById('nameFilter').value;
    const type = document.getElementById('typeFilter').value;
    let url = '/api/v1/item-templates';
    const params = new URLSearchParams();
    if (name) params.set('name', name);
    else if (type) params.set('type', type);
    if (params.toString()) url += '?' + params;

    const res = await fetch(url);
    const templates = await res.json();
    const tbody = document.getElementById('templateTableBody');
    tbody.innerHTML = templates.map(t => `
        <tr>
            <td>${t.id}</td>
            <td>${t.name}</td>
            <td><span class="badge badge-${t.itemType}">${t.itemType}</span></td>
            <td>${t.weight}</td>
            <td>${t.stackable ? '✓' : '✗'}</td>
            <td>
                <button class="btn btn-sm btn-outline-info py-0" onclick="editTemplate(${t.id})">편집</button>
                <button class="btn btn-sm btn-outline-danger py-0" onclick="deleteTemplate(${t.id}, '${t.name}')">삭제</button>
            </td>
        </tr>`).join('');
}

function showCreateForm() {
    document.getElementById('formTitle').textContent = '◆ 새 아이템 템플릿';
    document.getElementById('editId').value = '';
    document.getElementById('itemType').value = '';
    document.getElementById('name').value = '';
    document.getElementById('description').value = '';
    document.getElementById('weight').value = '';
    document.getElementById('stackable').checked = false;
    ['weaponStatModifiers','equipmentStatModifiers','accessoryStatModifiers'].forEach(id => {
        document.getElementById(id).innerHTML = '';
    });
    document.querySelectorAll('.type-section').forEach(s => s.classList.remove('active'));
    document.getElementById('formMessage').textContent = '';
    document.getElementById('formPanel').style.display = 'block';
    document.getElementById('formPanel').scrollIntoView({behavior:'smooth'});
}

async function editTemplate(id) {
    const res = await fetch('/api/v1/item-templates/' + id);
    const t = await res.json();
    document.getElementById('formTitle').textContent = '◆ 템플릿 편집 (ID: ' + id + ')';
    document.getElementById('editId').value = id;
    document.getElementById('itemType').value = t.itemType;
    document.getElementById('name').value = t.name;
    document.getElementById('description').value = t.description || '';
    document.getElementById('weight').value = t.weight;
    document.getElementById('stackable').checked = t.stackable;
    document.querySelectorAll('.type-section').forEach(s => s.classList.remove('active'));
    document.getElementById('section-' + t.itemType)?.classList.add('active');

    if (t.itemType === 'FOOD') {
        document.getElementById('hpRecovery').value = t.hpRecovery || 0;
        document.getElementById('mpRecovery').value = t.mpRecovery || 0;
        document.getElementById('apRecovery').value = t.apRecovery || 0;
    } else if (t.itemType === 'WEAPON') {
        document.getElementById('weaponType').value = t.weaponType;
        const c = document.getElementById('weaponStatModifiers');
        c.innerHTML = '';
        (t.statModifiers || []).forEach(sm => {
            addStatRow('weaponStatModifiers');
            const rows = c.querySelectorAll('.stat-row');
            const last = rows[rows.length - 1];
            last.querySelector('.stat-type').value = sm.statType;
            last.querySelector('.stat-value').value = sm.value;
        });
    } else if (t.itemType === 'EQUIPMENT') {
        document.getElementById('equipmentSlot').value = t.equipmentSlot;
        const c = document.getElementById('equipmentStatModifiers');
        c.innerHTML = '';
        (t.statModifiers || []).forEach(sm => {
            addStatRow('equipmentStatModifiers');
            const rows = c.querySelectorAll('.stat-row');
            const last = rows[rows.length - 1];
            last.querySelector('.stat-type').value = sm.statType;
            last.querySelector('.stat-value').value = sm.value;
        });
    } else if (t.itemType === 'ACCESSORY') {
        document.getElementById('accessoryType').value = t.accessoryType;
        const c = document.getElementById('accessoryStatModifiers');
        c.innerHTML = '';
        (t.statModifiers || []).forEach(sm => {
            addStatRow('accessoryStatModifiers');
            const rows = c.querySelectorAll('.stat-row');
            const last = rows[rows.length - 1];
            last.querySelector('.stat-type').value = sm.statType;
            last.querySelector('.stat-value').value = sm.value;
        });
    } else if (t.itemType === 'MARTIAL_ARTS_BOOK') {
        document.getElementById('skillRef').value = t.skillRef || '';
    } else if (t.itemType === 'MISSION') {
        document.getElementById('missionItemType').value = t.missionItemType;
        document.getElementById('targetRef').value = t.targetRef || '';
    }

    document.getElementById('formMessage').textContent = '';
    document.getElementById('formPanel').style.display = 'block';
    document.getElementById('formPanel').scrollIntoView({behavior:'smooth'});
}

function hideForm() {
    document.getElementById('formPanel').style.display = 'none';
}

function buildRequest() {
    const type = document.getElementById('itemType').value;
    const base = {
        itemType: type,
        name: document.getElementById('name').value,
        description: document.getElementById('description').value,
        weight: parseInt(document.getElementById('weight').value) || 0,
        stackable: document.getElementById('stackable').checked
    };
    if (type === 'FOOD') {
        return {...base,
            hpRecovery: parseInt(document.getElementById('hpRecovery').value) || 0,
            mpRecovery: parseInt(document.getElementById('mpRecovery').value) || 0,
            apRecovery: parseInt(document.getElementById('apRecovery').value) || 0
        };
    } else if (type === 'WEAPON') {
        return {...base,
            weaponType: document.getElementById('weaponType').value,
            statModifiers: collectStatModifiers('weaponStatModifiers')
        };
    } else if (type === 'EQUIPMENT') {
        return {...base,
            equipmentSlot: document.getElementById('equipmentSlot').value,
            statModifiers: collectStatModifiers('equipmentStatModifiers')
        };
    } else if (type === 'ACCESSORY') {
        return {...base,
            accessoryType: document.getElementById('accessoryType').value,
            statModifiers: collectStatModifiers('accessoryStatModifiers')
        };
    } else if (type === 'MARTIAL_ARTS_BOOK') {
        return {...base, skillRef: document.getElementById('skillRef').value};
    } else if (type === 'MISSION') {
        return {...base,
            missionItemType: document.getElementById('missionItemType').value,
            targetRef: document.getElementById('targetRef').value
        };
    }
    return base;
}

async function saveTemplate() {
    const id = document.getElementById('editId').value;
    const body = buildRequest();
    const url = id ? '/api/v1/item-templates/' + id : '/api/v1/item-templates';
    const method = id ? 'PUT' : 'POST';
    const res = await fetch(url, {
        method, headers: {'Content-Type':'application/json'}, body: JSON.stringify(body)
    });
    const msg = document.getElementById('formMessage');
    if (res.ok) {
        msg.style.color = '#4ade80';
        msg.textContent = '✓ 저장 완료';
        hideForm();
        loadTemplates();
    } else {
        msg.style.color = '#f87171';
        msg.textContent = '✗ 저장 실패 (status: ' + res.status + ')';
    }
}

async function deleteTemplate(id, name) {
    if (!confirm(`"${name}" 템플릿을 삭제하시겠습니까?`)) return;
    const res = await fetch('/api/v1/item-templates/' + id, {method:'DELETE'});
    if (res.ok) loadTemplates();
    else alert('삭제 실패');
}

loadTemplates();
</script>
</body>
</html>
```

- [ ] **Step 2: 컴파일 확인**

```bash
./gradlew compileJava
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add src/main/resources/templates/web/item-template-management.html
git commit -m "feat: 아이템 템플릿 관리 페이지 추가 (/item-template-management)"
```

---

## Task 11: item-instance-management.html

**Files:**
- Create: `src/main/resources/templates/web/item-instance-management.html`

- [ ] **Step 1: HTML 페이지 생성**

`item-instance-management.html`:
```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Item Instance Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #0a0b10; color: #e4e4e7; font-family: 'IBM Plex Mono', monospace; }
        .page-header { background: #0d0e14; border-bottom: 1px solid #1e2030; padding: 12px 20px; }
        .form-panel { background: #0d0e14; border: 1px solid #1e2030; border-radius: 6px; padding: 20px; max-width: 700px; }
        .step-label { color: #60a5fa; font-size: 11px; font-weight: bold; margin-bottom: 8px; }
        input, select { background: #1a1c24 !important; color: #e4e4e7 !important; border-color: #333 !important; }
        input:focus, select:focus { border-color: #4ade80 !important; box-shadow: none !important; }
        .search-results { background: #111; border: 1px solid #333; border-radius: 4px; max-height: 200px; overflow-y: auto; }
        .search-item { padding: 6px 12px; cursor: pointer; border-bottom: 1px solid #222; font-size: 12px; }
        .search-item:hover, .search-item.selected-item { background: #1a1c24; }
        .selected-preview { background: #0a1f0a; border: 1px solid #1a3320; border-radius: 4px; padding: 8px 12px; font-size: 12px; color: #4ade80; }
        .type-badge { padding: 2px 6px; border-radius: 3px; font-size: 10px; margin-right: 6px; }
        .badge-FOOD { background:#1a3320; color:#4ade80; }
        .badge-WEAPON { background:#1e3a5f; color:#60a5fa; }
        .badge-EQUIPMENT { background:#3b2a1a; color:#f59e0b; }
        .badge-ACCESSORY { background:#2a1a3b; color:#c084fc; }
        .badge-MARTIAL_ARTS_BOOK { background:#1a2a3b; color:#38bdf8; }
        .badge-MISSION { background:#3b1a1a; color:#f87171; }
        .btn-green { background: #4ade80; color: #000; border: none; font-weight: bold; }
        .btn-green:hover { background: #22c55e; color: #000; }
        label.form-check-label { color: #aaa; font-size: 13px; }
    </style>
</head>
<body>
<div class="page-header d-flex justify-content-between align-items-center">
    <span style="color:#4ade80;font-weight:bold;">◆ ITEM INSTANCE PLACEMENT</span>
    <a href="/admin" style="color:#aaa;font-size:12px;">← Admin 메뉴</a>
</div>

<div class="container-fluid p-4">
    <div class="form-panel">

        <!-- STEP 1: 템플릿 검색 -->
        <div class="mb-4">
            <div class="step-label">STEP 1 — 아이템 템플릿 검색</div>
            <input type="text" id="templateSearch" class="form-control form-control-sm"
                placeholder="🔍 이름으로 검색 (예: 철검, 만두...)"
                oninput="searchTemplates()" autocomplete="off">
            <div id="searchResults" class="search-results mt-1" style="display:none"></div>
            <div id="selectedPreview" class="selected-preview mt-2" style="display:none"></div>
        </div>

        <!-- STEP 2: 수량 -->
        <div class="mb-4">
            <div class="step-label">STEP 2 — 수량</div>
            <div class="d-flex align-items-center gap-3">
                <input type="number" id="quantity" class="form-control form-control-sm"
                    min="1" value="1" style="width:100px">
                <span id="quantityHint" class="text-secondary" style="font-size:11px"></span>
            </div>
        </div>

        <!-- STEP 3: 배치 위치 -->
        <div class="mb-4">
            <div class="step-label">STEP 3 — 배치 위치</div>
            <div class="d-flex gap-3 mb-3">
                <div class="form-check">
                    <input class="form-check-input" type="radio" name="locType" id="locRoom" value="ROOM"
                        checked onchange="onLocTypeChange()">
                    <label class="form-check-label" for="locRoom">방 (Room)</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input" type="radio" name="locType" id="locChar" value="CHARACTER"
                        onchange="onLocTypeChange()">
                    <label class="form-check-label" for="locChar">캐릭터 소지품</label>
                </div>
            </div>

            <!-- 방 선택 -->
            <div id="roomSection">
                <div style="background:#111320;border:1px solid #333;border-radius:4px;padding:12px">
                    <label class="form-label small text-secondary">방 ID 입력</label>
                    <div class="d-flex gap-2 align-items-center">
                        <input type="number" id="roomId" class="form-control form-control-sm"
                            placeholder="예: 42" style="width:120px">
                        <button type="button" class="btn btn-sm btn-outline-secondary" onclick="verifyRoom()">확인</button>
                        <span id="roomName" style="color:#4ade80;font-size:12px"></span>
                    </div>
                    <div class="text-secondary mt-1" style="font-size:11px">ID 입력 후 확인 버튼으로 방 이름 조회</div>
                </div>
            </div>

            <!-- 캐릭터 선택 -->
            <div id="charSection" style="display:none">
                <div style="background:#111320;border:1px solid #333;border-radius:4px;padding:12px">
                    <label class="form-label small text-secondary">캐릭터 닉네임 검색</label>
                    <input type="text" id="charSearch" class="form-control form-control-sm"
                        placeholder="닉네임 입력..." oninput="searchCharacters()" autocomplete="off">
                    <div id="charResults" class="search-results mt-1" style="display:none"></div>
                    <div id="selectedChar" class="mt-2" style="font-size:12px;color:#4ade80"></div>
                </div>
            </div>
        </div>

        <!-- 실행 -->
        <div class="d-flex align-items-center gap-3">
            <button type="button" class="btn btn-green" onclick="placeInstance()">배치 실행</button>
            <span class="text-secondary" style="font-size:11px">→ DB 저장 + 게임 인메모리 즉시 반영</span>
        </div>
        <div id="placeMessage" class="mt-3" style="font-size:12px"></div>
    </div>
</div>

<script>
let selectedTemplate = null;
let selectedCharacterId = null;
let debounceTimer = null;

function debounce(fn, delay) {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(fn, delay);
}

async function searchTemplates() {
    const query = document.getElementById('templateSearch').value.trim();
    const resultsEl = document.getElementById('searchResults');
    if (!query) { resultsEl.style.display = 'none'; return; }
    debounce(async () => {
        const res = await fetch('/api/v1/item-templates?name=' + encodeURIComponent(query));
        const templates = await res.json();
        if (templates.length === 0) { resultsEl.style.display = 'none'; return; }
        resultsEl.innerHTML = templates.slice(0, 10).map(t =>
            `<div class="search-item" onclick="selectTemplate(${JSON.stringify(t).replace(/"/g,'&quot;')})">
                <span class="type-badge badge-${t.itemType}">${t.itemType}</span>
                <strong>${t.name}</strong>
                <span style="color:#555;font-size:10px;margin-left:6px">무게 ${t.weight}${t.stackable ? ' · stackable' : ''}</span>
            </div>`
        ).join('');
        resultsEl.style.display = 'block';
    }, 200);
}

function selectTemplate(t) {
    selectedTemplate = t;
    document.getElementById('searchResults').style.display = 'none';
    document.getElementById('templateSearch').value = t.name;
    const preview = document.getElementById('selectedPreview');
    preview.style.display = 'block';
    let detail = '';
    if (t.itemType === 'FOOD') detail = `HP회복: ${t.hpRecovery||0} | MP회복: ${t.mpRecovery||0} | AP회복: ${t.apRecovery||0}`;
    else if (t.weaponType) detail = `무기타입: ${t.weaponType}`;
    else if (t.equipmentSlot) detail = `슬롯: ${t.equipmentSlot}`;
    preview.innerHTML = `✓ 선택됨: <strong>${t.name}</strong> (ID:${t.id}) · ${t.itemType}${t.stackable ? ' · stackable' : ''}${detail ? '<br><span style="color:#aaa">' + detail + '</span>' : ''}`;

    const quantityInput = document.getElementById('quantity');
    const hint = document.getElementById('quantityHint');
    if (!t.stackable) {
        quantityInput.value = 1;
        quantityInput.disabled = true;
        hint.textContent = 'non-stackable: 수량 1 고정';
    } else {
        quantityInput.disabled = false;
        hint.textContent = 'stackable: 수량 입력 가능';
    }
}

function onLocTypeChange() {
    const isRoom = document.querySelector('input[name="locType"]:checked').value === 'ROOM';
    document.getElementById('roomSection').style.display = isRoom ? 'block' : 'none';
    document.getElementById('charSection').style.display = isRoom ? 'none' : 'block';
}

async function verifyRoom() {
    const roomId = document.getElementById('roomId').value;
    if (!roomId) return;
    const res = await fetch('/api/v1/rooms/' + roomId);
    const nameEl = document.getElementById('roomName');
    if (res.ok) {
        const room = await res.json();
        nameEl.textContent = '→ "' + room.name + '"';
    } else {
        nameEl.style.color = '#f87171';
        nameEl.textContent = '→ 방을 찾을 수 없음';
    }
}

async function searchCharacters() {
    const query = document.getElementById('charSearch').value.trim();
    const resultsEl = document.getElementById('charResults');
    if (!query) { resultsEl.style.display = 'none'; return; }
    debounce(async () => {
        const res = await fetch('/api/v1/player-characters/search?nickname=' + encodeURIComponent(query));
        const chars = await res.json();
        if (chars.length === 0) { resultsEl.style.display = 'none'; return; }
        resultsEl.innerHTML = chars.slice(0, 10).map(c =>
            `<div class="search-item" onclick="selectCharacter('${c.id}', '${c.nickname}')">
                <strong>${c.nickname}</strong>
                <span style="color:#555;font-size:10px;margin-left:6px">${c.id}</span>
            </div>`
        ).join('');
        resultsEl.style.display = 'block';
    }, 200);
}

function selectCharacter(id, nickname) {
    selectedCharacterId = id;
    document.getElementById('charResults').style.display = 'none';
    document.getElementById('charSearch').value = nickname;
    document.getElementById('selectedChar').textContent = '✓ 선택됨: ' + nickname + ' (' + id + ')';
}

async function placeInstance() {
    const msg = document.getElementById('placeMessage');
    if (!selectedTemplate) { msg.style.color = '#f87171'; msg.textContent = '✗ 아이템 템플릿을 선택해주세요.'; return; }

    const locType = document.querySelector('input[name="locType"]:checked').value;
    let locationId;
    if (locType === 'ROOM') {
        locationId = document.getElementById('roomId').value;
        if (!locationId) { msg.style.color = '#f87171'; msg.textContent = '✗ 방 ID를 입력해주세요.'; return; }
    } else {
        if (!selectedCharacterId) { msg.style.color = '#f87171'; msg.textContent = '✗ 캐릭터를 선택해주세요.'; return; }
        locationId = selectedCharacterId;
    }

    const body = {
        templateId: selectedTemplate.id,
        quantity: parseInt(document.getElementById('quantity').value) || 1,
        locationType: locType,
        locationId: locationId
    };

    const res = await fetch('/api/v1/item-instances/place', {
        method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body)
    });
    const result = await res.json();
    if (result.success) {
        msg.style.color = '#4ade80';
        const target = locType === 'ROOM'
            ? '방 ID:' + locationId
            : '캐릭터:' + document.getElementById('charSearch').value;
        msg.textContent = `✓ ${selectedTemplate.name} ×${body.quantity} → ${target} 배치 완료 (Instance ID: ${result.instanceId})`;
    } else {
        msg.style.color = '#f87171';
        msg.textContent = '✗ 배치 실패: ' + (result.message || '알 수 없는 오류');
    }
}
</script>
</body>
</html>
```

- [ ] **Step 2: 앱 실행 후 UI 검증**

```bash
./gradlew bootRun
```

브라우저에서 다음을 확인한다:
1. `http://localhost:8080/admin` → Item Templates / Item Instances 카드 표시
2. `http://localhost:8080/item-template-management` → 템플릿 목록 + 폼 표시
3. `http://localhost:8080/item-instance-management` → 3단계 배치 폼 표시
4. 아이템 타입 선택 시 해당 섹션만 표시됨
5. 템플릿 이름 검색 live 필터 작동
6. 방 ID 입력 후 확인 클릭 → 방 이름 표시

- [ ] **Step 3: 전체 테스트 통과 확인**

```bash
./gradlew test
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋**

```bash
git add src/main/resources/templates/web/item-instance-management.html
git commit -m "feat: 아이템 인스턴스 배치 페이지 추가 (/item-instance-management)"
```
