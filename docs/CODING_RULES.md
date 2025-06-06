# MUD-MK2 코딩 룰

이 문서는 MUD-MK2 프로젝트의 일관된 코드 품질과 스타일을 유지하기 위한 코딩 룰을 정의합니다.

## 1. 프로젝트 구조

### 1.1 패키지 구조
프로젝트는 육각형 아키텍처(Hexagonal Architecture)를 따르며 다음과 같은 패키지 구조를 사용합니다:

```
com.jefflife.mudmk2
├── config               # 전역 설정 클래스
│   └── auth             # 인증 관련 설정
├── gamedata             # 게임 데이터 관련 기능
│   ├── adapter          # 어댑터 계층
│   │   ├── in           # 입력 어댑터 (컨트롤러 등)
│   │   └── out          # 출력 어댑터 (저장소 구현체 등)
│   ├── application      # 응용 계층
│   │   ├── domain       # 도메인 모델
│   │   │   └── model    # 도메인 엔티티 및 값 객체
│   │   ├── port         # 포트 계층
│   │   │   ├── in       # 입력 포트 (유스케이스)
│   │   │   └── out      # 출력 포트 (저장소 인터페이스)
│   │   └── service      # 비즈니스 로직 구현체
│   │       ├── model    # 서비스 모델 (DTO)
│   │       │   ├── request   # 요청 DTO
│   │       │   └── response  # 응답 DTO
├── gameplay             # 게임 플레이 관련 기능
├── user                 # 사용자 관련 기능
```

### 1.2 레이어 구조
코드는 다음 레이어 구조를 따릅니다:
1. **어댑터(Adapter)**: 외부 시스템과의 통신을 처리 (컨트롤러, 리포지토리 구현체)
2. **포트(Port)**: 인터페이스 (유스케이스, 리포지토리 인터페이스)
3. **응용 계층(Application)**: 비즈니스 로직과 도메인 모델

### 1.3 의존성 방향
의존성은 항상 안쪽 레이어를 향해야 합니다:
- 어댑터 → 포트 → 응용 계층 → 도메인 모델
- 바깥 레이어에서 안쪽 레이어를 참조할 수 있지만, 그 반대는 불가능합니다.

## 2. 명명 규칙

### 2.1 일반 규칙
- 모든 이름은 설명적이고 의도를 명확하게 전달해야 합니다.
- 약어는 가급적 사용하지 않습니다. 단, 널리 알려진 약어(HTTP, URL 등)는 예외적으로 사용 가능합니다.

### 2.2 클래스 명명 규칙
- **컨트롤러**: `[도메인명]Controller` (예: `AreaController`, `RoomController`)
- **서비스**: `[도메인명]Service` (예: `AreaService`, `RoomService`)
- **리포지토리**: `[도메인명]Repository` (예: `AreaRepository`, `RoomRepository`)
- **도메인 모델**: 단순 명사 (예: `Area`, `Room`, `NPC`)
- **UseCase 인터페이스**: 동사+명사 형태 (예: `CreateAreaUseCase`, `GetRoomUseCase`)

### 2.3 메소드 명명 규칙
- **조회 메소드**: `get[엔티티]`, `find[엔티티]` (예: `getArea`, `findAllRooms`)
- **생성 메소드**: `create[엔티티]` (예: `createRoom`, `createNPC`)
- **수정 메소드**: `update[엔티티]` (예: `updateArea`, `updateRoom`)
- **삭제 메소드**: `delete[엔티티]` (예: `deleteNPC`, `deleteMonsterType`)

### 2.4 변수 명명 규칙
- **클래스 멤버 변수**: 카멜 케이스, 명확한 명사 사용 (예: `private final AreaRepository areaRepository;`)
- **메소드 매개변수**: 카멜 케이스 (예: `createArea(CreateAreaRequest createAreaRequest)`)
- **지역 변수**: 카멜 케이스, 간결하지만 의미를 명확하게 (예: `Area area = new Area();`)

