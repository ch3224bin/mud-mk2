package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.common.fixture.RoomFixture;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomRegisterRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.RoomRegister;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
record RoomRegisterTest(RoomRegister roomRegister, EntityManager entityManager) {
    @Test
    void registerSuccess() {
        RoomRegisterRequest registerRequest = RoomFixture.createRoomRegisterRequest();

        Room room = roomRegister.register(registerRequest);
        entityManager.flush();
        entityManager.clear();

        assertThat(room.getId()).isNotNull();
        assertThat(room.getName()).isEqualTo(registerRequest.name());
        assertThat(room.getSummary()).isEqualTo(registerRequest.summary());
        assertThat(room.getDescription()).isEqualTo(registerRequest.description());
    }

    @Test
    void registerFail() {
        assertThatThrownBy(() -> roomRegister.register(new RoomRegisterRequest(null, "name", "summary", "description")))
                .isInstanceOf(ConstraintViolationException.class);

        assertThatThrownBy(() -> roomRegister.register(new RoomRegisterRequest(1L, null, "summary", "description")))
                .isInstanceOf(ConstraintViolationException.class);

        assertThatThrownBy(() -> roomRegister.register(new RoomRegisterRequest(1L, "name", null, "description")))
                .isInstanceOf(ConstraintViolationException.class);

        assertThatThrownBy(() -> roomRegister.register(new RoomRegisterRequest(1L, "name", "summary", null)))
                .isInstanceOf(ConstraintViolationException.class);
    }
}