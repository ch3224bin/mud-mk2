package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultPartyServiceTest {

    @Mock private ActivePartyRepository parties;
    @Mock private ApplicationEventPublisher eventPublisher;
    private DefaultPartyService service;

    @BeforeEach
    void setUp() {
        service = new DefaultPartyService(parties, eventPublisher);
    }

    @Test
    void create_addsPartyToRepository_andPublishesPartyCreatedEvent() {
        Party party = Party.createParty(UUID.randomUUID());

        service.create(party);

        verify(parties).add(party);
        ArgumentCaptor<PartyCreatedEvent> captor = ArgumentCaptor.forClass(PartyCreatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getParty()).isEqualTo(party);
    }

    @Test
    void disband_removesPartyFromRepository_andPublishesPartyDisbandedEvent_whenPartyExists() {
        UUID partyId = UUID.randomUUID();
        Party party = Party.createParty(UUID.randomUUID());
        when(parties.findById(partyId)).thenReturn(Optional.of(party));

        service.disband(partyId);

        verify(parties).remove(partyId);
        ArgumentCaptor<PartyDisbandedEvent> captor = ArgumentCaptor.forClass(PartyDisbandedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getPartyId()).isEqualTo(partyId);
    }

    @Test
    void disband_isNoOp_whenPartyNotFound() {
        UUID partyId = UUID.randomUUID();
        when(parties.findById(partyId)).thenReturn(Optional.empty());

        service.disband(partyId);

        verify(parties, never()).remove(any(UUID.class));
        verifyNoInteractions(eventPublisher);
    }
}
