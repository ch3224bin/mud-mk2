package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.event.PlayerCharacterCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryPlayerRepositoryTest {

    @Mock private PlayerCharacterRepository jpa;
    private InMemoryPlayerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPlayerRepository(jpa);
    }

    @Test
    void bootstrap_loadsAllPlayersFromJpa_andInitializesAssociations() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        PlayerCharacter p1 = mock(PlayerCharacter.class);
        PlayerCharacter p2 = mock(PlayerCharacter.class);
        when(p1.getId()).thenReturn(id1);
        when(p1.getUserId()).thenReturn(10L);
        when(p2.getId()).thenReturn(id2);
        when(p2.getUserId()).thenReturn(20L);
        when(jpa.findAll()).thenReturn(List.of(p1, p2));

        repository.bootstrap();

        verify(p1).initializeAssociatedEntities();
        verify(p2).initializeAssociatedEntities();
        assertThat(repository.findById(id1)).contains(p1);
        assertThat(repository.findById(id2)).contains(p2);
        assertThat(repository.findByUserId(10L)).contains(p1);
        assertThat(repository.findByUserId(20L)).contains(p2);
    }

    @Test
    void findById_returnsEmpty_whenNotCached() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findByUserId_returnsEmpty_whenNotCached() {
        assertThat(repository.findByUserId(999L)).isEmpty();
    }

    @Test
    void findAll_returnsAllCachedPlayers() {
        PlayerCharacter p1 = mock(PlayerCharacter.class);
        PlayerCharacter p2 = mock(PlayerCharacter.class);
        when(p1.getId()).thenReturn(UUID.randomUUID());
        when(p1.getUserId()).thenReturn(1L);
        when(p2.getId()).thenReturn(UUID.randomUUID());
        when(p2.getUserId()).thenReturn(2L);

        repository.add(p1);
        repository.add(p2);

        assertThat(repository.findAll()).containsExactlyInAnyOrder(p1, p2);
    }

    @Test
    void add_putsPlayerIntoBothIndexes() {
        UUID id = UUID.randomUUID();
        PlayerCharacter p = mock(PlayerCharacter.class);
        when(p.getId()).thenReturn(id);
        when(p.getUserId()).thenReturn(42L);

        repository.add(p);

        assertThat(repository.findById(id)).contains(p);
        assertThat(repository.findByUserId(42L)).contains(p);
    }

    @Test
    void removeByUserId_dropsPlayerFromBothIndexes() {
        UUID id = UUID.randomUUID();
        PlayerCharacter p = mock(PlayerCharacter.class);
        when(p.getId()).thenReturn(id);
        when(p.getUserId()).thenReturn(42L);
        repository.add(p);

        repository.removeByUserId(42L);

        assertThat(repository.findById(id)).isEmpty();
        assertThat(repository.findByUserId(42L)).isEmpty();
    }

    @Test
    void removeByUserId_isNoOp_whenUserIdNotPresent() {
        repository.removeByUserId(999L);

        assertThat(repository.findByUserId(999L)).isEmpty();
    }

    @Test
    void onCreated_addsPlayerAndInitializesAssociations() {
        UUID id = UUID.randomUUID();
        PlayerCharacter p = mock(PlayerCharacter.class);
        when(p.getId()).thenReturn(id);
        when(p.getUserId()).thenReturn(7L);
        PlayerCharacterCreatedEvent event = new PlayerCharacterCreatedEvent(new Object(), p);

        repository.onCreated(event);

        verify(p).initializeAssociatedEntities();
        assertThat(repository.findById(id)).contains(p);
        assertThat(repository.findByUserId(7L)).contains(p);
    }

    @Test
    void syncToDb_savesCachedPlayersViaJpa() {
        PlayerCharacter p1 = mock(PlayerCharacter.class);
        PlayerCharacter p2 = mock(PlayerCharacter.class);
        when(p1.getId()).thenReturn(UUID.randomUUID());
        when(p1.getUserId()).thenReturn(1L);
        when(p2.getId()).thenReturn(UUID.randomUUID());
        when(p2.getUserId()).thenReturn(2L);
        repository.add(p1);
        repository.add(p2);

        repository.syncToDb();

        verify(jpa).saveAll(argThat(players -> {
            List<PlayerCharacter> list = new java.util.ArrayList<>();
            players.forEach(list::add);
            return list.size() == 2 && list.contains(p1) && list.contains(p2);
        }));
    }
}
