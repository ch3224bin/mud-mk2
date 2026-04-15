# MUD MK2 프론트엔드 리디자인 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 플레이어용 화면(login, chat, profile)을 모던 다크 터미널 테마로 통일하고, 채팅창 메시지 유형을 색상으로 명확히 구분한다.

**Architecture:** `layout/base.html` Thymeleaf 프래그먼트에 Tailwind CDN + 색상 설정 + 게임 전용 CSS를 집중 관리한다. 각 페이지는 이 프래그먼트로 `<head>`를 교체하고, 기존 인라인 `<style>` 블록을 제거한다. 게임플레이 프래그먼트(room-info, combat-action, status 등)는 서버가 HTML로 렌더링 후 WebSocket으로 전송하므로, 이 CSS 클래스들도 base.html에 포함한다.

**Tech Stack:** Spring Boot 3.4.5, Thymeleaf, Tailwind CSS CDN (Play CDN), SockJS, STOMP

---

## 파일 구조

| 작업 | 파일 |
|------|------|
| 신규 생성 | `src/main/resources/templates/layout/base.html` |
| 재작성 | `src/main/resources/templates/web/login.html` |
| 리팩토링 | `src/main/resources/templates/web/chat.html` |
| 재작성 | `src/main/resources/templates/web/profile.html` |
| 정리 | `src/main/resources/templates/gameplay/room-info.html` |
| 업데이트 | `src/main/resources/templates/gameplay/combat-action.html` |
| 업데이트 | `src/main/resources/templates/gameplay/status.html` |
| 업데이트 | `src/main/resources/templates/gameplay/game-time.html` |
| 업데이트 | `src/main/resources/templates/gameplay/weather.html` |

---

## Task 1: layout/base.html 생성

**Files:**
- Create: `src/main/resources/templates/layout/base.html`

이 파일이 모든 페이지의 `<head>`를 제공한다. Tailwind CDN, 커스텀 색상 설정, 게임 전용 CSS 클래스(gameplay 프래그먼트가 WebSocket으로 주입될 때도 필요한 클래스들)를 포함한다.

- [ ] **Step 1: base.html 파일 생성**

