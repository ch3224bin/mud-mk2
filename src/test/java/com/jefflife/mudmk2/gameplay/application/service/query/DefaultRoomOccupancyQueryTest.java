package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.Monster;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveMonsterRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveNpcRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultRoomOccupancyQueryTest {

    @Mock private ActivePlayerRepository players;
    @Mock private ActiveNpcRepository npcs;
    @Mock private ActiveMonsterRepository monsters;
    private DefaultRoomOccupancyQuery query;

    @BeforeEach
    void setUp() {
        query = new DefaultRoomOccupancyQuery(players, npcs, monsters);
    }

    @Test
    void playersIn_returnsOnlyPlayersInThatRoom() {
        PlayerCharacter p1 = mock(PlayerCharacter.class);
        PlayerCharacter p2 = mock(PlayerCharacter.class);
        when(p1.getCurrentRoomId()).thenReturn(100L);
        when(p2.getCurrentRoomId()).thenReturn(200L);
        when(players.findAll()).thenReturn(List.of(p1, p2));

        assertThat(query.playersIn(100L)).containsExactly(p1);
    }

    @Test
    void npcsIn_returnsOnlyNpcsInThatRoom() {
        NonPlayerCharacter n1 = mock(NonPlayerCharacter.class);
        NonPlayerCharacter n2 = mock(NonPlayerCharacter.class);
        when(n1.getCurrentRoomId()).thenReturn(100L);
        when(n2.getCurrentRoomId()).thenReturn(200L);
        when(npcs.findAll()).thenReturn(List.of(n1, n2));

        assertThat(query.npcsIn(100L)).containsExactly(n1);
    }

    @Test
    void monstersIn_returnsOnlyAliveMonstersInThatRoom() {
        Monster aliveInRoom = mock(Monster.class);
        Monster deadInRoom = mock(Monster.class);
        Monster aliveOtherRoom = mock(Monster.class);
        when(aliveInRoom.isAlive()).thenReturn(true);
        when(aliveInRoom.getCurrentRoomId()).thenReturn(100L);
        when(deadInRoom.isAlive()).thenReturn(false);
        when(aliveOtherRoom.isAlive()).thenReturn(true);
        when(aliveOtherRoom.getCurrentRoomId()).thenReturn(200L);
        when(monsters.findAll()).thenReturn(List.of(aliveInRoom, deadInRoom, aliveOtherRoom));

        assertThat(query.monstersIn(100L)).containsExactly(aliveInRoom);
    }

    @Test
    void playersIn_returnsEmpty_whenNoMatch() {
        when(players.findAll()).thenReturn(List.of());

        assertThat(query.playersIn(100L)).isEmpty();
    }
}
