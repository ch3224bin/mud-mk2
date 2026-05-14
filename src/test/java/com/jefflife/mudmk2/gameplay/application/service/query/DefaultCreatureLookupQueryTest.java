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
class DefaultCreatureLookupQueryTest {

    @Mock private ActivePlayerRepository players;
    @Mock private ActiveNpcRepository npcs;
    @Mock private ActiveMonsterRepository monsters;
    private DefaultCreatureLookupQuery query;

    @BeforeEach
    void setUp() {
        query = new DefaultCreatureLookupQuery(players, npcs, monsters);
    }

    @Test
    void findPlayerByName_returnsPlayer_caseInsensitive() {
        PlayerCharacter p = mock(PlayerCharacter.class);
        when(p.getName()).thenReturn("Alice");
        when(players.findAll()).thenReturn(List.of(p));

        assertThat(query.findPlayerByName("ALICE")).contains(p);
        assertThat(query.findPlayerByName("alice")).contains(p);
    }

    @Test
    void findPlayerByName_returnsEmpty_whenNoMatch() {
        when(players.findAll()).thenReturn(List.of());

        assertThat(query.findPlayerByName("missing")).isEmpty();
    }

    @Test
    void findNpcByName_returnsNpc_caseInsensitive() {
        NonPlayerCharacter npc = mock(NonPlayerCharacter.class);
        when(npc.getName()).thenReturn("Goblin");
        when(npcs.findAll()).thenReturn(List.of(npc));

        assertThat(query.findNpcByName("GOBLIN")).contains(npc);
    }

    @Test
    void findNpcByName_returnsEmpty_whenNoMatch() {
        when(npcs.findAll()).thenReturn(List.of());

        assertThat(query.findNpcByName("nobody")).isEmpty();
    }

    @Test
    void findMonstersByType_returnsAllMonstersWithMatchingTypeId() {
        Monster m1 = mock(Monster.class);
        Monster m2 = mock(Monster.class);
        Monster otherType = mock(Monster.class);
        when(m1.getMonsterTypeId()).thenReturn(10L);
        when(m2.getMonsterTypeId()).thenReturn(10L);
        when(otherType.getMonsterTypeId()).thenReturn(20L);
        when(monsters.findAll()).thenReturn(List.of(m1, m2, otherType));

        assertThat(query.findMonstersByType(10L)).containsExactlyInAnyOrder(m1, m2);
    }

    @Test
    void findMonstersByType_returnsEmpty_whenNoMatch() {
        when(monsters.findAll()).thenReturn(List.of());

        assertThat(query.findMonstersByType(99L)).isEmpty();
    }
}
