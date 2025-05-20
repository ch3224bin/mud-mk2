# MUD-MK2 웹 인터페이스 스타일 가이드

이 스타일 가이드는 MUD-MK2 프로젝트의 웹 인터페이스를 개발하는 데 있어 일관된 디자인과 사용자 경험을 제공하기 위한 지침입니다.

## 1. 색상 체계

### 1.1 기본 컬러 팔레트

#### 1.1.1 배경 색상
- **기본 배경**: 그라데이션 `linear-gradient(135deg, #1a1c24 0%, #2d2f3b 100%)`
- **카드 배경**: `rgba(20, 21, 26, 0.95)`
- **섹션 배경**: `rgba(0, 0, 0, 0.3)`
- **입력 필드 배경**: `rgba(0, 0, 0, 0.6)`

#### 1.1.2 텍스트 색상
- **기본 텍스트**: `#e4e4e7` (밝은 회색)
- **강조 텍스트**: `#00ff00` (녹색, 터미널 스타일)
- **부제목/라벨**: `#9ca3af` (회색)
- **경고**: `#ef4444` (빨간색)
- **링크/특별 항목**: `#00ffff` (밝은 청록색)

#### 1.1.3 액션 색상
- **기본 버튼**: 그라데이션 `linear-gradient(45deg, #00aa00 0%, #00ff00 100%)`
- **위험 버튼**: 그라데이션 `linear-gradient(45deg, #b91c1c 0%, #ef4444 100%)`
- **링크 버튼**: 특정 기능의 테마 색상 (영역: 주황색, 방: 파란색, NPC: 보라색)
- **몬스터 관련**: `#ff5252` (밝은 빨간색)

### 1.2 주제별 특수 색상

#### 1.2.1 게임 요소 색상
- **NPC 이름**: `#ff9900` (주황색)
- **플레이어 이름**: `#60a5fa` (파란색)
- **몬스터 이름**: `#ff5252` (빨간색)
- **출구/방향**: `#00ff00` (녹색)
- **아이템**: `#ffa500` (주황색)

#### 1.2.2 시간대별 색상
- **새벽**: `#9fa8da` (연한 보라색)
- **아침**: `#ffb74d` (주황색)
- **낮**: `#ffd54f` (노란색)
- **저녁**: `#ffa000` (진한 주황색)
- **밤**: `#7986cb` (보라색)
- **심야**: `#5c6bc0` (진한 보라색)

## 2. 타이포그래피

### 2.1 폰트 패밀리
- **기본 폰트**: `'Inter', sans-serif`
- **터미널 텍스트/코드**: `'IBM Plex Mono', monospace`

### 2.2 폰트 크기
- **페이지 제목**: `1.8rem`
- **섹션 제목**: `1.5rem`
- **서브 헤딩**: `1.2rem`
- **기본 텍스트**: `14px`
- **작은 텍스트/설명**: `0.9rem` (12.6px)

### 2.3 폰트 스타일
- **강조**: `font-weight: 600;`
- **시스템 메시지**: `font-style: italic;`
- **터미널 명령**: `font-weight: 600; color: #00ff88;`

## 3. 레이아웃 요소

### 3.1 카드 스타일
```css
.terminal-card {
    background: rgba(20, 21, 26, 0.95);
    backdrop-filter: blur(12px);
    border-radius: 16px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 0 60px rgba(0, 255, 0, 0.05);
    overflow: hidden;
}
```

### 3.2 카드 헤더
```css
.card-header {
    background: linear-gradient(90deg, rgba(0, 255, 0, 0.05) 0%, transparent 100%);
    border-bottom: 1px solid rgba(0, 255, 0, 0.2);
    padding: 16px 24px;
}
```

### 3.3 컨테이너 패딩
- **기본 패딩**: `24px`
- **모바일 패딩**: `16px`

