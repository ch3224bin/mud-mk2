package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.EatCommand;

public interface EatUseCase {
    void eat(EatCommand command);
}
