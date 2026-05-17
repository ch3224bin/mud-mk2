package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gameplay.application.service.model.template.MartialArtViewVariables;

public interface SendMartialArtViewMessagePort {
    void sendMessage(MartialArtViewVariables variables);
}
