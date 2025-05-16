package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.port.in.DisplayRoomInfoUseCase;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class DisplayRoomInfoService implements DisplayRoomInfoUseCase {
    private static final Logger logger = LoggerFactory.getLogger(DisplayRoomInfoService.class);

    private final GameWorldService gameWorldService;
    private final SendMessageToUserPort sendMessageToUserPort;
    private final TemplateEngine templateEngine;

    public DisplayRoomInfoService(
            final GameWorldService gameWorldService,
            final SendMessageToUserPort sendMessageToUserPort,
            final TemplateEngine templateEngine
    ) {
        this.gameWorldService = gameWorldService;
        this.sendMessageToUserPort = sendMessageToUserPort;
        this.templateEngine = templateEngine;
    }

    @Override
    public void displayRoomInfo(Long userId) {
        final PlayerCharacter character = gameWorldService.getPlayerByUserId(userId);

        if (character == null) {
            logger.info("Player character not found for user {}. Room info will not be displayed.", userId);
            return;
        }

        final Room currentRoom = gameWorldService.getRoom(character.getCurrentRoomId());

        Context context = new Context();
        context.setVariable("roomName", currentRoom.getName());
        context.setVariable("roomDescription", currentRoom.getDescription());
        context.setVariable("exits", currentRoom.getExitString());

        String htmlContent = templateEngine.process("gameplay/room-info", context);

        sendMessageToUserPort.messageToUser(userId, htmlContent);
        logger.info("Sent room info to user {}", userId);
    }
}

