package com.jefflife.mudmk2.gamedata.application.domain.repository;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 캐릭터 직업 엔티티를 위한 리포지토리 인터페이스
 */
@Repository
public interface CharacterClassRepository extends JpaRepository<CharacterClassEntity, Long> {

    /**
     * 직업 코드로 CharacterClassEntity를 조회합니다.
     *
     * @param code 직업 코드 (WARRIOR, MAGE 등)
     * @return 해당 코드에 대한 CharacterClassEntity (없는 경우 Optional.empty())
     */
    Optional<CharacterClassEntity> findByCode(String code);

    /**
     * 직업 코드의 존재 여부를 확인합니다.
     *
     * @param code 직업 코드 (WARRIOR, MAGE 등)
     * @return 해당 코드가 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByCode(String code);
}