### 3.4 섹션 스타일
```css
.form-section {
    margin-bottom: 30px;
    padding: 20px;
    border: 1px solid #ddd;
    border-radius: 5px;
    background-color: rgba(0, 0, 0, 0.2);
}
```

## 4. 입력 요소

### 4.1 텍스트 입력
```css
.form-control {
    background: transparent;
    color: #00ff00;
    border: none;
    font-family: 'IBM Plex Mono', monospace;
    font-size: 14px;
    padding: 12px;
}

.form-control:focus {
    background: transparent;
    color: #00ff00;
    box-shadow: none;
    outline: none;
}
```

### 4.2 터미널 입력
```css
.terminal-input {
    background: rgba(0, 0, 0, 0.6);
    border: 1px solid rgba(0, 255, 0, 0.3);
    border-radius: 8px;
    margin: 0 24px 24px;
    transition: all 0.3s ease;
    display: flex;
    overflow: hidden;
}

.terminal-input:focus-within {
    border-color: rgba(0, 255, 0, 0.6);
    box-shadow: 0 0 20px rgba(0, 255, 0, 0.15);
}
```

### 4.3 버튼 스타일
```css
.btn-system {
    font-family: 'IBM Plex Mono', monospace;
    font-weight: 600;
    padding: 8px 20px;
    border-radius: 6px;
    border: 1px solid;
    transition: all 0.3s ease;
    font-size: 0.9rem;
}
```

### 4.4 커스텀 드롭다운
```css
select.form-control {
    background-color: rgba(0, 0, 0, 0.5);
    color: #e4e4e7;
    border: 1px solid rgba(0, 255, 0, 0.3);
    border-radius: 4px;
    padding: 8px 12px;
    appearance: none;
    background-image: url("data:image/svg+xml,..."); /* 화살표 SVG */
    background-repeat: no-repeat;
    background-position: right 10px center;
}
```

## 5. 게임 요소

### 5.1 메시지 스타일
```css
.message {
    margin-bottom: 4px;
    padding: 2px 0;
    display: flex;
    align-items: flex-start;
    width: 100%;
    font-size: 14px;
    opacity: 0;
    animation: fadeIn 0.3s ease forwards;
}

@keyframes fadeIn {
    to { opacity: 1; }
}
```

### 5.2 방 정보
```css
.room-info {
    padding: 20px 20px 20px 0px;
    margin-bottom: 15px;
    background-color: transparent;
    font-family: 'IBM Plex Mono', monospace;
}

.room-header {
    border-bottom: 1px solid rgba(0, 255, 0, 0.2);
    padding-bottom: 10px;
    margin-bottom: 12px;
}
```

### 5.3 캐릭터 정보
```css
.characters-info {
    margin-top: 15px;
    color: #9ca3af;
    font-size: 14px;
}
```

### 5.4 몬스터 정보
```css
.monsters-info {
    margin-top: 10px;
    margin-bottom: 10px;
}

.monsters-header {
    margin-bottom: 5px;
}
```

### 5.5 출구 정보
```css
.exits-info {
    margin-top: 15px;
    color: #9ca3af;
    font-size: 14px;
}

.exits-label {
    /* text-warning 클래스 사용 */
    margin-right: 10px;
}

.exits-list {
    list-style-type: none;
    padding: 0;
    display: inline-block;
    margin: 0;
}
```

## 6. 특수 효과

### 6.1 스크롤바 스타일
```css
::-webkit-scrollbar {
    width: 8px;
}

::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.3);
    border-radius: 4px;
}

::-webkit-scrollbar-thumb {
    background: linear-gradient(180deg, #00ff00 0%, #00aa00 100%);
    border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
    background: linear-gradient(180deg, #00ff88 0%, #00cc00 100%);
}
```

### 6.2 깜빡이는 커서
```css
@keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
}

.cursor {
    display: inline-block;
    width: 10px;
    height: 18px;
    background: #00ff00;
    margin-left: 4px;
    animation: blink 1s infinite;
    vertical-align: text-bottom;
    box-shadow: 0 0 5px #00ff00;
}
```

