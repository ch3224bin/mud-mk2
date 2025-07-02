package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomRegisterRequest;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomUpdateRequest;
import com.jefflife.mudmk2.gamedata.application.service.model.response.RoomResponse;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
record RoomUpdaterTest(RoomRegister roomRegister, RoomUpdater roomUpdater, EntityManager entityManager) {
    @Test
    void updateSuccess() {
        RoomResponse room = roomRegister.register(new RoomRegisterRequest(1L, "방1", "summary", "description"));
        entityManager.flush();
        entityManager.clear();

        RoomUpdateRequest roomUpdateRequest = new RoomUpdateRequest("방2", "summary2", "description2");
        RoomResponse updated = roomUpdater.update(room.id(), roomUpdateRequest);

        assertThat(updated.name()).isEqualTo(roomUpdateRequest.name());
        assertThat(updated.summary()).isEqualTo(roomUpdateRequest.summary());
        assertThat(updated.description()).isEqualTo(roomUpdateRequest.description());
    }

    @Test
    void updateFail() {
        RoomResponse room = roomRegister.register(new RoomRegisterRequest(1L, "방1", "summary", "description"));
        entityManager.flush();
        entityManager.clear();

        RoomUpdateRequest roomUpdateRequest = new RoomUpdateRequest("", "summary2", "description2");
        assertThatThrownBy(() -> roomUpdater.update(room.id(), roomUpdateRequest))
            .isInstanceOf(ConstraintViolationException.class);
    }
}