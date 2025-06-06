package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.service.model.request.UpdateCharacterClassRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.CharacterClassResponse;

/**
 * 캐릭터 직업 수정을 위한 유스케이스 인터페이스
 */
public interface UpdateCharacterClassUseCase {
    /**
     * 기존 캐릭터 직업을 수정합니다.
     *
     * @param id 수정할 직업의 ID
     * @param request 직업 수정 요청 DTO
     * @return 수정된 캐릭터 직업 엔티티
     */
    CharacterClassResponse updateCharacterClass(Long id, UpdateCharacterClassRequest request);
}
