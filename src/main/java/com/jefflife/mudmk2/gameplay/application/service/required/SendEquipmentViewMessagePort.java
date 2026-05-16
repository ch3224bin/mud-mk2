package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gameplay.application.service.model.template.EquipmentViewVariables;

public interface SendEquipmentViewMessagePort {
    void sendMessage(EquipmentViewVariables variables);
}
