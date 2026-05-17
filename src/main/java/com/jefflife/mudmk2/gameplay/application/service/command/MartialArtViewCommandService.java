package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.MartialArtViewCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.command.martialart.MartialArtViewMapper;
import com.jefflife.mudmk2.gameplay.application.service.provided.MartialArtViewUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMartialArtViewMessagePort;
import org.springframework.stereotype.Service;

@Service
public class MartialArtViewCommandService implements MartialArtViewUseCase {

    private final ActivePlayerRepository players;
    private final MartialArtViewMapper mapper;
    private final SendMartialArtViewMessagePort port;

    public MartialArtViewCommandService(ActivePlayerRepository players,
                                        MartialArtViewMapper mapper,
                                        SendMartialArtViewMessagePort port) {
        this.players = players;
        this.mapper = mapper;
        this.port = port;
    }

    @Override
    public void showMartialArts(MartialArtViewCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
        port.sendMessage(mapper.toMartialArtVariables(command.userId(), player));
    }
}
