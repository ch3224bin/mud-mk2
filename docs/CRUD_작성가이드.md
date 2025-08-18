# CRUD 작성 가이드

이 문서는 MUD MK2 프로젝트에서 새로운 도메인의 CRUD 기능을 구현하는 방법을 설명합니다.

## 프로젝트 아키텍처 개요

이 프로젝트는 **헥사고날 아키텍처(포트&어댑터 패턴)**를 기반으로 구성되어 있습니다.

### 레이어 구조
```
adapter.webapi        - REST API 컨트롤러
application
  ├── domain.model    - 도메인 엔티티 및 값 객체
  ├── service         - 비즈니스 로직 구현
  │   ├── provided    - 외부에서 사용할 수 있는 서비스 인터페이스 (포트)
  │   └── required    - 외부 의존성 인터페이스 (포트)
```

## Area 도메인 분석

현재 구현된 Area 도메인을 통해 패턴을 이해해보겠습니다.

### 1. 도메인 모델 (Domain Model)

#### 엔티티 (Entity)
```java
// Area.java - 메인 도메인 엔티티
@Entity
public class Area {
    @Id @GeneratedValue
    private Long id;
    
    @Embedded
    private AreaName name;  // Value Object 사용
    
    @Enumerated(EnumType.STRING)
    private AreaType type;
    
    // 정적 팩토리 메서드
    public static Area create(AreaCreateRequest request) { ... }
    
    // 도메인 로직
    public void changeName(AreaModifyRequest request) { ... }
}
```

#### 값 객체 (Value Object)
```java
// AreaName.java - 이름을 캡슐화한 값 객체
@Embeddable
public class AreaName {
    private static final String VALID_AREA_NAME_PATTERN = "^[a-zA-Z0-9가-힣]+$";
    
    @Column(nullable = false)
    private String name;
    
    public static AreaName of(String name) {
        validateAreaName(name);  // 검증 로직 포함
        // ...
    }
    
    private static void validateAreaName(String name) {
        // 비즈니스 규칙 검증
    }
}
```

#### 요청 DTO
```java
// AreaCreateRequest.java - 생성 요청
public record AreaCreateRequest(
    @NotBlank @Size(min = 1, max = 255) String name,
    @NotNull AreaType type
) {}

// AreaModifyRequest.java - 수정 요청  
public record AreaModifyRequest(
    @NotBlank @Size(min = 1, max = 255) String name
) {}
```

### 2. 서비스 레이어 (Service Layer)

#### Provided 포트 (서비스 인터페이스)
```java
// 각 CRUD 작업별로 인터페이스 분리 (ISP 원칙)
public interface AreaCreator {
    Area createArea(@Valid AreaCreateRequest request);
}

public interface AreaFinder {
    Area getArea(Long id);
    List<Area> getAreas();
}

public interface AreaModifier {
    Area updateArea(Long id, @Valid AreaModifyRequest request);
}

public interface AreaRemover {
    void deleteArea(Long id);
}
```

#### 서비스 구현체
```java
// AreaService.java - 모든 인터페이스를 구현
@Service
public class AreaService implements AreaCreator, AreaModifier, AreaFinder, AreaRemover {
    private final AreaRepository areaRepository;
    
    @Override
    public Area createArea(AreaCreateRequest request) {
        return areaRepository.save(Area.create(request));
    }
    
    // 나머지 CRUD 메서드들...
}
```

#### Required 포트 (리포지토리 인터페이스)
```java
// AreaRepository.java - 외부 의존성 인터페이스
public interface AreaRepository extends CrudRepository<Area, Long> {
}
```

### 3. 웹 어댑터 (Web Adapter)

#### 컨트롤러
```java
@RestController
@RequestMapping(AreaController.BASE_PATH)
public class AreaController {
    public static final String BASE_PATH = "/api/v1/areas";
    
    // 필요한 서비스만 의존성 주입
    private final AreaCreator areaCreator;
    private final AreaModifier areaModifier;
    private final AreaFinder areaFinder;
    private final AreaRemover areaRemover;
    
    @PostMapping
    public ResponseEntity<AreaResponse> createArea(@RequestBody AreaCreateRequest request) {
        Area area = areaCreator.createArea(request);
        return ResponseEntity.created(/* ... */).body(AreaResponse.of(area));
    }
    
    // 나머지 엔드포인트들...
}
```

