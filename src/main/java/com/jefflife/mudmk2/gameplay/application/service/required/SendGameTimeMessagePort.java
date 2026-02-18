package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gameplay.application.service.model.template.GameTimeVariables;

public interface SendGameTimeMessagePort {
    void sendMessage(GameTimeVariables gameTimeVariables);
}
