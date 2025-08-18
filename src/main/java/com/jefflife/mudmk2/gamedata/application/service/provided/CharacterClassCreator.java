package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.service.model.request.CreateCharacterClassRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.CharacterClassResponse;

/**
 * 캐릭터 직업 생성을 위한 유스케이스 인터페이스
 */
public interface CharacterClassCreator {
    /**
     * 새로운 캐릭터 직업을 생성합니다.
     *
     * @param request 직업 생성 요청 DTO
     * @return 생성된 캐릭터 직업 응답 DTO
     */
    CharacterClassResponse createCharacterClass(CreateCharacterClassRequest request);
}
