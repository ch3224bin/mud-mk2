package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.StatusCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.StatusUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.SendStatusMessagePort;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import com.jefflife.mudmk2.gameplay.application.service.model.template.StatusVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service implementation for the StatusUseCase.
 * Handles status commands in the game.
 */
@Service
public class StatusCommandService implements StatusUseCase {
    private static final Logger logger = LoggerFactory.getLogger(StatusCommandService.class);

    private final GameWorldService gameWorldService;
    private final SendStatusMessagePort sendStatusMessagePort;

    public StatusCommandService(
            final GameWorldService gameWorldService,
            final SendStatusMessagePort sendStatusMessagePort
    ) {
        this.gameWorldService = gameWorldService;
        this.sendStatusMessagePort = sendStatusMessagePort;
    }

    @Override
    public void showStatus(final StatusCommand command) {
        PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
        Room currentRoom = gameWorldService.getRoom(player.getCurrentRoomId());
        
        CharacterStats stats = player.getStats();
        
        StatusVariables statusVariables = new StatusVariables(
                player.getUserId(),
                player.getName(),
                player.getCharacterClass(),
                player.getBaseCharacterInfo().getGender(),
                player.getState(),
                player.getPlayableCharacterInfo().getLevel(),
                player.getPlayableCharacterInfo().getExperience(),
                player.getPlayableCharacterInfo().getNextLevelExp(),
                stats.hp(),
                stats.maxHp(),
                stats.mp(),
                stats.maxMp(),
                stats.str(),
                stats.dex(),
                stats.con(),
                stats.intelligence(),
                stats.pow(),
                stats.cha(),
                currentRoom.getName()
        );
        
        sendStatusMessagePort.sendMessage(statusVariables);
        logger.debug("Sent status message for player: {}", player.getName());
    }
}