package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterStats;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.StatusCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.exception.RoomNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.provided.StatusUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendStatusMessagePort;
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

    private final ActiveRoomRepository rooms;
    private final ActivePlayerRepository players;
    private final SendStatusMessagePort sendStatusMessagePort;

    public StatusCommandService(
            final ActiveRoomRepository rooms,
            final ActivePlayerRepository players,
            final SendStatusMessagePort sendStatusMessagePort
    ) {
        this.rooms = rooms;
        this.players = players;
        this.sendStatusMessagePort = sendStatusMessagePort;
    }

    @Override
    public void showStatus(final StatusCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
        Long currentRoomId = player.getCurrentRoomId();
        Room currentRoom = rooms.findById(currentRoomId)
                .orElseThrow(() -> new RoomNotFoundException(currentRoomId));
        
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
                stats.ap(),
                stats.maxAp(),
                stats.vigor(),
                stats.physique(),
                stats.agility(),
                stats.intellect(),
                stats.will(),
                stats.meridian(),
                stats.innerPower(),
                stats.specialTechnique(),
                stats.lightStep(),
                stats.fistsAndPalms(),
                stats.swordMethod(),
                stats.bladeMethod(),
                stats.longWeapon(),
                stats.esotericWeapon(),
                stats.archery(),
                currentRoom.getName()
        );
        
        sendStatusMessagePort.sendMessage(statusVariables);
        logger.debug("Sent status message for player: {}", player.getName());
    }
}