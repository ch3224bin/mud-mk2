package com.jefflife.mudmk2.gameplay.application.service.query;

import com.jefflife.mudmk2.gamedata.application.domain.model.party.Party;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePartyRepository;
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
class DefaultPartyMembershipQueryTest {

    @Mock private ActivePartyRepository parties;
    private DefaultPartyMembershipQuery query;

    @BeforeEach
    void setUp() {
        query = new DefaultPartyMembershipQuery(parties);
    }

    @Test
    void findByMemberId_returnsParty_whenMemberBelongs() {
        UUID memberId = UUID.randomUUID();
        Party party = mock(Party.class);
        when(party.contains(memberId)).thenReturn(true);
        when(parties.findAll()).thenReturn(List.of(party));

        assertThat(query.findByMemberId(memberId)).contains(party);
    }

    @Test
    void findByMemberId_returnsEmpty_whenMemberInNoParty() {
        UUID memberId = UUID.randomUUID();
        Party party = mock(Party.class);
        when(party.contains(memberId)).thenReturn(false);
        when(parties.findAll()).thenReturn(List.of(party));

        assertThat(query.findByMemberId(memberId)).isEmpty();
    }

    @Test
    void isInParty_returnsTrue_whenMemberBelongs() {
        UUID memberId = UUID.randomUUID();
        Party party = mock(Party.class);
        when(party.contains(memberId)).thenReturn(true);
        when(parties.findAll()).thenReturn(List.of(party));

        assertThat(query.isInParty(memberId)).isTrue();
    }

    @Test
    void isInParty_returnsFalse_whenMemberInNoParty() {
        UUID memberId = UUID.randomUUID();
        when(parties.findAll()).thenReturn(List.of());

        assertThat(query.isInParty(memberId)).isFalse();
    }
}