### 6.3 그라데이션 배경
```css
.chat-container {
    height: calc(100vh - 280px);
    min-height: 500px;
    overflow-y: auto;
    padding: 24px;
    background: radial-gradient(circle at 50% 0%, rgba(0, 255, 0, 0.02) 0%, transparent 70%);
    font-family: 'IBM Plex Mono', monospace;
    font-size: 14px;
    line-height: 1.6;
    color: #00ff00;
    position: relative;
}
```

## 7. 반응형 디자인 가이드라인

### 7.1 브레이크포인트
- **모바일**: < 768px
- **태블릿**: 768px ~ 992px
- **데스크톱**: > 992px

### 7.2 모바일 조정
```css
@media (max-width: 768px) {
    .chat-container {
        height: calc(100vh - 320px);
        min-height: 400px;
        padding: 16px;
    }

    .terminal-input, .system-controls {
        margin: 0 16px 16px;
    }

    .btn-execute {
        padding: 12px 16px;
        font-size: 0.9rem;
    }

    .admin-grid {
        grid-template-columns: 1fr;
    }
}
```

## 8. 관리자 페이지 요소

### 8.1 관리자 그리드
```css
.admin-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 24px;
}
```

### 8.2 관리자 카드
```css
.admin-card {
    background: rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(0, 255, 0, 0.3);
    border-radius: 8px;
    padding: 24px;
    text-align: center;
    transition: all 0.3s ease;
}

.admin-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5), 0 5px 15px rgba(0, 255, 0, 0.1);
}
```

### 8.3 테이블 스타일
```css
.table-fixed {
    table-layout: fixed;
}

.table-fixed td {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
```

## 9. 애니메이션

### 9.1 트랜지션
모든 상호작용 요소에는 다음과 같은 트랜지션 효과를 적용합니다:
```css
transition: all 0.3s ease;
```

### 9.2 호버 효과
```css
.btn-execute:hover {
    background: linear-gradient(45deg, #00cc00 0%, #00ff88 100%);
    color: #000;
    transform: translateX(2px);
    box-shadow: 0 0 15px rgba(0, 255, 0, 0.4);
}
```

## 10. 공통 컴포넌트

### 10.1 날씨 표시
```css
.weather-message {
    font-family: 'IBM Plex Mono', monospace;
    margin: 4px 0;
    color: #fbbf24;
    font-style: italic;
}

.weather-icon::before {
    font-size: 16px;
    vertical-align: middle;
}

.weather-label {
    color: #9ca3af;
}

.weather-value {
    font-weight: 600;
    text-shadow: 0 0 3px currentColor;
}
```

### 10.2 게임 시간 표시
```css
.game-time-message {
    font-family: 'IBM Plex Mono', monospace;
    margin: 4px 0;
    color: #fbbf24;
    font-style: italic;
}

.time-icon::before {
    font-size: 16px;
    vertical-align: middle;
}

.time-value {
    font-weight: 600;
    text-shadow: 0 0 3px currentColor;
}
```

## 11. 추상화된 CSS 클래스 시스템

### 11.1 색상 클래스
```css
/* 컬러 시스템 - 공통 색상 클래스 */
.text-primary {
    color: #00ff00 !important; /* 기본 터미널 색상 */
}

.text-secondary {
    color: #9ca3af !important; /* 부가 정보, 라벨 색상 */
}

.text-highlight {
    color: #00ffff !important; /* 강조 색상 */
    font-weight: bold;
}

.text-danger {
    color: #ff4444 !important; /* 경고, 위험 색상 */
    font-weight: bold;
}

.text-warning {
    color: #fbbf24 !important; /* 주의, 알림 색상 */
}

.text-info {
    color: #60a5fa !important; /* 정보 색상 */
}

.text-success {
    color: #00ff88 !important; /* 성공, 긍정 색상 */
}

.text-item {
    color: #ffa500 !important; /* 아이템 관련 색상 */
}

.text-npc {
    color: #ff9900 !important; /* NPC 관련 색상 */
    font-weight: 500;
}

.text-player {
    color: #60a5fa !important; /* 플레이어 관련 색상 */
    font-weight: 500;
}

.text-monster {
    color: #ff5252 !important; /* 몬스터 관련 색상 */
    font-weight: 600;
}

.text-exit {
    color: #00ff00 !important; /* 출구 관련 색상 */
    font-weight: 600;
}
```

