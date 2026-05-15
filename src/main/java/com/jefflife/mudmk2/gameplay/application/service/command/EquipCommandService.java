package com.jefflife.mudmk2.gameplay.application.service.command;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.*;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.EquippedItems;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.Inventory;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipCommand;
import com.jefflife.mudmk2.gameplay.application.exception.PlayerNotFoundException;
import com.jefflife.mudmk2.gameplay.application.service.provided.EquipUseCase;
import com.jefflife.mudmk2.gameplay.application.service.required.ActivePlayerRepository;
import com.jefflife.mudmk2.gameplay.application.service.required.SendMessageToUserPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipCommandService implements EquipUseCase {

    private final ActivePlayerRepository players;
    private final SendMessageToUserPort sendMessageToUserPort;

    public EquipCommandService(ActivePlayerRepository players, SendMessageToUserPort sendMessageToUserPort) {
        this.players = players;
        this.sendMessageToUserPort = sendMessageToUserPort;
    }

    @Override
    public void equip(EquipCommand command) {
        PlayerCharacter player = players.findByUserId(command.userId())
                .orElseThrow(() -> new PlayerNotFoundException(command.userId()));

        Inventory inventory = player.getInventory();
        EquippedItems equipped = player.getEquippedItems();

        List<ItemInstance> matches = inventory.findItemsByName(command.itemName());
        int idx = command.index();
        if (matches.size() < idx) {
            sendMessageToUserPort.messageToUser(command.userId(), command.itemName() + "을(를) 가지고 있지 않습니다.");
            return;
        }
        ItemInstance instance = matches.get(idx - 1);

        ItemTemplate template = instance.getTemplate();
        if (!(template instanceof EquippableItemTemplate)) {
            sendMessageToUserPort.messageToUser(command.userId(), template.getName() + "은(는) 장착할 수 없습니다.");
            return;
        }

        EquipmentSlot targetSlot = resolveSlot(template, equipped);

        // swap 시 인벤토리 무게 사전 검사
        // instance 제거 후 무게 + 반환될 기존 장착 아이템 무게가 용량 초과 여부 확인
        Optional<ItemInstance> currentOpt = equipped.getSlot(targetSlot);
        if (currentOpt.isPresent()) {
            ItemInstance current = currentOpt.get();
            int weightAfterRemove = inventory.currentWeight() - template.getWeight() * instance.getQuantity();
            int weightAfterSwap = weightAfterRemove + current.getTemplate().getWeight() * current.getQuantity();
            if (weightAfterSwap > inventory.getMaxWeightCapacity()) {
                sendMessageToUserPort.messageToUser(command.userId(),
                        template.getName() + "을(를) 장착하려면 " + current.getTemplate().getName()
                                + "을(를) 해제해야 하지만 소지품 무게가 부족합니다.");
                return;
            }
        }

        Optional<ItemInstance> swapped = equipped.equip(targetSlot, instance);
        inventory.removeItem(instance);
        swapped.ifPresent(inventory::addItem);

        if (swapped.isPresent()) {
            sendMessageToUserPort.messageToUser(command.userId(),
                    "기존 " + swapped.get().getTemplate().getName()
                            + "을(를) 해제하고 " + template.getName() + "을(를) 장착했다.");
        } else {
            sendMessageToUserPort.messageToUser(command.userId(), template.getName() + "을(를) 장착했다.");
        }
    }

    private EquipmentSlot resolveSlot(ItemTemplate template, EquippedItems equipped) {
        if (template instanceof WeaponTemplate) {
            return EquipmentSlot.WEAPON;
        }
        if (template instanceof EquipmentTemplate eq) {
            return eq.getEquipmentSlot();
        }
        if (template instanceof AccessoryTemplate acc) {
            return switch (acc.getAccessoryType()) {
                case NECKLACE -> EquipmentSlot.NECKLACE;
                case RING -> pickRingSlot(equipped);
            };
        }
        throw new IllegalStateException("Unknown equippable template: " + template.getClass());
    }

    private EquipmentSlot pickRingSlot(EquippedItems equipped) {
        if (equipped.getSlot(EquipmentSlot.RING_LEFT).isEmpty()) return EquipmentSlot.RING_LEFT;
        if (equipped.getSlot(EquipmentSlot.RING_RIGHT).isEmpty()) return EquipmentSlot.RING_RIGHT;
        return EquipmentSlot.RING_LEFT;
    }
}
