package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.PlayerCharacterService;
import com.jefflife.mudmk2.gameplay.application.port.in.DisplayRoomInfoUseCase;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import com.jefflife.mudmk2.user.domain.User;
import com.jefflife.mudmk2.user.service.UserSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Optional;

@Service
public class DisplayRoomInfoService implements DisplayRoomInfoUseCase {
    private static final Logger logger = LoggerFactory.getLogger(DisplayRoomInfoService.class);

    private final PlayerCharacterService playerCharacterService;
    private final GameWorldService gameWorldService;
    private final SendMessageToUserPort sendMessageToUserPort;
    private final TemplateEngine templateEngine;

    public DisplayRoomInfoService(
            final PlayerCharacterService playerCharacterService,
            final GameWorldService gameWorldService,
            final SendMessageToUserPort sendMessageToUserPort,
            final TemplateEngine templateEngine
    ) {
        this.playerCharacterService = playerCharacterService;
        this.gameWorldService = gameWorldService;
        this.sendMessageToUserPort = sendMessageToUserPort;
        this.templateEngine = templateEngine;
    }

    @Override
    public void displayRoomInfo(String principalName) {
        User user = UserSessionManager.getConnectedUser(principalName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with principal name: " + principalName));

        PlayerCharacter character = playerCharacterService.getCharacterByUserId(user.getId());

        if (character == null) {
            logger.info("Player character not found for user {}. Room info will not be displayed.", principalName);
            return;
        }

        Optional<Room> roomOpt = gameWorldService.getRoom(character.getCurrentRoomId());

        if (roomOpt.isPresent()) {
            Room currentRoom = roomOpt.get();
            Context context = new Context();
            context.setVariable("roomName", currentRoom.getName());
            context.setVariable("roomDescription", currentRoom.getDescription());
            context.setVariable("exits", currentRoom.getExitString());

            String htmlContent = templateEngine.process("gameplay/room-info", context);

            sendMessageToUserPort.messageToUser(principalName, htmlContent);
            logger.info("Sent room info to user {}", principalName);
        } else {
            logger.warn("Room with ID {} not found for character {}", character.getCurrentRoomId(), principalName);
        }
    }
}