#### 응답 DTO
```java
// AreaResponse.java - API 응답용 DTO
@Getter
public class AreaResponse {
    private final Long id;
    private final String name;
    private final AreaType type;
    
    public static AreaResponse of(Area area) {
        return new AreaResponse(area.getId(), area.getName(), area.getType());
    }
}
```

## 새로운 도메인 CRUD 작성 단계

### 1단계: 도메인 모델 작성

#### 1.1 패키지 구조 생성
```
com.jefflife.mudmk2.gamedata.application.domain.model.{domain}
```

#### 1.2 메인 엔티티 작성
```java
@Entity
@Getter @EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YourEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 비즈니스 필드들...
    
    // 정적 팩토리 메서드
    public static YourEntity create(YourCreateRequest request) {
        // 생성 로직
    }
    
    // 도메인 메서드들
    public void updateSomething(YourModifyRequest request) {
        // 수정 로직
    }
}
```

#### 1.3 값 객체 작성 (필요한 경우)
```java
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class YourValueObject {
    private static final String VALIDATION_PATTERN = "...";
    
    @Column(nullable = false)
    private String value;
    
    public static YourValueObject of(String value) {
        validateValue(value);
        // 생성 로직
    }
    
    private static void validateValue(String value) {
        // 검증 로직
        if (/* 조건 */) {
            throw new InvalidYourValueObjectException("...");
        }
    }
}
```

#### 1.4 요청/응답 DTO 작성
```java
// 생성 요청
public record YourCreateRequest(
    @NotBlank @Size(min = 1, max = 255) String field1,
    @NotNull YourEnum field2
) {}

// 수정 요청
public record YourModifyRequest(
    @NotBlank @Size(min = 1, max = 255) String field1
) {}
```

#### 1.5 Custom Exception 작성 (필요한 경우)
```java
public class InvalidYourValueObjectException extends RuntimeException {
    public InvalidYourValueObjectException(String message) {
        super(message);
    }
}
```

### 2단계: 서비스 레이어 작성

#### 2.1 Provided 포트 인터페이스 작성
```java
// com.jefflife.mudmk2.gamedata.application.service.provided 패키지

public interface YourEntityCreator {
    YourEntity createEntity(@Valid YourCreateRequest request);
}

public interface YourEntityFinder {
    YourEntity getEntity(Long id);
    List<YourEntity> getEntities();
}

public interface YourEntityModifier {
    YourEntity updateEntity(Long id, @Valid YourModifyRequest request);
}

public interface YourEntityRemover {
    void deleteEntity(Long id);
}
```

#### 2.2 Required 포트 인터페이스 작성
```java
// com.jefflife.mudmk2.gamedata.application.service.required 패키지

public interface YourEntityRepository extends CrudRepository<YourEntity, Long> {
    // 필요한 경우 추가 메서드 정의
}
```

#### 2.3 서비스 구현체 작성
```java
// com.jefflife.mudmk2.gamedata.application.service 패키지

@Validated
@Service
public class YourEntityService implements 
    YourEntityCreator, YourEntityFinder, YourEntityModifier, YourEntityRemover {
    
    private final YourEntityRepository repository;
    
    public YourEntityService(YourEntityRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public YourEntity createEntity(YourCreateRequest request) {
        return repository.save(YourEntity.create(request));
    }
    
    @Override
    public YourEntity getEntity(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Entity not found"));
    }
    
    @Override
    public List<YourEntity> getEntities() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
            .toList();
    }
    
    @Override
    public YourEntity updateEntity(Long id, YourModifyRequest request) {
        YourEntity entity = getEntity(id);
        entity.updateSomething(request);
        return repository.save(entity);
    }
    
    @Override
    public void deleteEntity(Long id) {
        YourEntity entity = getEntity(id);
        repository.delete(entity);
    }
}
```

### 3단계: 웹 어댑터 작성

