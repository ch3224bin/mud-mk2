package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClassEntity;

/**
 * 단일 캐릭터 직업 조회를 위한 유스케이스 인터페이스
 */
public interface CharacterClassFinder {
    /**
     * ID로 캐릭터 직업을 조회합니다.
     *
     * @param id 조회할 직업의 ID
     * @return 조회된 캐릭터 직업 엔티티 (없는 경우 Optional.empty())
     */
    CharacterClassEntity getCharacterClassById(Long id);

    /**
     * 코드로 캐릭터 직업을 조회합니다.
     *
     * @param code 조회할 직업의 코드 (WARRIOR, MAGE 등)
     * @return 조회된 캐릭터 직업 엔티티 (없는 경우 Optional.empty())
     */
    CharacterClassEntity getCharacterClassByCode(String code);
}
