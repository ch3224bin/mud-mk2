package com.jefflife.mudmk2.gameplay.application.domain.model.look;

import com.jefflife.mudmk2.common.fixture.GameTestFixture;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import com.jefflife.mudmk2.gameplay.application.service.command.look.DirectionLookable;
import com.jefflife.mudmk2.gameplay.application.service.command.look.Lookable;
import com.jefflife.mudmk2.gameplay.application.service.command.look.LookableTargetFinder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
record LookableTargetFinderTest(LookableTargetFinder targetFinder, GameWorldService gameWorldService) {

    private static final Long TEST_USER_ID = 99999L;
    private static final Long TEST_ROOM_ID = 99001L;
    private static final Long TEST_NEXT_ROOM_ID = 99002L;

    @BeforeEach
    void setUp() {
        GameTestFixture.DirectionTestSetup setup = GameTestFixture.createDirectionTestSetup(
                TEST_USER_ID, TEST_ROOM_ID, TEST_NEXT_ROOM_ID
        );
        gameWorldService.loadRooms(List.of(setup.currentRoom(), setup.nextRoom()));
        gameWorldService.addPlayer(setup.player());
    }

    @AfterEach
    void tearDown() {
        gameWorldService.removePlayer(TEST_USER_ID);
        gameWorldService.removeRoom(TEST_ROOM_ID);
        gameWorldService.removeRoom(TEST_NEXT_ROOM_ID);
    }

    @Test
    void findNoTarget() {
        Optional<Lookable> result = targetFinder.findTargetInRoom(TEST_USER_ID, "없는 것");

        assertThat(result).isEmpty();
    }

    @Test
    void findDirection() {
        Optional<Lookable> result = targetFinder.findTargetInRoom(TEST_USER_ID, "동");

        assertThat(result).isNotEmpty();
        assertThat(result.get()).isInstanceOf(DirectionLookable.class);
    }
}
