package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.NonPlayerCharacterRepository;
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
class InMemoryNpcRepositoryTest {

    @Mock private NonPlayerCharacterRepository jpa;
    private InMemoryNpcRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNpcRepository(jpa);
    }

    @Test
    void bootstrap_loadsAllNpcsFromJpa_andInitializesAssociations() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        NonPlayerCharacter n1 = mock(NonPlayerCharacter.class);
        NonPlayerCharacter n2 = mock(NonPlayerCharacter.class);
        when(n1.getId()).thenReturn(id1);
        when(n2.getId()).thenReturn(id2);
        when(jpa.findAll()).thenReturn(List.of(n1, n2));

        repository.bootstrap();

        verify(n1).initializeAssociatedEntities();
        verify(n2).initializeAssociatedEntities();
        assertThat(repository.findById(id1)).contains(n1);
        assertThat(repository.findById(id2)).contains(n2);
    }

    @Test
    void findById_returnsEmpty_whenNotCached() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findAll_returnsAllCachedNpcs() {
        NonPlayerCharacter n1 = mock(NonPlayerCharacter.class);
        NonPlayerCharacter n2 = mock(NonPlayerCharacter.class);
        when(n1.getId()).thenReturn(UUID.randomUUID());
        when(n2.getId()).thenReturn(UUID.randomUUID());

        repository.add(n1);
        repository.add(n2);

        assertThat(repository.findAll()).containsExactlyInAnyOrder(n1, n2);
    }

    @Test
    void add_putsNpcIntoCache() {
        UUID id = UUID.randomUUID();
        NonPlayerCharacter n = mock(NonPlayerCharacter.class);
        when(n.getId()).thenReturn(id);

        repository.add(n);

        assertThat(repository.findById(id)).contains(n);
    }

    @Test
    void remove_dropsNpcFromCache() {
        UUID id = UUID.randomUUID();
        NonPlayerCharacter n = mock(NonPlayerCharacter.class);
        when(n.getId()).thenReturn(id);
        repository.add(n);

        repository.remove(id);

        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void remove_isNoOp_whenIdNotPresent() {
        repository.remove(UUID.randomUUID());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void syncToDb_savesCachedNpcsViaJpa() {
        NonPlayerCharacter n1 = mock(NonPlayerCharacter.class);
        NonPlayerCharacter n2 = mock(NonPlayerCharacter.class);
        when(n1.getId()).thenReturn(UUID.randomUUID());
        when(n2.getId()).thenReturn(UUID.randomUUID());
        repository.add(n1);
        repository.add(n2);

        repository.syncToDb();

        verify(jpa).saveAll(argThat(npcs -> {
            java.util.List<NonPlayerCharacter> list = new java.util.ArrayList<>();
            npcs.forEach(list::add);
            return list.size() == 2 && list.contains(n1) && list.contains(n2);
        }));
    }
}
