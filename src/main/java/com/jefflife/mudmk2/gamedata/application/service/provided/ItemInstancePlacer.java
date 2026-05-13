package com.jefflife.mudmk2.gamedata.application.service.provided;

import com.jefflife.mudmk2.gamedata.application.domain.model.item.ItemInstance;
import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemInstancePlaceRequest;

public interface ItemInstancePlacer {
    ItemInstance place(ItemInstancePlaceRequest request);
}
