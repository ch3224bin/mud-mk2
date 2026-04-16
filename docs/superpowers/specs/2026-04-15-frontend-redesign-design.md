# MUD MK2 프론트엔드 리디자인 설계 문서

**날짜:** 2026-04-15  
**대상 범위:** 플레이어용 화면 (login, chat, profile)  
**스택:** Spring Boot + Thymeleaf + Tailwind CSS

---

## 1. 배경 및 목표

현재 프론트엔드의 두 가지 문제:

1. **CSS 비일관성**: `chat.html`에 400줄 이상의 인라인 CSS가 있으며, `login.html` / `profile.html`은 Bootstrap 기본 스타일만 사용. 페이지 간 디자인 언어가 통일되지 않음.
2. **텍스트 게임 느낌 부족**: 채팅창 내 방 설명, 전투 로그, 시스템 메시지 등이 시각적으로 구분되지 않아 게임 몰입감이 낮음.

**목표**: 플레이어가 경험하는 모든 화면을 "모던 다크 터미널" 테마로 통일하고, 메시지 유형별 시각적 구분을 명확히 한다.

---

## 2. 미적 방향: 모던 다크 터미널

GitHub Dark 스타일의 다크 테마. CRT 복고풍이 아닌, 현대적인 코드 에디터/터미널 느낌.

**색상 팔레트 (Tailwind `extend.colors`로 정의 — `layout/base.html`의 `tailwind.config` 블록):**

| 역할 | 색상 | 용도 |
|------|------|------|
| `--bg-base` | `#0d1117` | 페이지 배경 |
| `--bg-surface` | `#161b22` | 카드/패널 배경 |
| `--bg-elevated` | `#21262d` | 타이틀바, 버튼 배경 |
| `--border` | `#30363d` | 테두리 |
| `--border-muted` | `#21262d` | 구분선 |
| `--text-primary` | `#e6edf3` | 제목, 중요 텍스트 |
| `--text-normal` | `#c9d1d9` | 일반 텍스트, 방 설명 |
| `--text-muted` | `#8b949e` | 라벨, 부가 정보 |
| `--text-dimmed` | `#6e7681` | 시스템 메시지, 타임스탬프 |
| `--color-blue` | `#79c0ff` | 방 이름, 로고 |
| `--color-green` | `#56d364` | 출구, 공격 성공, 버튼 |
| `--color-red` | `#ff7b72` | 피격, 위험 |
| `--color-yellow` | `#f8e45c` | 전투 이벤트 |
| `--color-purple` | `#d2a8ff` | NPC |
| `--color-orange` | `#ffa657` | 아이템 |
| `--color-sky` | `#a5d6ff` | 경험치, 레벨업 |
| `--color-blue-link` | `#58a6ff` | 플레이어 입력, 커서 |

**타이포그래피:**
- 게임 화면: `'Consolas', 'Menlo', 'Courier New', monospace`
- UI 레이블/버튼: system sans-serif

---

## 3. CSS 관리 전략: Tailwind CSS 도입

### 현재 문제
- `chat.html` 안에 인라인 `<style>` 약 400줄
- `login.html`, `profile.html`은 Bootstrap CDN만 사용
- gameplay 프래그먼트들이 `chat.html`의 CSS 클래스에 암묵적으로 의존

### 변경 방향
- **Bootstrap 제거**, **Tailwind CSS CDN** 도입
- 공통 색상/폰트 변수는 Tailwind 설정의 `extend.colors`로 정의
- 기존 인라인 `<style>` 블록 전부 제거
- Thymeleaf 레이아웃 프래그먼트로 공통 `<head>` 공유

### Thymeleaf 레이아웃 구조

```
templates/
  layout/
    base.html        ← 공통 <head>: Tailwind CDN, Google Fonts (monospace), 색상 변수
  web/
    login.html       ← th:replace="layout/base :: head" 사용
    chat.html        ← th:replace="layout/base :: head" 사용
    profile.html     ← th:replace="layout/base :: head" 사용
  gameplay/
    room-info.html
    status.html
    combat-action.html
    welcome.html
    game-time.html
    weather.html
```

---

## 4. 메시지 타입 시스템

채팅창 내 메시지를 유형별로 시각 구분. 서버에서 렌더링하는 Thymeleaf 프래그먼트 각각에 대응 CSS 클래스 적용.

| 메시지 타입 | CSS 클래스 | 색상 | 설명 |
|------------|-----------|------|------|
| 시스템/환경 | `msg-system` | `--text-dimmed` (회색, 이탤릭) | 게임 시간, 날씨 |
| 방 제목 | `msg-room-title` | `--color-blue` (굵게) | 방 이름 |
| 방 설명 | `msg-room-desc` | `--text-normal` | 방 묘사, 탐색 결과 |
| 출구 | `msg-exits` | `--color-green` (방향), `--text-muted` (라벨) | 이동 가능 출구 |
| 플레이어 입력 | `msg-player-input` | `--color-blue-link` + `$` 프롬프트 | 내가 입력한 명령 |
| 플레이어 발언 | `msg-player-say` | 이름 파랑, 내용 흰색 | 내 말 |
| NPC | `msg-npc` | `--color-purple` | NPC 대사/행동 |
| 전투 개시/종료 | `msg-combat-event` | `--color-yellow` (굵게) | ⚔ 접두사 |
| 공격 성공 (내가 줌) | `msg-dmg-dealt` | `--color-green` + ↑ 화살표 | 데미지 수치 강조 |
| 피격 (내가 받음) | `msg-dmg-taken` | `--color-red` + ↓ 화살표 | 데미지 수치 강조 |
| 아이템 | `msg-item` | `--color-orange` | 아이템 획득/사용 |
| 경험치/레벨업 | `msg-exp` | `--color-sky` (이탤릭) | ✦ 접두사 |

