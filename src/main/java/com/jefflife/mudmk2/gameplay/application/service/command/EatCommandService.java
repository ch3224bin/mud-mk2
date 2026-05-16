package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.FoodTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.service.required.ItemInstanceRepository;
import com.jefflife.mudmk2.gamedata.application.service.required.PlayerCharacterRepository;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EatCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.model.template.EatSuccessVariables;
import com.jefflife.mudmk2.gameplay.application.service.provided.EatUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendEatSuccessMessagePort;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EatCommandService implements EatUseCase {

    private final ActivePlayerRepository players;
    private final ItemInstanceRepository itemInstanceRepository;
    private final PlayerCharacterRepository playerCharacterRepository;
    private final SendMessageToUserPort sendMessageToUserPort;
    private final SendEatSuccessMessagePort sendEatSuccessMessagePort;

    public EatCommandService(
            ActivePlayerRepository players,
            ItemInstanceRepository itemInstanceRepository,
            PlayerCharacterRepository playerCharacterRepository,
            SendMessageToUserPort sendMessageToUserPort,
            SendEatSuccessMessagePort sendEatSuccessMessagePort
    ) {
        this.players = players;
        this.itemInstanceRepository = itemInstanceRepository;
        this.playerCharacterRepository = playerCharacterRepository;
        this.sendMessageToUserPort = sendMessageToUserPort;
        this.sendEatSuccessMessagePort = sendEatSuccessMessagePort;
    }

    @Override
    public void eat(EatCommand command) {
        if (command.index() < 1) {
            sendMessageToUserPort.messageToUser(command.userId(), "올바른 번호를 입력해주세요.");
            return;
        }

        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));
        Inventory inventory = player.getInventory();

        List<ItemInstance> matching = inventory.findItemsByName(command.itemName());
        if (matching.isEmpty()) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    command.itemName() + "(을)를 가지고 있지 않습니다.");
            return;
        }
        if (command.index() > matching.size()) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    command.itemName() + " " + command.index() + "번째 아이템을 찾을 수 없습니다.");
            return;
        }

        ItemInstance instance = matching.get(command.index() - 1);
        ItemTemplate template = instance.getTemplate();
        if (!(template instanceof FoodTemplate food)) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    template.getName() + "은(는) 먹을 수 없습니다.");
            return;
        }

        food.getEatEffects().forEach(e -> e.applyTo(player));

        boolean removed = inventory.consumeOne(instance);
        if (removed) {
            itemInstanceRepository.delete(instance);
        }
        if (template.requiresImmediateDeletion()) {
            playerCharacterRepository.save(player);
        }

        sendEatSuccessMessagePort.sendMessage(new EatSuccessVariables(
                command.userId(),
                food.getName(),
                food.getHpRecovery(),
                food.getMpRecovery(),
                food.getApRecovery()
        ));
    }
}