#### 3.1 응답 DTO 작성
```java
// com.jefflife.mudmk2.gamedata.adapter.webapi.response 패키지

@Getter
public class YourEntityResponse {
    private final Long id;
    private final String field1;
    private final YourEnum field2;
    
    private YourEntityResponse(Long id, String field1, YourEnum field2) {
        this.id = id;
        this.field1 = field1;
        this.field2 = field2;
    }
    
    public static YourEntityResponse of(YourEntity entity) {
        return new YourEntityResponse(
            entity.getId(), 
            entity.getField1(), 
            entity.getField2()
        );
    }
}
```

#### 3.2 컨트롤러 작성
```java
// com.jefflife.mudmk2.gamedata.adapter.webapi 패키지

@RestController
@RequestMapping(YourEntityController.BASE_PATH)
public class YourEntityController {
    public static final String BASE_PATH = "/api/v1/your-entities";
    
    private final YourEntityCreator creator;
    private final YourEntityModifier modifier;
    private final YourEntityFinder finder;
    private final YourEntityRemover remover;
    
    public YourEntityController(
        YourEntityCreator creator,
        YourEntityModifier modifier,
        YourEntityFinder finder,
        YourEntityRemover remover
    ) {
        this.creator = creator;
        this.modifier = modifier;
        this.finder = finder;
        this.remover = remover;
    }
    
    @PostMapping
    public ResponseEntity<YourEntityResponse> create(@RequestBody YourCreateRequest request) {
        YourEntity entity = creator.createEntity(request);
        YourEntityResponse response = YourEntityResponse.of(entity);
        return ResponseEntity
            .created(URI.create(String.format("%s/%s", BASE_PATH, response.getId())))
            .body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<YourEntityResponse> get(@PathVariable Long id) {
        YourEntity entity = finder.getEntity(id);
        return ResponseEntity.ok(YourEntityResponse.of(entity));
    }
    
    @GetMapping
    public ResponseEntity<List<YourEntityResponse>> getAll() {
        List<YourEntityResponse> responses = finder.getEntities()
            .stream()
            .map(YourEntityResponse::of)
            .toList();
        return ResponseEntity.ok(responses);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<YourEntityResponse> update(
        @PathVariable Long id,
        @RequestBody YourModifyRequest request
    ) {
        YourEntity entity = modifier.updateEntity(id, request);
        return ResponseEntity.ok(YourEntityResponse.of(entity));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        remover.deleteEntity(id);
        return ResponseEntity.noContent().build();
    }
}
```

## 핵심 원칙

### 1. 단일 책임 원칙 (SRP)
- 각 인터페이스는 하나의 책임만 가집니다 (Create, Read, Update, Delete)
- 각 클래스는 명확한 단일 책임을 가집니다

### 2. 인터페이스 분리 원칙 (ISP)
- CRUD 작업을 별도의 인터페이스로 분리
- 컨트롤러는 필요한 인터페이스만 의존

### 3. 의존성 역전 원칙 (DIP)
- 서비스는 Repository 인터페이스에 의존
- 컨트롤러는 서비스 인터페이스에 의존

### 4. 도메인 중심 설계
- 비즈니스 로직은 도메인 엔티티에 위치
- 값 객체를 통한 데이터 무결성 보장
- 정적 팩토리 메서드 사용

## 체크리스트

### 도메인 모델
- [ ] 메인 엔티티 클래스 작성
- [ ] 값 객체 작성 (필요시)
- [ ] 요청 DTO 작성 (Create/Modify)
- [ ] Custom Exception 작성 (필요시)
- [ ] 검증 로직 구현

### 서비스 레이어
- [ ] Provided 포트 인터페이스 4개 작성 (Creator, Finder, Modifier, Remover)
- [ ] Required 포트 인터페이스 작성 (Repository)
- [ ] 서비스 구현체 작성
- [ ] @Validated 어노테이션 추가

### 웹 어댑터
- [ ] 응답 DTO 작성
- [ ] 컨트롤러 작성
- [ ] 모든 CRUD 엔드포인트 구현
- [ ] 적절한 HTTP 상태 코드 반환

### 테스트 (권장)
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] API 테스트 작성

이 가이드를 따라 구현하면 일관된 아키텍처와 코드 스타일을 유지할 수 있습니다.