package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.InventoryCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.InventoryUseCase;
import org.springframework.stereotype.Component;

@Component
public class InventoryCommandExecutor implements CommandExecutor {
    private final InventoryUseCase inventoryUseCase;

    public InventoryCommandExecutor(InventoryUseCase inventoryUseCase) {
        this.inventoryUseCase = inventoryUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof InventoryCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof InventoryCommand inventoryCommand)) {
            throw new IllegalArgumentException("Command must be an InventoryCommand");
        }
        inventoryUseCase.showInventory(inventoryCommand);
    }
}
