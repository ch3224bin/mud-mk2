package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClassEntity;

import java.util.List;

/**
 * 모든 캐릭터 직업 조회를 위한 유스케이스 인터페이스
 */
public interface CharacterClassesRetriever {
    /**
     * 모든 캐릭터 직업을 조회합니다.
     *
     * @return 모든 캐릭터 직업 엔티티 목록
     */
    List<CharacterClassEntity> getAllCharacterClasses();
}
