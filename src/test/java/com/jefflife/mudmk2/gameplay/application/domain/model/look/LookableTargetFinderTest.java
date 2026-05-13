package com.jefflife.mudmk2.gameplay.application.domain.model.look;

import com.jefflife.mudmk2.common.fixture.GameTestFixture;
import com.jefflife.mudmk2.gameplay.application.service.command.look.DirectionLookable;
import com.jefflife.mudmk2.gameplay.application.service.command.look.Lookable;
import com.jefflife.mudmk2.gameplay.application.service.command.look.LookableTargetFinder;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
record LookableTargetFinderTest(
        LookableTargetFinder targetFinder,
        ActivePlayerRepository players,
        ActiveRoomRepository rooms
) {

    private static final Long TEST_USER_ID = 99999L;
    private static final Long TEST_ROOM_ID = 99001L;
    private static final Long TEST_NEXT_ROOM_ID = 99002L;

    @BeforeEach
    void setUp() {
        GameTestFixture.DirectionTestSetup setup = GameTestFixture.createDirectionTestSetup(
                TEST_USER_ID, TEST_ROOM_ID, TEST_NEXT_ROOM_ID
        );
        rooms.add(setup.currentRoom());
        rooms.add(setup.nextRoom());
        players.add(setup.player());
    }

    @AfterEach
    void tearDown() {
        players.removeByUserId(TEST_USER_ID);
        rooms.remove(TEST_ROOM_ID);
        rooms.remove(TEST_NEXT_ROOM_ID);
    }

    @Test
    void findNoTarget() {
        Optional<Lookable> result = targetFinder.findTargetInRoom(TEST_USER_ID, "없는 것", 1);

        assertThat(result).isEmpty();
    }

    @Test
    void findDirection() {
        Optional<Lookable> result = targetFinder.findTargetInRoom(TEST_USER_ID, "동", 1);

        assertThat(result).isNotEmpty();
        assertThat(result.get()).isInstanceOf(DirectionLookable.class);
    }
}
