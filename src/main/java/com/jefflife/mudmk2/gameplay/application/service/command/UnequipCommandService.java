package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.EquipmentSlot;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.EquippedItems;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.UnequipCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.provided.UnequipUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UnequipCommandService implements UnequipUseCase {

    private final ActivePlayerRepository players;
    private final SendMessageToUserPort sendMessageToUserPort;

    public UnequipCommandService(ActivePlayerRepository players, SendMessageToUserPort sendMessageToUserPort) {
        this.players = players;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void unequip(UnequipCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));

        Inventory inventory = player.getInventory();
        EquippedItems equipped = player.getEquippedItems();

        Optional<Map.Entry<EquipmentSlot, ItemInstance>> found = equipped.findByItemName(command.itemName());
        if (found.isEmpty()) {
            sendMessageToUserPort.messageToUser(command.userId(), command.itemName() + "을(를) 장착하고 있지 않습니다.");
            return;
        }

        ItemInstance instance = found.get().getValue();
        if (!inventory.canAdd(instance.getTemplate(), instance.getQuantity())) {
            sendMessageToUserPort.messageToUser(command.userId(), "소지품 무게가 부족해 해제할 수 없습니다.");
            return;
        }

        equipped.unequip(found.get().getKey());
        inventory.addItem(instance);
        sendMessageToUserPort.messageToUser(command.userId(), instance.getTemplate().getName() + "을(를) 해제했다.");
    }
}
