package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultMonsterRespawnServiceTest {

    @Mock private ActiveMonsterRepository monsters;
    private DefaultMonsterRespawnService service;

    @BeforeEach
    void setUp() {
        service = new DefaultMonsterRespawnService(monsters);
    }

    @Test
    void respawnAll_invokesRespawn_onDeadRespawnableMonsters_andReturnsCount() {
        Monster dead1 = mock(Monster.class);
        Monster dead2 = mock(Monster.class);
        when(dead1.isAlive()).thenReturn(false);
        when(dead2.isAlive()).thenReturn(false);
        when(dead1.canRespawn()).thenReturn(true);
        when(dead2.canRespawn()).thenReturn(true);
        when(monsters.findAll()).thenReturn(List.of(dead1, dead2));

        int count = service.respawnAll();

        verify(dead1).respawn();
        verify(dead2).respawn();
        assertThat(count).isEqualTo(2);
    }

    @Test
    void respawnAll_skipsAliveMonsters() {
        Monster alive = mock(Monster.class);
        when(alive.isAlive()).thenReturn(true);
        when(monsters.findAll()).thenReturn(List.of(alive));

        int count = service.respawnAll();

        verify(alive, never()).respawn();
        assertThat(count).isZero();
    }

    @Test
    void respawnAll_skipsDeadButNotRespawnableMonsters() {
        Monster dead = mock(Monster.class);
        when(dead.isAlive()).thenReturn(false);
        when(dead.canRespawn()).thenReturn(false);
        when(monsters.findAll()).thenReturn(List.of(dead));

        int count = service.respawnAll();

        verify(dead, never()).respawn();
        assertThat(count).isZero();
    }

    @Test
    void respawnAll_returnsZero_whenNoMonsters() {
        when(monsters.findAll()).thenReturn(List.of());

        int count = service.respawnAll();

        assertThat(count).isZero();
    }
}
