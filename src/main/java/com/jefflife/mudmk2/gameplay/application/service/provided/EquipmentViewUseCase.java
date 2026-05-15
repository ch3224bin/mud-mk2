package com.jefflife.mudmk2.gameplay.application.service.provided;

import com.jefflife.mudmk2.gameplay.application.domain.model.command.EquipmentViewCommand;

public interface EquipmentViewUseCase {
    void showEquipment(EquipmentViewCommand command);
}
