package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gameplay.application.service.model.template.EatSuccessVariables;

public interface SendEatSuccessMessagePort {
    void sendMessage(EatSuccessVariables variables);
}
