package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.EquipUseCase;
import org.springframework.stereotype.Component;

@Component
public class EquipCommandExecutor implements CommandExecutor {
    private final EquipUseCase equipUseCase;

    public EquipCommandExecutor(EquipUseCase equipUseCase) {
        this.equipUseCase = equipUseCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof EquipCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof EquipCommand equipCommand)) {
            throw new IllegalArgumentException("Command must be an EquipCommand");
        }
        equipUseCase.equip(equipCommand);
    }
}
