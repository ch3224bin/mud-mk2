package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.MartialArtViewCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.command.martialart.MartialArtViewMapper;
import com.jefflife.mudmk2.gameplay.application.service.model.template.MartialArtViewVariables;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMartialArtViewMessagePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MartialArtViewCommandServiceTest {

    private ActivePlayerRepository players;
    private MartialArtViewMapper mapper;
    private SendMartialArtViewMessagePort sender;
    private MartialArtViewCommandService service;

    @BeforeEach
    void setUp() {
        players = mock(ActivePlayerRepository.class);
        mapper = mock(MartialArtViewMapper.class);
        sender = mock(SendMartialArtViewMessagePort.class);
        service = new MartialArtViewCommandService(players, mapper, sender);
    }

    @Test
    void showMartialArts_callsMapperAndSendsVariables() {
        PlayerCharacter pc = mock(PlayerCharacter.class);
        when(players.findByUserId(1L)).thenReturn(Optional.of(pc));
        MartialArtViewVariables vars =
                new MartialArtViewVariables(1L, List.of(), List.of());
        when(mapper.toMartialArtVariables(1L, pc)).thenReturn(vars);

        service.showMartialArts(new MartialArtViewCommand(1L));

        verify(sender).sendMessage(vars);
    }

    @Test
    void showMartialArts_whenPlayerNotFound_throws() {
        when(players.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.showMartialArts(new MartialArtViewCommand(99L)))
                .isInstanceOf(PlayerNotFoundException.class);
    }
}
