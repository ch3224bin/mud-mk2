package com.jefflife.mudmk2.gameplay.application.domain.model.look;

import com.jefflife.mudmk2.gameplay.application.service.command.look.DirectionLookable;
import com.jefflife.mudmk2.gameplay.application.service.command.look.Lookable;
import com.jefflife.mudmk2.gameplay.application.service.command.look.LookableTargetFinder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
record LookableTargetFinderTest(LookableTargetFinder targetFinder) {

    @Test
    void findNoTarget() {
        Optional<Lookable> result = targetFinder.findTargetInRoom(1L, "없는 것");

        assertThat(result).isEmpty();
    }

    @Test
    void findDirection() {
        // ?? 왜 통과하지?
        Optional<Lookable> result = targetFinder.findTargetInRoom(1L, "동");

        assertThat(result).isNotEmpty();
        assertThat(result.get()).isInstanceOf(DirectionLookable.class);
    }
}