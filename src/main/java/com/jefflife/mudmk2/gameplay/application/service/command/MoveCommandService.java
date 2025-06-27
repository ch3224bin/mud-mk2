package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Direction;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.event.PlayerMoveEvent;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.MoveCommand;
import com.jefflife.mudmk2.gameplay.application.port.in.RoomDescriber;
import com.jefflife.mudmk2.gameplay.application.port.in.MoveUseCase;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class MoveCommandService implements MoveUseCase {
    private final GameWorldService gameWorldService;
    private final RoomDescriber roomDescriber;
    private final SendMessageToUserPort sendMessageToUserPort;
    private final ApplicationEventPublisher eventPublisher;

    public MoveCommandService(
            final GameWorldService gameWorldService,
            final RoomDescriber roomDescriber,
            final SendMessageToUserPort sendMessageToUserPort,
            final ApplicationEventPublisher eventPublisher
    ) {
        this.gameWorldService = gameWorldService;
        this.roomDescriber = roomDescriber;
        this.sendMessageToUserPort = sendMessageToUserPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void move(final MoveCommand command) {
        PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
        Long fromRoomId = player.getCurrentRoomId();
        Room room = gameWorldService.getRoom(fromRoomId);
        Direction direction = Direction.valueOfString(command.direction());
        PlayerCharacter.MoveResult moveResult = player.move(room, direction);
        switch (moveResult) {
            case SUCCESS:
                sendMessageToUserPort.messageToUser(command.userId(), String.format("당신은 %s쪽으로 이동 합니다.", direction.getName()));
                roomDescriber.describe(command.userId());
                eventPublisher.publishEvent(new PlayerMoveEvent(player.getId(), fromRoomId, player.getCurrentRoomId()));
                break;
            case NO_WAY, FAILED:
                sendMessageToUserPort.messageToUser(command.userId(), String.format("%s쪽으로는 갈 수 없습니다.", direction.getName()));
                break;
            case LOCKED:
                sendMessageToUserPort.messageToUser(command.userId(), String.format("%s쪽은 잠겨있습니다.", direction.getName()));
                break;
        }
    }
}
