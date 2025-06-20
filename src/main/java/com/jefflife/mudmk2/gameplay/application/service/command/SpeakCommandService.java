package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Statable;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.SpeakCommand;
import com.jefflife.mudmk2.gameplay.application.port.in.SpeakUseCase;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for the SpeakUseCase.
 * Handles speak commands in the game.
 */
@Service
public class SpeakCommandService implements SpeakUseCase {
    private static final Logger logger = LoggerFactory.getLogger(SpeakCommandService.class);

    private final GameWorldService gameWorldService;
    private final SendMessageToUserPort sendMessageToUserPort;

    public SpeakCommandService(
            final GameWorldService gameWorldService,
            final SendMessageToUserPort sendMessageToUserPort
    ) {
        this.gameWorldService = gameWorldService;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Async("taskExecutor")
    @Override
    public void speak(final SpeakCommand command) {
        PlayerCharacter speaker = getSpeaker(command.userId());
        Long roomId = speaker.getCurrentRoomId();

        Statable speakTarget = null;
        if (command.hasTarget()) {
            speakTarget = findTarget(command.target(), roomId);
            if (speakTarget == null) {
                sendMessageToUser(command.userId(), String.format("%s은(는) 이 방안에 없습니다.", command.target()));
                return;
            }
        }

        List<PlayerCharacter> playersInRoom = gameWorldService.getPlayersInRoom(roomId);
        for (PlayerCharacter playerInRoom : playersInRoom) {
            String message = createMessageForPlayer(command, playerInRoom, speaker, speakTarget);
            sendMessageToUser(playerInRoom.getUserId(), message);
        }
    }

    private static String createMessageForPlayer(SpeakCommand command, PlayerCharacter playerInRoom, PlayerCharacter speaker, Statable speakTarget) {
        if (playerInRoom.equals(speaker)) {
            String message = String.format("당신은 \"%s\"라고 말합니다.", command.message());
            if (speakTarget != null) {
                message = String.format("당신은 %s에게 \"%s\"라고 말합니다.", speakTarget.getName(), command.message());
            }
            return message;
        } else if (playerInRoom.equals(speakTarget)) {
            return String.format("%s이(가) 당신에게 \"%s\"라고 말합니다.", speaker.getName(), command.message());
        } else {
            String message = String.format("%s이(가) \"%s\"라고 말합니다", speaker.getName(), command.message());
            if (speakTarget != null) {
                message = String.format("%s이(가) %s에게 \"%s\"라고 말합니다", speaker.getName(), speakTarget.getName(), command.message());
            }
            return message;
        }
    }

    private Statable findTarget(String target, Long roomId) {
        PlayerCharacter targetPlayer = gameWorldService.getPlayerByName(target);
        if (targetPlayer != null && isInSameRoom(targetPlayer.getCurrentRoomId(), roomId)) {
            return targetPlayer;
        }

        NonPlayerCharacter targetNpc = gameWorldService.getNpcByName(target);
        if (targetNpc != null && isInSameRoom(targetNpc.getCurrentRoomId(), roomId)) {
            return targetNpc;
        }

        return null;
    }

    private PlayerCharacter getSpeaker(Long userId) {
        return gameWorldService.getPlayerByUserId(userId);
    }

    private boolean isInSameRoom(Long targetRoomId, Long speakerRoomId) {
        return targetRoomId.equals(speakerRoomId);
    }

    private void sendMessageToUser(Long userId, String message) {
        sendMessageToUserPort.messageToUser(userId, message);
    }
}
