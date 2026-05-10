package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.DropCommand;

public interface DropUseCase {
    void drop(DropCommand command);
}
