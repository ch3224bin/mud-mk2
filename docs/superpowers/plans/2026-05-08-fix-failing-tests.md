# Fix Failing Tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 26개의 실패 테스트를 원인에 맞게 올바르게 수정한다.

**Architecture:**
- Task 1: Spring Boot 4.x에서 `@AutoConfigureMockMvc`가 `springSecurity()`를 자동 적용하지 않아 발생하는 컨트롤러 시스템 테스트 25개 실패를 `MockMvcBuilderCustomizer` 빈으로 수정한다.
- Task 2: `LookableTargetFinderTest.findDirection()`이 `GameWorldService` 인메모리 상태에 테스트 데이터 없이 실행되는 문제를 `@BeforeEach`/`@AfterEach`로 올바른 테스트 격리 상태를 만들어 수정한다.

**Tech Stack:** Spring Boot 4.0.3, Spring Security 7.x, JUnit 5, spring-security-test, H2 in-memory DB

---

## Task 1: SecurityMockMvcConfiguration 생성

**원인:** Spring Boot 4.x에서 `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`는 `spring-security-test`의 `SecurityMockMvcConfigurer`를 자동 적용하지 않는다. 따라서 `@WithMockUser`가 MockMvc 요청에 반영되지 않아 모든 요청이 `/oauth2/authorization/google`으로 302 리다이렉트된다.

**Files:**
- Create: `src/test/java/com/jefflife/mudmk2/config/SecurityMockMvcConfiguration.java`

- [ ] **Step 1: SecurityMockMvcConfiguration 클래스 생성**

