package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CharacterClassEntityTest {

    @Test
    @DisplayName("CharacterClassCreateRequest로 CharacterClassEntity를 생성한다")
    void create_ShouldCreateEntityFromRequest() {
        // given
        CharacterClassCreateRequest request = new CharacterClassCreateRequest(
            "WARRIOR",
            "전사",
            "강력한 물리 공격력을 가진 직업",
            100,
            10,
            15,
            10,
            12,
            8,
            10,
            8
        );

        // when
        CharacterClassEntity entity = CharacterClassEntity.create(request);

        // then
        assertThat(entity.getCode()).isEqualTo(request.code());
        assertThat(entity.getName()).isEqualTo(request.name());
        assertThat(entity.getDescription()).isEqualTo(request.description());
        assertThat(entity.getBaseHp()).isEqualTo(request.baseHp());
        assertThat(entity.getBaseMp()).isEqualTo(request.baseMp());
        assertThat(entity.getBaseStr()).isEqualTo(request.baseStr());
        assertThat(entity.getBaseDex()).isEqualTo(request.baseDex());
        assertThat(entity.getBaseCon()).isEqualTo(request.baseCon());
        assertThat(entity.getBaseIntelligence()).isEqualTo(request.baseIntelligence());
        assertThat(entity.getBasePow()).isEqualTo(request.basePow());
        assertThat(entity.getBaseCha()).isEqualTo(request.baseCha());
    }
}