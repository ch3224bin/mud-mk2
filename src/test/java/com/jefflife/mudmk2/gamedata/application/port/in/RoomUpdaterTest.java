package com.jefflife.mudmk2.gamedata.application.port.in;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomRegisterRequest;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.RoomUpdateRequest;
import com.jefflife.mudmk2.gamedata.application.service.provided.RoomRegister;
import com.jefflife.mudmk2.gamedata.application.service.provided.RoomUpdater;
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
        Room room = roomRegister.register(new RoomRegisterRequest(1L, "방1", "summary", "description"));
        entityManager.flush();
        entityManager.clear();

        RoomUpdateRequest roomUpdateRequest = new RoomUpdateRequest("방2", "summary2", "description2");
        Room updated = roomUpdater.update(room.getId(), roomUpdateRequest);

        assertThat(updated.getName()).isEqualTo(roomUpdateRequest.name());
        assertThat(updated.getSummary()).isEqualTo(roomUpdateRequest.summary());
        assertThat(updated.getDescription()).isEqualTo(roomUpdateRequest.description());
    }

    @Test
    void updateFail() {
        Room room = roomRegister.register(new RoomRegisterRequest(1L, "방1", "summary", "description"));
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> roomUpdater.update(room.getId(), new RoomUpdateRequest("", "summary2", "description2")))
            .isInstanceOf(ConstraintViolationException.class);

        assertThatThrownBy(() -> roomUpdater.update(room.getId(), new RoomUpdateRequest("name", "", "description2")))
                .isInstanceOf(ConstraintViolationException.class);

        assertThatThrownBy(() -> roomUpdater.update(room.getId(), new RoomUpdateRequest("name", "summary2", "")))
                .isInstanceOf(ConstraintViolationException.class);
    }
}