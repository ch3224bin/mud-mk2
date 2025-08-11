package com.jefflife.mudmk2.gamedata.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClassEntity;
import com.jefflife.mudmk2.gamedata.application.service.provided.*;
import com.jefflife.mudmk2.gamedata.application.service.required.CharacterClassRepository;
import com.jefflife.mudmk2.gamedata.application.service.exception.CharacterClassNotFoundException;
import com.jefflife.mudmk2.gamedata.application.service.exception.DuplicateCharacterClassException;
import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateCharacterClassRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateCharacterClassRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.CharacterClassResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 캐릭터 직업 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CharacterClassService implements
        CreateCharacterClassUseCase,
        GetCharacterClassUseCase,
        GetAllCharacterClassesUseCase,
        UpdateCharacterClassUseCase,
        DeleteCharacterClassUseCase {

    private final CharacterClassRepository characterClassRepository;

    /**
     * 새로운 캐릭터 직업을 생성합니다.
     *
     * @param request 생성할 캐릭터 직업 정보
     * @return 생성된 캐릭터 직업 응답 DTO
     * @throws DuplicateCharacterClassException 동일한 코드를 가진 직업이 이미 존재하는 경우
     */
    @Override
    public CharacterClassResponse createCharacterClass(CreateCharacterClassRequest request) {
        if (characterClassRepository.existsByCode(request.getCode())) {
            throw new DuplicateCharacterClassException("이미 존재하는 직업 코드입니다: " + request.getCode());
        }

        CharacterClassEntity entity = CharacterClassEntity.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .baseHp(request.getBaseHp())
                .baseMp(request.getBaseMp())
                .baseStr(request.getBaseStr())
                .baseDex(request.getBaseDex())
                .baseCon(request.getBaseCon())
                .baseIntelligence(request.getBaseIntelligence())
                .basePow(request.getBasePow())
                .baseCha(request.getBaseCha())
                .build();

        CharacterClassEntity savedEntity = characterClassRepository.save(entity);
        return toResponse(savedEntity);
    }

    /**
     * ID로 캐릭터 직업을 조회합니다.
     *
     * @param id 조회할 캐릭터 직업 ID
     * @return 조회된 캐릭터 직업 응답 DTO
     * @throws CharacterClassNotFoundException 해당 ID의 직업이 존재하지 않는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public CharacterClassResponse getCharacterClassById(Long id) {
        CharacterClassEntity entity = characterClassRepository.findById(id)
                .orElseThrow(() -> new CharacterClassNotFoundException("존재하지 않는 직업 ID: " + id));
        return toResponse(entity);
    }

    /**
     * 코드로 캐릭터 직업을 조회합니다.
     *
     * @param code 조회할 캐릭터 직업 코드
     * @return 조회된 캐릭터 직업 응답 DTO
     * @throws CharacterClassNotFoundException 해당 코드의 직업이 존재하지 않는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public CharacterClassResponse getCharacterClassByCode(String code) {
        CharacterClassEntity entity = characterClassRepository.findByCode(code)
                .orElseThrow(() -> new CharacterClassNotFoundException("존재하지 않는 직업 코드: " + code));
        return toResponse(entity);
    }

    /**
     * 모든 캐릭터 직업을 조회합니다.
     *
     * @return 모든 캐릭터 직업 응답 DTO 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<CharacterClassResponse> getAllCharacterClasses() {
        List<CharacterClassEntity> entities = characterClassRepository.findAll();
        return toResponseList(entities);
    }

    /**
     * 캐릭터 직업 정보를 업데이트합니다.
     *
     * @param id      업데이트할 캐릭터 직업 ID
     * @param request 업데이트할 캐릭터 직업 정보
     * @return 업데이트된 캐릭터 직업 응답 DTO
     * @throws CharacterClassNotFoundException 해당 ID의 직업이 존재하지 않는 경우
     */
    @Override
    public CharacterClassResponse updateCharacterClass(Long id, UpdateCharacterClassRequest request) {
        CharacterClassEntity entity = characterClassRepository.findById(id)
                .orElseThrow(() -> new CharacterClassNotFoundException("존재하지 않는 직업 ID: " + id));

        // 코드가 변경되었고, 새 코드가 이미 존재하는 경우 예외 발생
        if (!entity.getCode().equals(request.getCode()) && characterClassRepository.existsByCode(request.getCode())) {
            throw new DuplicateCharacterClassException("이미 존재하는 직업 코드입니다: " + request.getCode());
        }

        CharacterClassEntity updatedEntity = CharacterClassEntity.builder()
                .id(entity.getId())
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .baseHp(request.getBaseHp())
                .baseMp(request.getBaseMp())
                .baseStr(request.getBaseStr())
                .baseDex(request.getBaseDex())
                .baseCon(request.getBaseCon())
                .baseIntelligence(request.getBaseIntelligence())
                .basePow(request.getBasePow())
                .baseCha(request.getBaseCha())
                .build();

        CharacterClassEntity savedEntity = characterClassRepository.save(updatedEntity);
        return toResponse(savedEntity);
    }

    /**
     * 캐릭터 직업을 삭제합니다.
     *
     * @param id 삭제할 캐릭터 직업 ID
     * @throws CharacterClassNotFoundException 해당 ID의 직업이 존재하지 않는 경우
     */
    @Override
    public void deleteCharacterClass(Long id) {
        if (!characterClassRepository.existsById(id)) {
            throw new CharacterClassNotFoundException("존재하지 않는 직업 ID: " + id);
        }
        characterClassRepository.deleteById(id);
    }

    /**
     * 애플리케이션 초기화 시 기본 캐릭터 직업 데이터를 생성합니다.
     * 이미 존재하는 코드의 직업은 건너뜁니다.
     */
    @Transactional
    public void initializeDefaultCharacterClasses() {
        createDefaultCharacterClassIfNotExists(
                "WARRIOR", "전사", "물리적 전투에 능숙한 강인한 전사입니다.",
                100, 20, 12, 8, 10, 5, 6, 7);

        createDefaultCharacterClassIfNotExists(
                "MAGE", "마법사", "강력한 마법을 사용하는 지식이 풍부한 마법사입니다.",
                60, 100, 4, 6, 6, 14, 12, 8);

        createDefaultCharacterClassIfNotExists(
                "ROGUE", "도적", "은밀한 행동과 빠른 공격에 능숙한 도적입니다.",
                70, 40, 8, 14, 7, 8, 7, 10);

        createDefaultCharacterClassIfNotExists(
                "CLERIC", "성직자", "치유와 신성한 마법을 사용하는 성직자입니다.",
                80, 80, 6, 6, 8, 10, 10, 12);

        createDefaultCharacterClassIfNotExists(
                "RANGER", "레인저", "자연과 교감하며 원거리 공격에 능숙한 레인저입니다.",
                75, 50, 9, 12, 8, 8, 8, 8);
    }

    /**
     * 주어진 정보로 기본 캐릭터 직업을 생성하되, 이미 존재하는 경우 건너뜁니다.
     */
    private void createDefaultCharacterClassIfNotExists(
            String code, String name, String description,
            int baseHp, int baseMp, int baseStr, int baseDex, int baseCon,
            int baseIntelligence, int basePow, int baseCha) {

        if (!characterClassRepository.existsByCode(code)) {
            CharacterClassEntity entity = CharacterClassEntity.builder()
                    .code(code)
                    .name(name)
                    .description(description)
                    .baseHp(baseHp)
                    .baseMp(baseMp)
                    .baseStr(baseStr)
                    .baseDex(baseDex)
                    .baseCon(baseCon)
                    .baseIntelligence(baseIntelligence)
                    .basePow(basePow)
                    .baseCha(baseCha)
                    .build();

            characterClassRepository.save(entity);
        }
    }

    /**
     * CharacterClassEntity를 CharacterClassResponse로 변환합니다.
     *
     * @param entity 변환할 엔티티
     * @return 변환된 응답 DTO
     */
    public CharacterClassResponse toResponse(CharacterClassEntity entity) {
        return CharacterClassResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .baseHp(entity.getBaseHp())
                .baseMp(entity.getBaseMp())
                .baseStr(entity.getBaseStr())
                .baseDex(entity.getBaseDex())
                .baseCon(entity.getBaseCon())
                .baseIntelligence(entity.getBaseIntelligence())
                .basePow(entity.getBasePow())
                .baseCha(entity.getBaseCha())
                .build();
    }

    /**
     * 엔티티 리스트를 응답 DTO 리스트로 변환합니다.
     *
     * @param entities 변환할 엔티티 리스트
     * @return 변환된 응답 DTO 리스트
     */
    public List<CharacterClassResponse> toResponseList(List<CharacterClassEntity> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
