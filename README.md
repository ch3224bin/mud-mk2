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
- 방(Room) 관리 (생성, 조회, 수정, 삭제)
- 방 연결 및 문(Door) 관리
- 방 지도 시각화
- 방 정보 표시 시스템
  - 방 내의 크리쳐 상태 표시 기능
  - 크리쳐 상태에 따른 다양한 메시지 표시 (서있음, 죽어가고 있음, 전투중, 사망)
  - 템플릿 기반 정보 표시 및 유지보수성 향상
- NPC(Non-Player Character) 관리 (생성, 조회, 수정, 삭제)
- 몬스터 타입 관리 (생성, 조회, 수정, 삭제)
  - 기본 스탯 설정 (HP, MP, STR, DEX 등)
  - 레벨당 스탯 증가량 설정
  - 스폰 룸 및 스폰 정보 관리
  - 공격성 및 리스폰 시간 설정
- 캐릭터 클래스 관리 (생성, 조회, 수정, 삭제)
  - 기본 스탯 설정 (HP, MP, STR, DEX, CON, INT, POW, CHA)
  - 클래스별 특성 정보 관리
  - 기본 클래스 초기화 기능
- 인스턴스 시나리오 관리 (생성, 조회, 수정, 삭제)
  - 시나리오 상태관리
  - 퀘스트 정보 설정
  - 구역 연결 설정
- 명령어 처리 시스템 (Chain of Responsibility 패턴)
  - 이동 명령어 파싱 및 실행
  - 대화 명령어 파싱 및 실행
  - 방향 기반 탐색 명령어 ("동", "서", "남", "북" 방향에 있는 장소 보기)
  - 잘못된 명령어 처리 및 사용자 알림
- 캐릭터 시스템
  - 플레이어 캐릭터 생성 및 관리
  - 캐릭터 클래스별 능력치 차등화
  - 캐릭터 정보 조회
  - 크리쳐(Creature) 통합 시스템
    - PC, NPC, Monster를 통틀어 크리쳐라는 개념으로 통합
    - BaseCharacter 클래스를 통한 공통 속성 및 기능 통합 관리
    - 모든 크리쳐에 상태(State) 시스템 구현 (일반, 전투중, 사망 등)
    - 크리쳐의 체력 상태에 따른 표시 방식 차별화 (낮은 체력 상태 표시)
    - Combatable 인터페이스를 통한 전투 참여 객체 추상화
- 전투 시스템
  - 턴 기반 전투 메커니즘
  - 이니셔티브 시스템을 통한 행동 순서 결정
  - 명중 판정 (1d20 + 공격자 명중 보너스 vs 1d20 + 방어자 회피 보너스)
  - 데미지 계산 (무기 데미지 + STR 보정값 + 기타 보너스)
  - 방어력 적용 (기본 AC + 방어구 AC + DEX 보정값)
  - 관통력 시스템 (방어력 - 관통력 = 유효 방어력)
  - 크리티컬 히트 및 실패 시스템
  - 전투 로그 기록 및 표시
  - 파티 단위 전투 지원
  - 전투 참여자 관리
    - CombatParticipant를 통한 전투 참여자 추적 및 상태 관리
    - 어그로(Aggro) 시스템 구현으로 몬스터 타겟팅 메커니즘 개선
    - 전투 상태 표시 기능 (누구와 싸우고 있는지 표시)
- 게임 환경 시스템
  - 게임 내 시간 관리
  - 날씨 시스템
- 이벤트 시스템
  - 캐릭터 생성 이벤트 발행 및 구독
  - 게임 상태 변화에 따른 이벤트 처리
- 인메모리 게임 월드 관리
  - 활성 플레이어 추적 및 관리
  - 룸 정보 캐싱 및 빠른 접근
- 한국어 명령어 지원 (이동, 대화 등)
  - 방향 명령어: "동", "서", "남", "북" 또는 단축키 "ㄷ", "ㅅ", "ㄴ", "ㅂ"
  - 방향 탐색: "동 봐" 또는 "ㄷ 봐"으로 해당 방향에 있는 장소 확인 가능
- 터미널 스타일의 사용자 인터페이스

## 기술 스택

