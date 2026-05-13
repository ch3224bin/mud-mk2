package com.jefflife.mudmk2.gameplay.application.service.command.look;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.domain.model.map.Room;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayerCharacter;
import com.jefflife.mudmk2.gameplay.application.service.GameWorldService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ItemSearchStrategy implements TargetSearchStrategy {

    private final GameWorldService gameWorldService;

    public ItemSearchStrategy(GameWorldService gameWorldService) {
        this.gameWorldService = gameWorldService;
    }

    @Override
    public Optional<Lookable> search(Long userId, String targetName, int index) {
        if (index < 1) {
            return Optional.empty();
        }

        PlayerCharacter player = gameWorldService.getPlayerByUserId(userId);
        Room currentRoom = gameWorldService.getRoom(player.getCurrentRoomId());

        List<MatchedItem> matches = new ArrayList<>();
        for (ItemInstance item : currentRoom.findFloorItemsByName(targetName)) {
            matches.add(new MatchedItem(item, ItemLookable.ItemLocation.ROOM));
        }
        for (ItemInstance item : player.getInventory().findItemsByName(targetName)) {
            matches.add(new MatchedItem(item, ItemLookable.ItemLocation.INVENTORY));
        }

        if (index > matches.size()) {
            return Optional.empty();
        }

        MatchedItem picked = matches.get(index - 1);
        return Optional.of(new ItemLookable(picked.instance(), picked.location()));
    }

    @Override
    public int getPriority() {
        return 2;
    }

    private record MatchedItem(ItemInstance instance, ItemLookable.ItemLocation location) {}
}
