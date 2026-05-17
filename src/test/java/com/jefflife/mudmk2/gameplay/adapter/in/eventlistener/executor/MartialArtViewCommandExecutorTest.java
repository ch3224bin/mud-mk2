package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.MartialArtViewCommand;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.StatusCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.MartialArtViewUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MartialArtViewCommandExecutorTest {

    private MartialArtViewUseCase useCase;
    private MartialArtViewCommandExecutor executor;

    @BeforeEach
    void setUp() {
        useCase = mock(MartialArtViewUseCase.class);
        executor = new MartialArtViewCommandExecutor(useCase);
    }

    @Test
    void canExecute_returnsTrueForMartialArtViewCommand() {
        assertThat(executor.canExecute(new MartialArtViewCommand(1L))).isTrue();
    }

    @Test
    void canExecute_returnsFalseForOtherCommand() {
        assertThat(executor.canExecute(new StatusCommand(1L))).isFalse();
    }

    @Test
    void execute_callsUseCase() {
        MartialArtViewCommand cmd = new MartialArtViewCommand(1L);

        executor.execute(cmd);

        verify(useCase).showMartialArts(cmd);
    }

    @Test
    void execute_whenWrongCommand_throws() {
        assertThatThrownBy(() -> executor.execute(new StatusCommand(1L)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
