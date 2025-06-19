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
import java.util.Optional;

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
        PlayerCharacter speaker = getSpeaker(command.userId());
        Long roomId = speaker.getCurrentRoomId();

        if (hasTarget(command)) {
            processSpeakToTarget(command, speaker, roomId);
        } else {
            processSpeakToRoom(command, speaker, roomId);
        }
    }

    private PlayerCharacter getSpeaker(Long userId) {
        return gameWorldService.getPlayerByUserId(userId);
    }

    private boolean hasTarget(SpeakCommand command) {
        return command.target() != null && !command.target().isEmpty();
    }

    private void processSpeakToTarget(SpeakCommand command, PlayerCharacter speaker, Long roomId) {
        Optional<String> targetMessage = tryFindTargetAndCreateMessage(command, speaker, roomId);

        if (targetMessage.isPresent()) {
            sendMessageToAllPlayersInRoom(roomId, targetMessage.get());
        } else {
            notifyTargetNotFound(command);
        }
    }

    private Optional<String> tryFindTargetAndCreateMessage(SpeakCommand command, PlayerCharacter speaker, Long roomId) {
        // Try to find NPC target
        Optional<String> npcMessage = tryFindNpcTargetAndCreateMessage(command, speaker, roomId);
        if (npcMessage.isPresent()) {
            return npcMessage;
        }

        // Try to find PC target
        return tryFindPlayerTargetAndCreateMessage(command, speaker, roomId);
    }

    private Optional<String> tryFindNpcTargetAndCreateMessage(SpeakCommand command, PlayerCharacter speaker, Long roomId) {
        NonPlayerCharacter targetNpc = gameWorldService.getNpcByName(command.target());

        if (targetNpc != null && isInSameRoom(targetNpc.getCurrentRoomId(), roomId)) {
            return Optional.of(formatDirectedMessage(speaker.getName(), targetNpc.getName(), command.message()));
        }

        return Optional.empty();
    }

    private Optional<String> tryFindPlayerTargetAndCreateMessage(SpeakCommand command, PlayerCharacter speaker, Long roomId) {
        List<PlayerCharacter> playersInRoom = gameWorldService.getPlayersInRoom(roomId);

        return playersInRoom.stream()
                .filter(player -> player.getName().equals(command.target()))
                .map(player -> formatDirectedMessage(speaker.getName(), player.getName(), command.message()))
                .findFirst();
    }

    private boolean isInSameRoom(Long targetRoomId, Long speakerRoomId) {
        return targetRoomId.equals(speakerRoomId);
    }

    private void notifyTargetNotFound(SpeakCommand command) {
        sendMessageToUser(command.userId(), formatTargetNotFoundMessage(command.target()));
    }

    private void processSpeakToRoom(SpeakCommand command, PlayerCharacter speaker, Long roomId) {
        String message = formatRoomMessage(speaker.getName(), command.message());
        sendMessageToAllPlayersInRoom(roomId, message);
    }

    private String formatDirectedMessage(String speakerName, String targetName, String message) {
        return String.format("%s가 %s에게 \"%s\"라고 말합니다", speakerName, targetName, message);
    }

    private String formatRoomMessage(String speakerName, String message) {
        return String.format("%s가 \"%s\"라고 말합니다", speakerName, message);
    }

    private String formatTargetNotFoundMessage(String targetName) {
        return String.format("%s은 이 방안에 없습니다.", targetName);
    }

    private void sendMessageToUser(Long userId, String message) {
        sendMessageToUserPort.messageToUser(userId, message);
    }

    private void sendMessageToAllPlayersInRoom(Long roomId, String message) {
        List<PlayerCharacter> playersInRoom = gameWorldService.getPlayersInRoom(roomId);
        for (PlayerCharacter playerInRoom : playersInRoom) {
            sendMessageToUser(playerInRoom.getUserId(), message);
        }
        logger.debug("Sent message to all players in room {}: {}", roomId, message);
    }
}
