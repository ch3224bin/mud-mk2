package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemType;

import java.util.List;

public interface ItemTemplateFinder {
    List<ItemTemplate> findAll();
    List<ItemTemplate> findByType(ItemType type);
    List<ItemTemplate> findByNameContaining(String name);
    ItemTemplate findById(Long id);
}
