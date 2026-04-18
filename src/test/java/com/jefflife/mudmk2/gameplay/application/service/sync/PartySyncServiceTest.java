package com.jefflife.mudmk2.gameplay.application.service.sync;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gamedata.application.event.PartyCreatedEvent;
import com.jefflife.mudmk2.gamedata.application.event.PartyDisbandedEvent;
import com.jefflife.mudmk2.gamedata.application.service.required.PartyRepository;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartySyncServiceTest {

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private GameWorldService gameWorldService;

    private PartySyncService sut;

    @BeforeEach
    void setUp() {
        sut = new PartySyncService(partyRepository, gameWorldService);
    }

    @Test
    @DisplayName("syncToDb 호출 시 활성 파티 전체를 저장한다")
    void syncToDb_savesAllActiveParties() {
        Party party = mock(Party.class);
        when(gameWorldService.getActiveParties()).thenReturn(List.of(party));

        sut.syncToDb();

        verify(partyRepository).saveAll(List.of(party));
    }

    @Test
    @DisplayName("PartyCreatedEvent 수신 시 파티를 즉시 저장한다")
    void onPartyCreated_savesPartyImmediately() {
        Party party = mock(Party.class);
        PartyCreatedEvent event = new PartyCreatedEvent(this, party);

        sut.onPartyCreated(event);

        verify(partyRepository).save(party);
    }

    @Test
    @DisplayName("PartyDisbandedEvent 수신 시 파티를 즉시 삭제한다")
    void onPartyDisbanded_deletesPartyImmediately() {
        UUID partyId = UUID.randomUUID();
        PartyDisbandedEvent event = new PartyDisbandedEvent(this, partyId);

        sut.onPartyDisbanded(event);

        verify(partyRepository).deleteById(partyId);
    }
}
