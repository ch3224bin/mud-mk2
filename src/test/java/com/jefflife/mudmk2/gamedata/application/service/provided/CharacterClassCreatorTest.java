package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClassCreateRequest;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClassEntity;
import com.jefflife.mudmk2.gamedata.application.service.exception.DuplicateCharacterClassException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
record CharacterClassCreatorTest(CharacterClassCreator characterClassCreator, EntityManager entityManager) {

    @Test
    void createSuccess() {
        CharacterClassCreateRequest request = new CharacterClassCreateRequest(
            "WARRIOR", "전사", "물리 전투에 능숙한 전사", 
            100, 20, 12, 8, 10, 5, 6, 7
        );

        CharacterClassEntity response = characterClassCreator.createCharacterClass(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getCode()).isEqualTo(request.code());
        assertThat(response.getName()).isEqualTo(request.name());
        assertThat(response.getDescription()).isEqualTo(request.description());
        assertThat(response.getBaseHp()).isEqualTo(request.baseHp());
        assertThat(response.getBaseMp()).isEqualTo(request.baseMp());
        assertThat(response.getBaseStr()).isEqualTo(request.baseStr());
    }

    @Test
    void createFailWhenDuplicateCode() {
        CharacterClassCreateRequest request1 = new CharacterClassCreateRequest(
            "DUPLICATE", "첫번째", "설명1", 100, 20, 10, 10, 10, 10, 10, 10
        );
        CharacterClassCreateRequest request2 = new CharacterClassCreateRequest(
            "DUPLICATE", "두번째", "설명2", 90, 30, 12, 12, 12, 12, 12, 12
        );

        characterClassCreator.createCharacterClass(request1);
        
        assertThatThrownBy(() -> characterClassCreator.createCharacterClass(request2))
                .isInstanceOf(DuplicateCharacterClassException.class);
    }

}