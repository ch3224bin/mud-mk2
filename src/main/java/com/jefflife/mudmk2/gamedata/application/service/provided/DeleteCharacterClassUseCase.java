package com.jefflife.mudmk2.gamedata.application.service.provided;

/**
 * 캐릭터 직업 삭제를 위한 유스케이스 인터페이스
 */
public interface DeleteCharacterClassUseCase {
    /**
     * 캐릭터 직업을 삭제합니다.
     *
     * @param id 삭제할 직업의 ID
     */
    void deleteCharacterClass(Long id);
}
