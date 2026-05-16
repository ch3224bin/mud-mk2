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

        CharacterStats base = player.getBaseStats();
        CharacterStats total = player.getStats();

        StatusVariables statusVariables = new StatusVariables(
                player.getUserId(),
                player.getName(),
                player.getCharacterClass(),
                player.getBaseCharacterInfo().getGender(),
                player.getState(),
                player.getPlayableCharacterInfo().getLevel(),
                player.getPlayableCharacterInfo().getExperience(),
                player.getPlayableCharacterInfo().getNextLevelExp(),
                total.hp(),
                total.maxHp(),
                total.mp(),
                total.maxMp(),
                total.ap(),
                total.maxAp(),
                new StatusVariables.StatValue(base.vigor(),            total.vigor()            - base.vigor()),
                new StatusVariables.StatValue(base.physique(),         total.physique()         - base.physique()),
                new StatusVariables.StatValue(base.agility(),          total.agility()          - base.agility()),
                new StatusVariables.StatValue(base.intellect(),        total.intellect()        - base.intellect()),
                new StatusVariables.StatValue(base.will(),             total.will()             - base.will()),
                new StatusVariables.StatValue(base.meridian(),         total.meridian()         - base.meridian()),
                new StatusVariables.StatValue(base.innerPower(),       total.innerPower()       - base.innerPower()),
                new StatusVariables.StatValue(base.specialTechnique(), total.specialTechnique() - base.specialTechnique()),
                new StatusVariables.StatValue(base.lightStep(),        total.lightStep()        - base.lightStep()),
                new StatusVariables.StatValue(base.fistsAndPalms(),    total.fistsAndPalms()    - base.fistsAndPalms()),
                new StatusVariables.StatValue(base.swordMethod(),      total.swordMethod()      - base.swordMethod()),
                new StatusVariables.StatValue(base.bladeMethod(),      total.bladeMethod()      - base.bladeMethod()),
                new StatusVariables.StatValue(base.longWeapon(),       total.longWeapon()       - base.longWeapon()),
                new StatusVariables.StatValue(base.esotericWeapon(),   total.esotericWeapon()   - base.esotericWeapon()),
                new StatusVariables.StatValue(base.archery(),          total.archery()          - base.archery()),
                currentRoom.getName()
        );

        sendStatusMessagePort.sendMessage(statusVariables);
        logger.debug("Sent status message for player: {}", player.getName());
    }
}