### 11.2 타이포그래피 클래스
```css
.font-terminal {
    font-family: 'IBM Plex Mono', monospace !important;
}

.font-normal {
    font-family: 'Inter', sans-serif !important;
}

.font-bold {
    font-weight: 600 !important;
}

.font-italic {
    font-style: italic !important;
}
```

### 11.3 마진 유틸리티 클래스
```css
/* 마진 유틸리티 클래스 */
.mt-1 {
    margin-top: 4px !important;
}

.mb-1 {
    margin-bottom: 4px !important;
}

.my-1 {
    margin-top: 4px !important;
    margin-bottom: 4px !important;
}

.mt-2 {
    margin-top: 8px !important;
}

.mb-2 {
    margin-bottom: 8px !important;
}

.my-2 {
    margin-top: 8px !important;
    margin-bottom: 8px !important;
}

.mt-3 {
    margin-top: 16px !important;
}

.mb-3 {
    margin-bottom: 16px !important;
}

.my-3 {
    margin-top: 16px !important;
    margin-bottom: 16px !important;
}
```

### 11.4 레이아웃 유틸리티 클래스
```css
/* 정렬 유틸리티 */
.list-unstyled {
    list-style-type: none !important;
    padding-left: 0 !important;
    margin: 0 !important;
}
```

### 11.5 효과 유틸리티 클래스
```css
/* 테두리와 그림자 효과 */
.text-shadow-sm {
    text-shadow: 0 0 3px currentColor !important;
}

.text-shadow-md {
    text-shadow: 0 0 5px currentColor !important;
}

.text-shadow-lg {
    text-shadow: 0 0 8px currentColor !important;
}
```

## 스타일 적용 지침

1. 모든 새 페이지는 터미널 스타일 디자인 언어를 따릅니다.
2. 특정 컴포넌트의 스타일보다는 추상화된 유틸리티 클래스 사용을 우선시합니다.
   예: `<span class="monster-name">` 대신 `<span class="text-monster">`
3. 레이아웃을 위해서는 마진 유틸리티 클래스 사용을 권장합니다.
   예: `margin-top: 10px` 대신 `class="mt-3"`
4. 타이포그래피는 지정된 폰트 클래스를 사용합니다.
   예: `font-family: 'IBM Plex Mono'` 대신 `class="font-terminal"`
5. 모든 게임 요소(방, NPC, 몬스터 등)는 정해진 색상 클래스를 따라 일관되게 표시합니다.
6. 모든 상호작용 요소는 명확한 호버 효과와 트랜지션을 가져야 합니다.
7. 스타일 적용 시 논리적인 순서로 클래스를 적용합니다:
   - 컴포넌트 클래스 (예: room-name)
   - 색상 클래스 (예: text-success)
   - 타이포그래피 (예: font-bold)
   - 마진 및 패딩 (예: mt-3)
   - 효과 (예: text-shadow-lg)

이 스타일 가이드는 MUD-MK2 프로젝트의 웹 인터페이스가 일관된 디자인과 사용자 경험을 제공할 수 있도록 돕기 위한 참고 자료입니다. 새로운 기능이나 페이지를 추가할 때 이 가이드라인을 따라 개발하여 시각적 일관성을 유지하세요.
