package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.InventoryCommand;

public interface InventoryUseCase {
    void showInventory(InventoryCommand command);
}
