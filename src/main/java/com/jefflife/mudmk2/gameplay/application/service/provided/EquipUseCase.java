package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipCommand;

public interface EquipUseCase {
    void equip(EquipCommand command);
}