### 2.5 패키지 명명 규칙
- 패키지 이름은 모두 소문자로 작성합니다.
- 단일 단어를 사용합니다. (예: `model`, `service`, `controller`)

## 3. 코드 스타일

### 3.1 일반 스타일
- 들여쓰기는 4칸 공백을 사용합니다.
- 한 줄의 최대 길이는 120자로 제한합니다.
- 클래스, 메소드, 블록 사이에는 빈 줄을 사용하여 가독성을 높입니다.

### 3.2 클래스 구조
클래스는 다음 순서대로 구성합니다:

1. 상수
2. 필드(멤버 변수)
3. 생성자
4. 공개 메소드
5. 비공개 메소드
6. 내부 클래스/인터페이스

### 3.3 주석 규칙
- 주석은 '무엇'이 아니라 '왜'에 초점을 맞춥니다.
- 모든 공개 API에는 JavaDoc 주석을 작성합니다.
- TODO 주석은 임시적으로만 사용하고, 가능한 빨리 해결합니다.

### 3.4 예외 처리
- 확인된 예외(checked exception)보다 런타임 예외를 선호합니다.
- 세부적인 예외 클래스를 사용하여 예외 원인을 명확히 합니다.
- 비즈니스 로직에서의 예외는 커스텀 예외 클래스를 생성하여 처리합니다.
- 예외는 반드시 로그를 남기거나 상위 레이어로 전파합니다.

## 4. 아키텍처 규칙

### 4.1 컨트롤러
- 컨트롤러는 입력 검증과 HTTP 관련 로직만 처리합니다.
- 모든 비즈니스 로직은 서비스 계층에 위임합니다.
- ResponseEntity를 사용하여 HTTP 응답을 일관되게 처리합니다.

### 4.2 서비스
- 트랜잭션 경계는 서비스 계층에서 관리합니다.
- 서비스는 여러 리포지토리를 조합하여 비즈니스 로직을 수행합니다.
- 서비스 간의 직접적인 호출 대신 이벤트를 통한 통신을 선호합니다.
- 서비스 메소드는 도메인 엔티티가 아닌 Response DTO를 반환해야 합니다.
- 도메인 엔티티를 직접 외부로 노출하지 않고, 항상 Response DTO로 변환하여 반환합니다.
- Entity와 Response DTO 간의 변환은 서비스 계층 내에서 처리합니다.

### 4.3 리포지토리
- JPA 네이티브 쿼리는 필요한 경우에만 제한적으로 사용합니다.
- 리포지토리는 직접적인 비즈니스 로직을 포함하지 않습니다.
- 복잡한 쿼리는 별도의 메소드로 분리하고 명확한 이름을 부여합니다.

### 4.4 도메인 모델
- 도메인 엔티티는 가능한 불변(immutable)으로 설계합니다.
- JPA 관련 코드는 도메인 모델에 최소한으로 노출합니다.
- 도메인 로직은 가능한 도메인 모델 내에 캡슐화합니다.

## 5. DTO 규칙

### 5.1 일반 규칙
- DTO는 서로 다른 레이어 간의 데이터 전송에만 사용합니다.
- DTO는 비즈니스 로직을 포함하지 않습니다.
- DTO와 도메인 모델 간의 변환은 서비스 계층에서 처리합니다.

### 5.2 Request/Response DTO
- **Request DTO**: `[동작][도메인명]Request` (예: `CreateAreaRequest`, `UpdateRoomRequest`)
- **Response DTO**: `[도메인명]Response` (예: `AreaResponse`, `RoomResponse`)

### 5.3 Record 사용
- 단순 데이터 전달 목적의 DTO는 Java의 Record 타입을 활용합니다.
- DTO 내부에 복잡한 비즈니스 로직이 필요한 경우 일반 클래스를 사용합니다.

## 6. 테스트 규칙

