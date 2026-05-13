package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultNpcLocationServiceTest {

    @Mock private ActiveNpcRepository npcs;
    private DefaultNpcLocationService service;

    @BeforeEach
    void setUp() {
        service = new DefaultNpcLocationService(npcs);
    }

    @Test
    void move_callsMoveToOnNpc_andReturnsTrue_whenNpcExists() {
        UUID npcId = UUID.randomUUID();
        Long roomId = 200L;
        NonPlayerCharacter npc = mock(NonPlayerCharacter.class);
        when(npcs.findById(npcId)).thenReturn(Optional.of(npc));

        boolean result = service.move(npcId, roomId);

        verify(npc).moveTo(roomId);
        assertThat(result).isTrue();
    }

    @Test
    void move_returnsFalse_whenNpcNotFound() {
        UUID npcId = UUID.randomUUID();
        when(npcs.findById(npcId)).thenReturn(Optional.empty());

        boolean result = service.move(npcId, 200L);

        assertThat(result).isFalse();
    }
}