```java
package com.jefflife.mudmk2.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@TestConfiguration
public class SecurityMockMvcConfiguration {

    @Bean
    MockMvcBuilderCustomizer securityMockMvcBuilderCustomizer() {
        return builder -> builder.apply(springSecurity());
    }
}
```

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew compileTestJava`
Expected: BUILD SUCCESSFUL (컴파일 오류 없음)

---

## Task 2: 컨트롤러 시스템 테스트에 SecurityMockMvcConfiguration 적용

**Files:**
- Modify: `src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/AreaControllerSystemTest.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/MonsterTypeControllerSystemTest.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/RoomControllerSystemTest.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/NonPlayerCharacterControllerSystemTest.java`
- Modify: `src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/InstanceScenarioControllerSystemTest.java`

- [ ] **Step 1: AreaControllerSystemTest에 @Import 추가**

현재:
```java
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
// ...

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
public class AreaControllerSystemTest {
```

변경 후:
```java
import com.jefflife.mudmk2.config.SecurityMockMvcConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
// ...

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
public class AreaControllerSystemTest {
```

- [ ] **Step 2: MonsterTypeControllerSystemTest에 @Import 추가**

현재:
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER")
public class MonsterTypeControllerSystemTest {
```

변경 후:
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
public class MonsterTypeControllerSystemTest {
```

- [ ] **Step 3: RoomControllerSystemTest에 @Import 추가**

현재:
```java
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
public class RoomControllerSystemTest {
```

변경 후:
```java
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
public class RoomControllerSystemTest {
```

- [ ] **Step 4: NonPlayerCharacterControllerSystemTest에 @Import 추가**

현재:
```java
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
public class NonPlayerCharacterControllerSystemTest {
```

변경 후:
```java
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
public class NonPlayerCharacterControllerSystemTest {
```

- [ ] **Step 5: InstanceScenarioControllerSystemTest에 @Import 추가**

현재:
```java
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
public class InstanceScenarioControllerSystemTest {
```

변경 후:
```java
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = "USER")
@Import(SecurityMockMvcConfiguration.class)
public class InstanceScenarioControllerSystemTest {
```

- [ ] **Step 6: 컨트롤러 시스템 테스트 실행**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gamedata.adapter.webapi.*"`
Expected: 전체 통과 (이전 25개 실패 모두 PASS로 전환)

- [ ] **Step 7: 커밋**

```bash
git add src/test/java/com/jefflife/mudmk2/config/SecurityMockMvcConfiguration.java \
        src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/AreaControllerSystemTest.java \
        src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/MonsterTypeControllerSystemTest.java \
        src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/RoomControllerSystemTest.java \
        src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/NonPlayerCharacterControllerSystemTest.java \
        src/test/java/com/jefflife/mudmk2/gamedata/adapter/webapi/InstanceScenarioControllerSystemTest.java
git commit -m "fix: Spring Boot 4.x MockMvc 보안 설정 수정 - SecurityMockMvcConfiguration 추가"
```

---

## Task 3: GameWorldService에 removePlayer/removeRoom 메서드 추가

**원인:** `LookableTargetFinderTest`는 `@BeforeEach`에서 인메모리 게임 상태에 테스트 데이터를 주입하고 `@AfterEach`에서 정리해야 한다. 현재 `GameWorldService`에는 플레이어/방 제거 메서드가 없다.

**Files:**
- Modify: `src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java`

- [ ] **Step 1: removePlayer(Long userId) 메서드 추가**

`GameWorldService.java`의 `addPlayer` 메서드 다음에 추가:

```java
public void removePlayer(Long userId) {
    PlayerCharacter player = activePlayersByUserId.remove(userId);
    if (player != null) {
        activePlayers.remove(player.getId());
        logger.debug("Player removed from game world: userId={}", userId);
    }
}
```

- [ ] **Step 2: removeRoom(Long roomId) 메서드 추가**

`getRoom` 메서드 다음에 추가:

```java
public void removeRoom(Long roomId) {
    Room removed = rooms.remove(roomId);
    if (removed != null) {
        logger.debug("Room removed from game world: roomId={}", roomId);
    }
}
```

- [ ] **Step 3: 컴파일 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

---

## Task 4: LookableTargetFinderTest 재작성

**원인:**
- 테스트가 `GameWorldService`의 인메모리 상태에 userId=1인 플레이어와 동쪽 출구가 있는 방이 필요한데, 테스트 격리 없이 실행된다.
- `@SpringBootTest`는 H2 메모리 DB를 비어있는 상태로 시작하고, `GameWorldService`도 초기 데이터 없이 시작된다.
- 고전파(디트로이트) 방식: `@MockBean` 사용 금지, 실제 객체로 상태 설정.

**Files:**
- Modify: `src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/look/LookableTargetFinderTest.java`

- [ ] **Step 1: LookableTargetFinderTest 재작성**

```java
package com.jefflife.mudmk2.gameplay.application.domain.model.look;

import com.jefflife.mudmk2.common.fixture.GameTestFixture;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import com.jefflife.mudmk2.gameplay.application.service.command.look.DirectionLookable;
import com.jefflife.mudmk2.gameplay.application.service.command.look.Lookable;
import com.jefflife.mudmk2.gameplay.application.service.command.look.LookableTargetFinder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
record LookableTargetFinderTest(LookableTargetFinder targetFinder, GameWorldService gameWorldService) {

    private static final Long TEST_USER_ID = 99999L;
    private static final Long TEST_ROOM_ID = 99001L;
    private static final Long TEST_NEXT_ROOM_ID = 99002L;

    @BeforeEach
    void setUp() {
        GameTestFixture.DirectionTestSetup setup = GameTestFixture.createDirectionTestSetup(
                TEST_USER_ID, TEST_ROOM_ID, TEST_NEXT_ROOM_ID
        );
        gameWorldService.loadRooms(List.of(setup.currentRoom(), setup.nextRoom()));
        gameWorldService.addPlayer(setup.player());
    }

    @AfterEach
    void tearDown() {
        gameWorldService.removePlayer(TEST_USER_ID);
        gameWorldService.removeRoom(TEST_ROOM_ID);
        gameWorldService.removeRoom(TEST_NEXT_ROOM_ID);
    }

    @Test
    void findNoTarget() {
        Optional<Lookable> result = targetFinder.findTargetInRoom(TEST_USER_ID, "없는 것");

        assertThat(result).isEmpty();
    }

    @Test
    void findDirection() {
        Optional<Lookable> result = targetFinder.findTargetInRoom(TEST_USER_ID, "동");

        assertThat(result).isNotEmpty();
        assertThat(result.get()).isInstanceOf(DirectionLookable.class);
    }
}
```

- [ ] **Step 2: LookableTargetFinderTest 단독 실행**

Run: `./gradlew test --tests "com.jefflife.mudmk2.gameplay.application.domain.model.look.LookableTargetFinderTest"`
Expected: 2 tests passed (findNoTarget, findDirection 모두 PASS)

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/jefflife/mudmk2/gameplay/application/service/GameWorldService.java \
        src/test/java/com/jefflife/mudmk2/gameplay/application/domain/model/look/LookableTargetFinderTest.java
git commit -m "fix: LookableTargetFinderTest - @BeforeEach/@AfterEach로 인메모리 상태 격리"
```

---

## Task 5: 전체 테스트 통과 확인

- [ ] **Step 1: 전체 테스트 실행**

Run: `./gradlew test`
Expected: 104 tests completed, 0 failed (또는 이전과 동일한 통과 수 + 26개 추가 통과)

- [ ] **Step 2: 실패가 있다면 확인 후 처리**

`build/reports/tests/test/index.html` 확인 또는 콘솔 출력 분석.