- Spring Boot 3.4.5 (실제 버전은 다를 수 있음)
- Spring Security (OAuth2)
- Spring Data JPA
- Spring WebSocket
- Thymeleaf
- H2 Database / MySQL
- Lombok
- Bootstrap 5
- SockJS & STOMP
- Docker & Docker Compose (MySQL 환경 설정)

## 설계 패턴

이 프로젝트에서 사용된 주요 설계 패턴:

- **Chain of Responsibility**: 명령어 파싱 및 실행 체인 구현
- **Observer 패턴**: Spring의 이벤트 발행/구독 시스템 활용
- **Repository 패턴**: 데이터 접근 계층 추상화
- **DTO(Data Transfer Object)**: 계층 간 데이터 전달
- **DDD(Domain-Driven Design)**: 명확한 도메인 모델과 계층 구분
- **포트와 어댑터 아키텍처**: 외부 시스템과의 통합 추상화
- **의존성 주입**: Spring의 DI 컨테이너 활용

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

### 3. 데이터베이스 설정

Docker Compose를 사용하여 MySQL 데이터베이스를 실행할 수 있습니다:

```bash
docker-compose up -d
```

또는 H2 인메모리 데이터베이스를 사용할 수 있도록 `application.properties`를 설정할 수 있습니다:

```properties
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

## 관리자 화면

### 기능 개요
MUD 게임에서는 관리자 화면을 통해 게임 세계를 구성하는 여러 요소들을 쉽게 관리할 수 있습니다:

1. **영역 관리 (Area Management)**
   - 게임 세계의 다양한 영역을 생성, 조회, 수정 및 삭제
   - 각 영역에 대한 자세한 설명 제공

2. **방 관리 (Room Management)**
   - 각 영역 내에 방을 생성하고 관리
   - 방끼리의 연결을 통한 이동 경로 설정
   - 방 정보 상세 설정 (설명, 특성 등)

3. **방 지도 (Room Map)**
   - 시각적인 방 지도 제공
   - 전체 게임 세계의 연결 상태 확인

4. **NPC 관리 (NPC Management)**
   - 비플레이어 캐릭터 생성 및 관리
   - NPC 위치, 대화, 행동 설정

5. **몬스터 타입 관리 (Monster Type Management)**
   - 다양한 몬스터 타입 생성 및 관리
   - 몬스터 기본 스탯 및 레벨별 성장 설정
   - 스폰 정보 및 공격성 설정

6. **인스턴스 시나리오 (Instance Scenarios)**
   - 특별 인스턴스 및 시나리오 관리
   - 퀘스트 정보 및 구역 연결 설정

7. **전투 시스템 관리 (Combat System Management)**
   - 전투 규칙 설정 및 관리
   - 무기 데미지 및 방어구 효과 설정
   - 전투 로그 확인 및 분석

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

#### 게임 데이터 관련
- `com.jefflife.mudmk2.gamedata.application`: 게임 데이터 비즈니스 로직
  - `domain.model`: 게임 도메인 모델 (Area, Room, NPC, MonsterType 등)
  - `service.provided`: 입력 포트 인터페이스 (UseCase)
  - `service.required`: 출력 포트 인터페이스 (Repository)
  - `service`: 비즈니스 서비스 구현체

- `com.jefflife.mudmk2.gamedata.adapter`: 포트 어댑터
  - `in`: 입력 어댑터 (Controller)
  - `out`: 출력 어댑터 (Repository 구현체)

#### 게임 시스템 관련
- `com.jefflife.mudmk2.gameplay`: 게임 플레이 관련 컴포넌트
  - `application`: 게임 플레이 비즈니스 로직
    - `domain.model.combat`: 전투 시스템 도메인 모델 (Combat, CombatGroup, CombatParticipant 등)
    - `service`: 게임 서비스 구현체 (CombatService 등)
    - `port.in/out`: 전투 시스템 포트 인터페이스
  - `adapter`: 전투 메시지 및 이벤트 어댑터
  - `time`: 게임 내 시간 관리
  - `weather`: 날씨 시스템

## 기여 방법

1. 이 저장소를 포크합니다.
2. 새 브랜치를 생성합니다 (`git checkout -b feature/amazing-feature`)
3. 변경사항을 커밋합니다 (`git commit -m 'Add some amazing feature'`)
4. 브랜치에 푸시합니다 (`git push origin feature/amazing-feature`)
5. Pull Request를 제출합니다.
