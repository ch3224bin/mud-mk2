package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemTemplate;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;

public interface ItemTemplateCreator {
    ItemTemplate create(ItemTemplateRequest request);
}
