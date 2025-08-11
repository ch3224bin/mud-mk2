package com.jefflife.mudmk2.gamedata.application.service.required;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.MonsterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonsterTypeRepository extends JpaRepository<MonsterType, Long> {
    // 기본 CRUD 기능은 JpaRepository에서 제공
    // 필요한 경우 추가 쿼리 메서드 구현 가능
}
