package com.jefflife.mudmk2.gameplay.adapter.out.cache;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import com.jefflife.mudmk2.gamedata.application.service.required.PartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryPartyRepositoryTest {

    @Mock private PartyRepository jpa;
    private InMemoryPartyRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPartyRepository(jpa);
    }

    @Test
    void bootstrap_loadsAllPartiesFromJpa() {
        Party p1 = Party.createParty(UUID.randomUUID());
        Party p2 = Party.createParty(UUID.randomUUID());
        when(jpa.findAll()).thenReturn(List.of(p1, p2));

        repository.bootstrap();

        assertThat(repository.findById(p1.getId())).contains(p1);
        assertThat(repository.findById(p2.getId())).contains(p2);
    }

    @Test
    void findById_returnsEmpty_whenNotCached() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findAll_returnsAllCachedParties() {
        Party p1 = Party.createParty(UUID.randomUUID());
        Party p2 = Party.createParty(UUID.randomUUID());

        repository.add(p1);
        repository.add(p2);

        assertThat(repository.findAll()).containsExactlyInAnyOrder(p1, p2);
    }

    @Test
    void add_putsPartyIntoCache() {
        Party party = Party.createParty(UUID.randomUUID());

        repository.add(party);

        assertThat(repository.findById(party.getId())).contains(party);
    }

    @Test
    void remove_dropsPartyFromCache() {
        Party party = Party.createParty(UUID.randomUUID());
        repository.add(party);

        repository.remove(party.getId());

        assertThat(repository.findById(party.getId())).isEmpty();
    }

    @Test
    void remove_isNoOp_whenIdNotPresent() {
        repository.remove(UUID.randomUUID());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void onPartyCreated_savesPartyImmediately_toJpa() {
        Party party = Party.createParty(UUID.randomUUID());
        PartyCreatedEvent event = new PartyCreatedEvent(this, party);

        repository.onPartyCreated(event);

        verify(jpa).save(party);
    }

    @Test
    void onPartyDisbanded_deletesPartyImmediately_fromJpa() {
        UUID partyId = UUID.randomUUID();
        PartyDisbandedEvent event = new PartyDisbandedEvent(this, partyId);

        repository.onPartyDisbanded(event);

        verify(jpa).deleteById(partyId);
    }

    @Test
    void syncToDb_savesCachedPartiesViaJpa() {
        Party p1 = Party.createParty(UUID.randomUUID());
        Party p2 = Party.createParty(UUID.randomUUID());
        repository.add(p1);
        repository.add(p2);

        repository.syncToDb();

        verify(jpa).saveAll(argThat(parties -> {
            java.util.List<Party> list = new java.util.ArrayList<>();
            parties.forEach(list::add);
            return list.size() == 2 && list.contains(p1) && list.contains(p2);
        }));
    }
}
