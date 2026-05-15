package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.UnequipCommand;

public interface UnequipUseCase {
    void unequip(UnequipCommand command);
}
