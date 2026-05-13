package com.jefflife.mudmk2.gameplay.application.service.command.look;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;

import java.util.Map;

public record ItemLookable(ItemInstance instance, ItemLocation location) implements Lookable {

    public enum ItemLocation {
        ROOM,
        INVENTORY
    }

    @Override
    public String getName() {
        return instance.getTemplate().getName();
    }

    @Override
    public LookableType getType() {
        return LookableType.ITEM;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Map.of(
                "instance", instance,
                "location", location
        );
    }
}
