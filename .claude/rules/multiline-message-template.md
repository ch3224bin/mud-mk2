---
paths:
  - "src/main/java/**/service/command/**/*.java"
  - "src/main/java/**/adapter/out/eventpublisher/**/*.java"
  - "src/main/resources/templates/**/*.html"
---

# 여러 줄 메시지는 Thymeleaf 템플릿으로 출력한다

사용자에게 보내는 메시지가 **두 줄 이상**이거나 **반복 구조(목록/표)**, **조건부 섹션**이 포함되면 절대 `StringBuilder` / `String.format` 으로 조합하지 않는다. 대신 다음 패턴을 따른다.

## 표준 패턴 (4 컴포넌트)

`ItemInfoMessageSender` / `EquipmentViewMessageSender` 사례를 정확히 따른다.

1. **Variables record** (`gameplay/application/service/model/template/XxxVariables.java`)
   - 출력에 필요한 모든 데이터를 record로 정의한다. 중첩 list/inner record로 표현하라.
   - 첫 필드는 항상 `Long userId`.

2. **Outgoing port 인터페이스** (`gameplay/application/service/required/SendXxxMessagePort.java`)
   - `void sendMessage(XxxVariables variables)` 단일 메서드.

3. **Adapter 구현** (`gameplay/adapter/out/eventpublisher/chat/XxxMessageSender.java`)
   - `@Component`, `TemplateEngine` + `ChatEventPublisher` 주입.
   - `Context` 에 variable 한 줄씩 set 후 `templateEngine.process("gameplay/xxx", context)` 호출 → `chatEventPublisher.messageToUser(userId, html)`.

4. **Thymeleaf 템플릿** (`src/main/resources/templates/gameplay/xxx.html`)
   - 기존 CSS 클래스(`font-terminal`, `msg-room-title`, `line`, `label`, `line-spacer`) 재사용.
   - 조건부 섹션은 `th:if`, 반복은 `th:each`.

## Service 측 책임

- 도메인 객체에서 Variables record 로 변환하는 매핑만 service 가 담당.
- 한글 라벨이 필요하면 `ItemDisplayLabels.of(...)` 활용 (영어 enum 이름을 사용자에게 노출 금지).
- Service 가 직접 `SendMessageToUserPort.messageToUser(userId, "...")` 로 multi-line String 을 만드는 패턴은 금지.

## 허용되는 String 직접 호출

다음 경우만 `SendMessageToUserPort.messageToUser(userId, "...")` 로 한 줄 String 을 보내도 된다.

- 결과 한 줄 통지: `"철검을 장착했다."`
- 단일 거부 메시지: `"철검을(를) 가지고 있지 않습니다."`
- 시스템 알림 한 줄: `"이미 캐릭터가 있습니다."`

여러 줄이 필요하면 즉시 새 Variables/Port/Sender/Template 셋을 만들어라. "잠깐만 두 줄이라서" 의 예외는 없다.

## 왜

- Thymeleaf 가 조사(을/를, 이/가) · 단위(`kg`) · 조건부 노출 등 표현 책임을 가져간다.
- Java 코드는 도메인 → Variables 매핑만 담당 → 테스트는 ArgumentCaptor 로 구조 검증.
- CSS 와 레이아웃이 한 곳(`templates/gameplay/`)에 모여 일관된 UI.
- 메시지 변경 시 Java 재컴파일 없이 템플릿만 수정 가능.
