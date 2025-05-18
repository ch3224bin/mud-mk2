package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.map.Direction;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.MoveCommand;
import com.jefflife.mudmk2.gameplay.application.port.in.DisplayRoomInfoUseCase;
import com.jefflife.mudmk2.gameplay.application.port.in.MoveUseCase;
import com.jefflife.mudmk2.gameplay.application.port.out.SendMessageToUserPort;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.springframework.stereotype.Service;

@Service
public class MoveService implements MoveUseCase {
    private final GameWorldService gameWorldService;
    private final DisplayRoomInfoUseCase displayRoomInfoUseCase;
    private final SendMessageToUserPort sendMessageToUserPort;

    public MoveService(
            final GameWorldService gameWorldService,
            final DisplayRoomInfoUseCase displayRoomInfoUseCase,
            final SendMessageToUserPort sendMessageToUserPort
    ) {
        this.gameWorldService = gameWorldService;
        this.displayRoomInfoUseCase = displayRoomInfoUseCase;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void move(final MoveCommand command) {
        final PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
        final Room room = gameWorldService.getRoom(player.getCurrentRoomId());
        final Direction direction = Direction.valueOfString(command.direction());
        final PlayerCharacter.MoveResult moveResult = player.move(room, direction);
        switch (moveResult) {
            case SUCCESS:
                sendMessageToUserPort.messageToUser(command.userId(), String.format("당신은 %s쪽으로 이동 합니다.", direction.getName()));
                displayRoomInfoUseCase.displayRoomInfo(command.userId());
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
