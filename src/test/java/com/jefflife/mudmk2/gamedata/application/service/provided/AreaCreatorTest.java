package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Area;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.AreaCreateRequest;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.AreaType;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
record AreaCreatorTest(AreaCreator areaCreator, EntityManager entityManager) {

    static RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange('a', 'z')
            .withinRange('A', 'Z')
            .withinRange('0', '9')
            .withinRange('가', '힣')
            .get();

    @Test
    void createFail() {
        checkValidation(new AreaCreateRequest(null, AreaType.OPEN_MAP));
        checkValidation(new AreaCreateRequest("", null));
        checkValidation(new AreaCreateRequest(generator.generate(256), AreaType.INSTANCE_MAP));

    }

    @Test
    void createSuccess() {
        AreaCreateRequest request = new AreaCreateRequest(generator.generate(255), AreaType.OPEN_MAP);

        Area area = areaCreator.createArea(request);

        assertThat(area.getId()).isNotNull();
        assertThat(area.getName()).isEqualTo(request.name());
        assertThat(area.getType()).isEqualTo(request.type());
    }

    private void checkValidation(AreaCreateRequest request) {
        assertThatThrownBy(() -> areaCreator.createArea(request))
                .isInstanceOf(ConstraintViolationException.class);
    }
}