package com.jefflife.mudmk2.gamedata.adapter.webapi;

import com.jefflife.mudmk2.gamedata.application.service.CharacterClassService;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateCharacterClassRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateCharacterClassRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.CharacterClassResponse;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * 캐릭터 직업 관련 RESTful API 컨트롤러
 */
@RestController
@RequestMapping("/api/character-classes")
@RequiredArgsConstructor
public class CharacterClassController {

    private final CharacterClassCreator characterClassCreator;
    private final CharacterClassFinder characterClassFinder;
    private final CharacterClassesRetriever characterClassesRetriever;
    private final CharacterClassModifier characterClassModifier;
    private final CharacterClassRemover characterClassRemover;
    private final CharacterClassService characterClassService; // 추가 기능을 위해 서비스 직접 주입

    /**
     * 새로운 캐릭터 직업을 생성합니다.
     *
     * @param request 생성할 캐릭터 직업 정보
     * @return 생성된 캐릭터 직업 정보와 API 응답
     */
    @PostMapping
    public ResponseEntity<CharacterClassResponse> createCharacterClass(
            @Valid @RequestBody CreateCharacterClassRequest request) {
        CharacterClassResponse response = characterClassCreator.createCharacterClass(request);
        return ResponseEntity
                .created(URI.create(String.format("/api/character-classes/%s", response.getId())))
                .body(response);
    }

    /**
     * 모든 캐릭터 직업을 조회합니다.
     *
     * @return 모든 캐릭터 직업 목록과 API 응답
     */
    @GetMapping
    public ResponseEntity<List<CharacterClassResponse>> getAllCharacterClasses() {
        List<CharacterClassResponse> responses = characterClassesRetriever.getAllCharacterClasses();
        return ResponseEntity.ok(responses);
    }

    /**
     * ID로 특정 캐릭터 직업을 조회합니다.
     *
     * @param id 조회할 캐릭터 직업 ID
     * @return 조회된 캐릭터 직업 정보와 API 응답
     */
    @GetMapping("/{id}")
    public ResponseEntity<CharacterClassResponse> getCharacterClassById(@PathVariable Long id) {
        CharacterClassResponse response = characterClassFinder.getCharacterClassById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 코드로 특정 캐릭터 직업을 조회합니다.
     *
     * @param code 조회할 캐릭터 직업 코드
     * @return 조회된 캐릭터 직업 정보와 API 응답
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<CharacterClassResponse> getCharacterClassByCode(@PathVariable String code) {
        CharacterClassResponse response = characterClassFinder.getCharacterClassByCode(code);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 캐릭터 직업 정보를 업데이트합니다.
     *
     * @param id      업데이트할 캐릭터 직업 ID
     * @param request 업데이트할 캐릭터 직업 정보
     * @return 업데이트된 캐릭터 직업 정보와 API 응답
     */
    @PutMapping("/{id}")
    public ResponseEntity<CharacterClassResponse> updateCharacterClass(
            @PathVariable Long id, @Valid @RequestBody UpdateCharacterClassRequest request) {
        CharacterClassResponse response = characterClassModifier.updateCharacterClass(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 캐릭터 직업을 삭제합니다.
     *
     * @param id 삭제할 캐릭터 직업 ID
     * @return 삭제 결과와 API 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacterClass(@PathVariable Long id) {
        characterClassRemover.deleteCharacterClass(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 기본 캐릭터 직업 데이터를 초기화합니다.
     *
     * @return 초기화 결과와 API 응답
     */
    @PostMapping("/initialize")
    public ResponseEntity<Void> initializeDefaultCharacterClasses() {
        characterClassService.initializeDefaultCharacterClasses();
        return ResponseEntity.ok().build();
    }
}
