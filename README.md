# MUD Game - Multi-User Dungeon

이 프로젝트는 Spring Boot를 사용하여 구현된 MUD(Multi-User Dungeon) 게임입니다. 실시간 채팅, 지역 관리, Google OAuth2 로그인 기능을 제공합니다.

## 기능

### 인증 시스템
- Google 계정을 통한 로그인/인증
- 사용자 정보 저장 및 조회
- 세션 관리
- 권한 기반 접근 제어

### 게임 시스템
- 실시간 채팅 (WebSocket)
- 지역(Area) 관리 (생성, 조회, 수정, 삭제)
- 터미널 스타일의 사용자 인터페이스

## 기술 스택

- Spring Boot 3.4.5
- Spring Security (OAuth2)
- Spring Data JPA
- Spring WebSocket
- Thymeleaf
- H2 Database
- Lombok
- Bootstrap 5
- SockJS & STOMP

## 설정 방법

### 1. Google Cloud Console에서 OAuth 클라이언트 ID 생성

1. [Google Cloud Console](https://console.cloud.google.com/)에 접속합니다.
2. 새 프로젝트를 생성하거나 기존 프로젝트를 선택합니다.
3. 왼쪽 메뉴에서 "API 및 서비스" > "사용자 인증 정보"를 선택합니다.
4. "사용자 인증 정보 만들기" > "OAuth 클라이언트 ID"를 클릭합니다.
5. 애플리케이션 유형으로 "웹 애플리케이션"을 선택합니다.
6. 이름을 입력하고, 승인된 리디렉션 URI에 `http://localhost:8080/login/oauth2/code/google`을 추가합니다.
7. "만들기"를 클릭하여 클라이언트 ID와 비밀번호를 생성합니다.

### 2. application.properties 설정

생성된 클라이언트 ID와 비밀번호를 `application.properties` 파일에 설정합니다:

```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
```

## 프로젝트 구조

### 주요 패키지 및 클래스

#### 인증 관련
- `com.jefflife.mudmk2.config.auth`: 인증 관련 설정 및 서비스
  - `SecurityConfig`: Spring Security 설정
  - `CustomOAuth2UserService`: OAuth2 사용자 정보 처리
  - `LoginUser`: 세션 사용자 정보 주입을 위한 어노테이션
  - `LoginUserArgumentResolver`: 어노테이션 처리기

- `com.jefflife.mudmk2.config.auth.dto`: 인증 관련 DTO
  - `OAuthAttributes`: OAuth2 속성 정보
  - `SessionUser`: 세션에 저장할 사용자 정보

- `com.jefflife.mudmk2.user.domain`: 사용자 도메인
  - `User`: 사용자 엔티티
  - `Role`: 사용자 권한 enum
  - `UserRepository`: 사용자 저장소

#### 게임 관련
- `com.jefflife.mudmk2.gamedata.adapter.in`: 게임 컨트롤러
  - `AreaController`: 지역 관리 API 컨트롤러

- `com.jefflife.mudmk2.gamedata.application.domain.model.map`: 게임 맵 도메인 모델
  - `Area`: 지역 엔티티
  - `AreaType`: 지역 타입 enum
  - `Room`: 방 엔티티
  - `Door`: 문 엔티티
  - `WayOut`: 출구 엔티티

- `com.jefflife.mudmk2.gamedata.application.service`: 게임 서비스
  - `AreaService`: 지역 관리 서비스

#### 채팅 관련
- `com.jefflife.mudmk2.chat.controller`: 채팅 컨트롤러
  - `ChatController`: WebSocket 채팅 컨트롤러

- `com.jefflife.mudmk2.chat.model`: 채팅 모델
  - `ChatMessage`: 채팅 메시지 모델

#### 웹 인터페이스
- `com.jefflife.mudmk2.web`: 웹 컨트롤러
  - `IndexController`: 메인 페이지 컨트롤러 (로그인, 채팅, 프로필, 지역 관리 페이지)

## 실행 방법

1. 프로젝트를 클론합니다.
2. Google OAuth 클라이언트 ID와 비밀번호를 `application.properties`에 설정합니다.
3. 프로젝트를 빌드하고 실행합니다:
   ```bash
   ./gradlew bootRun
   ```
4. 웹 브라우저에서 `http://localhost:8080`에 접속합니다.
5. Google 계정으로 로그인합니다.

## 사용 방법

### 채팅 시스템
- 로그인 후 자동으로 채팅 페이지로 이동합니다.
- 명령어를 입력하고 "Execute" 버튼을 클릭하거나 Enter 키를 누릅니다.
- 다른 사용자와 실시간으로 채팅할 수 있습니다.

### 지역 관리
- 채팅 화면에서 "AREA MANAGEMENT" 버튼을 클릭합니다.
- 지역 생성 폼에서 이름과 타입을 입력하여 새 지역을 생성합니다.
- 기존 지역을 수정하거나 삭제할 수 있습니다.

### 프로필 관리
- "PROFILE" 버튼을 클릭하여 사용자 프로필을 확인할 수 있습니다.

## 주의사항

- 실제 운영 환경에서는 클라이언트 ID와 비밀번호를 환경 변수나 외부 설정으로 관리하는 것이 좋습니다.
- 데이터베이스 설정을 실제 운영 환경에 맞게 변경해야 합니다.
- 이 프로젝트는 개발 및 학습 목적으로 만들어졌습니다.
