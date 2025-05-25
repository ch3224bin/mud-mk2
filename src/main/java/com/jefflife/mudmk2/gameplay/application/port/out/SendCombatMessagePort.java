package com.jefflife.mudmk2.gameplay.application.port.out;

import com.jefflife.mudmk2.gameplay.application.service.model.template.CombatStartVariables;

public interface SendCombatMessagePort {
    void sendCombatStartMessageToUser(CombatStartVariables variables);
}
