package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.InventoryCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.provided.InventoryUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;

@Service
public class InventoryCommandService implements InventoryUseCase {
    private final ActivePlayerRepository players;
    private final SendMessageToUserPort sendMessageToUserPort;

    public InventoryCommandService(ActivePlayerRepository players, SendMessageToUserPort sendMessageToUserPort) {
        this.players = players;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void showInventory(InventoryCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
        Inventory inventory = player.getInventory();

        StringBuilder sb = new StringBuilder("[ 소지품 ]\n");
        if (inventory.getItems().isEmpty()) {
            sb.append("소지품이 없습니다.\n");
        } else {
            for (ItemInstance item : inventory.getItems()) {
                sb.append("- ").append(item.getTemplate().getName());
                if (item.getTemplate().isStackable()) {
                    sb.append(" x").append(item.getQuantity());
                }
                sb.append(" (").append(item.getTemplate().getWeight() * item.getQuantity()).append("kg)\n");
            }
        }
        sb.append("무게: ").append(inventory.currentWeight())
          .append("/").append(inventory.getMaxWeightCapacity()).append("kg");

        sendMessageToUserPort.messageToUser(command.userId(), sb.toString());
    }
}
