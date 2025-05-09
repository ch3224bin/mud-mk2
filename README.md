# Google OAuth2 Login Implementation

이 프로젝트는 Spring Boot를 사용하여 Google OAuth2 로그인 기능을 구현한 예제입니다.

## 기능

- Google 계정을 통한 로그인/인증
- 사용자 정보 저장 및 조회
- 세션 관리
- 권한 기반 접근 제어

## 기술 스택

- Spring Boot 3.4.5
- Spring Security
- Spring Data JPA
- Thymeleaf
- H2 Database
- Lombok

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

- `com.jefflife.mudmk2.web`: 웹 컨트롤러
  - `IndexController`: 메인 페이지 컨트롤러

## 실행 방법

1. 프로젝트를 클론합니다.
2. Google OAuth 클라이언트 ID와 비밀번호를 `application.properties`에 설정합니다.
3. 프로젝트를 빌드하고 실행합니다:
   ```bash
   ./gradlew bootRun
   ```
4. 웹 브라우저에서 `http://localhost:8080`에 접속합니다.

## 주의사항

- 실제 운영 환경에서는 클라이언트 ID와 비밀번호를 환경 변수나 외부 설정으로 관리하는 것이 좋습니다.
- 데이터베이스 설정을 실제 운영 환경에 맞게 변경해야 합니다.