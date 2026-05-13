package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gameplay.application.service.model.template.ItemInfoVariables;

public interface SendItemInfoMessagePort {
    void sendMessage(ItemInfoVariables variables);
}
