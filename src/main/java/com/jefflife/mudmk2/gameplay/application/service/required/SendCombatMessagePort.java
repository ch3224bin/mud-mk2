package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gameplay.application.service.model.template.CombatActionVariables;
import com.jefflife.mudmk2.gameplay.application.service.model.template.CombatStartVariables;

public interface SendCombatMessagePort {
    void sendCombatStartMessageToUser(CombatStartVariables variables);
    void sendCombatActionMessageToUser(CombatActionVariables variables);
}
