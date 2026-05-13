package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.TakeCommand;
import com.jefflife.mudmk2.gameplay.application.exception.RoomNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import com.jefflife.mudmk2.gameplay.application.service.provided.TakeUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActiveRoomRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TakeCommandService implements TakeUseCase {
    private final GameWorldService gameWorldService;
    private final ActiveRoomRepository rooms;
    private final SendMessageToUserPort sendMessageToUserPort;

    public TakeCommandService(GameWorldService gameWorldService, ActiveRoomRepository rooms, SendMessageToUserPort sendMessageToUserPort) {
        this.gameWorldService = gameWorldService;
        this.rooms = rooms;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void take(TakeCommand command) {
        if (command.index() < 1) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    "올바른 번호를 입력해주세요.");
            return;
        }

        PlayerCharacter player = gameWorldService.getPlayerByUserId(command.userId());
        Long currentRoomId = player.getCurrentRoomId();
        Room room = rooms.findById(currentRoomId)
                .orElseThrow(() -> new RoomNotFoundException(currentRoomId));

        List<ItemInstance> matching = room.findFloorItemsByName(command.itemName());
        if (matching.isEmpty()) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    command.itemName() + "(을)를 찾을 수 없습니다.");
            return;
        }
        if (command.index() > matching.size()) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    command.itemName() + " " + command.index() + "번째 아이템을 찾을 수 없습니다.");
            return;
        }

        ItemInstance item = matching.get(command.index() - 1);
        Inventory inventory = player.getInventory();

        if (!inventory.canAdd(item.getTemplate(), item.getQuantity())) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    "무게 초과로 " + item.getTemplate().getName() + "(을)를 들 수 없습니다.");
            return;
        }

        room.removeFloorItem(item);
        inventory.addItem(item);

        sendMessageToUserPort.messageToUser(command.userId(),
                item.getTemplate().getName() + "(을)를 주웠습니다.");
    }
}
