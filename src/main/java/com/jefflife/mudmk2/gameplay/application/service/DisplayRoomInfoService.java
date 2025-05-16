package com.jefflife.mudmk2.gameplay.application.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.port.in.DisplayRoomInfoUseCase;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.stream.Collectors;

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

        // 현재 방에 있는 NPC 목록 가져오기
        List<NonPlayerCharacter> npcsInRoom = gameWorldService.getNpcsInRoom(currentRoom.getId());

        // 현재 방에 있는 다른 플레이어 캐릭터 목록 가져오기
        List<PlayerCharacter> otherPlayersInRoom = gameWorldService.getPlayersInRoom(currentRoom.getId())
                .stream()
                .filter(pc -> !pc.getId().equals(character.getId())) // 자신 제외
                .collect(Collectors.toList());

        Context context = new Context();
        context.setVariable("roomName", currentRoom.getName());
        context.setVariable("roomDescription", currentRoom.getDescription());
        context.setVariable("exits", currentRoom.getExitString());
        context.setVariable("npcsInRoom", npcsInRoom);
        context.setVariable("otherPlayersInRoom", otherPlayersInRoom);

        String htmlContent = templateEngine.process("gameplay/room-info", context);

        sendMessageToUserPort.messageToUser(userId, htmlContent);
        logger.info("Sent room info to user {}", userId);
    }
}

