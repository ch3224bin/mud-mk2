package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.SpeakCommand;
import com.jefflife.mudmk2.gameplay.application.port.in.SpeakUseCase;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for the SpeakUseCase.
 * Handles speak commands in the game.
 */
@Service
public class SpeakService implements SpeakUseCase {
    private static final Logger logger = LoggerFactory.getLogger(SpeakService.class);

    private final GameWorldService gameWorldService;
    private final SendMessageToUserPort sendMessageToUserPort;

    public SpeakService(
            final GameWorldService gameWorldService,
            final SendMessageToUserPort sendMessageToUserPort
    ) {
        this.gameWorldService = gameWorldService;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void speak(final SpeakCommand command) {
        // 1. 플레이어 정보 가져오기
        PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
        Long playerRoomId = player.getCurrentRoomId();
        String playerName = player.getName();

        // 2. 타겟이 있는 경우 타겟 찾기
        if (command.target() != null && !command.target().isEmpty()) {
            // 타겟이 NPC인지 확인
            NonPlayerCharacter targetNpc = gameWorldService.getNpcByName(command.target());
            if (targetNpc != null) {
                // 타겟이 같은 방에 있는지 확인
                if (targetNpc.getCurrentRoomId().equals(playerRoomId)) {
                    // 방에 있는 모든 플레이어에게 메시지 전송
                    sendMessageToAllPlayersInRoom(
                            playerRoomId,
                            String.format("%s가 %s에게 \"%s\"라고 말합니다", playerName, targetNpc.getName(), command.message())
                    );
                    return;
                } else {
                    // 타겟이 같은 방에 없는 경우
                    sendMessageToUserPort.messageToUser(
                            command.userId(),
                            String.format("%s은 이 방안에 없습니다.", command.target())
                    );
                    return;
                }
            }

            // 타겟이 PC인지 확인
            List<PlayerCharacter> playersInRoom = gameWorldService.getPlayersInRoom(playerRoomId);
            for (PlayerCharacter targetPlayer : playersInRoom) {
                if (targetPlayer.getName().equals(command.target())) {
                    // 방에 있는 모든 플레이어에게 메시지 전송
                    sendMessageToAllPlayersInRoom(
                            playerRoomId,
                            String.format("%s가 %s에게 \"%s\"라고 말합니다", playerName, targetPlayer.getName(), command.message())
                    );
                    return;
                }
            }

            // 타겟을 찾을 수 없는 경우
            sendMessageToUserPort.messageToUser(
                    command.userId(),
                    String.format("%s은 이 방안에 없습니다.", command.target())
            );
            return;
        }

        // 3. 타겟이 없는 경우 방에 있는 모든 플레이어에게 메시지 전송
        sendMessageToAllPlayersInRoom(
                playerRoomId,
                String.format("%s가 \"%s\"라고 말합니다", playerName, command.message())
        );
    }

    private void sendMessageToAllPlayersInRoom(Long roomId, String message) {
        List<PlayerCharacter> playersInRoom = gameWorldService.getPlayersInRoom(roomId);
        for (PlayerCharacter playerInRoom : playersInRoom) {
            sendMessageToUserPort.messageToUser(playerInRoom.getUserId(), message);
        }
        logger.debug("Sent message to all players in room {}: {}", roomId, message);
    }
}