---

## 5. 페이지별 변경 내용

### 5-1. `layout/base.html` (신규)

공통 `<head>` 프래그먼트.

```html
<head th:fragment="head(title)">
  <meta charset="UTF-8">
  <title th:text="${title}">MUD MK2</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script>
    tailwind.config = {
      theme: {
        extend: {
          colors: {
            'bg-base': '#0d1117',
            'bg-surface': '#161b22',
            'bg-elevated': '#21262d',
            'border-default': '#30363d',
            'text-muted': '#8b949e',
            'text-dimmed': '#6e7681',
            'mud-blue': '#79c0ff',
            'mud-green': '#56d364',
            'mud-red': '#ff7b72',
            'mud-yellow': '#f8e45c',
            'mud-purple': '#d2a8ff',
            'mud-orange': '#ffa657',
          },
          fontFamily: {
            'terminal': ['Consolas', 'Menlo', 'Courier New', 'monospace'],
          }
        }
      }
    }
  </script>
</head>
```

### 5-2. `login.html`

**현재**: Bootstrap 기본 카드, 흰 배경, 파란 헤더  
**변경 후**: 다크 배경, ASCII 박스 헤더, 게임 로고, 터미널 스타일 Google 로그인 버튼

핵심 구성:
- `body`: `bg-bg-base text-text-normal font-terminal`
- ASCII 박스 로고 (`╔══╗` 스타일)
- `MUD▋` 타이포 로고 (파랑 + 초록 커서)
- 단일 Google 로그인 버튼 (다크 테마)
- 버전/저작권 dimmed 텍스트

### 5-3. `chat.html`

**현재**: 인라인 `<style>` 400줄, Bootstrap 혼용  
**변경 후**: 인라인 CSS 제거, Tailwind 클래스로 교체

주요 변경:
- `<style>` 블록 전체 삭제
- Bootstrap CDN 제거, `th:replace="layout/base :: head('MUD Terminal')"` 추가
- `.terminal-card` → Tailwind `bg-bg-surface border border-border-default rounded-lg`
- `.chat-container` → 스크롤 영역 Tailwind로 재구성
- 메시지 렌더링 JS에서 타입별 CSS 클래스 적용 (기존 JS 로직 유지, className만 변경)
- WebSocket 연결/처리 JS 로직은 변경 없음

### 5-4. `profile.html`

**현재**: Bootstrap 기본 테이블, 흰 배경  
**변경 후**: 터미널 스타일 캐릭터 정보창

핵심 구성:
- `=== 캐릭터 정보 ===` 섹션 헤더 (노랑)
- 라벨/값 쌍 형태의 정보 표시
- HP/MP/경험치 CSS 프로그레스 바 (Bootstrap progress 대신)
- `▶ 게임으로 돌아가기` / `✕ 로그아웃` 버튼

### 5-5. `gameplay/room-info.html`

**현재**: `roomInfo`와 `roomDetails` 두 fragment가 공존 (중복, 일관성 없음)  
**변경 후**: `roomInfo` fragment 하나로 통일, `roomDetails` 제거

메시지 타입 클래스 적용:
- 방 이름 → `msg-room-title`
- 방 설명 → `msg-room-desc`
- 출구 → `msg-exits`
- NPC/몬스터/플레이어 → `msg-npc`

### 5-6. `gameplay/combat-action.html`

공격 성공 → `msg-dmg-dealt`, 피격 → `msg-dmg-taken` 클래스 적용. 기존 Thymeleaf 조건 로직 유지.

### 5-7. `gameplay/status.html`

`text-warning`, `text-primary` 등 Bootstrap 유틸리티 클래스를 Tailwind + 커스텀 색상으로 교체.  
ASCII 프로그레스 바(`████▒▒`) 유지 — 텍스트 게임 느낌을 주는 요소이므로 보존.

---

## 6. 변경하지 않는 것

- WebSocket 연결/메시지 처리 JavaScript 로직
- Thymeleaf 표현식 (`th:text`, `th:each`, `th:if` 등)
- 서버사이드 컨트롤러/서비스 로직
- 관리자 페이지 (`area-management`, `room-management`, `npc-management` 등)
- ASCII 프로그레스 바 (`████▒▒`) — 텍스트 게임 정체성 요소로 보존

---

## 7. 작업 순서

1. `layout/base.html` 생성 — Tailwind 설정 + 공통 head
2. `login.html` 재작성 — 가장 단순, 선행 작업으로 스타일 검증
3. `chat.html` 인라인 CSS 제거 + Tailwind 적용
4. gameplay 프래그먼트 메시지 클래스 적용 (`room-info`, `combat-action`, `status`)
5. `profile.html` 재작성
6. Bootstrap CDN 제거 전체 확인

---

## 8. 성공 기준

- login → chat → profile 세 페이지가 동일한 다크 터미널 테마로 통일됨
- 채팅창에서 방 입장 시 방 이름(파랑), 설명(흰색), 출구(초록)가 시각적으로 즉시 구분됨
- 전투 중 내 공격(초록 ↑)과 피격(빨강 ↓)이 직관적으로 구분됨
- 인라인 `<style>` 블록이 모든 Thymeleaf 템플릿에서 제거됨
- Bootstrap CDN 의존성 제거