### 6.1 단위 테스트
- 각 클래스는 해당 단위 테스트를 가져야 합니다.
- 테스트 클래스명은 `[테스트대상클래스]Test` 형식을 따릅니다.
- 테스트 메소드명은 `[테스트상황]_[예상결과]` 형식을 따릅니다.

### 6.2 통합 테스트
- 통합 테스트 클래스명은 `[테스트대상]IntegrationTest` 형식을 따릅니다.
- DB 통합 테스트는 인메모리 데이터베이스를 사용합니다.
- 외부 시스템에 대한 테스트는 모의 객체(Mock)를 사용합니다.

### 6.3 시스템 테스트
- 시스템 테스트 클래스명은 `[테스트대상]SystemTest` 형식을 따릅니다.
- API 테스트는 MockMvc 또는 TestRestTemplate을 사용합니다.

## 7. 로깅 규칙

### 7.1 로그 레벨
- **ERROR**: 시스템 오류 또는 예상치 못한 예외
- **WARN**: 잠재적인 문제 또는 중요한 이슈
- **INFO**: 중요한 비즈니스 이벤트
- **DEBUG**: 개발 및 디버깅에 유용한 상세 정보
- **TRACE**: 매우 상세한 진단 정보

### 7.2 로깅 내용
- 사용자 개인정보 및 민감한 정보는 로그에 기록하지 않습니다.
- 예외 로그는 스택 트레이스를 포함해야 합니다.
- 로그 메시지는 명확하고 정확해야 하며, 해결 방법 또는 다음 단계를 제안해야 합니다.

## 8. 보안 규칙

### 8.1 입력 검증
- 모든 사용자 입력은 검증 후 사용합니다.
- Jakarta Bean Validation을 사용하여 입력 데이터를 검증합니다.
- SQL 인젝션, XSS 등 보안 취약점에 주의합니다.

### 8.2 인증 및 권한
- 민감한 API는 적절한 인증 및 권한 확인 후 접근 가능하도록 합니다.
- JWT 토큰이나 세션 기반의 인증을 일관되게 적용합니다.
- Spring Security의 권한 체크 기능을 활용합니다.

## 9. 코드 품질

### 9.1 정적 분석
- SonarQube 또는 SpotBugs를 사용하여 코드 품질을 검사합니다.
- 모든 코드는 정적 분석 도구의 경고가 없어야 합니다.

### 9.2 코드 리뷰
- 모든 코드는 Pull Request를 통해 최소 한 명 이상의 리뷰를 받아야 합니다.
- 코드 리뷰는 기능적 측면뿐 아니라 이 가이드라인 준수 여부도 확인합니다.

### 9.3 테스트 커버리지
- 전체 코드의 테스트 커버리지는 최소 70% 이상을 유지합니다.
- 핵심 비즈니스 로직은 90% 이상의 테스트 커버리지를 목표로 합니다.

## 10. 버전 관리

### 10.1 Git 커밋 메시지
- 커밋 메시지는 다음 형식을 따릅니다: `[유형]: [제목]`
- 유형: feat(새 기능), fix(버그 수정), docs(문서), style(포맷), refactor(리팩토링), test(테스트), chore(기타)
- 커밋 본문에는 변경 이유와 영향을 자세히 설명합니다.

### 10.2 브랜치 전략
- `main`: 최신 안정 버전
- `develop`: 개발 중인 다음 버전
- `feature/<기능명>`: 새로운 기능 개발
- `bugfix/<버그명>`: 버그 수정
- `release/<버전>`: 릴리스 준비
- `hotfix/<버그명>`: 긴급 버그 수정

## 마무리

이 코딩 룰은 프로젝트가 진행됨에 따라 팀원 간의 합의를 통해 지속적으로 개선되어야 합니다. 모든 개발자는 이 가이드라인을 숙지하고 따르되, 필요한 경우 개선 제안을 할 수 있습니다.
