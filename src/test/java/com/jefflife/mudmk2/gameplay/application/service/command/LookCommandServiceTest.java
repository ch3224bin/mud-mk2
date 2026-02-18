package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.LookCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.RoomDescriber;
import com.jefflife.mudmk2.gameplay.application.service.command.look.LookTargetProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LookCommandServiceTest {

    @Mock
    private RoomDescriber roomDescriber;

    @Mock
    private LookTargetProcessor lookTargetProcessor;

    private LookCommandService lookCommandService;

    @BeforeEach
    void setUp() {
        lookCommandService = new LookCommandService(roomDescriber, lookTargetProcessor);
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
            lookCommandService.look(command);

            // Then
            verify(roomDescriber).describe(userId);
        }

        @Test
        @DisplayName("should call displayRoomInfo when target is empty")
        void shouldCallDisplayRoomInfoWhenTargetIsEmpty() {
            // Given
            LookCommand command = new LookCommand(userId, "");

            // When
            lookCommandService.look(command);

            // Then
            verify(roomDescriber).describe(userId);
        }

        @Test
        @DisplayName("should not call displayRoomInfo when target is specified")
        void shouldNotCallDisplayRoomInfoWhenTargetIsSpecified() {
            // Given
            LookCommand command = new LookCommand(userId, "도토리");

            // When
            lookCommandService.look(command);

            // Then
            verify(roomDescriber, never()).describe(any());
        }
    }
}