package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.LookCommand;
import com.jefflife.mudmk2.gameplay.application.port.in.DisplayRoomInfoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LookServiceTest {

    @Mock
    private DisplayRoomInfoUseCase displayRoomInfoUseCase;

    private LookService lookService;

    @BeforeEach
    void setUp() {
        lookService = new LookService(displayRoomInfoUseCase);
    }

    @Nested
    @DisplayName("look method tests")
    class LookTests {

        private final Long userId = 1L;

        @Test
        @DisplayName("should call displayRoomInfo when target is null")
        void shouldCallDisplayRoomInfoWhenTargetIsNull() {
            // Given
            LookCommand command = new LookCommand(userId, null);

            // When
            lookService.look(command);

            // Then
            verify(displayRoomInfoUseCase).displayRoomInfo(userId);
        }

        @Test
        @DisplayName("should call displayRoomInfo when target is empty")
        void shouldCallDisplayRoomInfoWhenTargetIsEmpty() {
            // Given
            LookCommand command = new LookCommand(userId, "");

            // When
            lookService.look(command);

            // Then
            verify(displayRoomInfoUseCase).displayRoomInfo(userId);
        }

        @Test
        @DisplayName("should not call displayRoomInfo when target is specified")
        void shouldNotCallDisplayRoomInfoWhenTargetIsSpecified() {
            // Given
            LookCommand command = new LookCommand(userId, "도토리");

            // When
            lookService.look(command);

            // Then
            verify(displayRoomInfoUseCase, never()).displayRoomInfo(any());
        }
    }
}