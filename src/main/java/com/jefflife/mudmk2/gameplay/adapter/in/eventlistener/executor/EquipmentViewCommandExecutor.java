package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener.executor;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.Command;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;
import com.jefflife.mudmk2.gameplay.application.service.provided.EquipmentViewUseCase;
import org.springframework.stereotype.Component;

@Component
public class EquipmentViewCommandExecutor implements CommandExecutor {
    private final EquipmentViewUseCase useCase;

    public EquipmentViewCommandExecutor(EquipmentViewUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public boolean canExecute(Command command) {
        return command instanceof EquipmentViewCommand;
    }

    @Override
    public void execute(Command command) {
        if (!(command instanceof EquipmentViewCommand viewCommand)) {
            throw new IllegalArgumentException("Command must be an EquipmentViewCommand");
        }
        useCase.showEquipment(viewCommand);
    }
}