`src/main/resources/templates/layout/base.html` 파일을 아래 내용으로 생성한다:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:fragment="head(title)">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${title}">MUD MK2</title>

    <!-- Tailwind CSS Play CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        'bg-base':      '#0d1117',
                        'bg-surface':   '#161b22',
                        'bg-elevated':  '#21262d',
                        'bd-default':   '#30363d',
                        'bd-muted':     '#21262d',
                        'tx-normal':    '#c9d1d9',
                        'tx-muted':     '#8b949e',
                        'tx-dimmed':    '#6e7681',
                        'mud-blue':     '#79c0ff',
                        'mud-green':    '#56d364',
                        'mud-red':      '#ff7b72',
                        'mud-yellow':   '#f8e45c',
                        'mud-purple':   '#d2a8ff',
                        'mud-orange':   '#ffa657',
                        'mud-sky':      '#a5d6ff',
                        'mud-link':     '#58a6ff',
                    },
                    fontFamily: {
                        'terminal': ['Consolas', 'Menlo', 'Courier New', 'monospace'],
                    }
                }
            }
        }
    </script>

    <!--
        게임 전용 CSS
        - 메시지 타입 시스템 (.msg-*)
        - gameplay 프래그먼트 호환 클래스 (WebSocket innerHTML 주입 시 필요)
        - chat.js가 참조하는 클래스 (.message, .system-message, .sender, .content, .cursor)
    -->
    <style>
        /* ── 기본 레이아웃 ── */
        body { background-color: #0d1117; color: #c9d1d9; font-family: Consolas, Menlo, 'Courier New', monospace; }

        /* ── chat.js 참조 클래스 ── */
        .message          { margin-bottom: 2px; padding: 1px 0; font-size: 14px; line-height: 1.7; }
        .system-message   { color: #6e7681; font-style: italic; }
        .other-message    { color: #c9d1d9; }
        .user-message     { color: #58a6ff; }
        .message .sender  { color: #6e7681; margin-right: 6px; }
        .message .content { flex: 1; }
        .cursor           { display: inline-block; width: 8px; height: 14px; background: #56d364;
                            margin-left: 2px; animation: blink 1s infinite; vertical-align: text-bottom; }
        @keyframes blink  { 0%,100%{opacity:1} 50%{opacity:0} }

        /* ── 메시지 타입 시스템 ── */
        .msg-system       { color: #6e7681; font-style: italic; font-size: 0.9em; }
        .msg-room-title   { color: #79c0ff; font-weight: 700; margin-top: 6px; }
        .msg-room-desc    { color: #c9d1d9; }
        .msg-exits        { margin: 2px 0; }
        .msg-exits .label { color: #8b949e; }
        .msg-exits .dir   { color: #56d364; font-weight: 600; }
        .msg-exits .sep   { color: #3d444d; margin: 0 4px; }
        .msg-player-input { color: #58a6ff; }
        .msg-npc          { color: #c9d1d9; }
        .msg-npc .name    { color: #d2a8ff; font-weight: 600; }
        .msg-combat-event { color: #f8e45c; font-weight: 600; }
        .msg-dmg-dealt    { color: #56d364; }
        .msg-dmg-dealt .val { font-weight: 700; }
        .msg-dmg-taken    { color: #ff7b72; }
        .msg-dmg-taken .val { font-weight: 700; }
        .msg-item         { color: #ffa657; }
        .msg-exp          { color: #a5d6ff; font-style: italic; }

        /* ── gameplay 프래그먼트 호환 ── */
        .font-terminal    { font-family: Consolas, Menlo, 'Courier New', monospace !important; }
        .font-bold        { font-weight: 700 !important; }
        .font-italic      { font-style: italic !important; }
        .text-primary     { color: #79c0ff !important; }
        .text-secondary   { color: #8b949e !important; }
        .text-warning     { color: #f8e45c !important; }
        .text-success     { color: #56d364 !important; }
        .text-danger      { color: #ff7b72 !important; }
        .text-info        { color: #58a6ff !important; }
        .text-highlight   { color: #a5d6ff !important; font-weight: 700; }
        .text-npc         { color: #ffa657 !important; font-weight: 500; }
        .text-player      { color: #58a6ff !important; font-weight: 500; }
        .text-monster     { color: #ff7b72 !important; font-weight: 600; }
        .text-exit        { color: #56d364 !important; font-weight: 600; }
        .text-item        { color: #ffa657 !important; }
        .text-green       { color: #56d364; }
        .text-orange      { color: #ffa657; }
        .text-gray        { color: #6e7681; }

        /* ── layout/spacing 유틸리티 ── */
        .line             { margin: 2px 0; }
        .line-spacer      { margin: 8px 0; }
        .list-unstyled    { list-style-type: none !important; padding-left: 0 !important; margin: 0 !important; }

        /* ── room-info 프래그먼트 ── */
        .room-info        { padding: 8px 0; font-family: Consolas, Menlo, 'Courier New', monospace; }

        /* ── status 프래그먼트 ── */
        .section-title    { color: #f8e45c; font-weight: 600; margin: 12px 0 6px;
                            padding-bottom: 4px; border-bottom: 1px solid #21262d; }
        .info-line, .stat-line, .equipment-line, .skill-line
                          { display: flex; align-items: center; margin: 3px 0; }
        .info-label, .stat-label, .equipment-slot
                          { min-width: 120px; margin-right: 8px; color: #8b949e; }
        .progress-line    { margin: 5px 0; display: block; }
        .progress-label   { display: inline-block; min-width: 120px; }
        .progress-text    { font-family: Consolas, Menlo, 'Courier New', monospace; }
        .progress-bar-ascii { font-family: Consolas, Menlo, 'Courier New', monospace;
                              font-size: 13px; margin-left: 8px; }
        .stat-modifier    { font-size: 12px; margin-left: 4px; }
        .stats-grid       { display: grid; grid-template-columns: 1fr 1fr; gap: 4px; }
        .skill-name       { min-width: 200px; }
        .skill-dots       { flex: 1; overflow: hidden; white-space: nowrap; color: #3d444d; }
        .skill-level      { min-width: 60px; text-align: right; }
        .equipment-item   { flex: 1; }
        .ascii-header     { font-size: 12px; line-height: 1.2; }
        .ascii-art        { font-size: 12px; line-height: 1.2; white-space: pre; color: #56d364; }
        .combat-action-message { margin: 6px 0; }

        /* ── weather/game-time 프래그먼트 ── */
        .weather-message, .game-time-message
                          { margin: 3px 0; color: #f8e45c; font-style: italic; }
        .weather-label, .time-label  { color: #8b949e; }
        .weather-value, .time-value  { font-weight: 600; }
        .period-value     { font-size: 0.9em; opacity: 0.9; margin-left: 4px; }
        .weather-icon, .time-icon    { display: inline-block; margin-right: 4px; }

        /* 날씨별 이모지 */
        .weather-icon-맑음::before  { content: "☀️"; }
        .weather-icon-흐림::before  { content: "☁️"; }
        .weather-icon-비::before    { content: "🌧️"; }
        .weather-icon-눈::before    { content: "❄️"; }
        .weather-icon-폭풍::before  { content: "⚡"; }
        .weather-icon-안개::before  { content: "🌫️"; }

        /* 시간대별 이모지 */
        .time-icon-새벽::before { content: "🌅"; }
        .time-icon-아침::before { content: "🌞"; }
        .time-icon-낮::before   { content: "☀️"; }
        .time-icon-저녁::before { content: "🌇"; }
        .time-icon-밤::before   { content: "🌙"; }
        .time-icon-심야::before { content: "✨"; }

        /* ── 스크롤바 ── */
        #chatContainer::-webkit-scrollbar { width: 6px; }
        #chatContainer::-webkit-scrollbar-track { background: #0d1117; }
        #chatContainer::-webkit-scrollbar-thumb { background: #30363d; border-radius: 3px; }
        #chatContainer::-webkit-scrollbar-thumb:hover { background: #56d364; }
    </style>
</head>
</html>
```

- [ ] **Step 2: 빌드 확인**

```bash
cd /home/ch3224bin/dev/apps/mud-mk2
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: 커밋**

```bash
git add src/main/resources/templates/layout/base.html
git commit -m "feat(frontend): 공통 레이아웃 프래그먼트 생성 (Tailwind CDN + 게임 CSS)"
```

---

## Task 2: login.html 재작성

**Files:**
- Modify: `src/main/resources/templates/web/login.html`

가장 단순한 페이지. 이걸 먼저 완성해서 base.html이 제대로 동작하는지 확인한다.

- [ ] **Step 1: login.html 전체 교체**

`src/main/resources/templates/web/login.html` 파일 전체를 아래 내용으로 교체한다:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{layout/base :: head('MUD MK2 — Login')}"></head>
<body class="min-h-screen flex items-center justify-center bg-bg-base">

<div class="w-full max-w-sm px-6">

    <!-- 창 프레임 -->
    <div class="bg-bg-surface border border-bd-default rounded-lg overflow-hidden">

        <!-- 타이틀바 -->
        <div class="bg-bg-elevated border-b border-bd-default px-4 py-2 flex items-center gap-2">
            <span class="w-3 h-3 rounded-full bg-red-400 inline-block"></span>
            <span class="w-3 h-3 rounded-full bg-yellow-400 inline-block"></span>
            <span class="w-3 h-3 rounded-full bg-green-400 inline-block"></span>
            <span class="ml-3 text-xs text-tx-dimmed font-terminal">mud-client — login</span>
        </div>

        <!-- 본문 -->
        <div class="p-8 flex flex-col items-center gap-5 font-terminal">

            <!-- ASCII 박스 -->
            <pre class="text-tx-dimmed text-center leading-tight select-none" style="font-size:0.55rem;">
╔════════════════════════╗
║  M U L T I  U S E R   ║
║  D U N G E O N  M K 2  ║
╚════════════════════════╝</pre>

            <!-- 로고 -->
            <div class="text-2xl font-bold tracking-widest text-mud-blue">
                MUD<span class="text-mud-green">▋</span>
            </div>

            <!-- 안내 문구 -->
            <p class="text-tx-dimmed text-xs tracking-wide text-center">
                # 멀티유저 던전에 접속하려면 인증이 필요합니다
            </p>

            <hr class="w-full border-bd-muted">

            <!-- Google 로그인 버튼 -->
            <a href="/oauth2/authorization/google"
               class="w-full flex items-center justify-center gap-3 bg-bg-elevated border border-bd-default
                      text-tx-normal text-sm px-5 py-2.5 rounded hover:border-mud-green hover:text-mud-green
                      transition-colors duration-200 no-underline">
                <!-- Google 색상 원형 아이콘 -->
                <svg width="16" height="16" viewBox="0 0 48 48">
                    <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
                    <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
                    <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
                    <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.18 1.48-4.97 2.31-8.16 2.31-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
                </svg>
                Google 계정으로 로그인
            </a>

            <!-- 하단 -->
            <p class="text-tx-dimmed text-center" style="font-size:0.6rem;">
                © 2025 MUD MK2 · unauthorized access is prohibited
            </p>

        </div>
    </div>
</div>

</body>
</html>
```

- [ ] **Step 2: 서버 실행 후 시각적 확인**

```bash
./gradlew bootRun
```

브라우저에서 `http://localhost:8080/login` (또는 서버가 뜨는 포트)를 열어 확인:
- 다크 배경에 중앙 정렬된 창 프레임이 보여야 함
- Bootstrap 기본 흰 배경이 아닌 `#0d1117` 다크 배경이어야 함
- 타이틀바에 빨강/노랑/초록 점 3개가 보여야 함
- "MUD▋" 로고가 파란색으로, ▋가 초록색으로 보여야 함

- [ ] **Step 3: 커밋**

```bash
git add src/main/resources/templates/web/login.html
git commit -m "feat(frontend): login 페이지 다크 터미널 테마 적용"
```

---

## Task 3: chat.html 리팩토링

**Files:**
- Modify: `src/main/resources/templates/web/chat.html`

이 파일의 핵심 작업은:
1. `<head>` 블록 전체(1~7번 줄 + `<style>` 블록 전체, 약 1~1233줄)를 layout 프래그먼트로 교체
2. `<body>` HTML 구조를 Tailwind 클래스로 재작성
3. `<script>` 태그들과 JavaScript 코드(1286~1490줄)는 **일절 변경하지 않는다**

**주의**: JS 코드에서 참조하는 `id="chatContainer"`, `id="messageInput"`, `id="sendButton"`은 반드시 유지해야 한다.

- [ ] **Step 1: chat.html 전체 교체**

`src/main/resources/templates/web/chat.html` 파일 전체를 아래 내용으로 교체한다:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{layout/base :: head('MUD Terminal')}"></head>
<body class="min-h-screen bg-bg-base py-5">

<div class="max-w-4xl mx-auto px-4">
    <div class="bg-bg-surface border border-bd-default rounded-lg overflow-hidden">

        <!-- 타이틀바 -->
        <div class="bg-bg-elevated border-b border-bd-default px-4 py-2 flex items-center gap-2">
            <span class="w-3 h-3 rounded-full bg-red-400 inline-block"></span>
            <span class="w-3 h-3 rounded-full bg-yellow-400 inline-block"></span>
            <span class="w-3 h-3 rounded-full bg-green-400 inline-block"></span>
            <span class="ml-2 text-xs text-tx-dimmed font-terminal">mud-client — MUD TERMINAL v1.0</span>
            <div th:if="${userName}" class="ml-auto flex items-center gap-2 text-xs font-terminal">
                <span class="text-tx-muted">USER:</span>
                <span th:text="${userName}" class="text-mud-green font-bold"></span>
                <img th:if="${userPicture}" th:src="${userPicture}" alt="Profile"
                     class="w-7 h-7 rounded-full border border-bd-default ml-1">
            </div>
        </div>

        <!-- 로그인 상태: 게임 화면 -->
        <div th:if="${userName}">

            <!-- 채팅 영역 -->
            <div id="chatContainer"
                 class="overflow-y-auto p-6 text-sm font-terminal"
                 style="height: calc(100vh - 220px); min-height: 500px;"></div>

            <!-- 입력창 -->
            <div class="border-t border-bd-default bg-bg-base px-4 py-3 flex items-center gap-3">
                <span class="text-mud-green font-bold font-terminal">$</span>
                <input type="text"
                       id="messageInput"
                       class="flex-1 bg-transparent text-tx-normal text-sm font-terminal
                              outline-none placeholder-tx-dimmed"
                       placeholder="명령어를 입력하세요...">
                <button id="sendButton"
                        class="text-xs text-tx-muted border border-bd-default px-3 py-1 rounded
                               font-terminal hover:border-mud-green hover:text-mud-green
                               transition-colors duration-200">
                    Execute
                </button>
            </div>

            <!-- 시스템 버튼 -->
            <div class="border-t border-bd-muted px-4 py-2 flex gap-2 justify-end">
                <a href="/admin"
                   class="text-xs text-tx-dimmed border border-bd-muted px-3 py-1 rounded font-terminal
                          hover:text-mud-yellow hover:border-mud-yellow transition-colors duration-200
                          no-underline">ADMIN</a>
                <a href="/profile"
                   class="text-xs text-tx-dimmed border border-bd-muted px-3 py-1 rounded font-terminal
                          hover:text-mud-blue hover:border-mud-blue transition-colors duration-200
                          no-underline">PROFILE</a>
                <a href="/logout"
                   class="text-xs text-tx-dimmed border border-bd-muted px-3 py-1 rounded font-terminal
                          hover:text-mud-red hover:border-mud-red transition-colors duration-200
                          no-underline">LOGOUT</a>
            </div>

        </div>

        <!-- 비로그인 상태 -->
        <div th:unless="${userName}"
             class="flex flex-col items-center justify-center py-20 px-6 text-center gap-6">
            <div class="border border-bd-muted rounded p-5 max-w-xs font-terminal">
                <p class="text-mud-blue text-sm mb-2">Authentication Required</p>
                <p class="text-tx-dimmed text-xs">게임 터미널에 접속하려면 로그인이 필요합니다.</p>
            </div>
            <a href="/oauth2/authorization/google"
               class="bg-bg-elevated border border-bd-default text-tx-normal text-sm px-6 py-2.5 rounded
                      font-terminal hover:border-mud-green hover:text-mud-green transition-colors duration-200
                      no-underline">
                ⟶ Google로 로그인
            </a>
        </div>

    </div>
</div>

<!-- WebSocket 클라이언트 라이브러리 (변경 금지) -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.1/sockjs.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>

<!-- 게임 클라이언트 JS (변경 금지 — WebSocket/STOMP 연결 로직) -->
<script th:inline="javascript">
    document.addEventListener('DOMContentLoaded', function() {
        const chatContainer = document.getElementById('chatContainer');
        const messageInput = document.getElementById('messageInput');
        const sendButton = document.getElementById('sendButton');

        // Set focus on messageInput when page loads
        if (messageInput) {
            messageInput.focus();
        }

        // Get username from Thymeleaf
        const username = /*[[${userName}]]*/ 'default-user';
        const userPicture = /*[[${userPicture}]]*/ '';

        // Message history functionality
        const messageHistory = [];
        let historyIndex = -1;
        const MAX_HISTORY_SIZE = 50;

        let stompClient = null;

        // Connect to WebSocket
        function connect() {
            const socket = new SockJS('/ws');
            stompClient = Stomp.over(socket);

            stompClient.connect({}, function(frame) {
                console.log('Connected: ' + frame);

                // Subscribe to public channel
                stompClient.subscribe('/topic/public', function(message) {
                    showMessage(JSON.parse(message.body));
                });

                // Subscribe to private channel (for personal messages)
                stompClient.subscribe('/user/queue/system-messages', function(message) {
                    showMessage(JSON.parse(message.body));
                });

                // Send join message
                sendJoinMessage();
            });
        }

        // Send join message
        function sendJoinMessage() {
            if (stompClient && username) {
                const chatMessage = {
                    sender: username,
                    type: 'JOIN'
                };
                stompClient.send("/app/chat.addUser", {}, JSON.stringify(chatMessage));
            }
        }

        // Scroll to bottom of chat container
        function scrollToBottom() {
            chatContainer.scrollTop = chatContainer.scrollHeight;
        }

        // Initial scroll to bottom
        scrollToBottom();

        // Show message in chat
        function showMessage(message) {
            const messageElement = document.createElement('div');
            let senderDisplay;

            // Determine message type and styling
            if (message.sender === 'System') {
                messageElement.className = 'message system-message';
                senderDisplay = '[System]';
            } else if (message.type === 'JOIN') {
                messageElement.className = 'message system-message';
                senderDisplay = '[System]';
                message.content = `${message.sender} has entered the realm.`;
            } else if (message.type === 'LEAVE') {
                messageElement.className = 'message system-message';
                senderDisplay = '[System]';
                message.content = `${message.sender} has left the realm.`;
            } else if (message.type === 'FULL_DESCRIPTION') {
                messageElement.className = 'message system-message';
                senderDisplay = '';
            } else {
                messageElement.className = 'message other-message';
                senderDisplay = `[${message.sender}]`;
            }

            // Create message with sender and content on the same line
            if (message.type === 'FULL_DESCRIPTION') {
                // For FULL_DESCRIPTION, render HTML content directly
                messageElement.innerHTML = message.content;
            } else {
                // For other message types, show sender and content
                messageElement.innerHTML = `
                    <span class="sender">${senderDisplay}</span>
                    <span class="content">${message.content}</span>
                `;
            }

            chatContainer.appendChild(messageElement);
            scrollToBottom();
        }

        // Track the last user command for game processing
        let lastUserCommand = '';

        // Send message function
        function sendMessage() {
            const message = messageInput.value.trim();
            if (stompClient && message) {
                // Store the command for processing
                lastUserCommand = message;

                // Add to message history if it's not the same as the last entry
                if (messageHistory.length === 0 || messageHistory[messageHistory.length - 1] !== message) {
                    // Add to history
                    messageHistory.push(message);

                    // Keep history size limited
                    if (messageHistory.length > MAX_HISTORY_SIZE) {
                        messageHistory.shift(); // Remove oldest entry
                    }
                }

                // Reset history index
                historyIndex = -1;

                const chatMessage = {
                    sender: username,
                    content: message,
                    type: 'CHAT'
                };

                // Send message to server
                stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));

                // Clear input
                messageInput.value = '';

                // Add blinking cursor after command
                setTimeout(() => {
                    const cursorElement = document.createElement('div');
                    cursorElement.className = 'message system-message';
                    cursorElement.innerHTML = '<span class="cursor"></span>';
                    chatContainer.appendChild(cursorElement);

                    // Remove cursor after a short delay
                    setTimeout(() => {
                        chatContainer.removeChild(cursorElement);
                    }, 500);
                }, 100);
            }
        }

        // Event listeners
        sendButton.addEventListener('click', sendMessage);
        messageInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });

        // Add keyboard navigation for message history
        messageInput.addEventListener('keydown', function(e) {
            // Up arrow key - navigate to previous command in history
            if (e.key === 'ArrowUp') {
                e.preventDefault(); // Prevent cursor from moving to start of input

                if (messageHistory.length > 0) {
                    // If we're at the beginning or haven't started navigating yet
                    if (historyIndex < messageHistory.length - 1) {
                        historyIndex++;
                        messageInput.value = messageHistory[messageHistory.length - 1 - historyIndex];
                    }
                }
            }
            // Down arrow key - navigate to next command in history
            else if (e.key === 'ArrowDown') {
                e.preventDefault(); // Prevent cursor from moving to end of input

                if (historyIndex > 0) {
                    historyIndex--;
                    messageInput.value = messageHistory[messageHistory.length - 1 - historyIndex];
                } else if (historyIndex === 0) {
                    // If we've reached the most recent command, clear the input
                    historyIndex = -1;
                    messageInput.value = '';
                }
            }
        });

        // Connect if username is available
        if (username) {
            connect();
        }
    });
</script>
</body>
</html>
```

- [ ] **Step 2: 서버 실행 후 시각적 확인**

```bash
./gradlew bootRun
```

브라우저에서 `http://localhost:8080/chat` 접속 후 확인:
- 다크 배경, 창 프레임 스타일이 적용되어야 함
- 입력창에 `$` 프롬프트가 보여야 함
- ADMIN / PROFILE / LOGOUT 버튼이 우측 하단에 위치해야 함
- WebSocket 연결이 정상 동작해야 함 (브라우저 DevTools Console에서 `Connected:` 로그 확인)
- 명령어 입력 시 서버 응답이 채팅창에 표시되어야 함
- 이전 명령어 히스토리(↑↓ 키)가 동작해야 함

- [ ] **Step 3: 커밋**

```bash
git add src/main/resources/templates/web/chat.html
git commit -m "feat(frontend): chat 페이지 Tailwind 적용, 인라인 CSS 1200줄 제거"
```

---

## Task 4: gameplay/room-info.html 정리

**Files:**
- Modify: `src/main/resources/templates/gameplay/room-info.html`

현재 파일에 `roomInfo`와 `roomDetails` 두 개의 fragment가 존재한다. `roomDetails`를 제거하고 `roomInfo`를 새 메시지 클래스로 정리한다.

- [ ] **Step 1: room-info.html 전체 교체**

```html
<div th:fragment="roomInfo" class="room-info font-terminal">
    <div class="msg-room-title" th:text="${'=== ' + roomName + ' ==='}"></div>
    <div class="msg-room-desc" th:utext="${roomDescription}"></div>
    <div class="line-spacer"></div>

    <div class="msg-exits">
        <span class="label">출구: </span>
        <span th:if="${not #lists.isEmpty(exits)}">
            <span th:each="exit, iter : ${exits}">
                <span class="dir" th:text="${exit}"></span><span th:if="${!iter.last}" class="sep">·</span>
            </span>
        </span>
        <span th:if="${#lists.isEmpty(exits)}" class="text-tx-dimmed">없음</span>
    </div>
    <div class="line-spacer"></div>

    <div th:if="${not #lists.isEmpty(npcsInRoom)}">
        <div class="msg-room-title">=== 이곳의 인물들 ===</div>
        <div class="msg-npc line" th:each="npc : ${npcsInRoom}">
            👤 <span th:text="${npc.description}"></span>
        </div>
        <div class="line-spacer"></div>
    </div>

    <div th:if="${not #lists.isEmpty(monstersInRoom)}">
        <div class="msg-room-title">=== 몬스터 ===</div>
        <div class="line" th:each="monster : ${monstersInRoom}">
            <span class="text-monster">🗡️ <span th:text="${monster.description}"></span></span>
        </div>
        <div class="line-spacer"></div>
    </div>

    <div th:if="${not #lists.isEmpty(otherPlayersInRoom)}">
        <div class="msg-room-title">=== 함께 있는 플레이어 ===</div>
        <div class="line" th:each="player : ${otherPlayersInRoom}">
            <span class="text-player">👤 <span th:text="${player.description}"></span></span>
        </div>
        <div class="line-spacer"></div>
    </div>
</div>
```

- [ ] **Step 2: 게임 접속 후 방 이동 확인**

```bash
./gradlew bootRun
```

게임에 접속 후 방을 이동해서 room-info가 렌더링되는지 확인:
- 방 제목이 파랑(`#79c0ff`)으로 보여야 함
- 출구 방향이 초록(`#56d364`)으로 보여야 함
- NPC, 몬스터, 다른 플레이어가 각자 다른 색상으로 보여야 함

- [ ] **Step 3: 커밋**

```bash
git add src/main/resources/templates/gameplay/room-info.html
git commit -m "feat(frontend): room-info fragment 통일, 메시지 타입 클래스 적용"
```

---

## Task 5: gameplay/combat-action.html 업데이트

**Files:**
- Modify: `src/main/resources/templates/gameplay/combat-action.html`

- [ ] **Step 1: combat-action.html 전체 교체**

```html
<div th:each="log : ${combatActionResult.logs}">
    <div class="combat-action-message font-terminal">

        <!-- 플레이어 공격 (내가 공격) -->
        <div th:if="${log.attackerId == userUuid}">
            <div th:if="${log.hitSuccess}" class="msg-dmg-dealt">
                ↑ <span class="text-player font-bold">당신</span>
                <span class="text-secondary">이(가) </span>
                <span class="text-monster font-bold" th:text="${log.targetName}"></span>
                <span class="text-secondary">을(를) 공격했습니다. 데미지: </span>
                <span class="val" th:text="${log.finalDamage}"></span>
            </div>
            <div th:unless="${log.hitSuccess}" class="text-secondary">
                <span class="text-player font-bold">당신</span>
                <span>이(가) </span>
                <span class="text-monster font-bold" th:text="${log.targetName}"></span>
                <span>을(를) 공격했지만 빗나갔습니다. </span>
                <span style="color:#3d444d;">(명중 <span th:text="${log.attackTotal}"></span> vs 방어 <span th:text="${log.defenseTotal}"></span>)</span>
            </div>
        </div>

        <!-- 피격 (몬스터가 나를 공격) -->
        <div th:if="${log.targetId == userUuid}">
            <div th:if="${log.hitSuccess}" class="msg-dmg-taken">
                ↓ <span class="font-bold" th:text="${log.attackerName}"></span>
                <span>이(가) </span>
                <span class="text-player font-bold">당신</span>
                <span>을 공격했습니다. 피해: </span>
                <span class="val" th:text="${log.finalDamage}"></span>
            </div>
            <div th:unless="${log.hitSuccess}" class="text-secondary">
                <span class="text-monster font-bold" th:text="${log.attackerName}"></span>
                <span>이(가) </span>
                <span class="text-player font-bold">당신</span>
                <span>을 공격했지만 빗나갔습니다. </span>
                <span style="color:#3d444d;">(명중 <span th:text="${log.attackTotal}"></span> vs 방어 <span th:text="${log.defenseTotal}"></span>)</span>
            </div>
        </div>

        <!-- 아군 전투 (다른 플레이어 vs 몬스터) -->
        <div th:if="${log.attackerId != userUuid && log.targetId != userUuid}">
            <div th:if="${log.hitSuccess}" class="text-secondary">
                <span class="text-player font-bold" th:text="${log.attackerName}"></span>
                <span>이(가) </span>
                <span class="text-monster font-bold" th:text="${log.targetName}"></span>
                <span>을(를) 공격했습니다. 데미지: </span>
                <span class="text-mud-green font-bold" th:text="${log.finalDamage}"></span>
            </div>
            <div th:unless="${log.hitSuccess}" class="text-secondary">
                <span class="text-player font-bold" th:text="${log.attackerName}"></span>
                <span>이(가) </span>
                <span class="text-monster font-bold" th:text="${log.targetName}"></span>
                <span>을(를) 공격했지만 빗나갔습니다.</span>
            </div>
        </div>

    </div>
</div>
```

- [ ] **Step 2: 전투 상황 확인**

게임 접속 후 몬스터와 전투하여 확인:
- 내 공격 성공 시 초록(↑) 메시지
- 피격 시 빨강(↓) 메시지
- 빗나간 경우 회색 메시지

- [ ] **Step 3: 커밋**

```bash
git add src/main/resources/templates/gameplay/combat-action.html
git commit -m "feat(frontend): combat-action 메시지 타입 클래스 적용"
```

---

## Task 6: gameplay/status.html 업데이트

**Files:**
- Modify: `src/main/resources/templates/gameplay/status.html`

현재 파일은 `text-warning`, `text-primary` 등 Bootstrap 유틸리티 클래스를 사용한다. 이 클래스들은 이미 `base.html`에 정의되어 있어 호환되지만, ASCII 헤더와 section-title 클래스 이름이 깨질 수 있어 확인 후 필요 시 조정한다.

- [ ] **Step 1: status.html에서 Bootstrap 의존 클래스 제거**

현재 파일의 1번째 줄부터 읽어서 `text-primary`, `text-warning`, `text-success`, `text-danger`, `text-info`, `text-secondary` 클래스를 사용하는 부분을 확인한다. 이 클래스들은 `base.html`에서 게임 색상으로 재정의되어 있으므로 파일을 그대로 유지하면 된다.

확인이 필요한 것은 `<div class="ascii-header text-primary my-2">` 부분의 `my-2`인데, Bootstrap의 `my-2`를 Tailwind의 `my-2`가 대체하므로 정상 동작한다.

**변경이 필요한 부분만 수정**: ASCII 헤더의 `<pre>` 태그가 너무 넓어서 overflow가 발생할 수 있다. 해당 부분에 `overflow-x: auto` 스타일 추가:

`src/main/resources/templates/gameplay/status.html` 파일의 3번째 줄을 변경:

변경 전:
```html
    <div class="ascii-header text-primary my-2">
```

변경 후:
```html
    <div class="ascii-header text-primary my-2" style="overflow-x:auto;">
```

- [ ] **Step 2: 상태창 확인**

게임 접속 후 `status` 명령어를 입력하여 확인:
- 색상이 올바르게 표시되어야 함 (노랑 섹션 제목, 초록/빨강 HP 바 등)
- ASCII 프로그레스 바(████▒▒)가 그대로 유지되어야 함

- [ ] **Step 3: 커밋**

```bash
git add src/main/resources/templates/gameplay/status.html
git commit -m "feat(frontend): status fragment overflow 처리"
```

---

## Task 7: gameplay/game-time.html + weather.html + combat-start.html 업데이트

**Files:**
- Modify: `src/main/resources/templates/gameplay/game-time.html`
- Modify: `src/main/resources/templates/gameplay/weather.html`
- Modify: `src/main/resources/templates/gameplay/combat-start.html`

세 파일 모두 `font-terminal`, `font-italic`, `text-warning`, `text-secondary` 클래스를 사용하며 base.html과 호환된다. `text-shadow-sm`은 base.html에 없으므로 제거한다. `combat-start.html`은 구조적 클래스(`combat-start-message`, `initiative-info` 등)만 추가로 사용하는데, CSS 정의가 없어도 레이아웃에 영향 없으므로 유지한다.

- [ ] **Step 1: game-time.html 수정**

`src/main/resources/templates/gameplay/game-time.html` 전체 교체:

```html
<div class="game-time-message font-terminal font-italic">
    <span class="time-icon" th:classappend="'time-icon-' + ${dayPeriod.toLowerCase()}"></span>
    <span class="time-label">현재 시각: </span>
    <span class="time-value" th:text="${gameHour} + '시'">12시</span>
    <span class="period-value" th:text="'(' + ${dayPeriod} + ')'">낮</span>
</div>
```

- [ ] **Step 2: weather.html 수정**

`src/main/resources/templates/gameplay/weather.html` 전체 교체:

```html
<div class="weather-message font-terminal font-italic">
    <span class="weather-icon" th:classappend="'weather-icon-' + ${weatherType.toLowerCase()}"></span>
    <span class="weather-label">날씨가 변했습니다: </span>
    <span class="weather-value" th:text="${weatherType}">맑음</span>
</div>
```

- [ ] **Step 3: combat-start.html에서 text-shadow-sm 제거**

`src/main/resources/templates/gameplay/combat-start.html` 4번째 줄 수정:

변경 전:
```html
        <h4 class="text-info text-shadow-sm mb-2">전투가 시작되었습니다</h4>
```

변경 후:
```html
        <h4 class="text-info mb-2">전투가 시작되었습니다</h4>
```

- [ ] **Step 4: 커밋**

```bash
git add src/main/resources/templates/gameplay/game-time.html \
        src/main/resources/templates/gameplay/weather.html \
        src/main/resources/templates/gameplay/combat-start.html
git commit -m "feat(frontend): gameplay fragments text-shadow-sm 의존성 제거"
```

---

## Task 8: profile.html 재작성

**Files:**
- Modify: `src/main/resources/templates/web/profile.html`

- [ ] **Step 1: profile.html 전체 교체**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{layout/base :: head('MUD — 프로필')}"></head>
<body class="min-h-screen bg-bg-base flex items-center justify-center py-10">

<div class="w-full max-w-md px-6">
    <div class="bg-bg-surface border border-bd-default rounded-lg overflow-hidden font-terminal">

        <!-- 타이틀바 -->
        <div class="bg-bg-elevated border-b border-bd-default px-4 py-2 flex items-center gap-2">
            <span class="w-3 h-3 rounded-full bg-red-400 inline-block"></span>
            <span class="w-3 h-3 rounded-full bg-yellow-400 inline-block"></span>
            <span class="w-3 h-3 rounded-full bg-green-400 inline-block"></span>
            <span class="ml-3 text-xs text-tx-dimmed">mud-client — profile</span>
        </div>

        <!-- 로그인 상태 -->
        <div th:if="${userName}" class="p-6 text-sm">

            <!-- 사용자 정보 -->
            <div class="flex items-center gap-4 mb-6">
                <div th:if="${userPicture}"
                     class="w-12 h-12 rounded-full overflow-hidden border border-bd-default flex-shrink-0">
                    <img th:src="${userPicture}" alt="Profile" class="w-full h-full object-cover">
                </div>
                <div th:unless="${userPicture}"
                     class="w-12 h-12 rounded-full bg-bg-elevated border border-bd-default
                            flex items-center justify-center text-xl flex-shrink-0">⚔</div>
                <div>
                    <div th:text="${userName}" class="text-tx-normal font-bold"></div>
                    <div th:text="${userEmail}" class="text-tx-dimmed text-xs mt-0.5"></div>
                </div>
            </div>

            <!-- 계정 정보 섹션 -->
            <div class="section-title text-xs tracking-widest">=== 계정 정보 ===</div>
            <div class="info-line my-1">
                <span class="info-label text-xs">이름</span>
                <span th:text="${userName}" class="text-mud-blue font-bold text-xs"></span>
            </div>
            <div class="info-line my-1">
                <span class="info-label text-xs">이메일</span>
                <span th:text="${userEmail}" class="text-tx-normal text-xs"></span>
            </div>
            <div class="info-line my-1">
                <span class="info-label text-xs">인증 방식</span>
                <span class="text-mud-green text-xs">Google OAuth2</span>
            </div>

            <!-- 버튼 -->
            <div class="flex gap-2 mt-8">
                <a href="/chat"
                   class="text-xs text-mud-green border border-mud-green px-4 py-1.5 rounded
                          hover:bg-mud-green hover:text-bg-base transition-colors duration-200
                          no-underline">
                    ▶ 게임으로 돌아가기
                </a>
                <a href="/logout"
                   class="text-xs text-mud-red border border-mud-red px-4 py-1.5 rounded
                          hover:bg-mud-red hover:text-bg-base transition-colors duration-200
                          no-underline">
                    ✕ 로그아웃
                </a>
            </div>

        </div>

        <!-- 비로그인 상태 -->
        <div th:unless="${userName}" class="p-8 flex flex-col items-center gap-4 text-center">
            <p class="text-mud-yellow text-sm">로그인이 필요합니다</p>
            <a href="/oauth2/authorization/google"
               class="text-xs text-tx-normal border border-bd-default px-5 py-2 rounded
                      hover:border-mud-green hover:text-mud-green transition-colors duration-200
                      no-underline">
                ⟶ Google로 로그인
            </a>
        </div>

    </div>
</div>

</body>
</html>
```

- [ ] **Step 2: 서버 실행 후 시각적 확인**

```bash
./gradlew bootRun
```

브라우저에서 `http://localhost:8080/profile` 접속 후 확인:
- 다크 배경, 창 프레임 적용되어야 함
- 프로필 사진이 원형으로 표시되어야 함
- `=== 계정 정보 ===` 섹션이 노랑색으로 표시되어야 함
- "게임으로 돌아가기" 버튼이 초록 테두리, "로그아웃"이 빨강 테두리이어야 함
- 호버 시 색상 반전이 동작해야 함

- [ ] **Step 3: 커밋**

```bash
git add src/main/resources/templates/web/profile.html
git commit -m "feat(frontend): profile 페이지 다크 터미널 테마 적용"
```

---

## Task 9: 최종 검증 및 Bootstrap 의존성 확인

**Files:**
- 없음 (검증만)

- [ ] **Step 1: Bootstrap CDN 참조 잔존 여부 확인**

```bash
grep -r "bootstrap" src/main/resources/templates/ --include="*.html" -l
```

Expected: 관리자 페이지 (`area-management.html`, `room-management.html` 등)만 나와야 함. `login.html`, `chat.html`, `profile.html`은 나오지 않아야 함.

- [ ] **Step 2: 인라인 style 블록 잔존 여부 확인**

```bash
grep -n "<style>" src/main/resources/templates/web/chat.html \
                  src/main/resources/templates/web/login.html \
                  src/main/resources/templates/web/profile.html
```

Expected: 출력 없음 (세 파일 모두 `<style>` 블록이 없어야 함)

- [ ] **Step 3: 전체 빌드 확인**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: 최종 커밋**

```bash
git add -A
git commit -m "feat(frontend): 프론트엔드 리디자인 완료 — Tailwind 다크 터미널 테마 통일"
```

---

## 성공 기준

- `login.html`, `chat.html`, `profile.html` 세 페이지가 동일한 다크 터미널 테마로 통일됨
- 세 파일 모두 인라인 `<style>` 블록 없음
- Bootstrap CDN 의존성이 플레이어 페이지에서 제거됨
- 채팅창에서 방 이름(파랑), 출구(초록), 피격(빨강 ↓), 공격(초록 ↑)이 시각적으로 구분됨
- WebSocket 연결 및 명령어 히스토리(↑↓ 키) 정상 동작
- ASCII 프로그레스 바(████▒▒)가 status 프래그먼트에서 유지됨
